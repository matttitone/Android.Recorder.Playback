package com.teamsix.recorddemo.recorddemo;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

/**
 * Created by Administrator on 2015/7/17.
 */
public class TimeDialogPreference extends DialogPreference {
    private int hour;
    private int minute;
    private int second;

    public TimeDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
