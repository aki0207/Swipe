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
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class SavingsAmountConfActivity extends Abstract {

    Calendar cal;
    Calendar target_cal;
    int current_year;
    String selected_start_month;
    String selected_end_month;
    int count = 1;

    Spinner start_month_spinner;
    Spinner start_day_spinner;
    Spinner end_month_spinner;
    Spinner end_day_spinner;

    EditText amount;
    TextView error_message_for_start_day;
    TextView error_message_for_end_day;


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
                String testStartDay = data.getString("startDay", "");
                String testEndDay = data.getString("endDay", "");
                String testAmount = data.getString("savingsAmount", "");
                boolean flg = data.getBoolean("flg", false);
                String flg_result;
                if (flg) {
                    flg_result = "true";
                } else {
                    flg_result = "false";
                }

                Context context = getApplicationContext();
                Toast.makeText(context, testStartDay + testEndDay + testAmount + flg_result, Toast.LENGTH_LONG).show();


            }
        });

        //決定ボタンリスナー
        Button ok_button = (Button) findViewById(R.id.ok_button);
        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*String start_day = (String) start_month_spinner.getSelectedItem() +
                        (String) start_day_spinner.getSelectedItem();*/
                //開始月
                String start_month = (String) start_month_spinner.getSelectedItem();
                //開始日
                String start_day = (String) start_day_spinner.getSelectedItem();
                //終了月
                String end_month = (String) end_month_spinner.getSelectedItem();
                //終了日
                String end_day = (String) end_day_spinner.getSelectedItem();

                //0埋め
                start_month = zeroPadding(start_month);
                start_day = zeroPadding(start_day);
                end_month = zeroPadding(end_month);
                end_day = zeroPadding(end_day);

                //目標貯金額
                amount = (EditText) findViewById(R.id.amount);
                String savings_amount = amount.getText().toString();

                if (checkInput(start_month, start_day, end_month, end_day, savings_amount)) {


                    //ファイルに書き込む
                    SharedPreferences prefs = getSharedPreferences("DataSave", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();


                    editor.putBoolean("flg", true);
                    editor.putString("startDay", start_month + start_day);
                    editor.putString("endDay", end_month + end_day);
                    editor.putString("savingsAmount", savings_amount);
                    editor.apply();


                    Context context = getApplicationContext();
                    Toast.makeText(context, "承りました", Toast.LENGTH_SHORT).show();
                }


            }
        });

    }

    //intentから値を取得
    public void getCurrentDay() {

        year = getIntent().getStringExtra("YEAR");
        month = getIntent().getStringExtra("MONTH");
        day = getIntent().getStringExtra("DAY");
    }

    //入力値チェック
    public boolean checkInput(String pStartMonth, String pStartDay, String pEndMonth, String pEndDay, String pAmount) {

        amount.setError(null);
        error_message_for_start_day = (TextView)findViewById(R.id.error_messsage_for_start_day);
        error_message_for_end_day = (TextView)findViewById(R.id.error_messsage_for_end_day);
        boolean ret = true;

        //起こり得ないが一応
        if (pStartMonth == null || pStartMonth.length() == 0) {
            ret = false;
        } else if (pStartDay == null || pStartDay.length() == 0) {
            ret = false;
        } else if (pEndMonth == null || pEndMonth.length() == 0) {
            ret = false;
        } else if (pEndDay == null || pEndDay.length() == 0) {
            ret = false;
        } else if (pAmount == null || pAmount.length() == 0) {
            amount.setError(getString(R.string.price_form_not_yet_entered_error_message));
            ret = false;
        } else if (pAmount.length() > 8) {
            amount.setError(getString(R.string.price_form_over_enterd_text_length_error_message));
            ret = false;
        } else if (!isNumber(pAmount)) {
            amount.setError(getString(R.string.price_form_text_type_error_message));
            ret = false;
        }

        //開始日に過去が選択されていないか確認
        cal = Calendar.getInstance();
        target_cal = Calendar.getInstance();

        //0埋め解除
        if (pStartMonth.substring(0,1).equals("0")) {
            pStartMonth = pStartMonth.substring(1,2);
        }

        if (pStartDay.substring(0,1).equals("0")) {
            pStartDay = pStartDay.substring(1,2);
        }

        int current_start_month = Integer.parseInt(pStartMonth);
        int current_start_day = Integer.parseInt(pStartDay);
        //比較日を持ったインスタンス完成
        target_cal.set(Calendar.MONTH,current_start_month - 1);
        target_cal.set(Calendar.DATE,current_start_day);
        //日数の差分計算
        int result = getDiffDays(target_cal,cal);

        if (result < 0) {
            error_message_for_start_day.setText(getString(R.string.start_day_over_date_error_message));
            ret = false;
        }

        //終了日が開始日180日を超えていないか
        //0埋め解除
        if (pEndMonth.substring(0,1).equals("0")) {
            pEndMonth = pEndMonth.substring(1,2);
        }

        if (pEndDay.substring(0,1).equals("0")) {
            pEndDay = pEndDay.substring(1,2);
        }


        int current_end_month = Integer.parseInt(pEndMonth);
        int current_end_day = Integer.parseInt(pEndDay);

        cal.set(Calendar.MONTH,current_end_month - 1);
        cal.set(Calendar.DATE,current_end_day);

        result = getDiffDays(cal,target_cal);

        if (result > 180) {
            error_message_for_end_day.setText(getString(R.string.end_day_over_date_error_message));
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

    public int getDiffDays(Calendar calendar1, Calendar calendar2) {
        //==== ミリ秒単位での差分算出 ====//
        long diffTime = calendar1.getTimeInMillis() - calendar2.getTimeInMillis();

        //==== 日単位に変換 ====//
        int MILLIS_OF_DAY = 1000 * 60 * 60 * 24;
        int diffDays = (int)(diffTime / MILLIS_OF_DAY);

        return diffDays;
    }
}