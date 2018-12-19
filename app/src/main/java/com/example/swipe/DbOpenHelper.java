package com.example.swipe;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbOpenHelper extends SQLiteOpenHelper {
    final static private int DB_VERSION = 1;
    SharedPreferences pref;
    boolean flg = false;

    public DbOpenHelper(Context context) {
        super(context, "Bank.db", null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // table create
        db.execSQL(
                "create table amount_used("+
                        "   date text not null," +
                        "   category text not null,"+
                        "   price text,"+
                        "   category_detail text not null" +
                        ");"
        );

        // table row insert
        db.execSQL("insert into amount_used(date,category,price,category_detail) values ('2018-1101','交遊費', 0,'盗んだバイク');");
        db.execSQL("insert into amount_used(date,category,price,category_detail) values ('2018-1101','生活費', 0,'おいしめの雑草');");
        db.execSQL("insert into amount_used(date,category,price,category_detail) values ('2018-1101','飲食費', 0,' ');");
        db.execSQL("insert into amount_used(date,category,price,category_detail) values ('2018-1101','買い物費', 0,' ');");
        db.execSQL("insert into amount_used(date,category,price,category_detail) values ('2018-1101','その他1', 0,' ');");
        db.execSQL("insert into amount_used(date,category,price,category_detail) values ('2018-1101','その他2', 0,' ');");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // データベースの変更が生じた場合は、ここに処理を記述する。
    }
}