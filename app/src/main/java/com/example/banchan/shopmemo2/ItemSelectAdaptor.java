package com.example.banchan.shopmemo2;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ItemSelectAdaptor extends BaseAdapter {
    Context context;
    LayoutInflater layoutInflater = null;
    ArrayList<String[]> selecterList;

    public ItemSelectAdaptor(Context context, ArrayList<String[]> mList) {
        this.context = context;
        this.selecterList = mList;
        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return selecterList.size();
    }

    @Override
    public Object getItem(int position) {
        return selecterList.get(position);
    }

    public String getItemMember(int position, int num){

        return selecterList.get(position)[num];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        View view = convertView;

        if(view == null){   //  viewの再利用
            view = layoutInflater.inflate(R.layout.inbox_selecter, parent, false);
        }

        final TextView tv  = (TextView) view.findViewById(R.id.textView1);
        //  フォントサイズをセット
        tv.setTextSize(Integer.parseInt(Constants.getPrefrenceString(context, Constants.TEXT_SIZE, "20")));
        //  テキストをセット
        tv.setText(selecterList.get(position)[1]);
        //  <a>に反応するようセット
        //MovementMethod movementmethod = LinkMovementMethod.getInstance();
        //tv.setMovementMethod(movementmethod);
        //  ListViewにTextViewを配置するとListViewはOnItemClickを拾えないので
        //  変わりにTextViewからItemClickを発生させる・
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 0L);
            }
        });

        tv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //  このリスナーが無いとtextview上の長押しに反応しない（textviewの領域外では反応）
                return false;
            }
        });

        return view;
    }
}
