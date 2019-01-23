package com.example.swipe;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;

public class Abstract extends AppCompatActivity  {

    SQLiteDatabase db;
    DbOpenHelper helper;
    Cursor c;


    String year = "";
    String month = "";
    String day = "";
    String current_day = "";

    public Intent setCurrentDay (Intent intent,String pYear, String pMonth, String pDay) {

        intent.putExtra("YEAR",pYear);
        intent.putExtra("MONTH",pMonth);
        intent.putExtra("DAY",pDay);

        return intent;

    }

    //1ケタなら0埋めかます
    public String zeroPadding(String pValue) {

        if (pValue.length() == 1) {
            pValue = String.format("%02d", Integer.parseInt(pValue));
        }
        return pValue;
    }

    //intentから値を取得
    public void getCurrentDay() {
        year = getIntent().getStringExtra("YEAR");
        month = getIntent().getStringExtra("MONTH");
        day = getIntent().getStringExtra("DAY");
    }






}

