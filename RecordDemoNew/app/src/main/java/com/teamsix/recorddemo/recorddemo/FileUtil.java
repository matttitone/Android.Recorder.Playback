package com.teamsix.recorddemo.recorddemo;

import android.content.Context;
import android.os.Environment;

/**
 * Created by Administrator on 2015/7/15.
 */
public class FileUtil
{
    public static String getRecordFolderPath(Context context,boolean bExternStorage)
    {
        if(bExternStorage)
        {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/Records";
        }
        else
        {
            return context.getFilesDir().getAbsolutePath();
        }
    }
    public static String getRecordTempFolderPath(Context context,boolean bExternStorage)
    {
        if(bExternStorage)
        {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/Records/temp";
        }
        else
        {
            return context.getFilesDir().getAbsolutePath() + "/temp";
        }
    }
}
