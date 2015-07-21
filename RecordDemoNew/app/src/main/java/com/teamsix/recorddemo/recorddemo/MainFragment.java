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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainFragment extends Fragment implements OnOverMaxRecordLenListener,OnRecordTimeChangeListener {

    private static final String LOG_TAG = "AudioRecordTest";


    //controlers
    private Button startRecord;
    private Button stopRecord;
    private ImageView bBackground;
    private TextView state;
    private TextView recordTime;
    private String maxTime = "";

    RecordUtil recordUtil = null;

    @Override
    public void onRecordTimeChange(int timeInSecond) {
        int millis = timeInSecond * 1000;
        String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
        hms += maxTime;
        recordTime.setText(hms);
    }

    public interface OnRecordFinishListener
    {
        void onRecordFinish();
        void onRecordStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        // get the max length of the record
        initRecordUtil();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentActivity faActivity  = (FragmentActivity)    super.getActivity();
        // Replace LinearLayout by the type of the root element of the layout you're trying to load
        RelativeLayout llLayout    = (RelativeLayout)    inflater.inflate(R.layout.fragment_main, container, false);
        super.onCreate(savedInstanceState);

        // Start the record
        startRecord = (Button)llLayout.findViewById(R.id.startRecord);
        //bind onclick listener
        startRecord.setOnClickListener(new startRecordListener());

        bBackground = (ImageView)llLayout.findViewById(R.id.bBackground);
        // Stop Record
        stopRecord = (Button)llLayout.findViewById(R.id.stopRecord);
        stopRecord.setOnClickListener(new stopRecordListener());
        stopRecord.setVisibility(View.INVISIBLE);


        // check for store sd card
        //checkSDCard = (CheckBox)findViewById(R.id.checkSDCard);

        //State
        state = (TextView)llLayout.findViewById(R.id.state);
        recordTime = (TextView)llLayout.findViewById(R.id.tvRecordTime);
        onRecordTimeChange(0);
        return llLayout;
    }


    // set the text of the textView
    private void setStateText(String text)
    {
        state.setText(text);
    }

    private void initRecordUtil()
    {
        SettingUtil settingUtil = new SettingUtil(getActivity().getApplicationContext());
        int length = settingUtil.getMaxHour()*3600+settingUtil.getMaxMinute()*60+settingUtil.getMaxSecond();
        //recordUtil = new AMRRecordUtil(getActivity().getApplicationContext(),settingUtil.getStoreInSDCard(),length);
        String type = new SettingUtil(getActivity().getApplicationContext()).getRecordFormat();
        if(type.equals("WAV")) {
            recordUtil = new WAVRecordUtil(getActivity().getApplicationContext(), settingUtil.getStoreInSDCard(), length);
        } else
        {
            recordUtil = new AMRRecordUtil(getActivity().getApplicationContext(), settingUtil.getStoreInSDCard(), length);
        }


        recordUtil.setOverMaxRecordTimeListener(this);
        recordUtil.setRecordTimeChangeListener(this);
    }

    void calcAvailableTime()
    {
        SettingUtil settingUtil = new SettingUtil(getActivity().getApplicationContext());
        long settingTime = settingUtil.getMaxHour()*3600+settingUtil.getMaxMinute()*60+settingUtil.getMaxSecond();
        settingTime *= 1000;
        String quality = settingUtil.getRecordQuality();
        int sizePerSecond = 1;
        switch (quality)
        {
            case "High":
                sizePerSecond = recordUtil.getPerSecFileSize(RecordUtil.RECORD_HIGH_QUALITY);
                break;
            case "Middle":
                sizePerSecond = recordUtil.getPerSecFileSize(RecordUtil.RECORD_MIDDLE_QUALITY);
                break;
            case "Low":
                sizePerSecond = recordUtil.getPerSecFileSize(RecordUtil.RECORD_LOW_QUALITY);
                break;
        }
        // calculate the max storage time
        long available = FileUtil.getAvailableStorageSpace(settingUtil.getStoreInSDCard());
        double availableTime = (double)available/sizePerSecond*1000;
        if(settingTime > availableTime)
            settingTime = (long)availableTime;
        maxTime = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(settingTime),
                TimeUnit.MILLISECONDS.toMinutes(settingTime) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(settingTime) % TimeUnit.MINUTES.toSeconds(1));
        maxTime = "/" + maxTime;
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
                    onRecordTimeChange(0);
                    ((OnRecordFinishListener)getActivity()).onRecordStart();
                    initRecordUtil();
                    calcAvailableTime();
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
            onRecordTimeChange(0);
            recordUtil.save();
            setStateText(getResources().getString(R.string.stateStopRecord));
            Toast.makeText(getActivity().getApplicationContext(),"Save Record Successfully!",Toast.LENGTH_SHORT).show();
            stopRecord.setVisibility(View.INVISIBLE);
            startRecord.setBackground(getResources().getDrawable(R.drawable.recordicon));
            bBackground.setBackground(getResources().getDrawable(R.drawable.recordofficon));
            ((OnRecordFinishListener)getActivity()).onRecordFinish();
            recordTime.setText("00:00:00");
            //calcAvailableTime();
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
