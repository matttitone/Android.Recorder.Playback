package com.teamsix.recorddemo.recorddemo;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Administrator on 2015/7/15.
 */
public class AMRRecordUtil
{
    private MediaRecorder mRecorder = null;
    private boolean isOnRecord;  // whether we are on the record
    private boolean isOnPaused;  // whether we are paused
    private ArrayList<String> listFile; // temp file list for the record

    private final String suffix = ".amr";

    private String tempPath; // tempfile path for pause

    private String recordFolderPath;       // record folder path
    private String recordTempFolderPath;  // temp record folder path

    public AMRRecordUtil(Context context,boolean externalStorage)
    {

        recordFolderPath = FileUtil.getRecordFolderPath(context, externalStorage);
        recordTempFolderPath = FileUtil.getRecordTempFolderPath(context,externalStorage);
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
        for (int i = 0; i < listFile.size(); i++)
        {
            File file = new File((String) listFile.get(i));
            if (file.exists()) {
                file.delete();
            }
        }
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

    // get a temp file name for the record
    private String getAvailableTempFileName()
    {
        return recordTempFolderPath + "/" + getTime() + suffix;
    }

    // get a file name for the combined record file
    private String getAvailableFileName()
    {
        ArrayList<String> fileList = new ArrayList<String>();

        File file = new File(recordFolderPath);
        try
        {
            File[] files = file.listFiles();
            if (files.length > 0)
            {
                for (int j = 0; j < files.length; j++)
                {
                    if (!files[j].isDirectory())
                    {
                        fileList.add(files[j].getName());
                    }
                }
            }
        }
        catch(Exception e)
        {

        }
        int fileNum = 0;

        // get the maximum number of the record file
        for(int i = 0; i < fileList.size(); i++)
        {
            try
            {
                int num = 0;
                num = Integer.parseInt(fileList.get(i).replace("Record", "").replace(suffix,""));
                if (num > fileNum) {
                    fileNum = num;
                }
            }
            catch (Exception e)
            {

            }
        }

        fileNum++;

        return recordFolderPath + "/Record" + fileNum + suffix;

    }

    // get the current time for string format
    private String getTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date curDate = new Date(System.currentTimeMillis());// get current time
        String time = formatter.format(curDate);
        return time;
    }
}
