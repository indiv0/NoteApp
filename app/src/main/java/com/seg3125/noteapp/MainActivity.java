package com.seg3125.noteapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import net.danlew.android.joda.JodaTimeAndroid;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the Joda-Time Android library.
        JodaTimeAndroid.init(this);
    }
}
