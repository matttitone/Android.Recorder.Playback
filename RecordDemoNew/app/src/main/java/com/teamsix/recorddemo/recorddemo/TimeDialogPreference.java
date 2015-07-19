package com.teamsix.recorddemo.recorddemo;

import android.content.Context;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

/**
 * Created by Administrator on 2015/7/17.
 */
public class TimeDialogPreference extends Preference {
    private Context mContext;
    private Button btnSetTime;
    private View.OnClickListener mSetTimeListener ; //the callback function of the button
    public TimeDialogPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }
    public TimeDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }
    public TimeDialogPreference(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        LayoutInflater layout = (LayoutInflater) mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View viewGroup = layout.inflate(R.layout.layout_usercontrol_timesetting, null);  //get our layout
        RelativeLayout frame = (RelativeLayout) viewGroup.findViewById(R.id.frame);
        btnSetTime = (Button) viewGroup.findViewById(R.id.set_time);  // find button
        btnSetTime.setOnClickListener(mSetTimeListener); // set onclick listener
        return frame;
    }

    public void setSetTimeListener(View.OnClickListener mOkListener) {
        this.mSetTimeListener = mOkListener;
    }
}
