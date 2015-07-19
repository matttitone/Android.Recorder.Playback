package com.teamsix.recorddemo.recorddemo;

import android.app.ActionBar;
import android.content.Context;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


public class SettingActivity extends ActionBarActivity {

    static TimeDialogPreference timeDialogPreference;
    static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        getFragmentManager().beginTransaction().replace(android.R.id.content,new PrefsFragement()).commit();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Toast.makeText(getApplicationContext(),"Settings have been saved",Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class PrefsFragement extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.layout.setting_preferences);
                    timeDialogPreference =
                    (TimeDialogPreference) getPreferenceScreen().findPreference("time_picker_preference"); //get our preference
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Intent intent = new Intent("xxxx");
                    //startActivity(intent);
                    final SetTimeDialog setTimeDialog = new SetTimeDialog(mContext, "Set Record Max Time", "Confirm", "Cancel");
                    setTimeDialog.show();
                    setTimeDialog.setClicklistener(new SetTimeDialog.ClickListenerInterface() {
                        @Override
                        public void doConfirm() {
                            // TODO Auto-generated method stub
                            Toast.makeText(mContext,"Settings have been saved",Toast.LENGTH_SHORT).show();
                            setTimeDialog.dismiss();
                        }

                        @Override
                        public void doCancel() {
                            // TODO Auto-generated method stub
                            setTimeDialog.dismiss();
                        }
                    });
                }
            };
            timeDialogPreference.setSetTimeListener(listener);
        }

    }
}
