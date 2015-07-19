package com.teamsix.recorddemo.recorddemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

interface OnRecordItemStateChangedListener
{
    void onStateChange(int newState);
    void setOnFragmentChangeListener(MainActivity.FragmentChangeListener listener);
    void setOnActionOperationListener(ActionOperationListener listener);
}

public class RecordListActivity extends Fragment implements MainActivity.FragmentChangeListener,ActionOperationListener {

    private static final String LOG_TAG = "AudioRecordTest";

    private ListView lv;  // listview
    private MyAdapter mAdapter;
    private ArrayList<String> list; // string of the data
    private Button btnPlay;
    private Button btnPause;
    private Button btnStop;
    private Button btnRename;
    private Button btnInfo;
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

    @Override
    public void onPauseSignal() {
        btnPause.performClick();
    }

    @Override
    public void onUpdateDataSignal() {
        refreshList();
    }

    @Override
    public void onResume() {
        super.onResume();
        // get the max length of the record
        SettingUtil settingUtil = new SettingUtil(getActivity().getApplicationContext());
        isStoreToSDCard = settingUtil.getStoreInSDCard();
        dir = new File(FileUtil.getRecordFolderPath(getActivity().getApplicationContext(),isStoreToSDCard));
        onUpdateDataSignal();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentActivity    faActivity  = (FragmentActivity)    super.getActivity();
        // Replace LinearLayout by the type of the root element of the layout you're trying to load
        RelativeLayout rlLayout    = (RelativeLayout)    inflater.inflate(R.layout.fragment_record, container, false);
        super.onCreate(savedInstanceState);
        lv = (ListView)rlLayout.findViewById(R.id.listView);

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
        btnInfo = (Button)rlLayout.findViewById(R.id.btnInfo);
        btnInfo.setOnClickListener(new onInfoListener());

        list = new ArrayList<String>();
        isMulChoice = false;


        // registe the pausecontrol
        ((OnRecordItemStateChangedListener) getActivity()).setOnFragmentChangeListener(this);
        ((OnRecordItemStateChangedListener) getActivity()).setOnActionOperationListener(this);

        // ��listView�ļ�����
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
                        btnReturn.performClick();
                        return;
                    }
                    mAdapter.setMulChoice(true);
                    mAdapter.notifyDataSetChanged();
                } else // not multichoice
                {
                    if(isPlaying)
                        stopPlayRecord();
                    // stop the record
                    if(searchKeyword.equals("") == false) // click from the search function
                    {
                        // save the file name
                        String selectedFileName = list.get(position);
                        btnReturn.performClick();
                        // find the position of selected file
                        for(int i = 0; i < list.size(); i++)
                        {
                            if(list.get(i).equals(selectedFileName)) {
                                mAdapter.setPos(i);
                                lv.setSelection(i);
                                break;
                            }
                        }

                        //dataChanged();
                        mAdapter.notifyDataSetChanged();
                        itemStateChanged(1); // single choice
                    }
                    else
                    {
                        if(position == MyAdapter.getPos()) // cancel
                        {
                            btnReturn.performClick();
                        }
                        else
                        {
                            MyAdapter.setPos(position);
                            itemStateChanged(1); // single choice
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                    //Toast.makeText(getApplicationContext(),"click" + position,Toast.LENGTH_SHORT).show();
                }
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if(isPlaying)
                    stopPlayRecord();
                isMulChoice = true;
                checkNum = 0;
                // change the button visiblity
                btnRename.setVisibility(View.INVISIBLE);
                btnPlay.setVisibility(View.INVISIBLE);
                btnInfo.setVisibility(View.INVISIBLE);
                btnStop.setVisibility(View.INVISIBLE);
                btnPause.setVisibility(View.INVISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                btnReturn.setVisibility(View.VISIBLE);

                for (int i = 0; i < list.size(); i++) {
                    MyAdapter.getIsSelected().put(i, false);
                }
                // notify the adapter
                MyAdapter.setPos(-1);
                tv_show.setVisibility(View.INVISIBLE);
                mAdapter.setMulChoice(false);
                mAdapter.notifyDataSetChanged();
                // notify the listener
                itemStateChanged(2); // mutichoice
                return false;
            }
        });
        refreshList();
        btnReturn.performClick();
        return rlLayout;
    }

    public void stopPlayRecord()
    {
        if(mMediaPlayer != null) {
            mMediaPlayer.stop();
            curPlayTime = 0;
            isPause = false;
            isPlaying = false;
            mMediaPlayer = null;
        }
        btnRename.setVisibility(View.VISIBLE);
        btnPlay.setVisibility(View.VISIBLE);
        btnInfo.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.INVISIBLE);
        btnPause.setVisibility(View.INVISIBLE);
        btnDelete.setVisibility(View.INVISIBLE);
        btnReturn.setVisibility(View.INVISIBLE);
    }


    private void initData() {
        initData("");
    }
    private void initData(String keyword) {
        list = null;
        list = new ArrayList<>();

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
                        if(keyword.equals("") == false && fileName.indexOf(keyword) >= 0)
                            list.add(files[j].getName());
                        else if(keyword.equals(""))
                            list.add(files[j].getName());
                    }
                }
            }
        }
        catch(Exception e)
        {

        }
    }


    // ˢ��listview��TextView����ʾ
//    private void dataChanged() {
//        if(isMulChoice == false)
//        {
//            // set all buttons invisible
//            if (MyAdapter.getPos() == -1) {
//                btnFirst.setVisibility(View.INVISIBLE);
//                btnSecond.setVisibility(View.INVISIBLE);
//                btnThird.setVisibility(View.INVISIBLE);
//                itemStateChanged(0); // nothing
//            }
//            else {
//                btnFirst.setVisibility(View.VISIBLE);
//                btnSecond.setVisibility(View.VISIBLE);
//                btnThird.setVisibility(View.VISIBLE);
//                itemStateChanged(1); // single choice
//            }
//            tv_show.setVisibility(View.INVISIBLE);
//            mAdapter.setMulChoice(false);
//        }
//        else
//        {
//            btnFirst.setVisibility(View.VISIBLE);
//            btnSecond.setVisibility(View.VISIBLE);
//            btnThird.setVisibility(View.INVISIBLE);
//            tv_show.setVisibility(View.VISIBLE);
//            mAdapter.setMulChoice(true);
//            itemStateChanged(2); // multi choice
//        }
//        mAdapter.notifyDataSetChanged();
//
//        tv_show.setText("select " + checkNum + " item(s)");
//    }

    // item state was changed,so let the container know
    private void itemStateChanged(int state)
    {
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
                        File file = new File(dir + "/" + list.get(mAdapter.getPos()));
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
                        File file = new File(dir + "/" + list.get(i));
                        System.out.println(dir + list.get(i));
                        if (!file.delete()) {
                            isSuccessfulDel = false;
                        }
                    }
                    isMulChoice = false;
                    checkNum = 0;
                    // change the button visiblity
                    btnRename.setVisibility(View.VISIBLE);
                    btnPlay.setVisibility(View.VISIBLE);
                    btnInfo.setVisibility(View.VISIBLE);
                    btnStop.setVisibility(View.INVISIBLE);
                    btnPause.setVisibility(View.INVISIBLE);
                    btnDelete.setVisibility(View.INVISIBLE);
                    btnReturn.setVisibility(View.INVISIBLE);
                }
                refreshList();
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
        if(searchKeyword.equals("") == false && keyword.equals("")) // cancel the search function
        {
            searchKeyword = keyword;
            return;
        }
        initData(keyword);
        mAdapter = new MyAdapter(list, getActivity().getApplicationContext());
        lv.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        checkNum = 0;

        searchKeyword = keyword;
    }

    @Override
    public void onShareFile() {

    }

    // play function
    public class onPlayListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            if(isPlaying == false) // start play a record
            {
                stopPlayRecord();
                isPlaying = true;
                curPlayTime = 0;
                // set the visiblity of buttons
                btnRename.setVisibility(View.INVISIBLE);
                btnPlay.setVisibility(View.INVISIBLE);
                btnInfo.setVisibility(View.INVISIBLE);
                btnStop.setVisibility(View.VISIBLE);
                btnPause.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.INVISIBLE);
                btnReturn.setVisibility(View.INVISIBLE);
                // get the selected record path
                String filePath = dir + "/" + list.get(MyAdapter.getPos());
                try {
                    mMediaPlayer = new MediaPlayer();
                    mMediaPlayer.setDataSource(getActivity().getApplicationContext(), Uri.parse(filePath));
                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            isPlaying = false;
                            isPause = false;
                            btnRename.setVisibility(View.VISIBLE);
                            btnPlay.setVisibility(View.VISIBLE);
                            btnInfo.setVisibility(View.VISIBLE);
                            btnStop.setVisibility(View.INVISIBLE);
                            btnPause.setVisibility(View.INVISIBLE);
                            btnDelete.setVisibility(View.INVISIBLE);
                            btnReturn.setVisibility(View.INVISIBLE);
                            mMediaPlayer = null;
                        }
                    });
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mMediaPlayer.start();
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
                        btnInfo.setVisibility(View.INVISIBLE);
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
                    btnInfo.setVisibility(View.INVISIBLE);
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
            if(isPlaying)
                stopPlayRecord();
        }
    }

    // pause function
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
                input.setText(list.get(MyAdapter.getPos()).toCharArray(), 0, list.get(MyAdapter.getPos()).lastIndexOf("."));
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
                        File from = new File(dir + "/" + list.get(MyAdapter.getPos()));
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
            btnInfo.setVisibility(View.VISIBLE);
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
            btnRename.setVisibility(View.VISIBLE);
            btnPlay.setVisibility(View.VISIBLE);
            btnInfo.setVisibility(View.VISIBLE);
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

    // pause function
    public class onInfoListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {

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
}
