package com.teamsix.recorddemo.recorddemo;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2015/7/18.
 */
public class WAVRecordUtil extends RecordUtil implements Runnable {


    class AudioRecordThread implements Runnable {
        @Override
        public void run() {
            writeDateTOFile();// write to file
            copyWaveFile(AudioName, NewAudioName);// convert
        }
    }

    //media operation object
    private int audioSource = MediaRecorder.AudioSource.MIC;
    // the rate of sampling 44100 better,or may be 22050 or 16000 or 11025
    private static int sampleRateInHz = 44100;
    // CHANNEL_IN_STEREO for both channel for CHANNEL_CONFIGURATION_MONO? for single channel
    private static int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    // set bit for PCM  stream
    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    // buffer to save temp data
    private int bufferSizeInBytes = 0;

    private AudioRecord audioRecord;
    private boolean isRecord = false;// whether we are recording between start and stop
    private boolean isPaused = false; // if we are paused.

    private OnOverMaxRecordLenListener maxRecordLenListener = null;
    private OnRecordTimeChangeListener recordTimeChangeListener = null;
    private Handler timeMessageHandler;

    //AudioName for pcm stream
    private String AudioName = "";
    //NewAudioName for wav file
    private String NewAudioName = "";

    private Thread threadTimeCounting;     // the thread to count the record time
    private String recordFolderPath;       // record folder path
    private String recordTempFolderPath;  // temp record folder path
    private Context context;

    private int maxRecordLen = 0; // max length of the record;
    private int curRecordLen = 0; // cur length of the record;

    public WAVRecordUtil(Context context,boolean externalStorage,int maxRecordLen) {
        super(context, externalStorage, maxRecordLen);
        super.suffix = ".wav";
        recordFolderPath = FileUtil.getRecordFolderPath(context, externalStorage);
        recordTempFolderPath = FileUtil.getRecordTempFolderPath(context, externalStorage);
        this.context = context;


        if(maxRecordLen > 0)
            this.maxRecordLen = maxRecordLen;
        else
            this.maxRecordLen = 0;
        threadTimeCounting = null;

        timeMessageHandler = new Handler() {
            public void handleMessage(Message msg) {
                if(isRecord == false)
                    return;
                switch (msg.what) {
                    case 1:
                        if(maxRecordLenListener!=null)
                        {
                            maxRecordLenListener.onOverRecordLength();
                        }
                        break;
                    case 2:
                        if(recordTimeChangeListener!=null)
                        {
                            recordTimeChangeListener.onRecordTimeChange(curRecordLen);
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        };
        //recordFolderPath = path + "/Records";
        //recordTempFolderPath = path + "/Records/temp";

        File file = new File(recordFolderPath);
        // if the folder does not exist,we will create it
        if (!file.exists()) {
            file.mkdir();
        }
        file = new File(recordTempFolderPath);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    @Override
    public void startRecord() throws IOException {
        // get the minimum buffer size of record
        NewAudioName = getAvailableFileName();
        AudioName = getAvailableTempFileName();
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig, audioFormat);
        // generate new record
        audioRecord = null;
        audioRecord = new AudioRecord(audioSource, sampleRateInHz,
                channelConfig, audioFormat, bufferSizeInBytes);

        audioRecord.startRecording();
        isPaused = false;
        isRecord = true;
        curRecordLen = 0;
        // start a new thread to record
        new Thread(new AudioRecordThread()).start();

        if(threadTimeCounting != null) // start record,so we just start to count
        {
            threadTimeCounting.stop();
            threadTimeCounting = null;
        }
        curRecordLen = 0;
        threadTimeCounting = new Thread(this);
        threadTimeCounting.start();
    }

    @Override
    public boolean isPause() {
        return isPaused;
    }

    @Override
    public boolean isRecord() {
        return isRecord;
    }

    @Override
    public void save() {
        // stop the record time counting
        if(threadTimeCounting != null)
            threadTimeCounting.interrupt();
        threadTimeCounting = null;
        if (audioRecord != null) {
            System.out.println("stopRecord");
            isRecord = false;
            isPaused = false;
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            curRecordLen = 0;
            // delete temp file
            File file = new File(AudioName);
            if(file.isFile())
            {
                file.delete();
            }
        }
    }

    @Override
    public void Pause() throws IOException {
        if(isRecord == false)
            return;
        if(isPaused) {
            System.out.println("resumeRecord");
            isPaused = false;
        }
        else {
            System.out.println("pauseRecord");
            isPaused = true;
        }
    }

    @Override
    public void setOverMaxRecordTimeListener(OnOverMaxRecordLenListener listener) {
        maxRecordLenListener = listener;
    }

    @Override
    public void setRecordTimeChangeListener(OnRecordTimeChangeListener listener) {
        recordTimeChangeListener = listener;
    }

    @Override
    public int getPerSecFileSize(int quality) {
        switch (quality)
        {
            case RECORD_LOW_QUALITY:
                return 80*1024;
            case RECORD_MIDDLE_QUALITY:
                return 120*1024;
            default:
                return 150*1024;
        }
    }

    private void writeDateTOFile() {
        // write pcm stream to file
        byte[] audiodata = new byte[bufferSizeInBytes];
        FileOutputStream fos = null;
        int readsize = 0;
        try {
            File file = new File(AudioName);
            if (file.exists()) {
                file.delete();
            }
            fos = new FileOutputStream(file);// ??????????????????
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (isRecord == true) {
            readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
            if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {
                if(isPaused == false) {
                    try {
                        fos.write(audiodata);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        try {
            fos.close();// ???§Õ????
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // change pcm stream to wav
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

    @Override
    public void run() {
        while(true)
        {
            try
            {
                Thread.sleep(1000);
                if(isRecord == false)
                    return;
                if(isRecord && !isPaused) // recording now
                {
                    curRecordLen++;
                    Message message = new Message();
                    message.what = 2;
                    timeMessageHandler.sendMessage(message);
                }
                if(curRecordLen >= maxRecordLen)
                {
                    Message message = new Message();
                    message.what = 1;
                    timeMessageHandler.sendMessage(message);
                    return;
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
