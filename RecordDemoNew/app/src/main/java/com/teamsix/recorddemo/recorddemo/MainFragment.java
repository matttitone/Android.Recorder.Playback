package com.teamsix.recorddemo.recorddemo;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class MainFragment extends Fragment implements OnOverMaxRecordLenListener {

    private static final String LOG_TAG = "AudioRecordTest";


    //controlers
    private Button startRecord;
    private Button stopRecord;
    private Button bBackground;
    private TextView state;


    RecordUtil recordUtil = null;

    public interface OnRecordFinishListener
    {
        void onRecordFinish();
    }

    @Override
    public void onResume() {
        super.onResume();
        // get the max length of the record
        SettingUtil settingUtil = new SettingUtil(getActivity().getApplicationContext());
        int length = settingUtil.getMaxHour()*3600+settingUtil.getMaxMinute()*60+settingUtil.getMaxSecond();
        //recordUtil = new AMRRecordUtil(getActivity().getApplicationContext(),settingUtil.getStoreInSDCard(),length);
        recordUtil = new WAVRecordUtil(getActivity().getApplicationContext(),settingUtil.getStoreInSDCard(),length);
        recordUtil.setOverMaxRecordTimeListener(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentActivity faActivity  = (FragmentActivity)    super.getActivity();
        // Replace LinearLayout by the type of the root element of the layout you're trying to load
        LinearLayout llLayout    = (LinearLayout)    inflater.inflate(R.layout.fragment_main, container, false);
        super.onCreate(savedInstanceState);

        // Start the record
        startRecord = (Button)llLayout.findViewById(R.id.startRecord);
        //bind onclick listener
        startRecord.setOnClickListener(new startRecordListener());


        // Stop Record
        stopRecord = (Button)llLayout.findViewById(R.id.stopRecord);
        stopRecord.setOnClickListener(new stopRecordListener());
        stopRecord.setVisibility(View.INVISIBLE);


        bBackground = (Button)llLayout.findViewById(R.id.bBackground);

        // check for store sd card
        //checkSDCard = (CheckBox)findViewById(R.id.checkSDCard);

        //State
        state = (TextView)llLayout.findViewById(R.id.state);

        return llLayout;
    }


    // set the text of the textView
    private void setStateText(String text)
    {
        state.setText(text);
    }


    @Override
    public void onOverRecordLength() {
        new stopRecordListener().onClick(null);
        setStateText(getResources().getString(R.string.stateOverTimeStopRecord));
        Toast.makeText(getActivity().getApplicationContext(),"Save Record Successfully!",Toast.LENGTH_SHORT).show();
    }


    // start record
    class startRecordListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if(!recordUtil.isRecord()) {
                try {
                    //recordUtil = new AMRRecordUtil(getApplicationContext(), checkSDCard.isChecked());
                    recordUtil.startRecord();
                    setStateText(getResources().getString(R.string.stateRecord));
                    startRecord.setBackground(getResources().getDrawable(R.drawable.pauseicon));
                    bBackground.setBackground(getResources().getDrawable(R.drawable.recordonicon));
                    stopRecord.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    Log.e(LOG_TAG, Log.getStackTraceString(e));
                    Toast.makeText(getActivity().getApplicationContext(), "Start Record Failed!", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                try {
                    recordUtil.Pause();
                } catch (IOException e) {
                    Log.e(LOG_TAG,Log.getStackTraceString(e));
                    Toast.makeText(getActivity().getApplicationContext(),"Pause Record Failed!",Toast.LENGTH_SHORT).show();
                }
                // set state
                if(recordUtil.isPause())
                {
                    setStateText(getResources().getString(R.string.statePause));
                    startRecord.setBackground(getResources().getDrawable(R.drawable.recordicon));
                    bBackground.setBackground(getResources().getDrawable(R.drawable.recordofficon));
                }
                else
                {
                    setStateText(getResources().getString(R.string.stateRecord));
                    startRecord.setBackground(getResources().getDrawable(R.drawable.pauseicon));
                    bBackground.setBackground(getResources().getDrawable(R.drawable.recordonicon));
                }
            }
        }

    }

    //stop record
    class stopRecordListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            recordUtil.save();
            setStateText(getResources().getString(R.string.stateStopRecord));
            Toast.makeText(getActivity().getApplicationContext(),"Save Record Successfully!",Toast.LENGTH_SHORT).show();
            stopRecord.setVisibility(View.INVISIBLE);
            startRecord.setBackground(getResources().getDrawable(R.drawable.recordicon));
            bBackground.setBackground(getResources().getDrawable(R.drawable.recordofficon));
            ((OnRecordFinishListener)getActivity()).onRecordFinish();
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
