package com.example.banchan.shopmemo2;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

////    カスタム定数定義クラス

public class Constants {

    public static final String DB_INITIALIZED ="db_initialized";
    public static final String TEXT_SIZE ="text_size";
    public static final String KEEP_MEMO_QTY ="keepmemoqty";
    public static final String DISPLAY_GUIDE ="displayguide";

    static void setPrefrenceString(Context context, String mKey, String mVal){
        SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = data.edit();
        editor.putString(mKey, mVal);
        editor.apply();
    }

    static String getPrefrenceString(Context context, String mKey, String mDefault){
        SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(context);
        return data.getString(mKey, mDefault);
    }


/*
    static void setPrefrenceInt(Context context, String mKey, Integer mVal){
        SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = data.edit();
        editor.putInt(mKey, mVal);
        editor.apply();
    }

    static Integer getPrefrenceInt(Context context, String mKey, Integer mDefault){
        SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(context);
        return data.getInt(mKey, mDefault);
    }
*/
    static void setPrefrenceBoolean(Context context, String mKey, Boolean mVal){
        SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = data.edit();
        editor.putBoolean(mKey, mVal);
        editor.apply();
    }

    static Boolean getPrefrenceBoolean(Context context, String mKey, Boolean mDefault){
        SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(context);
        return data.getBoolean(mKey, mDefault);
    }
/*
    static void setPrefrenceFloat(Context context, String mKey, Float mVal){
        SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = data.edit();
        editor.putFloat(mKey, mVal);
        editor.apply();
    }

    static Float getPrefrenceFloat(Context context, String mKey, Float mDefault){
        SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(context);
        return data.getFloat(mKey, mDefault);
    }

    */

}
