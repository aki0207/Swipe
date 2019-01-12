package com.example.swipe;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Calendar;

public class SavingsAmountConfActivity extends Abstract {

    Calendar cal;
    int current_year;
    String selected_start_month;
    String selected_end_month;
    int count = 1;

    Spinner start_month_spinner;
    Spinner start_day_spinner;
    Spinner end_month_spinner;
    Spinner end_day_spinner;



    ArrayAdapter start_month_adapter;
    ArrayAdapter start_day_adapter;
    ArrayAdapter end_month_adapter;
    ArrayAdapter end_day_adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings_amount_conf);

        cal = Calendar.getInstance();


        //1年is12ヶ月
        String[] start_month_list = new String[12];
        for (int i = 0; i < 12; i++) {
            start_month_list[i] = String.valueOf(i + 1);
        }

        //開始月のセレクトボックス
       start_month_spinner = (Spinner) findViewById(R.id.start_month_spinner);
       start_month_adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, start_month_list);

        //monthのスピナーのリスナー
        start_month_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        start_month_spinner.setAdapter(start_month_adapter);
        start_month_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (count != 1) {
                    selected_start_month = (String) parent.getSelectedItem();
                    cal.set(Calendar.MONTH, Integer.parseInt(selected_start_month) - 1);
                    int max_day = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                    //選択されている月に応じて選択できる日数を変更(2月→28日)
                    String[] month_list = new String[max_day];
                    for (int i = 0; i < max_day; i++) {
                        month_list[i] = String.valueOf(i + 1);
                    }

                    start_day_spinner = (Spinner) findViewById(R.id.start_day_spinner);
                    Context context = getApplicationContext();
                    start_day_adapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, month_list);

                    start_day_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    start_day_spinner.setAdapter(start_day_adapter);


                } else {
                    count += 1;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //なんもなし
            }
        });


        selected_start_month = (String) start_month_spinner.getSelectedItem();
        current_year = cal.get(Calendar.YEAR);
        int current_month = cal.get(Calendar.MONTH);
        cal.set(Calendar.YEAR, current_year);


        if (count == 1) {
            cal.set(Calendar.MONTH, current_month);
        } else {
            cal.set(Calendar.MONTH, Integer.parseInt(selected_start_month));
        }

        int max_day = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        start_month_list = new String[max_day];
        for (int i = 0; i < max_day; i++) {
            start_month_list[i] = String.valueOf(i + 1);
        }


        start_day_spinner = (Spinner) findViewById(R.id.start_day_spinner);
        start_day_adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, start_month_list);
        start_day_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        start_day_spinner.setAdapter(start_day_adapter);



        //1年is12ヶ月
        final String[] end_month_list = new String[12];
        for (int i = 0; i < 12; i++) {
            end_month_list[i] = String.valueOf(i + 1);
        }
        //終了月のセレクトボックス
        end_month_spinner = (Spinner) findViewById(R.id.end_month_spinner);
        end_month_adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, end_month_list);

        //monthのスピナーのリスナー
        end_month_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        end_month_spinner.setAdapter(end_month_adapter);

        end_month_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (count != 1) {
                    selected_end_month = (String) parent.getSelectedItem();
                    cal.set(Calendar.MONTH, Integer.parseInt(selected_end_month) - 1);
                    int max_day = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                    //選択されている月に応じて選択できる日数を変更(2月→28日)
                    String[] end_month_list = new String[max_day];
                    for (int i = 0; i < max_day; i++) {
                        end_month_list[i] = String.valueOf(i + 1);
                    }

                    end_day_spinner = (Spinner) findViewById(R.id.end_day_spinner);
                    Context context = getApplicationContext();
                    end_day_adapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, end_month_list);

                    end_day_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    end_day_spinner.setAdapter(end_day_adapter);


                } else {
                    count += 1;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //なんもなし
            }
        });






        //戻るボタンリスナー
        Button back_button = (Button) findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent intent = new Intent(SavingsAmountConfActivity.this, MainActivity.class);
                getCurrentDay();
                setCurrentDay(intent, year, month, day);
                startActivity(intent);*/


                SharedPreferences data = getSharedPreferences("DataSave", Context.MODE_PRIVATE);
                String testStartDay = data.getString("startDay","" );
                String testEndDay = data.getString("endDay","");
                String testAmount = data.getString("savingsAmount","");

                Context context = getApplicationContext();
                Toast.makeText(context , testStartDay + testEndDay + testAmount, Toast.LENGTH_LONG).show();


            }
        });

        //決定ボタンリスナー
        Button ok_button = (Button) findViewById(R.id.ok_button);
        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //開始日
                String start_day = (String) start_month_spinner.getSelectedItem() +
                        (String) start_day_spinner.getSelectedItem();

                //終了日
                String end_day = (String) end_month_spinner.getSelectedItem() +
                        (String)end_day_spinner.getSelectedItem();

                //目標貯金額
                EditText amount = (EditText)findViewById(R.id.amount);
                String savings_amount = amount.getText().toString();

                //ファイルに書き込む
                SharedPreferences prefs = getSharedPreferences("DataSave", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                editor.putBoolean("flg",true);
                editor.putString("startDay", start_day);
                editor.putString("endDay", end_day);
                editor.putString("savingsAmount",savings_amount);
                editor.apply();


                Context context = getApplicationContext();
                Toast.makeText(context , "承りました", Toast.LENGTH_SHORT).show();


            }
        });

    }

    //intentから値を取得
    public void getCurrentDay() {

        year = getIntent().getStringExtra("YEAR");
        month = getIntent().getStringExtra("MONTH");
        day = getIntent().getStringExtra("DAY");
    }

}