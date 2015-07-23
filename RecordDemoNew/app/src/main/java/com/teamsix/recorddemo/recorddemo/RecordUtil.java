package com.teamsix.recorddemo.recorddemo;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2015/7/18.
 */

interface OnOverMaxRecordLenListener
{
    void onOverRecordLength();
}

interface OnRecordTimeChangeListener
{
    void onRecordTimeChange(int timeInSecond);
}

public abstract class RecordUtil {
    private String recordFolderPath;       // record folder path
    private String recordTempFolderPath;  // temp record folder path
    protected String suffix = "";
    public static final int RECORD_HIGH_QUALITY = 1;
    public static final int RECORD_MIDDLE_QUALITY = 2;
    public static final int RECORD_LOW_QUALITY = 3;


    private String recordFileName;         // the record file name which user want to save

    public RecordUtil(Context context,boolean externalStorage,int maxRecordLen)
    {
        recordFolderPath = FileUtil.getRecordFolderPath(context, externalStorage);
        recordTempFolderPath = FileUtil.getRecordTempFolderPath(context, externalStorage);
    }
    public abstract void startRecord() throws IOException;
    public abstract boolean isPause();
    public abstract boolean isRecord();
    public abstract void save();
    public abstract void Pause() throws IOException;
    public String getAvailableTempFileName()
    {
        return recordTempFolderPath + "/" + getTime() + suffix;
    }

    public String getAvailableFileNumber()
    {
        if(recordFileName != null)
        {
            if(!recordFileName.equals(""))
            {
                if(recordFileName.endsWith(suffix))
                    return recordFileName;
                else
                    return recordFileName + suffix;
            }
        }
        File file = new File(recordFolderPath);
        int number = 0;
        try
        {
            File[] files = file.listFiles();
            if (files.length > 0)
            {
                for (int j = 0; j < files.length; j++)
                {
                    if (!files[j].isDirectory())
                    {
                        String fileName = files[j].getName();
                        fileName = fileName.substring(0,fileName.lastIndexOf('.'));
                        try {
                            int num = Integer.parseInt(fileName.replace("Record", ""));
                            if (num > number)
                                number = num;
                        }
                        catch (Exception e)
                        {

                        }
                    }
                }
            }
        }
        catch(Exception e)
        {

        }

        number++;

        return "/Record" + number + suffix;
    }

    protected String getAvailableFileName()
    {
        return recordFolderPath +"/"+ getAvailableFileNumber();
    }
    private String getTime()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date curDate = new Date(System.currentTimeMillis());// get current time
        String time = formatter.format(curDate);
        return time;
    }
    public static String getHMSTime(int ms)
    {
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(ms),
                TimeUnit.MILLISECONDS.toMinutes(ms) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(ms) % TimeUnit.MINUTES.toSeconds(1));
    }
    public String getRecordFileName() {
        return recordFileName;
    }

    public void setRecordFileName(String recordFileName) {
        this.recordFileName = recordFileName;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public abstract void setOverMaxRecordTimeListener(OnOverMaxRecordLenListener listener);
    public abstract void setRecordTimeChangeListener(OnRecordTimeChangeListener listener);
    public abstract int getPerSecFileSize(int quality);
}
