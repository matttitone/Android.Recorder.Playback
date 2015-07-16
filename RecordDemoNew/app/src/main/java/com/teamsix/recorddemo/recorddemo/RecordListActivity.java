package com.teamsix.recorddemo.recorddemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class RecordListActivity extends ActionBarActivity {

    private static final String LOG_TAG = "AudioRecordTest";

    private ListView lv;  // listview
    private MyAdapter mAdapter;
    private ArrayList<String> list; // string of the data
    private Button btnFirst;// return button
    private Button btnSecond;
    private Button btnThird;
    private int checkNum; // total selected number
    private TextView tv_show; // show the selected number
    private boolean isMulChoice; // whether we are in mulchoice mode
    private boolean isStoreToSDCard = false; // whether we store the record on sd card
    private File dir;

    private MediaPlayer mMediaPlayer = null;
    private int curPlayTime = 0; // the current play time for pause function
    private boolean isPlaying = false; // whether we are playing records
    private boolean isPause = false;   // whether we are paused

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        // get whether we are now store the records in the sdcard
        Intent intent=getIntent();
        isStoreToSDCard = intent.getBooleanExtra("isStoreToSDCard",false);
        dir = new File(FileUtil.getRecordFolderPath(getApplicationContext(),isStoreToSDCard));
        lv = (ListView) findViewById(R.id.listView);
        btnFirst = (Button)findViewById(R.id.btnFirst);
        btnSecond = (Button)findViewById(R.id.btnSecond);
        btnThird = (Button)findViewById(R.id.btnThird);
        tv_show = (TextView)findViewById(R.id.tvNumber);

        list = new ArrayList<String>();
        isMulChoice = false;

        initData();


        mAdapter = new MyAdapter(list,this);
        lv.setAdapter(mAdapter);

        // cancel the mulchoice
        btnFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isMulChoice == true) // now that will be the return function
                {
                    isMulChoice = false;
                    checkNum = 0;
                    // change the selector of the buttons
                    Drawable firstDrawable = getResources().getDrawable(R.drawable.selector_icon_rename);
                    btnFirst.setBackground(firstDrawable);
                    Drawable secondDrawable = getResources().getDrawable(R.drawable.selector_icon_play);
                    btnSecond.setBackground(secondDrawable);
                    dataChanged();
                    invalidateOptionsMenu();
                }
                else // now that will be the rename function
                {
                    AlertDialog.Builder builderR = new AlertDialog.Builder(RecordListActivity.this);
                    builderR.setTitle("Rename file");
                    builderR.setCancelable(true);


                    final EditText input = new EditText(getApplicationContext());
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
                            if(input.getText().toString().equals("")){
                                Toast.makeText(getApplicationContext(), "Please enter a name for the file", Toast.LENGTH_LONG);
                            }
                            else{
                                File from = new File(dir + "/" + list.get(MyAdapter.getPos()));
                                // get the extension of the record file
                                String suffix = "";
                                try
                                {
                                    suffix = from.getName().substring(from.getName().lastIndexOf("."),from.getName().length());
                                }
                                catch (Exception e)
                                {

                                }
                                // get the filename with extension
                                String fileName = input.getText().toString();
                                // get all the name before '.'
                                if(!fileName.endsWith(suffix))
                                {
                                    fileName = fileName + suffix;
                                }
                                File to = new File(dir + "/" + fileName);


                                if(from.renameTo(to)){
                                    System.out.println("The position is " + MyAdapter.getPos());
                                    /** I have to do it here, don't I? How should I update the listview with the renamed file name?     **/
                                    initData();
                                    mAdapter = new MyAdapter(list,getApplicationContext());
                                    lv.setAdapter(mAdapter);
                                    mAdapter.notifyDataSetChanged();
                                    checkNum = 0;
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


                    dataChanged();
                }
            }
        });

        // play and delete function
        btnSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isMulChoice == false) // now that will be the play function
                {
                    //Toast.makeText(getApplicationContext(),"We will play the records now...",Toast.LENGTH_SHORT).show();
                    if(isPlaying == false) // start play a record
                    {
                        stopPlayRecord();
                        isPlaying = true;
                        curPlayTime = 0;
                        // set the visiblity of buttons
                        btnFirst.setVisibility(View.INVISIBLE);
                        btnThird.setVisibility(View.INVISIBLE);
                        // set the selector of the button
                        Drawable secondDrawable = getResources().getDrawable(R.drawable.selector_icon_pause);
                        btnSecond.setBackground(secondDrawable);
                        // get the selected record path
                        String filePath = dir + "/" + list.get(MyAdapter.getPos());
                        try {
                            mMediaPlayer = new MediaPlayer();
                            mMediaPlayer.setDataSource(getApplicationContext(), Uri.parse(filePath));
                            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    isPlaying = false;
                                    isPause = false;
                                    Drawable secondDrawable = getResources().getDrawable(R.drawable.selector_icon_play);
                                    btnSecond.setBackground(secondDrawable);
                                    mMediaPlayer = null;
                                    btnFirst.setVisibility(View.VISIBLE);
                                    btnThird.setVisibility(View.VISIBLE);
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
                    else
                    {
                        if(isPause == true) {
                            isPause = false;
                            if (mMediaPlayer != null) {
                                // change the drawable
                                Drawable secondDrawable = getResources().getDrawable(R.drawable.selector_icon_pause);
                                btnSecond.setBackground(secondDrawable);
                                mMediaPlayer.seekTo(curPlayTime);
                                mMediaPlayer.start();
                            }
                        }
                        else
                        {
                            curPlayTime = mMediaPlayer.getCurrentPosition();
                            mMediaPlayer.pause();
                            // change the drawable
                            Drawable secondDrawable = getResources().getDrawable(R.drawable.selector_icon_play);
                            btnSecond.setBackground(secondDrawable);
                            isPause = true;
                        }
                    }
                }
                else // now that will be the share function
                {
                    // ask user if (s)he wants to delete the file
                    AlertDialog alertDialog = new AlertDialog.Builder(RecordListActivity.this).create();

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
                            // delete file
                            HashMap<Integer,Boolean> isSelected = MyAdapter.getIsSelected();
                            for(int i = 0 ; i < isSelected.size(); i++) {
                                if(isSelected.get(i) == false)
                                {
                                    continue;
                                }
                                File file = new File(dir + "/" + list.get(i));
                                System.out.println(dir + list.get(i));
                                if (!file.delete()) {
                                    isSuccessfulDel = false;
                                }
                            }
                            initData();
                            mAdapter = new MyAdapter(list,getApplicationContext());
                            lv.setAdapter(mAdapter);
                            mAdapter.notifyDataSetChanged();
                            btnFirst.callOnClick();
                            if(isSuccessfulDel) {
                                Toast.makeText(getApplicationContext(), "File Deleted", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(), "File Can't Deleted Totally", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    //show dialog
                    alertDialog.show();
                }
            }
        });

        // detail function
        btnThird.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"We will show the records detail...",Toast.LENGTH_SHORT).show();
            }
        });

        // ��listView�ļ�����
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(isMulChoice) {
                    ViewHolder holder = (ViewHolder) view.getTag();

                    holder.cb.toggle();

                    MyAdapter.getIsSelected().put(position, holder.cb.isChecked());

                    if (holder.cb.isChecked() == true) {
                        checkNum++;
                    }
                    else {
                        checkNum--;
                    }

                    tv_show.setText("select " + checkNum + " item(s)");
                    if (checkNum == 0)
                        btnFirst.performClick();
                }
                else // not multichoice
                {
                    // stop the record
                    stopPlayRecord();
                    MyAdapter.setPos(position);
                    dataChanged();
                    //Toast.makeText(getApplicationContext(),"click" + position,Toast.LENGTH_SHORT).show();
                }
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                stopPlayRecord();
                isMulChoice = true;
                // change the selector of the buttons
                Drawable firstDrawable = getResources().getDrawable(R.drawable.selector_icon_return);
                btnFirst.setBackground(firstDrawable);
                Drawable secondDrawable = getResources().getDrawable(R.drawable.selector_icon_delete);
                btnSecond.setBackground(secondDrawable);

                MyAdapter.setPos(-1);
                for(int i = 0; i < list.size(); i ++)
                {
                    MyAdapter.getIsSelected().put(i,false);
                }
                dataChanged();
                invalidateOptionsMenu();
                return false;
            }
        });

        dataChanged();
    }

    public void stopPlayRecord()
    {
        if(mMediaPlayer != null) {
            mMediaPlayer.stop();
            curPlayTime = 0;
            isPause = false;
            isPlaying = false;
            mMediaPlayer = null;
            Drawable secondDrawable = getResources().getDrawable(R.drawable.selector_icon_play);
            btnSecond.setBackground(secondDrawable);
        }
        // set the visible of button
        btnFirst.setVisibility(View.VISIBLE);
        btnThird.setVisibility(View.VISIBLE);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(isMulChoice) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_record, menu);
        }
        else
        {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if(isMulChoice) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_record, menu);
        }
        else
        {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            return true;
        }
        if (id == R.id.action_setting) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // ��ʼ������
    private void initData() {
        list = null;
        list = new ArrayList<>();
        File file = new File(FileUtil.getRecordFolderPath(getApplicationContext(),isStoreToSDCard));
        try
        {
            File[] files = file.listFiles();
            if (files.length > 0)
            {
                for (int j = 0; j < files.length; j++)
                {
                    if (!files[j].isDirectory())
                    {
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
    private void dataChanged() {
        if(isMulChoice == false)
        {
            // set all buttons invisible
            if (MyAdapter.getPos() == -1) {
                btnFirst.setVisibility(View.INVISIBLE);
                btnSecond.setVisibility(View.INVISIBLE);
                btnThird.setVisibility(View.INVISIBLE);
            }
            else {
                btnFirst.setVisibility(View.VISIBLE);
                btnSecond.setVisibility(View.VISIBLE);
                btnThird.setVisibility(View.VISIBLE);
            }
            tv_show.setVisibility(View.INVISIBLE);
            mAdapter.setMulChoice(false);
        }
        else
        {
            btnFirst.setVisibility(View.VISIBLE);
            btnSecond.setVisibility(View.VISIBLE);
            btnThird.setVisibility(View.INVISIBLE);
            tv_show.setVisibility(View.VISIBLE);
            mAdapter.setMulChoice(true);
        }
        mAdapter.notifyDataSetChanged();

        tv_show.setText("select " + checkNum + " item(s)");
    }



}
