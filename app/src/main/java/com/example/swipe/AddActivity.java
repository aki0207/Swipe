package com.example.swipe;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AddActivity extends Abstract {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);


        //intentから現在の日付取得
        getCurrentDay();

        //ヘッダーに日付挿入
        TextView header = (TextView) findViewById(R.id.current_day);
        header.setText(year + "年" + month + "月" + day + "日");

        //1ケタなら0埋めかます
        zeroPadding();

        //Sqlで使う
        current_day = year + "-" + month + day;
        helper = new DbOpenHelper(this);

        //戻るボタンのリスナー
        Button back_button = (Button) findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddActivity.this, AmountUsedList.class);
                setCurrentDay(intent, year, month, day);
                startActivity(intent);
            }
        });


        Button add_button = (Button) findViewById(R.id.edit_button);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (helper == null) {
                    helper = new DbOpenHelper(getApplicationContext());
                }

                if (db == null) {
                    db = helper.getWritableDatabase();
                }

                Spinner spinner = (Spinner) findViewById(R.id.spinner);
                // 選択されているカテゴリを取得
                String selected_category = (String) spinner.getSelectedItem();
                //カテゴリー詳細
                EditText category_detail = (EditText) findViewById(R.id.category_detail_form);
                String entered_category_detail = category_detail.getText().toString();
                //金額
                EditText price_form = (EditText) findViewById(R.id.price_form);
                String entered_price = price_form.getText().toString();

                //追加する
                insertData(db, current_day, selected_category, Integer.parseInt(entered_price), entered_category_detail);
            }
        });


    }

    //dbに値を登録する
    private void insertData(SQLiteDatabase pDb, String pDate, String pCategory, int pPrice, String pCategoryDetail) {

        ContentValues values = new ContentValues();
        values.put("date", pDate);
        values.put("category", pCategory);
        values.put("price", pPrice);
        values.put("category_detail",pCategoryDetail);

        db.insert("amount_used", null, values);
        Context context = getApplication();
        Toast.makeText(context, "追加しました", Toast.LENGTH_SHORT).show();
    }

    //intentから値を取得
    public void getCurrentDay() {
        year = getIntent().getStringExtra("YEAR");
        month = getIntent().getStringExtra("MONTH");
        day = getIntent().getStringExtra("DAY");
    }

    public void zeroPadding() {

        //1ケタなら0埋めかます
        if (month.length() == 1) {
            month = String.format("%02d", Integer.parseInt(month));
        }

        if (day.length() == 1) {
            day = String.format("%02d", Integer.parseInt(day));
        }
    }
}
