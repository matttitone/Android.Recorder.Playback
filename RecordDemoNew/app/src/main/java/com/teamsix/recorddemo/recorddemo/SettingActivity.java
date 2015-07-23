
package com.teamsix.recorddemo.recorddemo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.baidu.oauth.BaiduOAuth;



public class SettingActivity extends ActionBarActivity {

    static TimeDialogPreference timeDialogPreference;
    static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_setting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mContext = this;
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragement()).commit();
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
        if (id == R.id.action_setting) {
            return true;
        }
        if(id == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }

    public static class PrefsFragement extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private SharedPreferences mSharedPreferences;
        private final String mbApiKey = "clHB9RsjML7d1GhjZ4gGqMvr";// api key for baidu service
        private Handler handler;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
            handler = new android.os.Handler(){
                @Override
                public void handleMessage(Message msg){
                    String s = String.valueOf(msg.obj);
                    Toast.makeText(mContext,s,Toast.LENGTH_SHORT).show();
                }
            };
            reloadSetting();
        }

        private void reloadSetting()
        {
            try
            {
                try
                {
                    getPreferenceScreen().removePreference(findPreference("cSettings"));
                }
                catch (Exception e)
                {

                }

                addPreferencesFromResource(R.layout.setting_preferences);
                System.out.println(new SettingUtil(mContext).getRecordFormat());
                if(new SettingUtil(mContext).getRecordFormat().equals("AMR"))
                {
                    ((PreferenceGroup) findPreference("cSettings")).removePreference(findPreference("list_quality"));
                }
                PreferenceScreen backupPreference = (PreferenceScreen)getPreferenceScreen().findPreference("btn_backup");
                backupPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
//                        Intent intent = new Intent(mContext,BackupActiviy.class);
//                        startActivity(intent);
                        // show login to baidu service
                        BaiduOAuth oauthClient = new BaiduOAuth();

                        oauthClient.startOAuth(mContext, mbApiKey, new BaiduOAuth.OAuthListener() {
                            @Override
                            public void onException(String msg) {
                                Toast.makeText(mContext, "Login failed " + msg, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onComplete(BaiduOAuth.BaiduOAuthResponse response) {
                                if (null != response) {
                                    String userName = response.getUserName();
                                    if (userName.contains("*****")) {
                                        Toast.makeText(mContext, "Phone number is not supported,please try another account", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    CommonUploadUtils commonUploadUtils = new CommonUploadUtils(mContext, handler, userName);
                                    boolean isSDCard = new SettingUtil(mContext).getStoreInSDCard();
                                    String path = FileUtil.getRecordFolderPath(mContext, isSDCard);
                                    commonUploadUtils.setUploadPath(path);
                                    if (commonUploadUtils.isDownloading() || commonUploadUtils.isUploading()) {
                                        Toast.makeText(mContext, "A Uploading or a Downloading is on processing,Please try later", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    Toast.makeText(mContext, response.getUserName() + " Login success", Toast.LENGTH_SHORT).show();
                                    commonUploadUtils.runUpload();
                                }
                            }

                            @Override
                            public void onCancel() {
                                Toast.makeText(mContext, "Login cancelled", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return false;
                    }
                });

                PreferenceScreen restorePreference = (PreferenceScreen)getPreferenceScreen().findPreference("btn_restore");
                restorePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
//                        Intent intent = new Intent(mContext,BackupActiviy.class);
//                        startActivity(intent);
                        // show login to baidu service
                        BaiduOAuth oauthClient = new BaiduOAuth();

                        oauthClient.startOAuth(mContext, mbApiKey, new BaiduOAuth.OAuthListener() {
                            @Override
                            public void onException(String msg) {
                                Toast.makeText(mContext, "Login failed " + msg, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onComplete(BaiduOAuth.BaiduOAuthResponse response) {
                                if (null != response) {
                                    String userName = response.getUserName();
                                    if (userName.contains("*****")) {
                                        Toast.makeText(mContext, "Phone number is not supported,please try another account", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    CommonUploadUtils commonUploadUtils = new CommonUploadUtils(mContext, handler, userName);
                                    boolean isSDCard = new SettingUtil(mContext).getStoreInSDCard();
                                    String path = FileUtil.getRecordFolderPath(mContext, isSDCard);
                                    commonUploadUtils.setDownloadPath(path);
                                    if (commonUploadUtils.isDownloading() || commonUploadUtils.isUploading()) {
                                        Toast.makeText(mContext, "A Uploading or a Downloading is on processing,Please try later", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    Toast.makeText(mContext, response.getUserName() + " Login success", Toast.LENGTH_SHORT).show();
                                    commonUploadUtils.runDownload();

                                }
                            }

                            @Override
                            public void onCancel() {
                                Toast.makeText(mContext, "Login cancelled", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return false;
                    }
                });

                PreferenceScreen timePreference = (PreferenceScreen)getPreferenceScreen().findPreference("dialog_timepicker");
                timePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        final SetTimeDialog setTimeDialog = new SetTimeDialog(mContext, "Set Record Max Time", "Confirm", "Cancel");
                        setTimeDialog.show();
                        setTimeDialog.setClicklistener(new SetTimeDialog.ClickListenerInterface() {
                            @Override
                            public void doConfirm() {
                                // TODO Auto-generated method stub
                                setTimeDialog.dismiss();

                            }
                            @Override
                            public void doCancel() {
                                // TODO Auto-generated method stub
                                setTimeDialog.dismiss();
                            }
                        });
                        return true;
                    }
                });
                timeDialogPreference =
                        (TimeDialogPreference) getPreferenceScreen().findPreference("time_picker_preference"); //get our preference
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final SetTimeDialog setTimeDialog = new SetTimeDialog(mContext, "Set Record Max Time", "Confirm", "Cancel");
                        setTimeDialog.show();
                        setTimeDialog.setClicklistener(new SetTimeDialog.ClickListenerInterface() {
                            @Override
                            public void doConfirm() {
                                // TODO Auto-generated method stub
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
            catch (Exception e)
            {

            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            reloadSetting();
        }
    }
}
