package com.teamsix.recorddemo.recorddemo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.SearchView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements OnRecordItemStateChangedListener,MainFragment.OnRecordFinishListener {

    CustomPagerAdapter mCustomPagerAdapter;
    ViewPager mViewPager;

    int fragmentCount;     // the current fragment show
    int recordlistState;   // 0 for nothing 1 for single choice 2 for multi choice
    boolean isPlaying;     // whether we are playing
    boolean isRecording;   // whether we are recording
    FragmentChangeListener fragmentChangeListener; // components which will receive the change signal
    ActionOperationListener actionOperationListener; // when the action bar is
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences= getSharedPreferences("test",Activity.MODE_PRIVATE);

        fragmentCount = 0;
        recordlistState = 0;

        fragmentChangeListener = null;

        mCustomPagerAdapter = new CustomPagerAdapter(getSupportFragmentManager(), this);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mCustomPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                fragmentCount = position;
                invalidateOptionsMenu();
//                if (fragmentChangeListener != null && position == 0) {
//                    fragmentChangeListener.onPauseSignal();
//                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if(fragmentCount == 0)
        {
            if(isRecording)
            {
                getMenuInflater().inflate(R.menu.menu_main_nosetting, menu);
            }
            else {
                getMenuInflater().inflate(R.menu.menu_main, menu);

            }
        }
        if(fragmentCount == 1) {
            // Inflate the menu; this adds items to the action bar if it is present.
            if(recordlistState == 0) // single choice
            {
                getMenuInflater().inflate(R.menu.menu_search, menu);


                MenuItem searchItem=menu.findItem(R.id.action_search);
                final SearchView searchView=(SearchView) MenuItemCompat.getActionView(searchItem);

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
                {

                    @Override
                    public boolean onQueryTextSubmit(String arg0)
                    {
                        if(actionOperationListener != null)
                        {
                            actionOperationListener.onSearchFile(arg0);
                        }
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String arg0)
                    {
                        if(arg0.equals("") == false)
                            return true;
                        if(actionOperationListener != null)
                        {
                            actionOperationListener.onSearchFile(arg0);
                        }
                        return false;
                    }
                });
            }
            if(recordlistState == 1 || recordlistState == 2) // single choice
            {
                if(isPlaying) {
                    getMenuInflater().inflate(R.menu.menu_record_nosetting, menu);
                }
                else
                {
                    getMenuInflater().inflate(R.menu.menu_record, menu);
                }
            }
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            if(actionOperationListener != null)
            {
                actionOperationListener.onShareFile();
            }
            return true;
        }
        if (id == R.id.action_setting) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_delete)
        {
            if(actionOperationListener != null)
            {
                actionOperationListener.onDeleteFile();
            }
            return true;
        }
        if (id == R.id.action_search)
        {
            //Toast.makeText(getApplicationContext(),"click",Toast.LENGTH_SHORT).show();
            return false;
        }
        return false;
        //return super.onOptionsItemSelected(item);
    }

    @Override
    public void setOnFragmentChangeListener(FragmentChangeListener listener)
    {
        fragmentChangeListener = listener;
    }

    @Override
    public void setOnActionOperationListener(ActionOperationListener listener)
    {
        actionOperationListener = listener;
    }

    @Override
    public void onPlayStart() {
        isPlaying = true;
        invalidateOptionsMenu();
    }

    @Override
    public void onPlayStop() {
        isPlaying = false;
        invalidateOptionsMenu();
    }

    @Override
    public void onStateChange(int newState) {
        recordlistState = newState;
        invalidateOptionsMenu();
    }

    @Override
    public void onRecordFinish() {
        if (fragmentChangeListener != null) {
            isRecording = false;
            invalidateOptionsMenu();
            fragmentChangeListener.onUpdateDataSignal();
        }
    }

    @Override
    public void onRecordStart() {
        isRecording = true;
        invalidateOptionsMenu();
    }

    public interface FragmentChangeListener
    {
        void onPauseSignal();
        void onUpdateDataSignal();
    }

}

interface ActionOperationListener
{
    void onDeleteFile();
    void onSearchFile(String keyword);
    void onShareFile();
}