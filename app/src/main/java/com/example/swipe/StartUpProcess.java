package com.example.swipe;

import android.app.Application;

public class StartUpProcess extends Application {
    protected String globalData = "yaaaah";

    @Override
    public void onCreate() {
        //生成時


        super.onCreate();
    }



    public Object getGlobalData() {
        return this.globalData;
    }


}

