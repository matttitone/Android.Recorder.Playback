package com.teamsix.recorddemo.recorddemo;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class AMRRecordUtil extends RecordUtil implements Runnable
{
    // over max record len listener interface

    private MediaRecorder mRecorder = null;
    private int maxRecordLen = 0; // max length of the record;
    private int curRecordLen = 0; // cur length of the record;
    private boolean isOnRecord;  // whether we are on the record
    private boolean isOnPaused;  // whether we are paused
    private OnOverMaxRecordLenListener maxRecordLenListener = null;
    private OnRecordTimeChangeListener recordTimeChangeListener = null;
    private Handler timeMessageHandler;
    private ArrayList<String> listFile; // temp file list for the record

    private String tempPath; // tempfile path for pause

    private Thread threadTimeCounting;     // the thread to count the record time
    private String recordFolderPath;       // record folder path
    private String recordTempFolderPath;  // temp record folder path
    private Context context;

    public AMRRecordUtil(Context context,boolean externalStorage,int maxRecordLen)
    {
        super(context,externalStorage,maxRecordLen);
        super.suffix = ".amr";
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

    // startRecord
    public void startRecord() throws IOException {
        if(isOnRecord)
            return ;
        isOnRecord = true;
        isOnPaused = false;
        if(threadTimeCounting != null) // start record,so we just start to count
        {
            threadTimeCounting.interrupt();
            threadTimeCounting = null;
        }
        curRecordLen = 0;
        threadTimeCounting = new Thread(this);
        threadTimeCounting.start();
        startRecordHelper();
    }

    // return the pause state of the RecordUtil
    public boolean isPause()
    {
        return isOnPaused;
    }

    // return the record state of the RecordUtil
    public boolean isRecord()
    {
        return isOnRecord;
    }

    // save the record
    public void save() {
        if (isOnRecord == true && isPause() == false) // we still recording...
        {
            mRecorder.stop();
        }
        if(isRecord() == false)
        {
            return;
        }
        if (mRecorder != null && isPause() == false)
        {
            listFile.add(tempPath);
            mRecorder.release();
        }
        isOnRecord = false;

        // stop the record time counting
        threadTimeCounting.interrupt();
        threadTimeCounting = null;

        // merge the paused file to a record
        String fileName = getAvailableFileName();


        File bestFile = new File(getAvailableFileName());
        FileOutputStream fileOutputStream = null;


        if (!bestFile.exists()) {
            try {
                bestFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            fileOutputStream = new FileOutputStream(bestFile);


        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < listFile.size(); i++) {
            File file = new File((String) listFile.get(i));
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] myByte = new byte[fileInputStream.available()];
                // file length
                int length = myByte.length;
                // file head
                if (i == 0) {
                    while (fileInputStream.read(myByte) != -1) {
                        fileOutputStream.write(myByte, 0, length);
                    }
                }


                // other file remember to remove the head
                else {
                    while (fileInputStream.read(myByte) != -1) {
                        fileOutputStream.write(myByte, 6, length - 6);
                    }
                }


                fileOutputStream.flush();
                fileInputStream.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // close the stream
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // delete the temp file
        File file = new File(recordTempFolderPath);
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    files[i].delete();
                }
            }
            file.delete();
        }
        // write the record info to mediastore
        //FileUtil.SaveRecordMediaStoreInfo(context,bestFile.getName(),"record_album",bestFile.getAbsolutePath());
//        for (int i = 0; i < listFile.size(); i++)
//        {
//            File file = new File((String) listFile.get(i));
//            if (file.exists()) {
//                file.delete();
//            }
//        }
        listFile = null;

    }

    // pauseRecord
    public void Pause() throws IOException {
        if(isOnRecord == false) // if we are not on the record,we should not do something to respond
        {
            return;
        }

        // get a available filename for the temp file
        if(isOnPaused) // we are now paused,so we just start a new record
        {
            isOnPaused = false;
            startRecordHelper();
        }
        else            // we should save the record,add the file path to the list
        {
            mRecorder.stop();
            mRecorder.release();
            listFile.add(tempPath);
            isOnPaused = true;
        }

    }

    // assistant function for startRecord
    private void startRecordHelper() throws IOException {
        if(listFile == null)
        {
            listFile = new ArrayList<String>();
        }
        mRecorder = null;
        mRecorder = new MediaRecorder();
        tempPath = getAvailableTempFileName();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mRecorder.setOutputFile(tempPath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.prepare();
        mRecorder.start();
    }

    // set the overtime listener
    public void setOverMaxRecordTimeListener(OnOverMaxRecordLenListener listener)
    {
        maxRecordLenListener = listener;
    }

    @Override
    public void setRecordTimeChangeListener(OnRecordTimeChangeListener listener) {
        recordTimeChangeListener = listener;
    }

    @Override
    public int getPerSecFileSize(int quality) {
       return 3*1024;
    }

    @Override
    public void run() {
        while(true)
        {
            try
            {
                Thread.sleep(1000);
                if(isOnRecord && !isOnPaused) // recording now
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
