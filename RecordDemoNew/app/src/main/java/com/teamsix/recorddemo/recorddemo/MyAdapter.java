package com.teamsix.recorddemo.recorddemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 2015/7/15.
 */
public class MyAdapter extends BaseExpandableListAdapter {
    // ������ݵ�list
    private ArrayList<Record> list;

    // ��������CheckBox��ѡ�����
    private static HashMap<Integer,Boolean> isSelected;

    private Context context;

    // import the layout
    private LayoutInflater inflater = null;

    private boolean isMulChoice = false;

    //position
    private static int pos;

    // constructor
    public MyAdapter(ArrayList<Record> list,Context context)
    {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
        isSelected = new HashMap<Integer, Boolean>();
        // add data to the list��
        initDate();
        pos = -1;
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
    public int getGroupCount() {
        return list.size();
    }

    @Override
    public Object getGroup(int position) {
        return list.get(position);
    }

    @Override
    public long getGroupId(int position) {
        return position;
    }

    @Override
    public long getChildId(int gPosition, int cPosition) {return cPosition;}

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }



    @Override
    public View getChildView(final int position, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listviewdetail, null);
        }

        TextView created = (TextView) convertView.findViewById(R.id.dCreated);
        TextView length = (TextView) convertView.findViewById(R.id.length);
        TextView size = (TextView) convertView.findViewById(R.id.size);
        TextView path = (TextView) convertView.findViewById(R.id.path);

        created.setText(list.get(position).getDate());
        size.setText(list.get(position).getSize());
        length.setText(list.get(position).getLength());
        path.setText(list.get(position).getPath());

        created.setTextColor(-16777216);
        size.setTextColor(-16777216);
        length.setTextColor(-16777216);
        path.setTextColor(-16777216);

        return convertView;
    }

    @Override
    public View getGroupView(int position, boolean isExpanded, View convertView, ViewGroup parent) {
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
        holder.tv.setText(list.get(position).getName());
        holder.tv.setTextColor(-16777216);
        //Position
        if (pos == position) {
           // holder.tv.setTextColor();
            convertView.setBackgroundColor(-3355444);
        }
        else {
            convertView.setBackgroundColor(-1);
        }
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

    public static void setPos (int position) {
        pos = position;
    }

    public static int getPos() {
        return pos;
    }

    public Object getChild(int groupPosition, int childPosition) {
        return list.get(groupPosition);
    }

    public int getChildrenCount(int groupPosition) {
        return 1;
    }


}

class ViewHolder
{
    TextView tv;
    CheckBox cb;
}
