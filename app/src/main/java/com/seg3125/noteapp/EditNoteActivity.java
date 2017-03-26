package com.seg3125.noteapp;

import android.databinding.DataBindingUtil;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.seg3125.noteapp.databinding.ActivityEditNoteBinding;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.requery.Persistable;
import io.requery.reactivex.ReactiveEntityStore;

public class EditNoteActivity extends AppCompatActivity {

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
            // Action with ID `action_save_note` was selected
            case R.id.action_save_note:
                saveNote();
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
}
