package com.teamsix.recorddemo.recorddemo;

import java.util.Comparator;

/**
 * Created by Administrator on 2015/7/19.
 */
public class Record implements Comparable<Record>
{
    private String name;   // the name of the record
    private String length;   // the length of the record
    private String size;   // the size of the record
    private String date;   // the date which the record was created
    private boolean isStoredSD; // whether the record was stored in sdCard
    private String path; // path of file

    public Record()
    {
    }

    public Record(String name,String length,String date,String size,boolean isStoredSD) {
        this.name = name;
        this.length = length;
        this.date = date;
        this.size = size;
        this.isStoredSD = isStoredSD;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public boolean isStoredSD() {
        return isStoredSD;
    }

    public void setIsStoredSD(boolean isStoredSD) {
        this.isStoredSD = isStoredSD;
    }

    public void setPath(String path) { this.path = path;}

    public String getPath() {return path;}



    @Override
    public int compareTo(Record rec) {
        if (this.name != null && rec.name != null) {
            String a,b;
            a = this.name;
            b = rec.name;
            int an, bn;
            an = extractInt(a);
            bn = extractInt(b);
            a = a.replaceAll("\\d","");
            b = b.replaceAll("\\d","");
            int num = a.compareToIgnoreCase(b);
            if (num == 0)
                return (an-bn);
            else
                return num;
        }
        return 0;
    }

    int extractInt(String s) {
        String num = s.replaceAll("\\D", "");
        // return 0 if no digits found
        return num.isEmpty() ? 0 : Integer.parseInt(num);
    }

}
