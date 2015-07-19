package com.teamsix.recorddemo.recorddemo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2015/7/18.
 */
public class SettingUtil {
    private Context context;

    public SettingUtil(Context context)
    {
        this.context = context;
    }

    public void saveMaxRecordTime(int hour,int minute,int second)
    {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putInt("max_hour", hour);
        editor.putInt("max_min", minute);
        editor.putInt("max_sec", second);
        editor.commit();
    }

    public int getMaxHour()
    {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.getInt("max_hour",1);
    }

    public int getMaxMinute()
    {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.getInt("max_min",0);
    }

    public int getMaxSecond()
    {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.getInt("max_sec",0);
    }

    public String getRecordFormat()
    {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.getString("list_format", "AMR");

    }

    public String getRecordQuality()
    {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.getString("list_quality","HIGH");

    }

    public boolean getStoreInSDCard()
    {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.getBoolean("checkbox_storeinsdcard",false);
    }

    private SharedPreferences getSharedPreferences()
    {
        SharedPreferences mySharedPreferences= context.getSharedPreferences("com.teamsix.recorddemo.recorddemo_preferences", Activity.MODE_PRIVATE);
        return mySharedPreferences;
    }
}
