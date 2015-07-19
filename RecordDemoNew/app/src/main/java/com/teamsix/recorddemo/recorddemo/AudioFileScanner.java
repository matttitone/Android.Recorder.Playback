package com.teamsix.recorddemo.recorddemo;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import java.io.File;

/**
 * Created by Administrator on 2015/7/18.
 */
public class AudioFileScanner implements MediaScannerConnection.MediaScannerConnectionClient {

    private MediaScannerConnection mMs;
    private File mFile;

    public AudioFileScanner(Context context, File f) {
        mFile = f;
        mMs = new MediaScannerConnection(context, this);
        mMs.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        File[] files = mFile.listFiles();
        if(files.length > 0)
        {
            for(int i = 0; i < files.length; i++)
            {
                mMs.scanFile(files[i].getAbsolutePath(), null);
            }
        }
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        mMs.disconnect();
    }

}