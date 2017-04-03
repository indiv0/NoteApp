package com.seg3125.noteapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.databinding.DataBindingUtil;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.seg3125.noteapp.databinding.ActivityEditNoteBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.requery.Persistable;
import io.requery.reactivex.ReactiveEntityStore;

public class EditNoteActivity extends AppCompatActivity {

    // Bluetooth Sharing
    private static final int DISCOVER_DURATION = 300;
    private static final int REQUEST_BLU = 1;

    // The key used for the (optional) Note ID provided in the `Intent`.
    static final String EXTRA_NOTE_ID = "noteId";

    private ReactiveEntityStore<Persistable> data;
    private NoteEntity note;
    private ActivityEditNoteBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        // Initialize the data binding for this activity.
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_note);

        // Get a support ActionBar.
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;

        // Enable the "Up" button to return to the previous activity.
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Set the title of the action bar.
        // TODO: do this from a localization string (ideally in the toolbar's XML).
        // TODO: make this switch between "Edit Note" and "Save Note" as necessary.
        actionBar.setTitle("Edit Note");

        // Retrieve the data store from the global `Application` context.
        data = ((NoteApplication) getApplication()).getData();

        // Load the note ID (if specified) from the `Intent` for this activity.
        int noteId = getIntent().getIntExtra(EXTRA_NOTE_ID, -1);

        // If a note ID was specified, edit that node; otherwise, create a new, blank note.
        if (noteId == -1) {
            note = new NoteEntity();
            binding.setNote(note);
        } else {
            data.findByKey(NoteEntity.class, noteId)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<NoteEntity>() {
                        @Override
                        public void accept(@NonNull NoteEntity note) {
                            EditNoteActivity.this.note = note;
                            binding.setNote(note);
                        }
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu, creating the necessary actions.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_note_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Back arrow was selected
            case android.R.id.home:
                finish();
                break;
            // Action with ID 'action_save_note' was selected
            case R.id.action_save_note:
                saveNote();
                break;
            // Action with ID 'action_lock_note' was selected
            case R.id.action_lock_note:
                lockNote();
                break;
            // Action with ID 'action_delete_note' was selected
            case R.id.action_delete_note:
                deleteNote();
                break;
            // Action with ID 'action_share_note' was selected from overflow menu
            case R.id.action_share_note:
                shareNote();
                break;
            default:
                break;
        }

        return true;
    }

    /**
     * Called when the user taps the "Save Note" button.
     */
    public void saveNote() {
        // TODO: make the binding 2-way.
        // Copy the text box contents to the in-memory representation of the `Note`.
        note.setTitle(binding.titleEditText.getText().toString());
        note.setContent(binding.contentEditText.getText().toString());

        // Save the note.
        if (note.getId() == 0) {
            data.insert(note).subscribe(new Consumer<NoteEntity>() {
                @Override
                public void accept(@NonNull NoteEntity noteEntity) {
                    finish();
                }
            });
        } else {
            data.update(note).subscribe(new Consumer<NoteEntity>() {
                @Override
                public void accept(@NonNull NoteEntity noteEntity) {
                    finish();
                }
            });
        }
    }

    public void lockNote() {

    }

    public void deleteNote() {
        data.delete(note).subscribe();
        finish();
    }

    public void shareNote() {
        sendViaBluetooth(null);
    }

    public void sendViaBluetooth(View v) {

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        if(btAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show();
        } else {
            enableBluetooth();
        }
    }

    public void enableBluetooth() {

        Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION);
        startActivityForResult(discoveryIntent, REQUEST_BLU);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == DISCOVER_DURATION && requestCode == REQUEST_BLU) {

            String filename = note.getTitle() + ".txt";
            String string = note.getContent();

            // get the path to sdcard
            File sdcard = Environment.getExternalStorageDirectory();
            // to this path add a new directory path
            File dir = new File(sdcard.getAbsolutePath() + "/NoteApp/");
            // create this directory if not already created
            dir.mkdir();
            Toast.makeText(this, "Dir", Toast.LENGTH_LONG).show();
            // create the file in which we will write the contents
            FileOutputStream os;

            try {
                File file = new File(dir, filename);
                os = new FileOutputStream(file);
                os.write(string.getBytes());
                os.close();
                Toast.makeText(this, "Note", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Send the file
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            File f = new File(dir, filename);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));

            PackageManager pm = getPackageManager();
            List<ResolveInfo> appsList = pm.queryIntentActivities(intent, 0);

            if(appsList.size() > 0) {
                String packageName = null;
                String className = null;
                boolean found = false;

                for(ResolveInfo info : appsList) {
                    packageName = info.activityInfo.packageName;
                    if(packageName.equals("com.android.bluetooth")) {
                        className = info.activityInfo.name;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    Toast.makeText(this, "No Bluetooth devices found", Toast.LENGTH_LONG).show();
                } else {
                    intent.setClassName(packageName, className);
                    startActivity(intent);
                }
            }
        } else {
            Toast.makeText(this, "Bluetooth cancelled", Toast.LENGTH_LONG).show();
        }

    }
}
