package com.teamsix.recorddemo.recorddemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 2015/7/15.
 */
public class MyAdapter extends BaseAdapter {
    // ������ݵ�list
    private ArrayList<String> list;

    // ��������CheckBox��ѡ�����
    private static HashMap<Integer,Boolean> isSelected;

    private Context context;

    // import the layout
    private LayoutInflater inflater = null;

    private boolean isMulChoice = false;

    // constructor
    public MyAdapter(ArrayList<String> list,Context context)
    {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
        isSelected = new HashMap<Integer, Boolean>();
        // add data to the list��
        initDate();
    }

    public void setMulChoice(boolean flag)
    {
        isMulChoice = flag;
    }

    // init�isSelected�to all false�
    private void initDate()
    {
        for(int i = 0; i < list.size(); i ++)
        {
            getIsSelected().put(i,false);
        }
    }

    // whether isSelected����
    public static HashMap<Integer,Boolean> getIsSelected() {
        return isSelected;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null)
        {
            // ���ViewHoloder����
            holder = new ViewHolder();
            // ���벼�ֲ���ֵ��convertview
            convertView = inflater.inflate(R.layout.listviewitem,null);
            holder.tv = (TextView)convertView.findViewById(R.id.item_tv);
            holder.cb = (CheckBox)convertView.findViewById(R.id.item_cb);
            // Ϊview���ñ�ǩ
            convertView.setTag(holder);
        }
        else
        {
            // ȡ��holder
            holder = (ViewHolder)convertView.getTag();
        }

        // ����list��TextView����ʾ
        holder.tv.setText(list.get(position));
        // ����isSelected������checkbox��ѡ�����
        holder.cb.setChecked(getIsSelected().get(position));
        if(isMulChoice == false)
        {
            holder.cb.setVisibility(View.INVISIBLE);
        }
        else
        {
            holder.cb.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    public static void setIsSelected(HashMap<Integer,Boolean> isSelected)
    {
        MyAdapter.isSelected = isSelected;
    }


}

class ViewHolder
{
    TextView tv;
    CheckBox cb;
}
