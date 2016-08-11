package com.example.banchan.shopmemo2;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;

//SQLite処理Helper
public class DatabaseHelper extends SQLiteOpenHelper{

        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_FILE_NAME = "ShoppingMemo_1_0_0.db";
        private Context mContext;
        private SQLiteDatabase mDb;

        public DatabaseHelper(Context context) {
                super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION);
                mContext = context;
                mDb = this.getWritableDatabase();
            }     // コンストラクタ

        public void onCreate(SQLiteDatabase db) {
                //
                //  getWritableDatabase();の度にチェックされ
                //  DBが無い時（作成された時）だけ実行される。
                //  DB自体を作成するメソッドは無い！無ければ自動的に作成されるが
                //  それは最初のテーブルをcreteした時。
                //
            try {
                db.execSQL(
                        "CREATE TABLE item_table ("
                                + "_id integer primary key autoincrement,"
                                + "category text, "             //  区分　
                                + "name text not null unique, "  //  名前
                                + "last_date text, "            //  最新の使用時刻  longを文字で保存
                                + "visible  integer not null"
                                + ")"
                );
                db.execSQL(
                        "CREATE TABLE category_table ("
                                + "code text not null unique, "             //
                                + "name text not null unique, "  //  名前
                                + "parent text, "            //
                                + "subcode text, "
                                + "visible  integer not null"
                                + ")"
                );
                db.execSQL(
                        "CREATE TABLE list_memo ("
                                + "_id integer primary key autoincrement,"
                                + "title text, "             //
                                + "memo text not null, "  //
                                + "reg_date text, "            //
                                + "protected  integer not null"
                                + ")"
                );
            }catch (Exception e){
                Log.d("■", "onCreate DB " + e.getMessage());
            }
        }    // DB生成

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public int initialSetting (TypedArray item){

        int cnt = 0;
        int result = 0;

        mDb.beginTransaction();
        try {
            //  一旦全て削除
            mDb.execSQL("delete from item_table");

            for (int i = 0; i < item.length(); i++) {

                String[] mItem = item.getString(i).split(";",0);

                long mFlg = insert(mItem[0], mItem[1]);
                if( mFlg > 0){
                    //  成功するとrowid 失敗は　-1
                    cnt ++;
                }
                else{
                    Log.d("■", "" + mFlg);
                }
            }
            mDb.setTransactionSuccessful();
        }catch (Exception e){
            Log.d("■", "" + e.getMessage());
            result = -1;
        }
        finally {
            mDb.endTransaction();
        }

        //  受け取った配列要素と処理数が合致しないと -1を返す
        if(item.length() == cnt){
            //Constants.setPrefrenceBoolean(mContext, Constants.DB_INITIALIZED, true);

            result =  cnt;
        } else {
            //  リソース数　　成功数
            result =  - ( item.length() * 1000 + cnt ) ;
        }
        return result;
    }

    public long insert(String mCategory, String mName){
        /////   新規登録

        if(mCategory.length() <= 0 || mName.length() <= 0){
            return -1000;
        }

        try {

            //  現在時刻を登録
            Date date = new Date();
            String strDate = String.format("%d", date.getTime());

            //  DBへInsert
            ContentValues cv = new ContentValues();
            cv.put("category", mCategory);   //
            cv.put("last_date", strDate);   //
            cv.put("name", mName); //
            cv.put("visible", 1);   //
            long mFlg = mDb.insert("item_table", null, cv);

            return mFlg;
        }
        catch(SQLiteConstraintException e){
            return -1;
        }
    }

    public long insertMemo(String mTitle, ArrayList<clsShoppingItem> mList){
        /////   新規登録（Memo）

        String mStr = array2string(mList);

        if( mStr.length() <= 0){
            return -1000;
        }

        try {

            //  現在時刻を登録
            Date date = new Date();
            String strDate = String.format("%d", date.getTime());

            mTitle = mTitle.replaceAll("^[\\s　]*", "").replaceAll("[\\s　]*$", "");

            if(mTitle.length() <=0){
                //  空白の場合は日付をtitleにする
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d");
                mTitle = sdf.format(date);
            }

            Cursor c = mDb.rawQuery
                    ("select title from list_memo where title like \"" + mTitle + "%\" ", null);
                    //  ※↑シングルクォーテーョンでは正規表現が効かない
            Pattern p = Pattern.compile(".*\\((\\d*)\\).*");
            int mMax = 0;
            if(c.moveToFirst()){
                do{
                    Matcher m = p.matcher(c.getString(0));
                    if(m.find()){

                        mMax = Math.max(mMax, Integer.parseInt(m.group(1)));

                    }

                }while(c.moveToNext());
            }
            c.close();
            mTitle += String.format("(%d)", mMax + 1);

            //  DBへInsert
            ContentValues cv = new ContentValues();
            cv.put("title", mTitle);   //
            cv.put("memo", mStr); //
            cv.put("reg_date", strDate);   //
            cv.put("protected", 0);   //  初期値は0 = 非保護

            long mFlg = mDb.insert("list_memo", null, cv);
            return mFlg;
        }
        catch(Exception e){
            Log.d("■", "" + e.getMessage());
            return -1;
        }
    }

    public int insertInitialCategory(TypedArray item){
        /////   カテゴリーの初期登録
        int cnt =0;
        mDb.beginTransaction();
        try {
            mDb.execSQL("delete from category_table");

            SQLiteStatement stmt = mDb.compileStatement(
                    "INSERT INTO category_table " +
                            "(code, name, parent, subcode, visible ) " +
                            " VALUES ( ?, ?, ?, ?, ? )");

            try {
                for (int i = 0; i < item.length(); i++) {
                    String[] mItem = item.getString(i).split(";", 0);
                    stmt.bindString(1, mItem[0]);
                    stmt.bindString(2, mItem[1]);
                    stmt.bindString(3, "");
                    stmt.bindString(4, "");
                    stmt.bindLong(5, 1);
                    if(stmt.executeInsert() >= 0){
                        cnt ++;
                    }
                }
            } finally {
                stmt.close();
            }
            mDb.setTransactionSuccessful();
        } finally {
            mDb.endTransaction();
        }
        return ( item.length() == cnt) ? cnt : -1;
    }

    public int updateName (int aID,String aNewName){
        /////   名前の変更
        try{
            if(aNewName.length() < 1){      //  ""は禁止
                throw new DataFormatException();
            }

            ContentValues cv = new ContentValues();
            cv.put("name", aNewName);
            return mDb.update("item_table", cv, "_id=" + aID, null);
        }
        catch(Exception e){
            return -1;
        }
    }

    public int updateLastTime (String aName){
        /////   使用時刻を更新　⇒sortに利用
        try{
            Date date = new Date();
            String strDate = String.format("%d", date.getTime());

            ContentValues cv = new ContentValues();
            cv.put("last_date", strDate);
            String[] aName0 = {aName};
            return mDb.update("item_table", cv, "name = ?" , aName0);
        }
        catch(Exception e){
            Log.d("■", "" + e.getMessage());
            return -1;
        }
    }

    public ArrayList<String[]> getItemiData (int visible){
        /////   表示対象の品目iデータを取得

        visible = visible == 1 ? 1 : 0; //  1=表示以外は 0=非表示

        try {
            String sql = "select category, name, _id from item_table where visible = " +
                    visible + " order by last_date DESC";
            Cursor csr = mDb.rawQuery(sql, null);

            if(csr.getCount() != 0) {

                ArrayList<String[]> rtnUri = new ArrayList<String[]>();
                csr.moveToFirst();
                do {
                    String[] aaa = new String[]{csr.getString(0), csr.getString(1), csr.getString(2)};
                    rtnUri.add(aaa);

                } while (csr.moveToNext());
                csr.close();
                return rtnUri;
            }
            else{
                csr.close();
                return null;
            }
        }
        catch (Exception e){
            Log.d("■", "getItemiData " + e.getMessage() );
            return null;
        }
    }

    public String[] getMemoByPosition (int position){
        /////   指定位置のメモを取り出し（新しい順）先頭は1

        try {
            //  指定位置 % レコード数で剰余を求めoffsetとする
            //  ⇒レコード数が不足しても循環して取り出せる
            int mPos = ( position - 1 ) % (int)DatabaseUtils.queryNumEntries(mDb, "list_memo");

            String sql = "select title, memo from list_memo order by _id DESC limit 1 offset " +
                    String.valueOf(mPos);
            Cursor csr = mDb.rawQuery(sql, null);
            if(csr.getCount() != 0) {

                csr.moveToFirst();

                    String[] rtn = new String[]{
                            csr.getString(0),
                            csr.getString(1)
                    };

                csr.close();
                return rtn;
            }
            else{
                csr.close();
                return null;
            }
        }
        catch (Exception e){
            Log.d("■", "getMemoDataAll " + e.getMessage() );
            return null;
        }
    }

    public int delete (Integer id){
        /////   削除

        int result = mDb.delete("item_table", "_id =" + id, null);
        return result;
    }

    public int deleteMemoOverLimit (int mLimit){

        try {
            //  現在の登録数を確認
            long regNum = DatabaseUtils.queryNumEntries(mDb, "list_memo");

            //  delete + limitは直接使えない？  サブクエリを使う
            //  offsetは除外する＝残す数、limit 1で削除する先頭の_idを取得し
            //  それ以下の_idを持つレコードを削除。protectedされているものは除く
            mDb.execSQL(
                    "delete from list_memo where protected = 1 or _id <= " +
                            "( select _id from list_memo order by reg_date DESC limit 1 offset "
                            + String.valueOf(mLimit) + " );");
            //  削除後の登録数を確認
            long regNum2 = DatabaseUtils.queryNumEntries(mDb, "list_memo");

            Log.d("■", regNum + " -> " + regNum2);

            return (int)(regNum - regNum2);

        }catch(Exception e){
            Log.d("■", "err " + e.getMessage());
            return -1;
        }

    }

    static String array2string(ArrayList<clsShoppingItem> mList){
        //  配列を改行付き文字列に変換
        String rtn ="";
        for (int i=0; i < mList.size(); i++){
            rtn += mList.get(i).item + "\n";

        }
        return  rtn;
    }

}