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
                if (fragmentChangeListener != null && position == 0) {
                    fragmentChangeListener.onPauseSignal();
                }
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
            getMenuInflater().inflate(R.menu.menu_main, menu);
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
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String arg0)
                    {
                        if(actionOperationListener != null)
                        {
                            actionOperationListener.onSearchFile(arg0);
                        }
                        return false;
                    }
                });
            }
            if(recordlistState == 1) // single choice
            {
                getMenuInflater().inflate(R.menu.menu_del, menu);
            }
            if(recordlistState == 2) // multi choice
            {
                getMenuInflater().inflate(R.menu.menu_record, menu);
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
            Toast.makeText(getApplicationContext(),"click",Toast.LENGTH_SHORT).show();
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
    public void onStateChange(int newState) {
        recordlistState = newState;
        invalidateOptionsMenu();
    }

    @Override
    public void onRecordFinish() {
        if (fragmentChangeListener != null) {
            fragmentChangeListener.onUpdateDataSignal();
        }
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