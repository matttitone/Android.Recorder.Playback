package com.teamsix.recorddemo.recorddemo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
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
    private TextView tvExtension;
    private EditText editFileName;
    private String maxTime = "";
    NotificationCompat.Builder nBuilder;
    NotificationManager mNotificationManager;

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
        if (recordUtil != null) {
            if (!recordUtil.isRecord())
                initRecordUtil();
        }
        else
                initRecordUtil();
        // get the max length of the record

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

        editFileName = (EditText)llLayout.findViewById(R.id.editFileName);
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                if (source instanceof SpannableStringBuilder) {
                    SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder)source;
                    for (int i = end - 1; i >= start; i--) {
                        char currentChar = source.charAt(i);
                        if (!Character.isLetterOrDigit(currentChar)) {
                            Toast.makeText(getActivity(), "Only Characters or Digits allowed!", Toast.LENGTH_LONG);
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
        editFileName.setFilters(new InputFilter[]{filter});
        tvExtension = (TextView)llLayout.findViewById(R.id.tvExtension);

        onRecordTimeChange(0);
        return llLayout;
    }


    // set the text of the textView
    private void setStateText(String text)
    {
        state.setText(text);
    }

    public void initRecordUtil()
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
        // get fileName of the record
        String fileName = editFileName.getText().toString();
        if(fileName.equals(""))
        {
            fileName = recordUtil.getAvailableFileNumber();
        }
        fileName = fileName.replace(recordUtil.getSuffix(),"");
        fileName = fileName.replace("/","");
        tvExtension.setText(recordUtil.getSuffix());
        editFileName.setText(fileName);
        // set fileName of the record
        recordUtil.setRecordFileName(fileName);
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
            if(!recordUtil.isRecord()) {
                initRecordUtil();
                String fileName = recordUtil.getAvailableFileName();
                File file = new File(fileName);
                if (file.exists()) {
                    // ask user if (s)he wants to delete the file
                    android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(getActivity()).create();
                    //set Title
                    alertDialog.setTitle("Confirm");
                    //prompt
                    alertDialog.setMessage("The file " + file.getName() + " already exist,Overwirte?");
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
                            startRecord();
                        }
                    });

                    //show dialog
                    alertDialog.show();
                } else {
                    startRecord();
                }
            }
            else
            {
                startRecord();
            }
        }

    }

    private void startRecord()
    {
        if(!recordUtil.isRecord()) {
            try {
                // check if file exist
                initRecordUtil();
                onRecordTimeChange(0);
                ((OnRecordFinishListener)getActivity()).onRecordStart();

                calcAvailableTime();
                // check if file exist
                recordUtil.startRecord();
                setStateText(getResources().getString(R.string.stateRecord));
                startRecord.setBackground(getResources().getDrawable(R.drawable.pauseicon));
                bBackground.setBackground(getResources().getDrawable(R.drawable.recordonicon));
                stopRecord.setVisibility(View.VISIBLE);
                nBuilder = new NotificationCompat.Builder(getActivity().getApplicationContext());
                nBuilder.setSmallIcon(R.drawable.recordonicon);
                nBuilder.setContentTitle("Recording");
                nBuilder.setContentText("You are Recording");
                nBuilder.setPriority(1);
                Intent resultIntent = new Intent(getActivity(),MainActivity.class);
                resultIntent.setAction(Intent.ACTION_MAIN);
                resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                PendingIntent resultPendingIntent = PendingIntent.getActivity(getActivity().getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                nBuilder.setContentIntent(resultPendingIntent);
                mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
                int nId = 0;
                mNotificationManager.notify(nId,nBuilder.build());

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
                bBackground.setBackground(getResources().getDrawable(R.drawable.recordonpause));

            }
            else
            {
                setStateText(getResources().getString(R.string.stateRecord));
                startRecord.setBackground(getResources().getDrawable(R.drawable.pauseicon));
                bBackground.setBackground(getResources().getDrawable(R.drawable.recordonicon));
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
            mNotificationManager.cancel(0);
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
