package com.example.swipe;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

public class Abstract extends AppCompatActivity  {

    public Intent setCurrentDay (Intent intent,String pYear, String pMonth, String pDay) {

        intent.putExtra("YEAR",pYear);
        intent.putExtra("MONTH",pMonth);
        intent.putExtra("DAY",pDay);

        return intent;

    }



}

