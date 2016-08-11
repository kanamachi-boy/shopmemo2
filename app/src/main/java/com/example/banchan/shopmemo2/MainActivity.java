package com.example.banchan.shopmemo2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    //  drawer関連
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawer;
    private ListView mDrawerList;
    private ItemSelectAdaptor ISA;

    private TextView TV1;
    private ListView LV1;
    private ItemListAdaptor ILA;
    private ArrayList<clsShoppingItem> shoppingItems;

    private clsShoppingItem lastRemoveItem;
    //private MenuItem undoMenu;
    private int memoPosition;
    private ImageView imgGuide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TV1 = (TextView)findViewById(R.id.memo_title);
        LV1 = (ListView)findViewById(R.id.memo_list);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.drawer_list);
        //undoMenu = (MenuItem)findViewById(R.id.action_undo);

        LV1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (id == 0L) {
                    //  取り消し線FLGの反転
                    shoppingItems.get(position).bought = !shoppingItems.get(position).bought;
                    refreshItemList();
                } else if (id == 1L) {  //  idを使ってImageButton（削除）のClick識別
                    //  各項目が保持している位置は整合化されていない
                    shoppingItems.get(position).position = position;
                    //  削除する前に記録しておく（positionがズレてしまうので）
                    lastRemoveItem = shoppingItems.get(position);
                    shoppingItems.remove(position);
                    refreshItemList();
                }
            }
        });

        shoppingItems = new ArrayList<clsShoppingItem>();
        //refreshItemList();

        registerForContextMenu(mDrawerList);
/*
        mDrawerList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, ISA.getItemMember(position, 1), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
*/
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawer,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                0,  /* "open drawer" description */
                0  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {

            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                //refreshDrawerList();
            }

        };

        mDrawer.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        lastRemoveItem = new clsShoppingItem();
        //  空のインスタンスを削除しておく
        lastRemoveItem = null;

        //  ガイドを表示
        if(Constants.getPrefrenceBoolean(this, Constants.DISPLAY_GUIDE, true)) {
            imgGuide = (ImageView) findViewById(R.id.img_guide);
            Bitmap mBMP = makeBalloon(getString(R.string.guide1), 18);  //  リソースを参照
            imgGuide.setImageBitmap(mBMP);
            imgGuide.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    setGuideUnvisible();
                    if ((event.getY() / imgGuide.getHeight()) > 0.85) {
                        //  最下行付近をタッチすると今後表示しない
                        Constants.setPrefrenceBoolean(MainActivity.this, Constants.DISPLAY_GUIDE, false);
                        Toast.makeText(MainActivity.this, "以降表示しません", Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            });
        }

    }

    @Override
    public void onResume(){
        super.onResume();

        refreshDrawerList();

        memoPosition = 0;
        int mTextSize = Integer.parseInt(Constants.getPrefrenceString(this, Constants.TEXT_SIZE, "20"));

        //  テキストサイズに応じ、レイアウト経由で幅を調整
        mDrawerList.getLayoutParams().width = mTextSize * 12 > 320 ? 320 : mTextSize * 12;
        mDrawerList.requestLayout();

    }

    @Override
    public void onPause(){
        super.onPause();
        mDrawer.closeDrawers();
    }

    @Override
    public void onCreateContextMenu
            (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        //■■■■ registerForContextMenu()で登録したViewが長押しされると、
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_context, menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        switch (item.getItemId()){
            case R.id.context_delete:
                //  削除
                mDrawer.closeDrawers();

                itemDeleteDialog(MainActivity.this,
                        Integer.parseInt(ISA.getItemMember(info.position, 2).toString()),
                        ISA.getItemMember(info.position, 1).toString()
                );

                refreshDrawerList();
                break;

            case R.id.context_update:
                //  品名変更
                mDrawer.closeDrawers();

                itemRenameDialog(MainActivity.this,
                        Integer.parseInt(ISA.getItemMember(info.position, 2).toString()),
                        ISA.getItemMember(info.position, 1).toString()
                );

                refreshDrawerList();
                break;

        }

        return true;
    }   //  Context メニュー

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){

        mDrawer.closeDrawers();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {

            return true;
        }

        switch (item.getItemId()){
            case R.id.action_clear:
                //  リストの初期化
                shoppingItems = new ArrayList<clsShoppingItem>();
                //ItemList = new ArrayList<String>();
                //undoStack = new UndoManager();
                refreshItemList();
                lastRemoveItem = null;    //  戻るの無効化

                break;

            case R.id.action_undo:
                //  リストに戻す
                if(lastRemoveItem != null) {
                    itemAddToList(lastRemoveItem.position, lastRemoveItem);
                    lastRemoveItem = null;
                }
                else{
                    Toast.makeText(this, "戻せる項目はありません", Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.action_add:
                //  DBにアイテム登録
                itemResisterDialog(this);

                break;

            case R.id.action_memory:
                //  メモを登録
                DatabaseHelper DBH1 = new DatabaseHelper(this);
                long rtn1 = DBH1.insertMemo("", shoppingItems);
                DBH1.close();

                Toast.makeText(this, rtn1>0 ? "登録しました":"空白メモは登録できません" ,
                        Toast.LENGTH_SHORT).show();

                break;

            case R.id.action_load:
                //  メモを呼び出し
                try {
                    DatabaseHelper DBH2 = new DatabaseHelper(this);
                    //  defaultで5件残して削除
                    DBH2.deleteMemoOverLimit
                            (Integer.parseInt(Constants.getPrefrenceString(this, Constants.KEEP_MEMO_QTY, "5")));
                    String rtn2[] = DBH2.getMemoByPosition(memoPosition); //
                    memoPosition ++;
                    DBH2.close();

                    TV1.setText(rtn2[0]);
                    loadListString(rtn2[1]);

                }catch(Exception e){
                    Toast.makeText(this, "呼び出せるメモがありません\n" + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }

                lastRemoveItem = null;    //  戻るの無効化

                break;

            case R.id.action_setting:
                //  設定
                Intent intent = new Intent(this, SettingFGActivity.class);
                startActivity(intent);
                //finish();
                break;

            case R.id.action_end:
                //  終了　メモも破棄される
                finish();
                break;

            default:
        }

        setGuideUnvisible();
        return true;

    }   //  Option Menu

    public void itemDeleteDialog (final Context context, final int mID, String mName){

        try {
            //  品名削除
            LinearLayout alertLayout0 = new LinearLayout(context);
            alertLayout0.setOrientation(LinearLayout.VERTICAL);
            alertLayout0.setPadding(15, 15, 15, 15);
            alertLayout0.setBackgroundColor(Color.argb(32, 255, 0, 0)); //  第一パラメータを0にすると透明

            final TextView textView = new TextView(context);
            textView.setText(mName + " を\n品目リストから削除します。");
            textView.setTextSize(16);
            alertLayout0.addView(textView);

            AlertDialog.Builder builder0 = new AlertDialog.Builder(context);
            builder0.setTitle("品目削除");
            //  作成したレイアウトをセット
            builder0.setView(alertLayout0)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DatabaseHelper DBH = new DatabaseHelper(context);
                                    int rtn = DBH.delete(mID);
                                    DBH.close();
                                    Toast.makeText(MainActivity.this, rtn == 1 ?
                                            "削除しました" :
                                            "削除に失敗しました", Toast.LENGTH_SHORT).show();

                                }
                            })
                    .setNegativeButton("キャンセル",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                    .show();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }   //

    public void itemRenameDialog (final Context context, final int mID, String mName){

        try {
            //  品名変更
            LinearLayout alertLayout0 = new LinearLayout(context);
            alertLayout0.setOrientation(LinearLayout.VERTICAL);
            alertLayout0.setPadding(15, 15, 15, 15);
            alertLayout0.setBackgroundColor(Color.argb(32, 255, 0, 0)); //  第一パラメータを0にすると透明

            final TextView textView = new TextView(context);
            textView.setText("新しい品目名を入力してください。");
            textView.setTextSize(16);
            alertLayout0.addView(textView);

            final EditText editText = new EditText(context);
            editText.setText(mName);
            editText.setTextSize(Integer.parseInt(Constants.getPrefrenceString(context, Constants.TEXT_SIZE, "20")));
            alertLayout0.addView(editText);

            AlertDialog.Builder builder0 = new AlertDialog.Builder(context);
            builder0.setTitle("品目名変更");
            //  作成したレイアウトをセット
            builder0.setView(alertLayout0)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DatabaseHelper DBH = new DatabaseHelper(context);
                                    int rtn = DBH.updateName(mID, editText.getText().toString());
                                    DBH.close();
                                    Toast.makeText(MainActivity.this, rtn == 1 ?
                                            "変更しました" :
                                            "変更に失敗しました", Toast.LENGTH_SHORT).show();

                                }
                            })
                    .setNegativeButton("キャンセル",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                    .show();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }   //

    public void itemResisterDialog (final Context context){

        try {
            //  品名登録
            LinearLayout alertLayout0 = new LinearLayout(context);
            alertLayout0.setOrientation(LinearLayout.VERTICAL);
            alertLayout0.setPadding(15, 15, 15, 15);
            alertLayout0.setBackgroundColor(Color.argb(32, 0, 255, 0)); //  第一パラメータを0にすると透明

            final EditText editText = new EditText(context);
            editText.setHint("品名を入力してください");
            editText.setTextSize(Integer.parseInt(Constants.getPrefrenceString(context, Constants.TEXT_SIZE, "20")));
            alertLayout0.addView(editText);

            AlertDialog.Builder builder0 = new AlertDialog.Builder(context);
            builder0.setTitle("品名入力＆登録");
            //  作成したレイアウトをセット
            builder0.setView(alertLayout0)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DatabaseHelper DBH = new DatabaseHelper(context);
                                    int rtn = (int)DBH.insert("999", editText.getText().toString());
                                    DBH.close();
                                    String mMsg;

                                    switch (rtn){
                                        case -1000:
                                            mMsg = "空白は入力できません";
                                            break;
                                        case -1:
                                            mMsg = "品名は登録済みです";
                                            break;

                                        default:
                                            mMsg = "登録しました";
                                            break;
                                    }
                                    if(rtn >= -1 ) {
                                        //  空白以外、表示はする
                                        clsShoppingItem mCls = new clsShoppingItem();
                                        mCls.item = editText.getText().toString();
                                        itemAddToList(-1, mCls);
                                    }

                                    Toast.makeText(MainActivity.this, mMsg, Toast.LENGTH_SHORT).show();

                                }
                            })
                    .setNegativeButton("キャンセル",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                    .show();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }   //

    public void refreshDrawerList(){

        final DatabaseHelper DBH = new DatabaseHelper(MainActivity.this);
        if(! Constants.getPrefrenceBoolean(MainActivity.this, Constants.DB_INITIALIZED, false)){
            //  初回起動時のXML⇒DB読み込み
            int rtn = DBH.initialSetting(getResources().obtainTypedArray(R.array.item_name));

            int rtn2 = DBH.insertInitialCategory(getResources().obtainTypedArray(R.array.category_name));

            Toast.makeText(this, "初期化しています\n" + rtn + "\n" + rtn2, Toast.LENGTH_SHORT).show();
            Constants.setPrefrenceBoolean(MainActivity.this, Constants.DB_INITIALIZED, true);
        }

        if(ISA != null){
            ISA = null;
        }

        ISA = new ItemSelectAdaptor(this, DBH.getItemiData(1));
        mDrawerList.setAdapter(ISA);
        DBH.close();

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                clsShoppingItem mCls = new clsShoppingItem();
                mCls.item = ISA.getItemMember(position, 1).toString();
                itemAddToList(-1, mCls);

            }
        });

    }

    public void itemAddToList(int mPosition, clsShoppingItem mCls){

        for (int i = 0; i < shoppingItems.size(); i++) {
            if (shoppingItems.get(i).item.equals(mCls.item)) {
                Toast.makeText(this, "品目が重複しています", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if(mPosition < 0 || mPosition > shoppingItems.size()){
            //  無効な位置は最後に追加
            mPosition = shoppingItems.size();
        }

        if(mCls.bought == null){
            //  買物済みが未設定ならfalse
            mCls.bought = false;
        }

        shoppingItems.add(mPosition, mCls);

        refreshItemList();

        lastRemoveItem = null;    //  戻るを無効化

        DatabaseHelper DBH = new DatabaseHelper(MainActivity.this);
        int rtn = DBH.updateLastTime(mCls.item);
        DBH.close();

        setGuideUnvisible();

    }

    public void loadListString(String mStr){

        //shoppingItems = new ArrayList<clsShoppingItem>();
        shoppingItems.clear();

        ArrayList<String> mList = string2array(mStr);
        for (int i=0; i < mList.size(); i++){
            clsShoppingItem mCls = new clsShoppingItem();
            mCls.item = mList.get(i);
            itemAddToList(-1, mCls);

        }

    }

    public void refreshItemList(){

        if ( ILA == null){
            ILA = new ItemListAdaptor(this, shoppingItems);
        }
        else{
            ILA.refresh(shoppingItems);
        }
        LV1.setAdapter(ILA);
    }

    static ArrayList<String> string2array(String mStr){
        //  改行付き文字列を配列に変換
        ArrayList<String> rtn = new ArrayList<String>();
        String[] mList = mStr.split("\\n",0);
        for(int i=0; i < mList.length; i++){
            if(mList[i] != null && ! mList[i].equals("")){
                rtn.add(mList[i]);

            }

        }
        return rtn;

    }

    public Bitmap makeBalloon (String aText, int aArgTextSize ){

        int aTextColor = Color.BLACK;
        //int aShadowColor = Color.GRAY;

        if (aText.length() == 0 || aArgTextSize < 6){
            return null;
        }
        //  表示サイズを端末の画面密度により調整する
        float density = getApplicationContext().getResources().getDisplayMetrics().scaledDensity;
        int aTextSize = (int)(aArgTextSize * density);

        //  テキストを行単位に分割する
        String[] subLines = aText.split("\n",0);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG); // テキスト用paint作成
        paint.setTextSize(aTextSize);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float mAscent = fontMetrics.ascent;
        int fontHeight = (int)(fontMetrics.descent - mAscent);
        int blockHeight = fontHeight * subLines.length;

        //  行毎にサイズを調べる
        int rowMax = 0;
        for(int j=0; j < subLines.length; j++){
            int thisRow = (int) paint.measureText(subLines[j]);
            rowMax = rowMax < thisRow ? thisRow : rowMax;
        }

        //  ベースキャンバスを作る
        int rectHeight = (int)(blockHeight * 1.1);
        Bitmap bitmap_rtn = Bitmap.createBitmap
                ((int)(rowMax * 1.1), rectHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap_rtn);
        canvas.drawColor(Color.rgb(171, 171, 237));
        paint.setColor(aTextColor);
        //float aDiff = 0.2F;
        //paint.setShadowLayer(2f, aDiff, aDiff, aShadowColor);

        int centeringX =(int)(rowMax * 0.05);
        for(int j=0; j < subLines.length; j++){

            int centeringY =(int)( ( rectHeight - blockHeight ) / 2
                    - mAscent +( fontHeight) * j  );

            canvas.drawText(subLines[j], centeringX, centeringY, paint);
        }

        //  噴出しに加工して返す
        return transferRoundRectImage(bitmap_rtn);

    }   //  画像にコメントを追加

    static  Bitmap transferRoundRectImage(Bitmap org){

        // 画像サイズ取得
        int width  = org.getWidth();
        int height = org.getHeight();

        // リサイズ後サイズ
        int w = width;
        int h = height;

        double mRadiusRatio = 0.10;

        int mR = (int) (w * mRadiusRatio);   //

        // 切り取り領域となるbitmap生成
        Bitmap clipArea = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        // 角丸矩形を描写（切り取り用）
        // RectFで角丸描写、arg3、arg4は半径、ANTI_ALIASでふちをぼかして滑らかにする
        Canvas c = new Canvas(clipArea);
        c.drawRoundRect(new RectF(0, 0, w, h), mR, mR, new Paint(Paint.ANTI_ALIAS_FLAG));

        // 角丸画像となるbitmap生成
        Bitmap newImage = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        // 切り取り領域を描写
        Canvas canvas = new Canvas(newImage);
        Paint paint = new Paint();
        canvas.drawBitmap(clipArea, 0, 0, paint);

        // 切り取り領域内にオリジナルの画像を描写
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(org, new Rect(0, 0, width, height), new Rect(0, 0, w, h), paint);

        //
        //  吹き出しの突起部分を作る
        Bitmap newImage2 = Bitmap.createBitmap(w + 5 , h+30, Bitmap.Config.ARGB_8888);
        Canvas cv2 = new Canvas(newImage2);
        Paint paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint2.setColor(Color.rgb(171, 171, 237));
        paint2.setStyle(Paint.Style.FILL);  //  塗りつぶし
        Path path = new Path();
        path.moveTo(30F, 35F);
        path.lineTo(15F, 5F);
        path.lineTo(60F, 30F);
        path.close();   //  三角形を閉じる
        cv2.drawPath(path, paint2);

        cv2.drawBitmap(newImage, 5, 30, paint2);

        return newImage2;


    }   //  角丸に加工する【新】

    public void setGuideUnvisible(){
        if(imgGuide != null &&
                imgGuide.getVisibility() == View.VISIBLE) {

            imgGuide.setVisibility(View.INVISIBLE); //  非表示にする
            imgGuide.setImageBitmap(null);
        }
    }

}
