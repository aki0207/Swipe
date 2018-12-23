package com.example.swipe;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Abstract {


    //スワイプ用
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
    private final int MP = ViewGroup.LayoutParams.MATCH_PARENT;

    //日付け格納用
    int current_year = 0;
    int current_month = 0;
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

    int evacuate_day = 0;
    //setTagで使う
    Object obj;
    private GestureDetector mGestureDetector;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGestureDetector = new GestureDetector(this, mOnGestureListener);

        current_year = getIntent().getIntExtra("YEAR", -999);
        current_month = getIntent().getIntExtra("MONTH", -999);
        Calendar cal = Calendar.getInstance();

        //起動後、ページ移動していない状態(intentに値があるかで判断)なら当月を取得
        if (current_year == -999 || current_month == -999) {

            current_year = cal.get(Calendar.YEAR);
            current_month = cal.get(Calendar.MONTH) + 1;
            //0月なんてものは存在しない
        } else if (current_month < 1) {

            current_year = current_year - 1;
            current_month = 12;

            //13月なんてものも存在しない
        } else if (current_month > 12) {

            current_year = current_year + 1;
            current_month = 1;

        }

        cal.set(Calendar.YEAR, current_year);
        cal.set(Calendar.MONTH, current_month - 1);
        cal.set(Calendar.DATE, 1);

        //レイアウトを作成していく
        // ヘッダー部分を作成する
        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.VERTICAL);
        headerLayout.setLayoutParams(new LinearLayout.LayoutParams(MP, MP));
        setContentView(headerLayout);

        //ヘッダーに表示する文字、背景色等を設定する
        TextView textView = new TextView(this);
        //文言
        String str = String.valueOf(current_year) + "年" + String.valueOf(current_month) + "月";
        textView.setText(str);
        //背景色
        textView.setBackgroundColor(Color.parseColor("#4169e1"));
        //位置
        textView.setGravity(Gravity.CENTER);
        //サイズ
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        headerLayout.addView(textView,
                new LinearLayout.LayoutParams(MP, WC));


        String current_day_used_sql = String.valueOf(current_year) + "-" + String.valueOf(current_month);
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


        LinearLayout amountLayout = new LinearLayout(this);
        LinearLayout.LayoutParams amountParams = new LinearLayout.LayoutParams(MP, WC);
        amountParams.setMargins(0, 30, 0, 0);
        amountLayout.setLayoutParams(amountParams);
        headerLayout.addView(amountLayout);

        //金額
        TextView the_amount = new TextView(this);
        the_amount.setText("0円");
        //当月の総使用額を取得
        the_amount = calculateTotalAmount(the_amount, current_day_used_sql);
        the_amount.setGravity(Gravity.CENTER);
        amountLayout.addView(the_amount, MP, WC);


        //カレンダー部分を作成していく
        LinearLayout base_layout = new LinearLayout(this);
        base_layout.setOrientation(LinearLayout.VERTICAL);
        base_layout.setLayoutParams(new LinearLayout.LayoutParams(MP, MP, 1));
        headerLayout.addView(base_layout);


        //レイアウトのマージン(高さ)を設定する
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) base_layout.getLayoutParams();
        params.setMargins(0, 350, 0, 0);
        base_layout.setLayoutParams(params);
        //base_layout.setLayoutParams(params);


        //レイアウトを入れ子にする
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

        //xmlファイルで言うところのlayout_weight?
        LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(MP, MP);
        param1.weight = 1.0f;

        //なんか知らんけど弟2引き数にこいつ入れたらlayout_weight = 1と同じ効果になるっぽい
        linearLayout.addView(sunday, param1);
        linearLayout.addView(monday, param1);
        linearLayout.addView(tuesday, param1);
        linearLayout.addView(wednesday, param1);
        linearLayout.addView(thursday, param1);
        linearLayout.addView(friday, param1);
        linearLayout.addView(saturday, param1);

        //レイアウトを入れ子にする
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


            //ボタンに番号を持たせる
            obj = i;
            current_day.setTag(obj);

            if (Calendar.SATURDAY == cal.get(Calendar.DAY_OF_WEEK)) {
                current_day.setTextColor(Color.parseColor("#00bfff"));
            } else if (Calendar.SUNDAY == cal.get(Calendar.DAY_OF_WEEK)) {
                current_day.setTextColor(Color.parseColor("#ff0000"));
            }


            //ここでリスナー設定しとくことで全ボタンに設定できる気がする
            current_day.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    //これでボタン上でもスワイプが可能
                    return mGestureDetector.onTouchEvent(event);
                }
            });

            current_day.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //ボタンに持たせた番号を取得
                    String evacuate_string = String.valueOf(v.getTag());
                    int chosen_button_num = Integer.parseInt(evacuate_string);

                    Intent intent = new Intent(MainActivity.this, AmountUsedList.class);
                    intent.putExtra("YEAR", String.valueOf(current_year));
                    intent.putExtra("MONTH", String.valueOf(current_month));
                    intent.putExtra("DAY", String.valueOf(chosen_button_num));
                    startActivity(intent);

                }
            });


            linearLayout.addView(current_day, param1);


            //土曜日なら次の列へ
            if (Calendar.SATURDAY == cal.get(Calendar.DAY_OF_WEEK)) {

                linearLayout = new LinearLayout(this);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(MP, MP, 1));
                base_layout.addView(linearLayout);

            }

        }

        //当月の最終日が土曜日じゃない時、土曜日まで次の月の日付で埋める
        if (cal.get(Calendar.DAY_OF_WEEK) != 7) {

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

    }

    //当月の総使用金額をTextViewにセットする
    public TextView calculateTotalAmount(TextView pTextVie, String pCurentDay) {

        try {

            //金額取得
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
        pDay = zeroPadding(pDay);
        String current_day_used_sql = current_year + "-" + current_month + pDay;

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

    // これがないとGestureDetectorが動かない
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }
}