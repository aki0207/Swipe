package com.example.swipe;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditActivity extends Abstract {

    //更新元の情報を取得
    String target_category_detail = "";
    int target_price = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        //intentから現在の日付取得
        getCurrentDay();

        //ヘッダーに日付挿入
        TextView header = (TextView) findViewById(R.id.current_day);
        header.setText(year + "年" + month + "月" + day + "日");

        //1ケタなら0埋めかます
        zeroPadding();

        //更新元の情報を取得
        String evacute =  getIntent().getStringExtra("TARGETVALUE");
        int index = evacute.indexOf(",");
        target_category_detail = evacute.substring(0,index);
        target_price = Integer.parseInt(evacute.substring(index + 1,evacute.length()));

        //EditTextに更新前の値をセットしておく
        EditText category_detail_form = (EditText)findViewById(R.id.category_detail_form);
        category_detail_form.setText(target_category_detail);
        //カーソルをtextの一番後ろに
        category_detail_form.setSelection(category_detail_form.getText().length());

        EditText price_form = (EditText)findViewById(R.id.price_form);
        price_form.setHint(String.valueOf(target_price));


        //Sqlで使う
        current_day = year + "-" + month + day;
        helper = new DbOpenHelper(this);

        //戻るボタンのリスナー
        Button back_button = (Button) findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditActivity.this, AmountUsedList.class);
                setCurrentDay(intent, year, month, day);
                startActivity(intent);
            }
        });

        Button edit_button = (Button) findViewById(R.id.edit_button);
        edit_button.setOnClickListener(new View.OnClickListener() {
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


                ContentValues values = new ContentValues();
                values.put("category", selected_category);
                values.put("price", entered_price);
                values.put("category_detail",entered_category_detail);

                db.beginTransaction();
                db.update("amount_used", values, "category_detail = '" + target_category_detail +
                        "' and price = " + target_price + " and date = '" + current_day + "'", null);
                db.setTransactionSuccessful();
                db.endTransaction();
                Context context = getApplicationContext();
                Toast.makeText(context, "更新が完了しました", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(EditActivity.this,DetailActivity.class);
                intent = setCurrentDay(intent,year,month,day);
                //秘技intent返し
                intent.putExtra("SELECTEDCATEGORY",getIntent().getStringExtra("SELECTEDCATEGORY"));
                startActivity(intent);

            }
        });

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
