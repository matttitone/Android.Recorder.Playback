package com.teamsix.recorddemo.recorddemo;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Comparator;
import java.util.Collection;

interface OnRecordItemStateChangedListener
{
    void onStateChange(int newState);
    void setOnFragmentChangeListener(MainActivity.FragmentChangeListener listener);
    void setOnActionOperationListener(ActionOperationListener listener);
    void onPlayStart();
    void onPlayStop();
}

public class RecordListActivity extends Fragment implements MainActivity.FragmentChangeListener,ActionOperationListener {

    private static final String LOG_TAG = "AudioRecordTest";

    private ExpandableListView lv;     // listview
    private MyAdapter mAdapter;
    private ArrayList<Record> list; // record list
    private Button btnPlay;
    private Button btnPause;
    private Button btnStop;
    private Button btnRename;
    private Button btnSingleDelete;
    private Button btnDelete;
    private Button btnReturn;
    private int checkNum; // total selected number
    private TextView tv_show; // show the selected number
    private boolean isMulChoice; // whether we are in mulchoice mode
    private boolean isStoreToSDCard = true; // whether we store the record on sd card
    private File dir;

    private MediaPlayer mMediaPlayer = null;
    private int curPlayTime = 0; // the current play time for pause function
    private boolean isPlaying = false; // whether we are playing records
    private boolean isPause = false;   // whether we are paused

    private String searchKeyword = ""; // the search keyword
    private boolean isSearchClicked = false; // to ensure the search close will not effect the search function

    private SeekBar seekBar;
    private TextView tvStartTime;
    private TextView tvEndTime;
    private boolean isChanging;

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        public void run() {
            this.update();
            handler.postDelayed(this, 250);
        }
        void update() {
            if(isChanging)
                return;
            if(isPlaying)
            {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int duration = mMediaPlayer.getDuration();
                int position = mMediaPlayer.getCurrentPosition();
                seekBar.setVisibility(View.VISIBLE);
                seekBar.setMax(duration);
                seekBar.setProgress(position);
                tvStartTime.setVisibility(View.VISIBLE);
                tvStartTime.setText(RecordUtil.getHMSTime(position));
                tvEndTime.setVisibility(View.VISIBLE);
                tvEndTime.setText(RecordUtil.getHMSTime(duration));
            }
            else
            {
                seekBar.setVisibility(View.INVISIBLE);
                tvStartTime.setVisibility(View.INVISIBLE);
                tvEndTime.setVisibility(View.INVISIBLE);
            }
        }
    };

    @Override
    public void onPauseSignal() {
        btnPause.performClick();
    }

    @Override
    public void onUpdateDataSignal() {
        refreshList();
    }

    @Override
    public void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 200);
        SettingUtil settingUtil = new SettingUtil(getActivity().getApplicationContext());
        isStoreToSDCard = settingUtil.getStoreInSDCard();
        dir = new File(FileUtil.getRecordFolderPath(getActivity().getApplicationContext(),isStoreToSDCard));
        if(isPlaying == false)
        {
            refreshList();
            itemStateChanged(0);
            btnPlay.setVisibility(View.INVISIBLE);
            btnRename.setVisibility(View.INVISIBLE);
            btnSingleDelete.setVisibility(View.INVISIBLE);
        }
        //onUpdateDataSignal();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentActivity    faActivity  = (FragmentActivity)    super.getActivity();
        // Replace LinearLayout by the type of the root element of the layout you're trying to load
        RelativeLayout rlLayout    = (RelativeLayout)    inflater.inflate(R.layout.fragment_record, container, false);
        super.onCreate(savedInstanceState);

        lv = (ExpandableListView)rlLayout.findViewById(R.id.listView);

        tv_show = (TextView)rlLayout.findViewById(R.id.tvNumber);

        btnPlay = (Button)rlLayout.findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new onPlayListener());
        btnPause = (Button)rlLayout.findViewById(R.id.btnPause);
        btnPause.setOnClickListener(new onPauseListener());
        btnStop = (Button)rlLayout.findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new onStopListener());
        btnRename = (Button)rlLayout.findViewById(R.id.btnRename);
        btnRename.setOnClickListener(new onRenameListener());
        btnReturn = (Button)rlLayout.findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(new onReturnListener());
        btnDelete = (Button)rlLayout.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new onDeleteListener());
        btnSingleDelete = (Button)rlLayout.findViewById(R.id.btnSingleDelete);
        btnSingleDelete.setOnClickListener(new singleDeleteListener());

        seekBar = (SeekBar)rlLayout.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new MySeekBarProgress());
        tvStartTime = (TextView)rlLayout.findViewById(R.id.tvStartTime);
        tvEndTime = (TextView)rlLayout.findViewById(R.id.tvEndTime);
        isChanging = true;

        list = new ArrayList<Record>();
        isMulChoice = false;


        // registe the pausecontrol
        ((OnRecordItemStateChangedListener) getActivity()).setOnFragmentChangeListener(this);
        ((OnRecordItemStateChangedListener) getActivity()).setOnActionOperationListener(this);


        lv.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View view, int position, long id) {
                if (isMulChoice) {
                    ViewHolder holder = (ViewHolder) view.getTag();
                    holder.cb.toggle();
                    MyAdapter.getIsSelected().put(position, holder.cb.isChecked());

                    if (holder.cb.isChecked() == true) {
                        checkNum++;
                    } else {
                        checkNum--;
                    }

                    tv_show.setText("select " + checkNum + " item(s)");
                    if (checkNum == 0) {
                        if (mAdapter.getPos() != -1) {
                            parent.collapseGroup(position);
                            mAdapter.setPos(-1);
                        }
                        btnReturn.performClick();
                        return true;
                    }
                    mAdapter.setMulChoice(true);
                    mAdapter.notifyDataSetChanged();
                } else // not multichoice
                {
                    if (isPlaying)
                        stopPlayRecord();
                    // stop the record
                    if (searchKeyword.equals("") == false) // click from the search function
                    {
                        isSearchClicked = true; // so we will ignore the next "" search key
                        // save the file name
                        String selectedFileName = list.get(position).getName();
                        btnReturn.performClick();
                        // find the position of selected file

                        //dataChanged();
                        refreshList();
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).getName().equals(selectedFileName)) {
                                lv.expandGroup(i);
                                mAdapter.setPos(i);
                                lv.setSelection(i);
                                break;
                            }
                        }
                        btnRename.setVisibility(View.VISIBLE);
                        btnPlay.setVisibility(View.VISIBLE);
                        btnSingleDelete.setVisibility(View.VISIBLE);
                        mAdapter.notifyDataSetChanged();
                        itemStateChanged(1); // single choice
                    } else {
                        if (position == MyAdapter.getPos()) // cancel
                        {
                            parent.collapseGroup(position);
                            itemStateChanged(0);
                        } else {
                            if (MyAdapter.getPos() != -1) {
                                parent.collapseGroup(MyAdapter.getPos());
                            }
                            MyAdapter.setPos(position);
                            itemStateChanged(1); // single choice
                            mAdapter.notifyDataSetChanged();
                            btnRename.setVisibility(View.VISIBLE);
                            btnPlay.setVisibility(View.VISIBLE);
                            btnSingleDelete.setVisibility(View.VISIBLE);
                            parent.expandGroup(position);
                        }
                    }
                    //Toast.makeText(getApplicationContext(),"click" + position,Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (isPlaying)
                    stopPlayRecord();
                isMulChoice = true;
                checkNum = 0;
                // change the button visiblity
                btnRename.setVisibility(View.INVISIBLE);
                btnPlay.setVisibility(View.INVISIBLE);
                btnSingleDelete.setVisibility(View.INVISIBLE);
                btnStop.setVisibility(View.INVISIBLE);
                btnPause.setVisibility(View.INVISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                btnReturn.setVisibility(View.VISIBLE);

                for (int i = 0; i < list.size(); i++) {
                    MyAdapter.getIsSelected().put(i, false);
                }
                // notify the adapter
                if(MyAdapter.getPos() != -1) {
                    lv.collapseGroup(MyAdapter.getPos());
                }
                MyAdapter.setPos(-1);
                tv_show.setVisibility(View.INVISIBLE);
                mAdapter.setMulChoice(false);
                mAdapter.notifyDataSetChanged();
                // notify the listener
                itemStateChanged(2); // mutichoice
                return false;
            }
        });

        lv.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                // Collapse previous parent if expanded.
                if ((MyAdapter.getPos() != -1) && (groupPosition != MyAdapter.getPos())) {
                    lv.collapseGroup(MyAdapter.getPos());
                }
                MyAdapter.setPos(groupPosition);
            }
        });

        lv.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                MyAdapter.setPos(-1);
                btnRename.setVisibility(View.INVISIBLE);
                btnPlay.setVisibility(View.INVISIBLE);
                btnSingleDelete.setVisibility(View.INVISIBLE);
            }
        });

        refreshList();
        btnReturn.performClick();
        return rlLayout;
    }

    public void stopPlayRecord()
    {
        isChanging = true;
        if(mMediaPlayer != null) {
            mMediaPlayer.stop();
            curPlayTime = 0;
            isPause = false;
            isPlaying = false;
            mMediaPlayer = null;
        }
        btnRename.setVisibility(View.VISIBLE);
        btnPlay.setVisibility(View.VISIBLE);
        btnSingleDelete.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.INVISIBLE);
        btnPause.setVisibility(View.INVISIBLE);
        btnDelete.setVisibility(View.INVISIBLE);
        btnReturn.setVisibility(View.INVISIBLE);
        isChanging = false;
    }


    private void initData() {
        initData("");
    }
    private void initData(String keyword) {
        if(list == null)
            list = new ArrayList<>();
        else
            list.clear();

        File file = new File(FileUtil.getRecordFolderPath(getActivity().getApplicationContext(),isStoreToSDCard));
        try
        {
            File[] files = file.listFiles();
            if (files.length > 0)
            {
                //MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                for (int j = 0; j < files.length; j++)
                {
                    if (!files[j].isDirectory())
                    {
                        //mmr.setDataSource(files[j].getAbsolutePath());
                        //mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        String fileName = files[j].getName();
                        if((keyword.equals("") == false && fileName.indexOf(keyword) >= 0) || keyword.equals("")) {
                            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                            mmr.setDataSource(files[j].getAbsolutePath());
                            int recordLen = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                            Record record = new Record(fileName,RecordUtil.getHMSTime(recordLen),FileSizeUtil.sDate(files[j]),FileSizeUtil.getAutoFileOrFilesSize(files[j].getAbsolutePath()),true);
                            list.add(record);
                            list.get(list.size()-1).setPath(files[j].getAbsolutePath());
                        }
                    }
                }
                Collections.sort(list);
            }
        }
        catch(Exception e)
        {

        }
    }

    // item state was changed,so let the container know
    private void itemStateChanged(int state)
    {
        ((OnRecordItemStateChangedListener) getActivity()).onPlayStop();
        ((OnRecordItemStateChangedListener) getActivity()).onStateChange(state);
    }

    private void deleteFile()
    {
        // ask user if (s)he wants to delete the file
        AlertDialog alertDialog = new AlertDialog.Builder(RecordListActivity.super.getActivity()).create();
        //set Title
        alertDialog.setTitle("Confirm");
        //prompt
        alertDialog.setMessage("Do you really want to delete the record?");
        //add Cancel button
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        });

        //add other button to the dialog
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean isSuccessfulDel = true;
                if(isMulChoice == false) // just delete the file
                {
                    try {
                        File file = new File(dir + "/" + list.get(mAdapter.getPos()).getName());
                        if (!file.delete()) {
                            isSuccessfulDel = false;
                        }
                    }
                    catch (Exception e)
                    {

                    }
                }
                else {
                    // delete files
                    HashMap<Integer, Boolean> isSelected = MyAdapter.getIsSelected();
                    for (int i = 0; i < isSelected.size(); i++) {
                        if (isSelected.get(i) == false) {
                            continue;
                        }
                        File file = new File(dir + "/" + list.get(i).getName());
                        System.out.println(dir + list.get(i).getName());
                        if (!file.delete()) {
                            isSuccessfulDel = false;
                        }
                    }
                    isMulChoice = false;
                    checkNum = 0;
                    // change the button visiblity

                }
                refreshList();
                btnRename.setVisibility(View.INVISIBLE);
                btnPlay.setVisibility(View.INVISIBLE);
                btnSingleDelete.setVisibility(View.INVISIBLE);
                btnStop.setVisibility(View.INVISIBLE);
                btnPause.setVisibility(View.INVISIBLE);
                btnDelete.setVisibility(View.INVISIBLE);
                btnReturn.setVisibility(View.INVISIBLE);
                itemStateChanged(0);
                if(isSuccessfulDel) {
                    Toast.makeText(getActivity().getApplicationContext(), "File Deleted", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getActivity().getApplicationContext(), "File Can't Deleted Totally", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //show dialog
        alertDialog.show();
    }
    @Override
    public void onDeleteFile() {
        deleteFile();
    }

    @Override
    public void onSearchFile(String keyword) {

//        if(keyword.equals("") && isSearchClicked)
//        {
//            isSearchClicked = false;
//            searchKeyword = keyword;
//            return;
//        }
        initData(keyword);
        mAdapter = new MyAdapter(list, getActivity().getApplicationContext());
        lv.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        checkNum = 0;

        searchKeyword = keyword;
    }

    @Override
    public void onShareFile() {
        btnReturn.performClick();
        ArrayList<Uri> uris = new ArrayList<Uri>();
        for(int i = 0; i < list.size(); i++){
            //File file=(File)list.get(selectedItemIndexes[i]).get("file");
            if(MyAdapter.getIsSelected().get(i)) {
                File file = new File(list.get(i).getPath());
                Uri u = Uri.fromFile(file);
                uris.add(u);
            }
        }
        if(uris.size() == 0)
        {
            Toast.makeText(getActivity(),"No files selected to share",Toast.LENGTH_SHORT).show();
            return;
        }
        boolean multiple = uris.size() > 1;
        Intent intent = new Intent(multiple ? android.content.Intent.ACTION_SEND_MULTIPLE
                : android.content.Intent.ACTION_SEND);
        if (multiple) {
            intent.setType("*/*");
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        } else {
            intent.setType("audio/*");
            intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
        }
        startActivity(Intent.createChooser(intent, "Share"));
    }

    // play function
    public class onPlayListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            if(isPlaying == false) // start play a record
            {
                isPlaying = true;
                isChanging = true;
                stopPlayRecord();
                curPlayTime = 0;
                // set the visiblity of buttons
                btnRename.setVisibility(View.INVISIBLE);
                btnPlay.setVisibility(View.INVISIBLE);
                btnSingleDelete.setVisibility(View.INVISIBLE);
                btnStop.setVisibility(View.VISIBLE);
                btnPause.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.INVISIBLE);
                btnReturn.setVisibility(View.INVISIBLE);
                // get the selected record path
                String filePath = dir + "/" + list.get(MyAdapter.getPos()).getName();
                try {
                    mMediaPlayer = new MediaPlayer();
                    mMediaPlayer.setDataSource(getActivity().getApplicationContext(), Uri.parse(filePath));
                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            ((OnRecordItemStateChangedListener) getActivity()).onPlayStop();
                            isChanging = true;
                            isPlaying = false;
                            isPause = false;
                            btnRename.setVisibility(View.VISIBLE);
                            btnPlay.setVisibility(View.VISIBLE);
                            btnSingleDelete.setVisibility(View.VISIBLE);
                            btnStop.setVisibility(View.INVISIBLE);
                            btnPause.setVisibility(View.INVISIBLE);
                            btnDelete.setVisibility(View.INVISIBLE);
                            btnReturn.setVisibility(View.INVISIBLE);
                            mMediaPlayer = null;
                            isChanging = false;
                        }
                    });
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            ((OnRecordItemStateChangedListener) getActivity()).onPlayStart();
                            if(mMediaPlayer != null)
                                mMediaPlayer.start();
                            isChanging = false;
                        }
                    });
                    mMediaPlayer.prepareAsync();

                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            else // resume
            {
                if(isPause)
                {
                    isPause = false;
                    if (mMediaPlayer != null) {
                        // change the drawable
                        btnRename.setVisibility(View.INVISIBLE);
                        btnPlay.setVisibility(View.INVISIBLE);
                        btnSingleDelete.setVisibility(View.INVISIBLE);
                        btnStop.setVisibility(View.VISIBLE);
                        btnPause.setVisibility(View.VISIBLE);
                        btnDelete.setVisibility(View.INVISIBLE);
                        btnReturn.setVisibility(View.INVISIBLE);
                        mMediaPlayer.seekTo(curPlayTime);
                        mMediaPlayer.start();
                    }
                }
            }
        }
    }

    // pause function
    public class onPauseListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            if(isPlaying)
            {
                if(isPause == false) {
                    curPlayTime = mMediaPlayer.getCurrentPosition();
                    mMediaPlayer.pause();
                    // change the drawable
                    btnRename.setVisibility(View.INVISIBLE);
                    btnPlay.setVisibility(View.VISIBLE);
                    btnSingleDelete.setVisibility(View.INVISIBLE);
                    btnStop.setVisibility(View.VISIBLE);
                    btnPause.setVisibility(View.INVISIBLE);
                    btnDelete.setVisibility(View.INVISIBLE);
                    btnReturn.setVisibility(View.INVISIBLE);
                    isPause = true;
                }

            }
        }
    }

    // stop function
    public class onStopListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            if(isPlaying) {
                ((OnRecordItemStateChangedListener) getActivity()).onPlayStop();
                stopPlayRecord();
            }
        }
    }

    // rename function
    public class onRenameListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            // build the rename dialog and then show
            final AlertDialog.Builder builderR = new AlertDialog.Builder(RecordListActivity.super.getActivity());
            builderR.setTitle("Rename file");
            builderR.setCancelable(true);
            // make sure the user can only input the character and digits
            InputFilter filter = new InputFilter() {
                @Override
                public CharSequence filter(CharSequence source, int start, int end,
                                           Spanned dest, int dstart, int dend) {
                    if (source instanceof SpannableStringBuilder) {
                        SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder)source;
                        for (int i = end - 1; i >= start; i--) {
                            char currentChar = source.charAt(i);
                            if (!Character.isLetterOrDigit(currentChar)) {
                                Toast.makeText(builderR.getContext(), "Only Characters or Digits allowed!", Toast.LENGTH_LONG);
                                sourceAsSpannableBuilder.delete(i, i+1);
                            }
                        }
                        return source;
                    } else {
                        StringBuilder filteredStringBuilder = new StringBuilder();
                        for (int i = start; i < end; i++) {
                            char currentChar = source.charAt(i);
                            if (Character.isLetterOrDigit(currentChar)) {
                                filteredStringBuilder.append(currentChar);
                            }
                        }
                        return filteredStringBuilder.toString();
                    }
                }
            };
            final EditText input = new EditText(getActivity().getApplicationContext());
            input.setFilters(new InputFilter[]{filter});
            try {
                input.setText(list.get(MyAdapter.getPos()).getName().toCharArray(), 0, list.get(MyAdapter.getPos()).getName().lastIndexOf("."));
            }
            catch(Exception e)
            {
                Log.e(LOG_TAG,Log.getStackTraceString(e));
                input.setText("");
            }

            input.setTextColor(-16777216);
            builderR.setView(input);

            builderR.setPositiveButton("Rename", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (input.getText().toString().equals("")) {
                        Toast.makeText(getActivity().getApplicationContext(), "Please enter a name for the file", Toast.LENGTH_LONG);
                    } else {
                        File from = new File(dir + "/" + list.get(MyAdapter.getPos()).getName());
                        // get the extension of the record file
                        String suffix = "";
                        try {
                            suffix = from.getName().substring(from.getName().lastIndexOf("."), from.getName().length());
                        } catch (Exception e) {

                        }
                        // get the filename with extension
                        String fileName = input.getText().toString();
                        // get all the name before '.'
                        if (!fileName.endsWith(suffix)) {
                            fileName = fileName + suffix;
                        }
                        File to = new File(dir + "/" + fileName);

                        if (from.renameTo(to)) {
                            System.out.println("The position is " + MyAdapter.getPos());
                            /** I have to do it here, don't I? How should I update the listview with the renamed file name?     **/
                            /** I think you are right.We will do it later*/
                            refreshList();
                        }
                    }
                }
            });

            builderR.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alertR = builderR.create();
            alertR.show();

            btnRename.setVisibility(View.VISIBLE);
            btnPlay.setVisibility(View.VISIBLE);
            btnSingleDelete.setVisibility(View.VISIBLE);
            btnStop.setVisibility(View.INVISIBLE);
            btnPause.setVisibility(View.INVISIBLE);
            btnDelete.setVisibility(View.INVISIBLE);
            btnReturn.setVisibility(View.INVISIBLE);
            // notify the adapter
            tv_show.setVisibility(View.INVISIBLE);
            mAdapter.setMulChoice(false);
            // notify the listener
            itemStateChanged(0); // nothing
        }
    }

    // pause function
    public class onReturnListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            isMulChoice = false;
            checkNum = 0;
            // change the button visiblity
            btnRename.setVisibility(View.INVISIBLE);
            btnPlay.setVisibility(View.INVISIBLE);
            btnSingleDelete.setVisibility(View.INVISIBLE);
            btnStop.setVisibility(View.INVISIBLE);
            btnPause.setVisibility(View.INVISIBLE);
            btnDelete.setVisibility(View.INVISIBLE);
            btnReturn.setVisibility(View.INVISIBLE);
            // notify the adapter
            MyAdapter.setPos(-1);
            tv_show.setVisibility(View.INVISIBLE);
            mAdapter.setMulChoice(false);
            mAdapter.notifyDataSetChanged();
            // notify the listener
            itemStateChanged(0); // nothing
        }
    }

    // info function
    public class singleDeleteListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            deleteFile();
        }
    }

    // pause function
    public class onDeleteListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            deleteFile();
        }
    }

    // reload the data
    private void refreshList()
    {
        initData();
        mAdapter = new MyAdapter(list, getActivity().getApplicationContext());
        lv.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        checkNum = 0;
    }

    // deal with the seek bar function
    class MySeekBarProgress implements SeekBar.OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            isChanging=true;
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            if(isPlaying) {
                if(isPause) {
                    curPlayTime = seekBar.getProgress();
                }
                mMediaPlayer.seekTo(seekBar.getProgress());
            }
            isChanging=false;
        }

    }
}
