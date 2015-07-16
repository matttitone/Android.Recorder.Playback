package com.teamsix.recorddemo.recorddemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;


public class RecordListActivity extends Activity {

    private ListView lv;  // listview
    private MyAdapter mAdapter;
    private ArrayList<String> list; // string of the data
    private Button btnSingleChoice;// return button
    private Button btnShare;
    private Button btnDelete;
    private int checkNum; // total selected number
    private TextView tv_show; // show the selected number
    private boolean isMulChoice; // whether we are in mulchoice mode
    private boolean isStoreToSDCard = false; // whether we store the record on sd card

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        // get whether we are now store the records in the sdcard
        Intent intent=getIntent();
        isStoreToSDCard = intent.getBooleanExtra("isStoreToSDCard",false);
        lv = (ListView) findViewById(R.id.listView);
        btnSingleChoice = (Button)findViewById(R.id.btnSingleChoice);
        btnShare = (Button)findViewById(R.id.btnShare);
        btnDelete = (Button)findViewById(R.id.btnDelete);
        tv_show = (TextView)findViewById(R.id.tvNumber);



        list = new ArrayList<String>();
        isMulChoice = false;

        initData();


        mAdapter = new MyAdapter(list,this);
        lv.setAdapter(mAdapter);

        // cancel the mulchoice
        btnSingleChoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMulChoice = false;
                checkNum = 0;
                dataChanged();
            }
        });

        //
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for(int i = 0; i < list.size(); i ++)
                {
                    MyAdapter.getIsSelected().put(i,false);
                }

                // ��������Ϊ0
                checkNum = 0;
                // ˢ��listview��textview
                dataChanged();
            }
        });

        // ���
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // ˢ��listview��textview
                dataChanged();
            }
        });

        // ��listView�ļ�����
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(isMulChoice) {
                    ViewHolder holder = (ViewHolder) view.getTag();

                    holder.cb.toggle();

                    MyAdapter.getIsSelected().put(position, holder.cb.isChecked());

                    if (holder.cb.isChecked() == true) {
                        checkNum++;
                    }
                    else {
                        checkNum--;
                    }

                    tv_show.setText("select " + checkNum + " item(s)");
                }
                else // not multichoice
                {
                    Toast.makeText(getApplicationContext(),"click" + position,Toast.LENGTH_SHORT).show();
                }
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                isMulChoice = true;
                for(int i = 0; i < list.size(); i ++)
                {
                    MyAdapter.getIsSelected().put(i,false);
                }
                dataChanged();
                return false;
            }
        });

        dataChanged();
    }

    // ��ʼ������
    private void initData() {
        File file = new File(FileUtil.getRecordFolderPath(getApplicationContext(),isStoreToSDCard));
        try
        {
            File[] files = file.listFiles();
            if (files.length > 0)
            {
                for (int j = 0; j < files.length; j++)
                {
                    if (!files[j].isDirectory())
                    {
                        list.add(files[j].getName());
                    }
                }
            }
        }
        catch(Exception e)
        {

        }
    }


    // ˢ��listview��TextView����ʾ
    private void dataChanged() {
        if(isMulChoice == false)
        {
            // set all buttons invisible
            btnSingleChoice.setVisibility(View.INVISIBLE);
            btnShare.setVisibility(View.INVISIBLE);
            btnDelete.setVisibility(View.INVISIBLE);
            tv_show.setVisibility(View.INVISIBLE);
            mAdapter.setMulChoice(false);
        }
        else
        {
            btnSingleChoice.setVisibility(View.VISIBLE);
            btnShare.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
            tv_show.setVisibility(View.VISIBLE);
            mAdapter.setMulChoice(true);
        }
        mAdapter.notifyDataSetChanged();

        tv_show.setText("select " + checkNum + " item(s)");
    }



}
