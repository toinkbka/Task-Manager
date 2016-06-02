package com.hust.activity;

import com.hust.activity.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class AboutActivity extends Activity{

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.option);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AboutActivity.this, AllListAppProcess.class);
        startActivity(intent);
        finish();
    }
}