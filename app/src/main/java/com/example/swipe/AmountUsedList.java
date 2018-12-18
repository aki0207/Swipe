package com.example.swipe;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class AmountUsedList extends Abstract {

    //日付
    String year = "";
    String month = "";
    String day = "";
    String current_day;

    //DB関連
    Cursor c;
    SQLiteDatabase db;
    DbOpenHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amount_used_list);

        //intentから現在の日付取得
        getCurrentDay();
        //ヘッダーに日付セット
        TextView header = (TextView) findViewById(R.id.current_day);
        header.setText(year + "年" + month + "月" + day + "日");
        //0埋め
        month = zeroPadding(month);
        day = zeroPadding(day);
        current_day = year + "-" + month + day;
        //db情報読取
        readData(current_day);


        //戻るボタンリスナー
        Button back_button = (Button) findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AmountUsedList.this, MainActivity.class);
                intent = setCurrentDay(intent, year, month, day);
                startActivity(intent);

            }
        });

        //編集画面へ
        Button go_edit_button = (Button) findViewById(R.id.go_edit_page);
        go_edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AmountUsedList.this, EditActivity.class);
                intent = setCurrentDay(intent, String.valueOf(year), String.valueOf(month), String.valueOf(day));
                startActivity(intent);

            }
        });

        //クリアボタン
        Button clear_button = (Button) findViewById(R.id.clear_button);
        clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clearData(current_day);
                Intent intent = new Intent(AmountUsedList.this, AmountUsedList.class);
                intent = setCurrentDay(intent, String.valueOf(year), String.valueOf(month), String.valueOf(day));
                startActivity(intent);

            }
        });

    }

    //dbの情報を検索していく
    public void readData(String pCurrentDay) {

        if (helper == null) {
            helper = new DbOpenHelper(this);
        }
        if (db == null) {
            db = helper.getReadableDatabase();
        }

        //総額
        TextView the_amount = (TextView) findViewById(R.id.the_amount);
        String sql = "select sum(price) from amount_used where date = ?";
        c = db.rawQuery(sql, new String[]{pCurrentDay});
        boolean isEof = c.moveToFirst();
        while (isEof) {

            the_amount.setText(String.format("%d", c.getInt(0)) + "円");
            isEof = c.moveToNext();
        }

        //検索結果入れ物
        ArrayList<ArrayList<String>> w_results = new ArrayList<ArrayList<String>>();

        //とってきたいのは項目名と金額
        sql = "select category,price from amount_used where date = ? order by category";
        c = db.rawQuery(sql, new String[]{pCurrentDay});
        isEof = c.moveToFirst();

        while (isEof) {

            //w_resultsに突っ込む用
            ArrayList<String> w_result = new ArrayList<>();
            w_result.add(c.getString(0));
            w_result.add(String.valueOf(c.getInt(1)));
            w_results.add(w_result);
            isEof = c.moveToNext();

        }

        //項目、金額だけやから2固定
        String[][] array = new String[w_results.size()][2];

        // TableLayoutのグループを取得
        ViewGroup w_view_group = (ViewGroup) findViewById(R.id.search_table);

        // Header部追加
        String[] w_header_array = "項目,金額".split(",");
        //行追加
        getLayoutInflater().inflate(R.layout.layout_table, w_view_group);
        TableRow w_table_row = (TableRow) w_view_group.getChildAt(0);


        // 配列分ループ(header部)
        for (int i = 0; i < w_header_array.length; i++) {
            ((TextView) (w_table_row.getChildAt(i))).setText(w_header_array[i]);
            ((TextView) (w_table_row.getChildAt(i))).setTypeface(Typeface.DEFAULT_BOLD);
        }

        // 背景色変更
        w_table_row.setBackgroundColor(Color.parseColor("#72CFF7"));


        for (int i = 0; i < w_results.size(); i++) {

            //行を追加
            getLayoutInflater().inflate(R.layout.layout_table, w_view_group);
            // 文字設定
            w_table_row = (TableRow) w_view_group.getChildAt(i + 1);

            for (int j = 0; j < w_results.get(i).size(); j++) {
                ((TextView) (w_table_row.getChildAt(j))).setText(w_results.get(i).get(j));
            }

        }

    }

    //deleteかまして初期化する
    public void clearData(String pCurrentDay) {

        try {

            if (helper == null) {
                helper = new DbOpenHelper(this);
            }
            if (db == null) {
                db = helper.getReadableDatabase();
            }


            String sql = "delete from amount_used where date = ?";
            c = db.rawQuery(sql, new String[]{pCurrentDay});
            // 以下の文を実行しないとRecordは削除できない！
            c.moveToFirst();
            Context context = getApplicationContext();
            Toast.makeText(context, "クリアしました", Toast.LENGTH_SHORT).show();


        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }

        }

    }

    public Intent setCurrentDay(Intent intent, String pYear, String pMonth, String pDay) {

        intent.putExtra("YEAR", year);
        intent.putExtra("MONTH", month);
        intent.putExtra("DAY", day);

        return intent;

    }

    public void getCurrentDay() {

        year = getIntent().getStringExtra("YEAR");
        month = getIntent().getStringExtra("MONTH");
        day = getIntent().getStringExtra("DAY");
    }

    //1ケタなら0埋めかます
    public String zeroPadding(String pValue) {

        if (pValue.length() == 1) {
            pValue = String.format("%02d", Integer.parseInt(pValue));
        }
        return pValue;
    }


}





