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


    SQLiteDatabase db;
    DbOpenHelper helper;
    Cursor c;


    String year = "";
    String month = "";
    String day = "";
    String current_day = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amount_used_list);

        year = getIntent().getStringExtra("YEAR");
        month = getIntent().getStringExtra("MONTH");
        day = getIntent().getStringExtra("DAY");

        //ヘッダーに日付挿入
        TextView header = (TextView) findViewById(R.id.current_day);
        header.setText(year + "年" + month + "月" + day + "日");


        //1ケタなら0埋めかます
        if (month.length() == 1) {
            month = String.format("%02d", month);
        }

        if (day.length() == 1) {
            day = String.format("%02d", day);
        }

        //Sqlで使う
        current_day = year + "-" + month + day;

        helper = new DbOpenHelper(this);

        //戻るボタンのリスナー
        Button back_button = (Button) findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(EditActivity.this, MainActivity.class);
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
                //金額
                EditText price_form = (EditText) findViewById(R.id.price_form);
                String entered_price = price_form.getText().toString();


                //更新する前に検索する
                String sql = "select price from amount_used where date = ? and category = ?";
                String[] array = {current_day, selected_category};
                c = db.rawQuery(sql, array);


                //検索結果がなければ追加する
                if (c.getCount() == 0) {

                    insertData(db, current_day, selected_category, Integer.parseInt(entered_price));


                    //結果があるなら対象を更新
                } else {


            /*        boolean isEof = c.moveToFirst();
                    while (isEof) {
                        TextView tv = (TextView) findViewById(R.id.the_amount);
                        tv.setText(String.format("%d", c.getInt(0)));
                        isEof = c.moveToNext();
                    }*/

                    ContentValues values = new ContentValues();
                    values.put("category", selected_category);
                    values.put("price", entered_price);

                    db.beginTransaction();
                    db.update("amount_used", values, "category = '" + selected_category + "'", null);
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    Context context = getApplicationContext();
                    Toast.makeText(context, "更新が完了しました", Toast.LENGTH_SHORT).show();
                }


            }
        });

    }

    private void insertData(SQLiteDatabase db, String date, String category, int price) {

        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("category", category);
        values.put("price", price);

        db.insert("amount_used", null, values);
        Context context = getApplication();
        Toast.makeText(context, "更新対象がなかったんでとりま新しく作りました", Toast.LENGTH_SHORT).show();
    }

}
