package com.example.swipe;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends Abstract implements View.OnClickListener {

    //スワイプ用
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
    private final int MP = ViewGroup.LayoutParams.MATCH_PARENT;

    //日付け格納用
    int current_year = 0;
    int current_month = 0;

    int evacuate_day = 0;
    //setTagで使う
    Object obj;
    MyDrawerLayout drawerLayout;
    Calendar cal;
    private GestureDetector mGestureDetector;
    SharedPreferences data;
    SharedPreferences.Editor editor;
    String start_day;
    String end_day;
    //貯金中かを表すフラグ
    boolean savings_amount_flg;
    //カレンダーダイアログ
    DatePickerDialog datePickerDialog;




    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //スワイプ処理のリスナー
        mGestureDetector = new GestureDetector(this, mOnGestureListener);

        data = getSharedPreferences("DataSave", Context.MODE_PRIVATE);
        editor = data.edit();
        //貯金中か判定
        savings_amount_flg = data.getBoolean("flg",false);
        //年、月取得
        getCurrentYearAndMonth();
        //表示するレイアウト作成
        makeLayout();

        if (savings_amount_flg) {
            //終了日ならダイアログ表示
            if (isEndToday()) {
                endNotification();
            }
        }




    }

    //現在の月と日をクラス変数にセット
    public void getCurrentYearAndMonth () {

        current_year = getIntent().getIntExtra("YEAR", -999);
        current_month = getIntent().getIntExtra("MONTH", -999);
        cal = Calendar.getInstance();

        //起動後、ページ移動していない状態(intentに値があるかで判断)なら当月を取得
        if (current_year == -999 || current_month == -999) {

            current_year = cal.get(Calendar.YEAR);
            current_month = cal.get(Calendar.MONTH) + 1;
            //1以下なら去年の12月へ
        } else if (current_month < 1) {

            current_year = current_year - 1;
            current_month = 12;

            //12以上なら来年の1月へ
        } else if (current_month > 12) {

            current_year = current_year + 1;
            current_month = 1;

        }

        cal.set(Calendar.YEAR, current_year);
        cal.set(Calendar.MONTH, current_month - 1);
        cal.set(Calendar.DATE, 1);

    }

    @Override
    public void onClick(View v) {

        //どのボタンが押されたか、Tagで判断
        String button_tag = String.valueOf(v.getTag());

        //サイドメニュー内のボタン
        if (button_tag.equals("go")) {

            Intent intent = new Intent(MainActivity.this, SavingsAmountConfActivity.class);
            startActivity(intent);

        } else if (button_tag.equals("icon")) {

            Context context = getApplicationContext();
            drawerLayout.openDrawer(Gravity.LEFT);

            //ギブアップボタン
        } else if (button_tag.equals("giveUp")) {

            SharedPreferences data = getSharedPreferences("DataSave", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = data.edit();
            editor.putBoolean("flg", false);
            editor.apply();

            Context context = getApplicationContext();
            Toast.makeText(context, "次またがんばりまっしょい", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);


        } else if (button_tag.equals("timeLeap")) {


            // 日付設定ダイアログの作成・リスナの登録
            datePickerDialog = new DatePickerDialog(this,
                    android.R.style.Theme_Holo_Dialog, DateSetListener, current_year,
                    current_month, 1);

            //日はいらない。7.0以上では通用しないらしい
            DatePicker datePicker = datePickerDialog.getDatePicker();
            int dayId = Resources.getSystem().getIdentifier("day", "id", "android");
            datePicker.findViewById(dayId).setVisibility(View.GONE);

            datePickerDialog.show();


            //日付ボタン
        } else {

            v.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    //これでボタン上でもスワイプが可能
                    return mGestureDetector.onTouchEvent(event);
                }
            });

            //ボタンに持たせた番号を取得
            String evacuate_string = String.valueOf(v.getTag());
            int chosen_button_num = Integer.parseInt(evacuate_string);

            Intent intent = new Intent(MainActivity.this, AmountUsedList.class);
            intent.putExtra("YEAR", String.valueOf(current_year));
            intent.putExtra("MONTH", String.valueOf(current_month));
            intent.putExtra("DAY", String.valueOf(chosen_button_num));
            startActivity(intent);

        }

    }


    //当月の総使用金額をTextViewにセットする
    public TextView calculateTotalAmount(TextView pTextVie, String pCurentDay) {

        try {


            if (helper == null) {
                helper = new DbOpenHelper(this);
            }
            if (db == null) {
                db = helper.getReadableDatabase();
            }

            //総額
            String sql = "select sum(price) from amount_used where date like '%' || ? || '%' ESCAPE '$'";
            c = db.rawQuery(sql, new String[]{pCurentDay});
            boolean isEof = c.moveToFirst();
            while (isEof) {

                pTextVie.setText(String.format("%d", c.getInt(0)) + "円");
                pTextVie.setTextSize(24);
                isEof = c.moveToNext();
            }

        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
         /*   if (db != null) {
                db.close();
            }*/

        }

        return pTextVie;

    }

    //当日の使用金額を返す
    public int usageAmountOfTheDay(String pDay) {

        int usage_amount_of_the_day = 0;
        String zero_padding_shaping_current_month = zeroPadding(String.valueOf(current_month));
        pDay = zeroPadding(pDay);
        String current_day_used_sql = current_year + "-" + zero_padding_shaping_current_month + pDay;

        if (helper == null) {
            helper = new DbOpenHelper(this);
        }
        if (db == null) {
            db = helper.getReadableDatabase();
        }


        String sql = "select sum(price) from amount_used where date = ?";
        c = db.rawQuery(sql, new String[]{current_day_used_sql});
        boolean isEof = c.moveToFirst();

        if (c.getCount() == 1) {
            while (isEof) {

                usage_amount_of_the_day = c.getInt(0);
                isEof = c.moveToNext();

            }
        }
        return usage_amount_of_the_day;
    }

    //(貯金目標額－今日までの使用金額)÷残り日数で1日あたりの使用可能金額を返す
    public int availableAmount (int pTartgetAmount, int pDaysLeft, int pUsageAmount) {

        try {

            int ret;
            ret = (pTartgetAmount - pUsageAmount) / pDaysLeft;
            return ret;
        } catch (ArithmeticException e) {
            int error_ret = 777;
            return  error_ret;
        }


    }

    //終了日までの残り日数を返す
    public int daysLeftCalculation (String pEndDay) {


        //今日が何日か取得
        Calendar w_cal = Calendar.getInstance();

        //終了日
        String end_month = pEndDay.substring(0,2);
        String end_day = pEndDay.substring(2,4);

        //0埋め解除
        if(end_month.substring(0,1).equals("0")) {
            end_month = end_month.substring(1,2);
        }

        if (end_day.substring(0,1).equals("0")) {
            end_day = end_day.substring(1,2);
        }

        int after_conversion_month = Integer.parseInt(end_month);
        int after_conversion_day = Integer.parseInt(end_day);
        //ようやく最終日の値を持ったcalendarインスタンスが完成する
        Calendar target_end_day = Calendar.getInstance();
        target_end_day.set(Calendar.MONTH,after_conversion_month - 1);
        target_end_day.set(Calendar.DATE,after_conversion_day);

        //現在から終了日までの日数
        int days = getDiffDays(target_end_day,w_cal);

        //当日を含めたい
        return days + 1;

    }

    public int getDiffDays(Calendar calendar1, Calendar calendar2) {
        //==== ミリ秒単位での差分算出 ====//
        long diffTime = calendar1.getTimeInMillis() - calendar2.getTimeInMillis();

        //==== 日単位に変換 ====//
        int MILLIS_OF_DAY = 1000 * 60 * 60 * 24;
        int diffDays = (int)(diffTime / MILLIS_OF_DAY);

        return diffDays;
    }

    //開始日から現在までの使用金額を返す
    public int curentUsageAmount(String pStartDay) {

        /*   try {


         */

        int ret = 0;
        Calendar w_cal = Calendar.getInstance();




        if (helper == null) {
            helper = new DbOpenHelper(this);
        }
        if (db == null) {
            db = helper.getReadableDatabase();
        }

        int current_year = w_cal.get(w_cal.YEAR);
        int current_month = w_cal.get(w_cal.MONTH) + 1;
        int current_day = w_cal.get(w_cal.DATE);

        //sql用に今日の日付を整形
        String before_shaping_current_year = zeroPadding(String.valueOf(current_year));
        String before_shaping_current_month = zeroPadding(String.valueOf(current_month));
        String before_shaping_current_day = zeroPadding(String.valueOf(current_day));
        //yyyy-mmdd
        String use_sql_current_day = before_shaping_current_year + "-" + before_shaping_current_month + before_shaping_current_day;
        pStartDay = before_shaping_current_year + "-" + pStartDay;

        String sql = "select sum(price) from amount_used where date between ? and ?";
        c = db.rawQuery(sql, new String[]{pStartDay,use_sql_current_day});
        boolean isEof = c.moveToFirst();
        while (isEof) {

            ret = c.getInt(0);
            isEof = c.moveToNext();

        }

        return ret;

    }

    // これがないとGestureDetectorが動かない
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    //レイアウト作成
    public void makeLayout() {

          /* ヘッダー部分作成
          サイドメニューを実装したいから頭はDrawerLayoutを使用*/
        drawerLayout = new MyDrawerLayout(this);
        drawerLayout.setLayoutParams(new MyDrawerLayout.LayoutParams(MP, WC));
        setContentView(drawerLayout);


        drawerLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //タッチ処理があった時、スワイプがどうかを始めに判断する
                return mGestureDetector.onTouchEvent(event);
            }
        });

        //画面スワイプとの兼ね合いでスワイプでサイドメニューを出す処理を禁じる
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);


        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.VERTICAL);
        headerLayout.setLayoutParams(new LinearLayout.LayoutParams(MP, MP));
        drawerLayout.addView(headerLayout);

        RelativeLayout headerRelativeLayout = new RelativeLayout(this);
        headerRelativeLayout.setBackgroundColor(Color.parseColor("#4169e1"));
        headerLayout.addView(headerRelativeLayout, MP, WC);

        //画面左上のアイコン。押すとサイドメニューが開く
        Button header_icon_button = new Button(this);
        header_icon_button.setBackgroundResource(R.drawable.side_menu);
        header_icon_button.setTag("icon");
        header_icon_button.setOnClickListener(this);
        headerRelativeLayout.addView(header_icon_button, 120, WC);


        //ヘッダーに表示する文字、背景色等を設定する
        TextView textView = new TextView(this);
        //文言
        textView.setText(String.valueOf(current_year) + "年" + String.valueOf(current_month) + "月");
        //サイズ
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        //親レイアウトの真ん中に配置する
        RelativeLayout.LayoutParams center_params = new RelativeLayout.LayoutParams(WC, WC);
        center_params.addRule(RelativeLayout.CENTER_IN_PARENT);
        headerRelativeLayout.addView(textView, center_params);


        //貯金中なら表示されるアイコン
        Button light_bulb_icon = new Button(this);
        light_bulb_icon.setBackgroundResource(R.drawable.light_bulb);
        //画面右上(親レイアウト右)に配置する
        RelativeLayout.LayoutParams right_parms = new RelativeLayout.LayoutParams(WC, WC);
        right_parms.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        headerRelativeLayout.addView(light_bulb_icon,right_parms);

        //貯金中でなければ隠す
        if (!savings_amount_flg) {
            light_bulb_icon.setVisibility(View.INVISIBLE);
        }





        RelativeLayout relativeLayout = new RelativeLayout(this);
        headerLayout.addView(relativeLayout);
        //こいつで画面上部のええ感じのとこに配置する
        RelativeLayout.LayoutParams top_center_params = new RelativeLayout.LayoutParams(WC, WC);
        top_center_params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        top_center_params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        top_center_params.setMargins(0, 60, 0, 0);

        //文言
        TextView the_amount_text = new TextView(this);
        the_amount_text.setText("当月使用金額");
        the_amount_text.setId(View.generateViewId());
        the_amount_text.setBackgroundResource(R.drawable.under_line);
        the_amount_text.setTextSize(24);
        relativeLayout.addView(the_amount_text, top_center_params);


        //当月使用した金額
        LinearLayout amountLayout = new LinearLayout(this);
        LinearLayout.LayoutParams amountParams = new LinearLayout.LayoutParams(MP, WC);
        amountParams.setMargins(0, 30, 0, 0);
        amountLayout.setLayoutParams(amountParams);
        headerLayout.addView(amountLayout);


        TextView the_amount = new TextView(this);
        the_amount.setText("0円");
        //当月の総使用額を取得
        String current_day_used_sql = String.valueOf(current_year) + "-" + zeroPadding(String.valueOf(current_month));
        the_amount = calculateTotalAmount(the_amount, current_day_used_sql);
        the_amount.setGravity(Gravity.CENTER);
        amountLayout.addView(the_amount, MP, WC);


        //貯金中なら表示される文言
        RelativeLayout secondRelativeLayout = new RelativeLayout(this);
        headerLayout.addView(secondRelativeLayout);

        TextView available_amount_text = new TextView(this);
        available_amount_text.setText("使用可能金額");
        available_amount_text.setId(View.generateViewId());
        available_amount_text.setBackgroundResource(R.drawable.under_line);
        available_amount_text.setTextSize(24);
        secondRelativeLayout.addView(available_amount_text, top_center_params);


        //1日辺り使用可能金額
        LinearLayout availableAmountLayout = new LinearLayout(this);
        availableAmountLayout.setLayoutParams(amountParams);
        headerLayout.addView(availableAmountLayout);

        TextView available_amount = new TextView(this);

        if (savings_amount_flg) {

            //設定ファイルから目標金額,開始日,終了日を取得
            String savings_amount = data.getString("savingsAmount","");
            start_day = data.getString("startDay","");
            end_day = data.getString("endDay","");

            //(目標貯金額-開始日から現在までの使用金額)÷日数で1日あたりの使用可能金額を算出
            //終了日までの残り日数
            int days_left = daysLeftCalculation(end_day);
            //開始日から現在までの使用金額
            int usage_amount = curentUsageAmount(start_day);
            //計算結果をセット
            available_amount.setText(String.valueOf(availableAmount(Integer.parseInt(savings_amount),days_left,usage_amount)));
            available_amount.setGravity(Gravity.CENTER);

        }

        availableAmountLayout.addView(available_amount, MP, WC);


        //貯金チャレンジ中でなければ隠す
        if (!savings_amount_flg) {
            available_amount_text.setVisibility(View.INVISIBLE);
            available_amount.setVisibility(View.GONE);
        }


        //カレンダー部分を作成していく
        LinearLayout base_layout = new LinearLayout(this);
        base_layout.setOrientation(LinearLayout.VERTICAL);
        base_layout.setLayoutParams(new LinearLayout.LayoutParams(MP, MP, 1));
        headerLayout.addView(base_layout);



        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(MP, MP, 1));
        linearLayout.setBackgroundColor(Color.GREEN);
        base_layout.addView(linearLayout);


        //上段の曜日を表示
        TextView sunday = new TextView(this);
        sunday.setText("日");
        sunday.setGravity(Gravity.CENTER);
        sunday.setBackgroundResource(R.drawable.text_layout);

        TextView monday = new TextView(this);
        monday.setText("月");
        monday.setGravity(Gravity.CENTER);
        monday.setBackgroundResource(R.drawable.text_layout);
        TextView tuesday = new TextView(this);

        tuesday.setText("火");
        TextView wednesday = new TextView(this);
        tuesday.setGravity(Gravity.CENTER);
        tuesday.setBackgroundResource(R.drawable.text_layout);

        wednesday.setText("水");
        TextView thursday = new TextView(this);
        wednesday.setGravity(Gravity.CENTER);
        wednesday.setBackgroundResource(R.drawable.text_layout);

        thursday.setText("木");
        TextView friday = new TextView(this);
        thursday.setGravity(Gravity.CENTER);
        thursday.setBackgroundResource(R.drawable.text_layout);

        friday.setText("金");
        TextView saturday = new TextView(this);
        friday.setGravity(Gravity.CENTER);
        friday.setBackgroundResource(R.drawable.text_layout);

        saturday.setText("土");
        saturday.setGravity(Gravity.CENTER);
        saturday.setBackgroundResource(R.drawable.text_layout);

        //弟2引き数にこいつ入れたらlayout_weight = 1と同じ効果に
        LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(MP, MP);
        param1.weight = 1.0f;

        linearLayout.addView(sunday, param1);
        linearLayout.addView(monday, param1);
        linearLayout.addView(tuesday, param1);
        linearLayout.addView(wednesday, param1);
        linearLayout.addView(thursday, param1);
        linearLayout.addView(friday, param1);
        linearLayout.addView(saturday, param1);


        linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(MP, MP, 1));
        base_layout.addView(linearLayout);


        //Calendarインスタンスを生成。先月の月をセット
        Calendar last_month_cal = Calendar.getInstance();
        last_month_cal.set(current_year, current_month - 2, 1);

        //当月の最終日
        int max_day = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        //前月の最終日
        int last_month_max_day = last_month_cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        //1日の曜日
        final int start_index = cal.get(Calendar.DAY_OF_WEEK);
        //今月の1日の曜日までを先月の末尾日を表示して埋めるための数字
        last_month_max_day = last_month_max_day - (start_index - 2);
        //穴埋め用
        TextView ohter_day;
        Button current_day;


        //1週目の1日までを先月の末尾日で埋める
        for (int i = 1; i < start_index; i++) {

            ohter_day = new TextView(this);
            ohter_day.setText(String.valueOf(last_month_max_day));
            ohter_day.setTextColor(Color.parseColor("#dddddd"));
            ohter_day.setBackgroundResource(R.drawable.text_layout);
            linearLayout.addView(ohter_day, param1);
            last_month_max_day++;

        }

        //当月の日数分ボタン生成
        for (int i = 1; i <= max_day; i++) {

            cal.set(Calendar.DATE, i);
            //ohter_day = new TextView(this);
            current_day = new Button(this);
            current_day.setText(String.valueOf(i));
            current_day.setGravity(Gravity.TOP);
            current_day.setGravity(Gravity.LEFT);
            //当日の使用金額によりアイコンを表示させる
            int usage_amount_of_the_day = 0;
            usage_amount_of_the_day = usageAmountOfTheDay(String.valueOf(i));

            if (usage_amount_of_the_day > 3000 && usage_amount_of_the_day < 5000) {
                current_day.setBackgroundResource(R.drawable.blue_siren);
            } else if (usage_amount_of_the_day > 5000) {
                current_day.setBackgroundResource(R.drawable.red_siren);
            } else {
                current_day.setBackgroundResource(R.drawable.text_layout);
            }

            //土日にはそれぞれ文字に色をつける
            if (Calendar.SATURDAY == cal.get(Calendar.DAY_OF_WEEK)) {
                current_day.setTextColor(Color.parseColor("#00bfff"));
            } else if (Calendar.SUNDAY == cal.get(Calendar.DAY_OF_WEEK)) {
                current_day.setTextColor(Color.parseColor("#ff0000"));
            }

            //何日が押されたか判別できるようにボタンに識別子を持たせる
            obj = i;
            current_day.setTag(obj);
            //ボタン上でスワイプきかせる
            current_day.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    //タッチ処理があった時、スワイプがどうかを始めに判断する
                    return mGestureDetector.onTouchEvent(event);
                }
            });
            current_day.setOnClickListener(this);
            linearLayout.addView(current_day, param1);


            //土曜日まで生成したら次の列へ
            if (Calendar.SATURDAY == cal.get(Calendar.DAY_OF_WEEK)) {

                linearLayout = new LinearLayout(this);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(MP, MP, 1));
                base_layout.addView(linearLayout);

            }

        }

        //当月の最終日が土曜日じゃない時、土曜日まで次の月の日付で埋める
        if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {

            int i = 1;
            for (int count = cal.get(Calendar.DAY_OF_WEEK); count < 7; count++) {

                ohter_day = new TextView(this);
                ohter_day.setText(String.valueOf(i));
                ohter_day.setTextColor(Color.parseColor("#dddddd"));
                ohter_day.setBackgroundResource(R.drawable.text_layout);
                linearLayout.addView(ohter_day, param1);
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(MP, MP, 1));
                i++;

            }
        }


        //サイドメニュー部
        LinearLayout sideMenuLayout = new LinearLayout(this);
        sideMenuLayout.setOrientation(LinearLayout.VERTICAL);
        sideMenuLayout.setBackgroundColor(Color.GRAY);
        DrawerLayout.LayoutParams lp = new DrawerLayout.LayoutParams(300, MP);
        lp.gravity = Gravity.LEFT;
        sideMenuLayout.setLayoutParams(lp);
        drawerLayout.addView(sideMenuLayout);



        //サイドメニュー内のボタン
        Button go_savings_amount_page_button = new Button(this);
        go_savings_amount_page_button.setText("貯金うぃる");
        go_savings_amount_page_button.setBackgroundColor(Color.GREEN);
        sideMenuLayout.addView(go_savings_amount_page_button, new LinearLayout.LayoutParams(MP, WC));
        go_savings_amount_page_button.setTag("go");
        go_savings_amount_page_button.setOnClickListener(this);

        Button give_up_button = new Button(this);
        give_up_button.setText("ぎぶあっぷ…");
        give_up_button.setBackgroundColor(Color.GREEN);
        sideMenuLayout.addView(give_up_button, new LinearLayout.LayoutParams(MP,WC));
        give_up_button.setTag("giveUp");
        give_up_button.setOnClickListener(this);

        Button time_leap_button = new Button(this);
        time_leap_button.setText("タイムリープ");
        time_leap_button.setBackgroundColor(Color.GREEN);
        sideMenuLayout.addView(time_leap_button,new LinearLayout.LayoutParams(MP,WC));
        time_leap_button.setTag("timeLeap");
        time_leap_button.setOnClickListener(this);

        //貯金中か否かで表示するボタンを変えている
        if (savings_amount_flg) {
            go_savings_amount_page_button.setVisibility(View.GONE);
        } else {
            give_up_button.setVisibility(View.GONE);
        }

    }

    //スワイプ判定
    private final GestureDetector.SimpleOnGestureListener mOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {

            try {

                if (Math.abs(event1.getY() - event2.getY()) > SWIPE_MAX_OFF_PATH) {
                    // 縦の移動距離が大きすぎる場合は無視
                    return false;
                }

                if (event1.getX() - event2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {

                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.putExtra("YEAR", current_year);
                    intent.putExtra("MONTH", current_month + 1);

                    startActivity(intent);
                    overridePendingTransition(R.animator.activity_open_enter, R.animator.activity_open_exit);

                } else if (event2.getX() - event1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {

                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.putExtra("YEAR", current_year);
                    intent.putExtra("MONTH", current_month - 1);

                    startActivity(intent);
                    overridePendingTransition(R.animator.activity_close_enter, R.animator.activity_close_exit);
                }

            } catch (Exception e) {
                // nothing
            }
            return false;
        }
    };


    //今日が終了日かどうか
    public boolean isEndToday () {

        boolean ret = false;
        cal = Calendar.getInstance();
        //sql用に0埋めで整形
        String current_month =  zeroPadding(String.valueOf(cal.get(cal.MONTH) + 1));
        String current_day =  zeroPadding(String.valueOf(cal.get(cal.DATE)));
        String after_shaping_current_day = current_month + current_day;

       if (after_shaping_current_day.equals(end_day)) {
           ret = true;
        }
        return ret;
    }


    //チャレンジ期間中使用した金額を返す
    public int resultAmountUsed () {

        int ret = 0;
        if (helper == null) {
            helper = new DbOpenHelper(this);
        }
        if (db == null) {
            db = helper.getReadableDatabase();
        }


        String after_shaping_start_day = String.valueOf(current_year) + "-" + start_day;
        String after_shaping_end_day = String.valueOf(current_year) + "-" + end_day;

        String sql = "select sum(price) from amount_used where date between ? and ?";
        //String sql = "select sum(price) from amount_used where date = ?";
        c = db.rawQuery(sql, new String[]{after_shaping_start_day,after_shaping_end_day});
        boolean isEof = c.moveToFirst();
        while (isEof) {

            ret = c.getInt(0);
            isEof = c.moveToNext();

        }
        return ret;
    }

    //終了した旨を伝えるダイアログ表示
    public void endNotification () {


        //目標金額
        int target_amount =  Integer.parseInt(data.getString("savingsAmount", "0"));
        //期間中使用金額
        int amount_used = resultAmountUsed();

        boolean judge_flg = false;
        //差額
        int difference = target_amount - amount_used;

        //目標金額-使用金額が0以上なら成功
        if (difference >= 0) {
            judge_flg = true;
        }



        // アラート表示
        android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("結果発表！！");

        if (judge_flg) {
            alertDialog.setMessage("成功です！使用額は" + String.valueOf(amount_used) + "円で、" + String.valueOf(difference) + "円の貯金に成功しました！");
        } else {
            alertDialog.setMessage("失敗です…。使用額は" + String.valueOf(amount_used) + "円で、" + String.valueOf(difference) + "円でした…");
        }

        alertDialog.setPositiveButton("閉じる", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //貯金チャレンジ終了
                editor.putBoolean("flg", false);
                editor.apply();

                Intent intent = new Intent(MainActivity.this,MainActivity.class);
                startActivity(intent);

            }
        });


        //追加
        alertDialog.setNegativeButton("詳細を確認", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                //貯金チャレンジ終了
                editor.putBoolean("flg", false);
                editor.apply();

                Intent intent = new Intent(MainActivity.this,MainActivity.class);
                startActivity(intent);

            }
        }).show();

    }

    // 日付設定時のリスナ作成
    DatePickerDialog.OnDateSetListener DateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(android.widget.DatePicker datePicker, int year,
                              int monthOfYear, int dayOfMonth) {


           Intent intent = new Intent(MainActivity.this,MainActivity.class);
            intent.putExtra("YEAR", year);
            intent.putExtra("MONTH", monthOfYear + 1);
           startActivity(intent);

        }
    };
}

