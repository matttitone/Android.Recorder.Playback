package com.teamsix.recorddemo.recorddemo;

/**
 * Created by Administrator on 2015/7/19.
 */
public class Record
{
    private String name;   // the name of the record
    private int length;   // the length of the record
    private String date;   // the date which the record was created

    public Record()
    {
    }

    public Record(String name,int len,String date) {
        this.name = name;
        this.length = len;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


}
