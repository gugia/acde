package com.unitslink.acde;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;

public class PanelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);
        initUI();
    }

    private void initUI() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        DigitalTextView tvDate = findViewById(R.id.tv_date);
        tvDate.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/lgdr.ttf"));
        //tvDate.setTextSize();
    }
}
