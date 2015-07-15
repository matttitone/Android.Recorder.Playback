package com.teamsix.recorddemo.audiorecordtest;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;



public class MainActivity extends Activity {

    private static final String LOG_TAG = "AudioRecordTest";
    //save path of the record file
    private String FileName = null;

    //list of record segment
    private ArrayList<String> listRecord;


    //controlers
    private Button startRecord;
    private Button startPlay;
    private Button stopRecord;
    private Button stopPlay;
    private Button pauseRecord;
    private TextView state;

    //media operation object
    // ��Ƶ��ȡԴ
    private int audioSource = MediaRecorder.AudioSource.MIC;
    // ������Ƶ�����ʣ�44100��Ŀǰ�ı�׼������ĳЩ�豸��Ȼ֧��22050��16000��11025
    private static int sampleRateInHz = 44100;
    // ������Ƶ��¼�Ƶ�����CHANNEL_IN_STEREOΪ˫������CHANNEL_CONFIGURATION_MONOΪ������
    private static int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    // ��Ƶ���ݸ�ʽ:PCM 16λÿ����������֤�豸֧�֡�PCM 8λÿ����������һ���ܵõ��豸֧�֡�
    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    // �������ֽڴ�С
    private int bufferSizeInBytes = 0;
    private Button Start;
    private Button Stop;
    private AudioRecord audioRecord;
    private boolean isRecord = false;// ��������¼�Ƶ�״̬
    //AudioName����Ƶ�����ļ�
    private static final String AudioName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/love.raw";
    //NewAudioName�ɲ��ŵ���Ƶ�ļ�
    //private static final String NewAudioName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/new.wav";

    private  String NewAudioName = "";
    //AMRRecordUtil recordUtil = new AMRRecordUtil();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        File dir = getFilesDir();
        NewAudioName = dir + "/new.wav";
        System.out.println(NewAudioName);
        creatAudioRecord();
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

        // Start Play
        startPlay = (Button)findViewById(R.id.startPlay);
        //bind onclick listener
        startPlay.setOnClickListener(new startPlayListener());

        // Pause Play
        pauseRecord = (Button)findViewById(R.id.pauseRecord);

        //Stop Play
        stopPlay = (Button)findViewById(R.id.stopPlay);
        stopPlay.setOnClickListener(new stopPlayListener());

        //State
        state = (TextView)findViewById(R.id.state);

        //Set the save path
        FileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        FileName += "/audiorecordtest.amr";

    }

    private void creatAudioRecord() {
        // ��û������ֽڴ�С
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig, audioFormat);
        // ����AudioRecord����
        audioRecord = new AudioRecord(audioSource, sampleRateInHz,
                channelConfig, audioFormat, bufferSizeInBytes);
    }


    private void startRecord() {
        audioRecord.startRecording();
        // ��¼��״̬Ϊtrue
        isRecord = true;
        // ������Ƶ�ļ�д���߳�
        new Thread(new AudioRecordThread()).start();
    }

    private void stopRecord() {
        close();
    }

    private void close() {
        if (audioRecord != null) {
            System.out.println("stopRecord");
            isRecord = false;//ֹͣ�ļ�д��
            audioRecord.stop();
            audioRecord.release();//�ͷ���Դ
            audioRecord = null;
        }
    }

    private boolean pause()
    {
        if (audioRecord != null) {
            if(isRecord) {
                System.out.println("pauseRecord");
                isRecord = false;//ֹͣ�ļ�д��
                //audioRecord.stop();
                return true;
            }
            else {
                //audioRecord.startRecording();
                isRecord = true;
                return false;
            }
        }
        return false;
    }
    class AudioRecordThread implements Runnable {
        @Override
        public void run() {
            writeDateTOFile();//���ļ���д��������
            copyWaveFile(AudioName, NewAudioName);//�������ݼ���ͷ�ļ�
        }
    }

    private void writeDateTOFile() {
        // newһ��byte����������һЩ�ֽ����ݣ���СΪ��������С
        byte[] audiodata = new byte[bufferSizeInBytes];
        FileOutputStream fos = null;
        int readsize = 0;
        try {
            File file = new File(AudioName);
            if (file.exists()) {
                file.delete();
            }
            fos = new FileOutputStream(file);// ����һ���ɴ�ȡ�ֽڵ��ļ�
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (isRecord == true) {
            readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
            if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {
                try {
                    fos.write(audiodata);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            fos.close();// �ر�д����
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ����õ��ɲ��ŵ���Ƶ�ļ�
    private void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = sampleRateInHz;
        int channels = 2;
        long byteRate = 16 * sampleRateInHz * channels / 8;
        byte[] data = new byte[bufferSizeInBytes];
        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * �����ṩһ��ͷ��Ϣ��������Щ��Ϣ�Ϳ��Եõ����Բ��ŵ��ļ���
     * Ϊ��Ϊɶ������44���ֽڣ��������û�����о�������������һ��wav
     * ��Ƶ���ļ������Է���ǰ���ͷ�ļ�����˵����һ��Ŷ��ÿ�ָ�ʽ���ļ�����
     * �Լ����е�ͷ�ļ���
     */
    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
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
            startRecord();
            setStateText(getResources().getString(R.string.stateRecord));
        }

    }



    //stop record
    class stopRecordListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            stopRecord();
            setStateText(getResources().getString(R.string.stateStopRecord));
        }

    }
    //play record
    class startPlayListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

        }

    }
    //stop record
    class stopPlayListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

        }

    }

    //pause listener
    class pauseListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if(pause()) {
                setStateText(getResources().getString(R.string.statePause));
            }
            else{
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
