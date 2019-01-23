package com.example.swipe;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditActivity extends Abstract implements View.OnClickListener {

    //更新元の情報を取得
    String target_category_detail = "";
    int target_price = 0;
    EditText category_detail;
    EditText price_form;

    // 戻るボタン禁止
    @Override
    public void onBackPressed() {
    }

    
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
        month = zeroPadding(month);
        day = zeroPadding(day);

        //更新元の情報を取得
        String evacute =  getIntent().getStringExtra("TARGETVALUE");
        int index = evacute.indexOf(",");
        target_category_detail = evacute.substring(0,index);
        target_price = Integer.parseInt(evacute.substring(index + 1,evacute.length()));

        //EditTextに更新前の値をセットしておく
        category_detail = (EditText)findViewById(R.id.category_detail_form);
        category_detail.setText(target_category_detail);
        //カーソルをtextの一番後ろに
        category_detail.setSelection(category_detail.getText().length());

        price_form = (EditText)findViewById(R.id.price_form);
        price_form.setHint(String.valueOf(target_price));


        //Sqlで使う
        current_day = year + "-" + month + day;
        helper = new DbOpenHelper(this);

        //ボタンのリスナー
        Button back_button = (Button) findViewById(R.id.back_button);
        back_button.setOnClickListener(this);
        Button edit_button = (Button) findViewById(R.id.edit_button);
        edit_button.setOnClickListener(this);

    }






    //入力値チェック
    public boolean checkInput (String pSelectedCategory, String pEnteredCategoryDetail, String pEnteredPrice ) {

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

    public boolean isNumber (String pNum) {

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

    @Override
    public void onClick(View v) {

        if (v == findViewById(R.id.back_button)) {

            Intent intent = new Intent(EditActivity.this, AmountUsedList.class);
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
    }


}
