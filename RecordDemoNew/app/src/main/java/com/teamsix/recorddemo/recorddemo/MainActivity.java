package com.teamsix.recorddemo.recorddemo;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    private static final String LOG_TAG = "AudioRecordTest";
    //save path of the record file
    private String FileName = null;

    //list of record segment
    private ArrayList<String> listRecord;

    // flags
    private boolean isPause; // flag on whether we have paused during this record
    private boolean inPause; // flag on whether we are now pause

    //controlers
    private Button startRecord;
    private Button stopRecord;
    private Button pauseRecord;
    private Button showRecordList;
    private CheckBox checkSDCard;
    private TextView state;

    //media operation object
    private MediaPlayer mPlayer = null;
    private MediaRecorder mRecorder = null;

    AMRRecordUtil recordUtil = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recordUtil = new AMRRecordUtil(getApplicationContext(),true);

        listRecord = new ArrayList<String>();

        // Start the record
        startRecord = (Button)findViewById(R.id.startRecord);
        //bind onclick listener
        startRecord.setOnClickListener(new startRecordListener());

        // Pause the record
        pauseRecord = (Button)findViewById(R.id.pauseRecord);
        //bind onclick listener
        pauseRecord.setOnClickListener(new pauseListener());

        // Stop Record
        stopRecord = (Button)findViewById(R.id.stopRecord);
        stopRecord.setOnClickListener(new stopRecordListener());

        // Show Record List
        showRecordList = (Button)findViewById(R.id.showRecordList);
        showRecordList.setOnClickListener(new showRecordListListener());

        // check for store sd card
        checkSDCard = (CheckBox)findViewById(R.id.checkSDCard);

        //State
        state = (TextView)findViewById(R.id.state);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    // set the text of the textView
    private void setStateText(String text)
    {
        state.setText(text);
    }


    // start record
    class startRecordListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            try {
                if(recordUtil.isRecord())
                    return;
                recordUtil = new AMRRecordUtil(getApplicationContext(),checkSDCard.isChecked());
                recordUtil.startRecord();
                setStateText(getResources().getString(R.string.stateRecord));
            } catch (IOException e) {
                Log.e(LOG_TAG,Log.getStackTraceString(e));
                Toast.makeText(getApplicationContext(),"Start Record Failed!",Toast.LENGTH_SHORT).show();
            }
            /*
            isPause = false;
            inPause = false;

            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            mRecorder.setOutputFile(FileName);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed");
            }
            // set state
            setStateText( getResources().getString(R.string.stateRecord));
            mRecorder.start();
            */
        }

    }

    class showRecordListListener implements View.OnClickListener
    {

        @Override
        public void onClick(View v) {
            Intent intent=new Intent();
            intent.putExtra("isStoreToSDCard", checkSDCard.isChecked());
            intent.setClass(MainActivity.this, RecordListActivity.class);
            startActivity(intent);
        }
    }
    //stop record
    class stopRecordListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            recordUtil.save();
            setStateText(getResources().getString(R.string.stateStopRecord));
            Toast.makeText(getApplicationContext(),"Save Record Successfully!",Toast.LENGTH_SHORT).show();
            /*
            isPause = false;
            inPause = false;
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            // set state
            setStateText( getResources().getString(R.string.stateStopRecord));
            */
        }

    }


    //pause listener
    class pauseListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            try {
                if(recordUtil.isRecord() == false)
                    return;
                recordUtil.Pause();
            } catch (IOException e) {
                Log.e(LOG_TAG,Log.getStackTraceString(e));
                Toast.makeText(getApplicationContext(),"Pause Record Failed!",Toast.LENGTH_SHORT).show();
            }
            // set state
            if(recordUtil.isPause())
            {
                setStateText(getResources().getString(R.string.statePause));
            }
            else
            {
                setStateText(getResources().getString(R.string.stateRecord));
            }
        }

    }

    // Play Complete
    class playCompleteListener implements MediaPlayer.OnCompletionListener
    {

        @Override
        public void onCompletion(MediaPlayer mp) {
            // set state
            setStateText( getResources().getString(R.string.stateStopPlay));
        }
    }
}
