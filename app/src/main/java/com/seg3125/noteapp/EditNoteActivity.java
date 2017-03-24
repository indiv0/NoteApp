package com.seg3125.noteapp;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class EditNoteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        // Get a support ActionBar.
        ActionBar actionBar = getSupportActionBar();

        // Enable the "Up" button to return to the previous activity.
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}
