package com.example.banchan.shopmemo2;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

public class ItemListAdaptor extends BaseAdapter {
    Context mContext;
    LayoutInflater layoutInflater = null;
    ArrayList<clsShoppingItem> shoppingItems;

    public ItemListAdaptor(Context context, ArrayList<clsShoppingItem> mItem) {
        mContext = context;
        shoppingItems = new ArrayList<clsShoppingItem>();
        shoppingItems.addAll(mItem);
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return shoppingItems.size();

    }

    @Override
    public Object getItem(int position) {
        return shoppingItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        View view = convertView;

        if(view == null){   //  viewの再利用
            view = layoutInflater.inflate(R.layout.inbox_itemlist, parent, false);
        }

        TextView tv  = (TextView) view.findViewById(R.id.itemText1);

        if(shoppingItems.get(position).bought) {
            tv.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            tv.setTextColor(Color.rgb(200, 200, 200));
        }
        else{
            tv.getPaint().setFlags((~Paint.STRIKE_THRU_TEXT_FLAG) & Paint.ANTI_ALIAS_FLAG);
            tv.setTextColor(Color.BLACK);
        }

        //  フォントサイズをセット
        tv.setTextSize(Integer.parseInt(Constants.getPrefrenceString(mContext, Constants.TEXT_SIZE, "20")));
        //  テキストをセット
        tv.setText(shoppingItems.get(position).item);
        //  ListViewにTextViewを配置するとListViewはOnItemClickを拾えないので
        //  変わりにTextViewからItemClickを発生させる・
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 0L);

            }
        });

        ImageButton iv = (ImageButton) view.findViewById(R.id.imageButton1);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  MainActivity側ではonClickしか拾えないのでID = 1Lにして区別する
                ((ListView) parent).performItemClick(v, position, 1L);
            }
        });

        return view;
    }

    public void refresh(ArrayList<clsShoppingItem> mList){
/*
        for(int i = 0; i < mList.size(); i++){
            Log.d("■", "mList " +mList.get(i).item + " : " +  mList.get(i).position);
            mList.get(i).position = i;
        }
*/
        shoppingItems.clear();
        shoppingItems.addAll(mList);

        /*
        ArrayList<String> mBuf = new ArrayList<String>();
        for(int i = 0; i < mList.size(); i++){
            mBuf.add(mList.get(i).item + ";" + (mList.get(i).bought ? "y" : "n"));
        }
        Collections.sort(mBuf);



        for(int j = 0; j < mBuf.size(); j++){
            clsShoppingItem mItem = new clsShoppingItem();
            String[] aaa = mBuf.get(j).split(";", 0);
            mItem.item = aaa[0];
            mItem.bought = aaa[1].equals("y") ? true : false;

            shoppingItems.add(mItem);
        }

        */
    }

}
