package com.seg3125.noteapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.seg3125.noteapp.databinding.NoteItemBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.requery.Persistable;
import io.requery.android.QueryRecyclerAdapter;
import io.requery.query.Result;
import io.requery.reactivex.ReactiveEntityStore;

/**
 * Activity displaying a list of notes. Notes can be tapped on to edit them.
 * Utilizes a {@link RecyclerView}, {@link QueryRecyclerAdapter}, and RxJava to display the data in
 * non-blocking, bound manner.
 */
public class MainActivity extends AppCompatActivity {

    private ReactiveEntityStore<Persistable> data;
    private ExecutorService executor;
    private NoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enable permissions for writing
        isStoragePermissionGranted();

        // Load the root view used by the activity.
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        // Load the data store instance from the global `Application` context.
        data = ((NoteApplication) getApplication()).getData();

        // Create a new concurrent executor, which is necessary for RxJava's async callback support.
        executor = Executors.newSingleThreadExecutor();

        // Initialize a new Note adapter and attach it to the executor.
        adapter = new NoteAdapter();
        adapter.setExecutor(executor);

        // Attach the adapter to the recycler view, and configure the layout manager for the view.
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        data.count(Note.class).get().single()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer integer) {
                        if (integer == 0) {
                            Observable.fromCallable(new CreateNotes(data))
                                    .flatMap(new Function<Observable<Iterable<Note>>, Observable<?>>() {
                                        @Override
                                        public Observable<?> apply(Observable<Iterable<Note>> o) {
                                            return o;
                                        }
                                    })
                                    .observeOn(Schedulers.computation())
                                    .subscribe(new Consumer<Object>() {
                                        @Override
                                        public void accept(Object o) {
                                            adapter.queryAsync();
                                        }
                                    });
                        }
                    }
                });
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
                return true;
            default:
                break;
        }

        return false;
    }

    /**
     * Called when the user taps the "Add Note" button
     */
    public void addNote() {
        Intent intent = new Intent(this, EditNoteActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        adapter.queryAsync();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        adapter.close();
        super.onDestroy();
    }

    /**
     * Adapter used to display {@link Note} items from an {@link ReactiveEntityStore} query.
     */
    private class NoteAdapter extends QueryRecyclerAdapter<NoteEntity,
            BindingHolder<NoteItemBinding>> implements View.OnClickListener {

        private static final String TAG = "NoteAdapter";

        NoteAdapter() {
            super(NoteEntity.$TYPE);
        }

        @Override
        public Result<NoteEntity> performQuery() {
            // This is every note in the database, sorted by their title.
            // NOTE: this method is executed in a background thread. This could instead be done via
            // RxJava with RxBinding.
            // FIXME: change this to use RxJava + RxBinding instead.
            return data.select(NoteEntity.class).orderBy(NoteEntity.TITLE.lower()).get();
        }

        @Override
        public void onBindViewHolder(NoteEntity item, BindingHolder<NoteItemBinding> holder,
                                     int position) {
            Log.v(TAG, "Binding note item to view holder");
            holder.binding.setNote(item);

            // TODO: determine if this is necessary.
            //holder.binding.executePendingBindings();
        }

        @Override
        public BindingHolder<NoteItemBinding> onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.v(TAG, "Creating a note view holder");
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            NoteItemBinding binding = NoteItemBinding.inflate(inflater);
            binding.getRoot().setTag(binding);
            binding.getRoot().setOnClickListener(this);
            return new BindingHolder<>(binding);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "Handling note view handler click");
            NoteItemBinding binding = (NoteItemBinding) v.getTag();
            if (binding != null) {
                Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
                intent.putExtra(EditNoteActivity.EXTRA_NOTE_ID, binding.getNote().getId());
                startActivity(intent);
            }
        }
    }

    // Used for bluetooth sharing
    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }
}
