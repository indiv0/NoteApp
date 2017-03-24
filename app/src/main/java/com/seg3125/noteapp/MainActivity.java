package com.seg3125.noteapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import net.danlew.android.joda.JodaTimeAndroid;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the Joda-Time Android library.
        JodaTimeAndroid.init(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu, creating the necessary actions.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Action with ID `action_add_note` was selected
            case R.id.action_add_note:
                addNote();
                break;
            default:
                break;
        }

        return true;
    }

    /**
     * Called when the user taps the "Add Note" button
     */
    public void addNote() {
        Intent intent = new Intent(this, EditNoteActivity.class);
        startActivity(intent);
    }
}
