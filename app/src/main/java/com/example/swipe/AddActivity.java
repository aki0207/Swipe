package com.example.swipe;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AddActivity extends Abstract implements View.OnClickListener {

    EditText category_detail;
    EditText price_form;

    // 戻るボタン禁止
    @Override
    public void onBackPressed() {
    }

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
        month = zeroPadding(month);
        day = zeroPadding(day);

        //Sqlで使う
        current_day = year + "-" + month + day;
        helper = new DbOpenHelper(this);

        //ボタンのリスナー登録
        Button back_button = (Button) findViewById(R.id.back_button);
        back_button.setOnClickListener(this);
        Button add_button = (Button) findViewById(R.id.edit_button);
        add_button.setOnClickListener(this);

    }

    //入力値チェック
    public boolean checkInput(String pSelectedCategory, String pEnteredCategoryDetail, String pEnteredPrice) {

        boolean ret = true;
        category_detail.setError(null);
        price_form.setError(null);

        if (pSelectedCategory == null || pSelectedCategory.length() == 0) {
            ret = false;
        } else if (pEnteredCategoryDetail == null || pEnteredCategoryDetail.length() == 0) {
            category_detail.setError(getString(R.string.category_detail_not_yet_entered_error_message));
            ret = false;
        } else if (pEnteredCategoryDetail.length() > 15) {
            category_detail.setError(getString(R.string.category_detail_over_enterd_text_length_error_message));
            ret = false;
        } else if (pEnteredPrice == null || pEnteredPrice.length() == 0) {
            price_form.setError(getString(R.string.price_form_not_yet_entered_error_message));
            ret = false;
        } else if (pEnteredPrice.length() > 8) {
            price_form.setError(getString(R.string.price_form_over_enterd_text_length_error_message));
            ret = false;
        } else if (!isNumber(pEnteredPrice)) {
            price_form.setError(getString(R.string.price_form_text_type_error_message));
            ret = false;
        }

        return ret;
    }


    public boolean isNumber(String pNum) {

        int num = 0;
        boolean ret = true;
        try {
            num = Integer.parseInt(pNum);
        } catch (NumberFormatException e) {
            ret = false;
        } finally {
            return ret;
        }

    }

    //dbに値を登録する
    private void insertData(SQLiteDatabase pDb, String pDate, String pCategory, int pPrice, String pCategoryDetail) {

        ContentValues values = new ContentValues();
        values.put("date", pDate);
        values.put("category", pCategory);
        values.put("price", pPrice);
        values.put("category_detail", pCategoryDetail);

        db.insert("amount_used", null, values);
        Context context = getApplication();
        Toast.makeText(context, "追加しました", Toast.LENGTH_SHORT).show();
    }





    @Override
    public void onClick(View v) {

        if (v == findViewById(R.id.back_button)) {

            Intent intent = new Intent(AddActivity.this, AmountUsedList.class);
            setCurrentDay(intent, year, month, day);
            startActivity(intent);

        } else {

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
            category_detail = (EditText) findViewById(R.id.category_detail_form);
            String entered_category_detail = category_detail.getText().toString();
            //金額
            price_form = (EditText) findViewById(R.id.price_form);
            String entered_price = price_form.getText().toString();
            //入力値チェック
            if (checkInput(selected_category, entered_category_detail, entered_price)) {
                //追加する
                insertData(db, current_day, selected_category, Integer.parseInt(entered_price), entered_category_detail);
            }

        }

    }
}
