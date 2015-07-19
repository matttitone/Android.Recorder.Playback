package com.teamsix.recorddemo.recorddemo;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by David on 17/07/2015.
 */
public class CustomPagerAdapter extends FragmentPagerAdapter {

    protected Context mContext;

    public CustomPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }
    @Override
    // This method returns the fragment associated with
    // the specified position.
    //
    // It is called when the Adapter needs a fragment
    // and it does not exists.
    public Fragment getItem(int position) {
        switch(position) {
            case 0:
                return new MainFragment();
            case 1:
                return new RecordListActivity();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

}