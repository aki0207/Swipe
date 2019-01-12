package com.example.swipe;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class DetailActivity extends Abstract {

    //ボタンにsetTagする用
    Object tag = 0;
    Context context;
    String selected_category = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //intentから現在の日付取得
        getCurrentDay();
        //ヘッダーに日付セット
        TextView header = (TextView) findViewById(R.id.current_day);
        header.setText(year + "年" + month + "月" + day + "日");
        //0埋め
        month = zeroPadding(month);
        day = zeroPadding(day);
        current_day = year + "-" + month + day;
        selected_category = getIntent().getStringExtra("SELECTEDCATEGORY");
        //db情報読取
        readData(current_day, selected_category);

        //戻るボタンリスナー
        Button back_button = (Button) findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(DetailActivity.this, AmountUsedList.class);
                intent = setCurrentDay(intent, year, month, day);
                startActivity(intent);

            }
        });


    }


    public void readData(String pCurrentDay, String pCategory) {

        if (helper == null) {
            helper = new DbOpenHelper(this);
        }
        if (db == null) {
            db = helper.getReadableDatabase();
        }

        //当日のカテゴリーの総額
        TextView total_category = (TextView) findViewById(R.id.total_category);
        String sql = "select sum(price) from amount_used where date = ? and category = ?";
        c = db.rawQuery(sql, new String[]{pCurrentDay, pCategory});
        boolean isEof = c.moveToFirst();
        while (isEof) {
            total_category.setText(String.format("%d", c.getInt(0)) + "円");
            isEof = c.moveToNext();
        }

        //検索結果入れ物
        ArrayList<ArrayList<String>> w_results = new ArrayList<ArrayList<String>>();

        //とってきたいのは項目名と金額
        sql = "select category_detail,price from amount_used where date = ? and category = ?";
        c = db.rawQuery(sql, new String[]{pCurrentDay, pCategory});
        isEof = c.moveToFirst();

        while (isEof) {

            //w_resultsに突っ込む用
            ArrayList<String> w_result = new ArrayList<>();
            w_result.add(c.getString(0));
            w_result.add(String.valueOf(c.getInt(1)));
            w_results.add(w_result);
            isEof = c.moveToNext();

        }

        //項目、金額だけやから2固定
        String[][] array = new String[w_results.size()][2];

        // TableLayoutのグループを取得
        ViewGroup w_view_group = (ViewGroup) findViewById(R.id.search_table);

        // Header部追加
        String[] w_header_array = "項目,金額".split(",");
        //行追加
        getLayoutInflater().inflate(R.layout.layout_table, w_view_group);
        TableRow w_table_row = (TableRow) w_view_group.getChildAt(0);


        // 配列分ループ(header部)
        for (int i = 0; i < w_header_array.length; i++) {
            ((TextView) (w_table_row.getChildAt(i))).setText(w_header_array[i]);
            ((TextView) (w_table_row.getChildAt(i))).setTypeface(Typeface.DEFAULT_BOLD);
        }

        // 背景色変更
        w_table_row.setBackgroundColor(Color.parseColor("#72CFF7"));


        for (int i = 0; i < w_results.size(); i++) {

            //行を追加
            getLayoutInflater().inflate(R.layout.layout_table, w_view_group);
            // 文字設定
            w_table_row = (TableRow) w_view_group.getChildAt(i + 1);

            for (int j = 0; j < w_results.get(i).size(); j++) {

                if (j == 0) {

                    ((Button) (w_table_row.getChildAt(j))).setText(w_results.get(i).get(j));
                    //ボタンにカテゴリ,金額を持たせておく
                    tag = w_results.get(i).get(j) + "," + w_results.get(i).get(j + 1);
                    ((Button) (w_table_row.getChildAt(j))).setTag(tag);
                    ((Button) (w_table_row.getChildAt(j))).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {

                            // アラート表示
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(DetailActivity.this);


                            // ダイアログの設定
                            //削除
                            alertDialog.setTitle("項目を…?");
                            alertDialog.setMessage(" ");
                            alertDialog.setPositiveButton("削除する", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    //ボタンに持たせてた情報からsqlで削除実行
                                    String delete_target = String.valueOf(v.getTag());
                                    int index = delete_target.indexOf(",");
                                    String categor_detail = delete_target.substring(0,index);
                                    String price = delete_target.substring(index + 1,delete_target.length());
                                    db.delete("amount_used", "category_detail='" + categor_detail +
                                            "' and price = " + price + " and date = '" + current_day + "'" ,null);

                                    context = getApplication();
                                    Toast.makeText(context, "削除しました", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(DetailActivity.this,DetailActivity.class);
                                    intent = setCurrentDay(intent,year,month,day);
                                    intent.putExtra("SELECTEDCATEGORY",selected_category);
                                    startActivity(intent);

                                }
                            });



                            //追加
                            alertDialog.setNegativeButton("編集する", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(DetailActivity.this, EditActivity.class);
                                    intent = setCurrentDay(intent, String.valueOf(year), String.valueOf(month), String.valueOf(day));
                                    //ボタンに持たせてた情報を更新ページに渡す
                                    intent.putExtra("TARGETVALUE",String.valueOf(v.getTag()));
                                    intent.putExtra("SELECTEDCATEGORY",selected_category);
                                    startActivity(intent);
                                }
                            }).show();



                          /*  tag = v.getTag();
                            String selected_button = String.valueOf(tag);
                            Intent intent = new Intent(AmountUsedList.this,DetailActivity.class);
                            intent.putExtra("SELECTEDBUTTON",selected_button);
                            startActivity(intent);*/

                        }
                    });
                }

                ((Button) (w_table_row.getChildAt(j))).setText(w_results.get(i).get(j));

            }

        }

        Button go_add_page_button = (Button)findViewById(R.id.go_add_page);
        go_add_page_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(DetailActivity.this, AddActivity.class);
                intent = setCurrentDay(intent, year, month, day);
                startActivity(intent);

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


