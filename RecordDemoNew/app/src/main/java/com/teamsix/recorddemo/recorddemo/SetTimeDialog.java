package com.teamsix.recorddemo.recorddemo;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/7/17.
 */
public class SetTimeDialog extends Dialog {

    private Context context;
    private String title;
    private String confirmButtonText;
    private String cacelButtonText;
    private ClickListenerInterface clickListenerInterface;
    PickerView hour_pv;
    PickerView minute_pv;
    PickerView second_pv;

    private int nHour = 0;
    private int nMinute = 0;
    private int nSecond = 0;

    public interface ClickListenerInterface {

        public void doConfirm();

        public void doCancel();
    }

    public SetTimeDialog(Context context, String title, String confirmButtonText, String cacelButtonText) {
        super(context);
        this.context = context;
        this.title = title;
        this.confirmButtonText = confirmButtonText;
        this.cacelButtonText = cacelButtonText;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        init();
    }

    public void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_settime, null);
        setContentView(view);
        hour_pv = (PickerView) findViewById(R.id.hour_pv);
        minute_pv = (PickerView) findViewById(R.id.minute_pv);
        second_pv = (PickerView) findViewById(R.id.second_pv);
        List<String> hour = new ArrayList<String>();
        List<String> minute = new ArrayList<String>();
        List<String> seconds = new ArrayList<String>();
        for (int i = 0; i < 10; i++)
        {
            hour.add("0" + i);
        }
        for (int i = 0; i < 60; i++)
        {
            minute.add(i < 10 ? "0" + i : "" + i);
        }
        for (int i = 0; i < 60; i++)
        {
            seconds.add(i < 10 ? "0" + i : "" + i);
        }
        SettingUtil settingUtil = new SettingUtil(context);
        nHour = settingUtil.getMaxHour();
        hour_pv.setData(hour, nHour);
        hour_pv.setOnSelectListener(new PickerView.onSelectListener()
        {

            @Override
            public void onSelect(String text)
            {
                nHour = Integer.parseInt(text);
            }
        });
        nMinute = settingUtil.getMaxMinute();
        minute_pv.setData(minute, nMinute);
        minute_pv.setOnSelectListener(new PickerView.onSelectListener()
        {

            @Override
            public void onSelect(String text)
            {
                nMinute = Integer.parseInt(text);
            }
        });
        nSecond = settingUtil.getMaxSecond();
        second_pv.setData(seconds,nSecond);
        second_pv.setOnSelectListener(new PickerView.onSelectListener()
        {

            @Override
            public void onSelect(String text)
            {
                nSecond = Integer.parseInt(text);
            }
        });
        Button btnConfirm = (Button) view.findViewById(R.id.confirm);
        Button btnCancel = (Button) view.findViewById(R.id.cancel);

        btnConfirm.setOnClickListener(new clickListener());
        btnCancel.setOnClickListener(new clickListener());

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        lp.width = (int) (d.widthPixels * 0.8);
        lp.setTitle(title);
        dialogWindow.setAttributes(lp);
    }

    public void setClicklistener(ClickListenerInterface clickListenerInterface) {
        this.clickListenerInterface = clickListenerInterface;
    }


    private class clickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            int id = v.getId();
            switch (id) {
                case R.id.confirm: {
                    new SettingUtil(context).saveMaxRecordTime(nHour,nMinute,nSecond);
                    clickListenerInterface.doConfirm();
                }
                    break;
                case R.id.cancel:
                    clickListenerInterface.doCancel();
                    break;
            }
        }

    };
}
