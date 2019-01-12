package com.example.swipe;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SavingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        String[] tests = {"1", "2", "3", "4"};
        ArrayAdapter adapter =
                new ArrayAdapter(this, android.R.layout.simple_spinner_item, tests);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
    }
}
