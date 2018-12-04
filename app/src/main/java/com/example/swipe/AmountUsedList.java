package com.example.swipe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AmountUsedList extends Abstract {

    String year = "";
    String month = "";
    String day = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amount_used_list);

        year = getIntent().getStringExtra("YEAR");
        month = getIntent().getStringExtra("MONTH");
        day = getIntent().getStringExtra("DAY");



        //戻るボタンのリスナー
        Button back_button = (Button) findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AmountUsedList.this, MainActivity.class);
                intent = setCurrentDay(intent,year,month,day);
                startActivity(intent);

            }
        });

        //編集画面への遷移
        Button go_edit_button = (Button) findViewById(R.id.back_button);
        go_edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AmountUsedList.this, EditActivity.class);
                intent = setCurrentDay(intent,year,month,day);
                startActivity(intent);

            }
        });



    }
}
