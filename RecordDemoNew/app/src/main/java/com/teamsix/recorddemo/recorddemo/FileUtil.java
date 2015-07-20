package com.teamsix.recorddemo.recorddemo;

import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;

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
    public static void SaveRecordMediaStoreInfo(Context context,String title,String album,String path)
    {
        // store the file to the mediastore
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.TITLE, title);
        values.put(MediaStore.Audio.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Audio.Media.ALBUM,album);
        values.put(MediaStore.Audio.Media.DATA, path);
        //Uri uri = MediaStore.Audio.Media.getContentUriForPath(path);
        //context.getContentResolver().delete(uri, null, null);
        Uri uri = context.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        // notify the system to update the file info
        //new AudioFileScanner(context, new File(path));
        MediaScannerConnection.scanFile(context, new String[]{path}, new String[]{"audio/amr"}, new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {
                Log.i("ExternalStorage", "Scanned " + path + ":");
                Log.i("ExternalStorage", "-> uri=" + uri);
            }
        });
    }
    public static long getAvailableStorageSpace(boolean externalStorage)
    {
        if(externalStorage)
        {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            return sf.getAvailableBytes();
        }
        else
        {
            File root = Environment.getRootDirectory();
            StatFs sf = new StatFs(root.getPath());
            return sf.getAvailableBytes();
        }
    }
}
