package com.teamsix.recorddemo.recorddemo;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Administrator on 2015/7/18.
 */
public abstract class RecordUtil {
    private String recordFolderPath;       // record folder path
    private String recordTempFolderPath;  // temp record folder path
    protected String suffix = "";
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
    protected String getAvailableTempFileName()
    {
        return recordTempFolderPath + "/" + getTime() + suffix;
    }
    protected String getAvailableFileName()
    {
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

        return recordFolderPath + "/Record" + number + suffix;
    }
    private String getTime()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date curDate = new Date(System.currentTimeMillis());// get current time
        String time = formatter.format(curDate);
        return time;
    }
    public abstract void setOverMaxRecordTimeListener(OnOverMaxRecordLenListener listener);
}
