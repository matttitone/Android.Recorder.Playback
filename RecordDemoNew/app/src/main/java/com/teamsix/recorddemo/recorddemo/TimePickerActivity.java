package com.teamsix.recorddemo.recorddemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/7/17.
 */
public class TimePickerActivity extends Activity {
    PickerView hour_pv;
    PickerView minute_pv;
    PickerView second_pv;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_timepicker);
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
        hour_pv.setData(hour,0);
        hour_pv.setOnSelectListener(new PickerView.onSelectListener()
        {

            @Override
            public void onSelect(String text)
            {
                Toast.makeText(TimePickerActivity.this, "Choose " + text + " hour",
                        Toast.LENGTH_SHORT).show();
            }
        });
        minute_pv.setData(minute,4);
        minute_pv.setOnSelectListener(new PickerView.onSelectListener()
        {

            @Override
            public void onSelect(String text)
            {
                Toast.makeText(TimePickerActivity.this, "Choose " + text + " minute",
                        Toast.LENGTH_SHORT).show();
            }
        });
        second_pv.setData(seconds,0);
        second_pv.setOnSelectListener(new PickerView.onSelectListener()
        {

            @Override
            public void onSelect(String text)
            {
                Toast.makeText(TimePickerActivity.this, "Choose " + text + " second",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
