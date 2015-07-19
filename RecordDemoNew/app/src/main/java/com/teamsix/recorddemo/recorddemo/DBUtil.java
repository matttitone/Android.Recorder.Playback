package com.teamsix.recorddemo.recorddemo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaMetadataRetriever;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Administrator on 2015/7/19.
 */
public class DBUtil extends SQLiteOpenHelper {
    private Context context;

    public DBUtil(Context context) {
        super(context, "recordDB", null, 1);
        // create recordinfo table
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create recordinfo table
        db.execSQL("CREATE TABLE RecordInfo(" +
                "fileName TEXT DEFAULT NONE," +
                "length TEXT DEFAULT NONE," +
                "size TEXT DEFAULT NONE," +
                "date TEXT DEFAULT NONE," +
                "isStoredSD TEXT DEFAULT 'YES')"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void addOrUpdateRecord(Record record)
    {
        String isSD = "No";
        if(record.isStoredSD())
        {
            isSD = "Yes";
        }
        SQLiteDatabase dbRead = getReadableDatabase();
        Cursor c = dbRead.rawQuery("SELCT * FROM RecordInfo WHERE fileName = '" + record.getName() + "'",null);
        if(c.getCount() == 0) // then add to db
        {
            SQLiteDatabase dbWrite = getWritableDatabase();
            dbWrite.execSQL("INSERT INTO RecordInfo(fileName,length,data,size)VALUES('"
                    + record.getName() + "','"
                    + record.getLength() + "','"
                    + record.getDate() + "','"
                    + record.getSize() + "','"
                    + isSD + "')");
            dbWrite.close();
        }
        else // update
        {
            SQLiteDatabase dbWrite = getWritableDatabase();
            dbWrite.execSQL("UPDATE RecordInfo SET "
                    + "length = '" + record.getLength() + "',"
                    + "date = '" +record.getDate() + "',"
                    + "size = '" + record.getSize() + "'"
                    + "isStoredSD = '" + isSD + "'"
                    + "WHERE fileName = '" + record.getName() + "'"
            );
            dbWrite.close();
        }
        c.close();
        dbRead.close();
    }


    public ArrayList<Record> getRecordInfo(boolean bExternalStorage)
    {
        String selection = "WHERE isStoredSD = 'Yes'";
        if(bExternalStorage == false)
            selection = "WHERE isStoredSD = 'No'";
        ArrayList<Record> recordList = new ArrayList<Record>();
        SQLiteDatabase dbRead = getReadableDatabase();
        Cursor c = dbRead.rawQuery("SELCT * FROM RecordInfo " + selection + " ORDER BY date DESC",null);
        while(c.moveToNext())
        {
            String name = c.getString(c.getColumnIndex("fileName"));
            String length = c.getString(c.getColumnIndex("length"));
            String size = c.getString(c.getColumnIndex("size"));
            String date = c.getString(c.getColumnIndex("date"));
            String isStoredSD = c.getString(c.getColumnIndex("isStoredSD"));
            boolean bSD = false;
            if(isStoredSD.equals("Yes"))
                bSD = true;
            recordList.add(new Record(name,length,date,size,bSD));
        }
        c.close();
        dbRead.close();
        return recordList;
    }

    public void deleteRecord(String recordName)
    {
        SQLiteDatabase dbWrite = getWritableDatabase();
        dbWrite.execSQL("DELETE FROM RecordInfo WHERE fileName = " + recordName);
        dbWrite.close();
    }

    public void deleteAllRecords()
    {
        SQLiteDatabase dbWrite = getWritableDatabase();
        dbWrite.execSQL("DELETE FROM RecordInfo");
        dbWrite.close();
    }

    private void scanFileHelper(boolean externalStorage)
    {
        File file = new File(FileUtil.getRecordFolderPath(context,externalStorage));
        try
        {
            File[] files = file.listFiles();
            if (files.length > 0)
            {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                for (int j = 0; j < files.length; j++)
                {
                    if (!files[j].isDirectory())
                    {

                        String fileName = files[j].getName();
                        String fileSize = FileSizeUtil.getAutoFileOrFilesSize(files[j].getAbsolutePath());
                        if(fileSize.equals("0B"))
                            files[j].delete();
                        String recordLen = "00:00";
                        String recordDate = "";
                        try {
                            mmr.setDataSource(files[j].getAbsolutePath());
                            recordLen = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");//初始化Formatter的转换格式。
                            recordLen = formatter.format(recordLen);
                            recordDate = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE);
                        }
                        catch(Exception e)
                        {
                            continue;
                        }

                        addOrUpdateRecord(new Record(fileName,recordLen,recordDate,fileSize,externalStorage));

                    }
                }
            }
        }
        catch(Exception e)
        {

        }
    }

    public void scanFileToDatabase()
    {
        deleteAllRecords();
        scanFileHelper(false);
        if( Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
        {
            scanFileHelper(true);
        }
    }
}
