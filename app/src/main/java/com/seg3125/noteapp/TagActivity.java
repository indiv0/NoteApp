package com.seg3125.noteapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.seg3125.noteapp.databinding.TagItemBinding;

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
public class TagActivity extends AppCompatActivity {

    private ReactiveEntityStore<Persistable> data;
    private ExecutorService executor;
    private TagAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        // Load the root view used by the activity.
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.tagRecyclerView);

        // This is an optimization to improve performance, as we know that the changes in content do
        // not change the layout size of the recycler view.
        recyclerView.setHasFixedSize(true);

        // Load the data store instance from the global `Application` context.
        data = ((NoteApplication) getApplication()).getData();

        // Create a new concurrent executor, which is necessary for RxJava's async callback support.
        executor = Executors.newSingleThreadExecutor();

        // Initialize a new `Tag` adapter and attach it to the executor.
        adapter = new TagAdapter();
        adapter.setExecutor(executor);

        // Attach the adapter to the recycler view, and configure the layout manager for the view.
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Attach a `DividerItemDecoration` to the recycler view, to provide a divider between
        // items.
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Retrieves a count of the tags available, and if no tags are available, `CreateTags` is
        // used to create a set of default example tags.
        // TODO: this should only be done the first time the app is launched.
        data.count(Tag.class).get().single()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer integer) {
                        if (integer == 0) {
                            Observable.fromCallable(new CreateTags(data))
                                    .flatMap(new Function<Observable<Iterable<Tag>>, Observable<?>>() {
                                        @Override
                                        public Observable<?> apply(Observable<Iterable<Tag>> o) {
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
        inflater.inflate(R.menu.activity_tag_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Back arrow was selected
            case android.R.id.home:
                finish();
                break;
            // Action with ID `action_add_tag` was selected
            case R.id.action_add_tag:
                addTag();
                return true;
            default:
                break;
        }

        return false;
    }

    /**
     * Called when the user taps the "Add Tag" button
     */
    public void addTag() {
        // FIXME: uncomment this once `EditTagActivity` is implemented.
        /*
        Intent intent = new Intent(this, EditTagActivity.class);
        startActivity(intent);
        */
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
     * Adapter used to display {@link Tag} items from a {@link ReactiveEntityStore} query.
     */
    private class TagAdapter extends QueryRecyclerAdapter<TagEntity,
            BindingHolder<TagItemBinding>> implements View.OnClickListener {

        private static final String LOGGING_TAG = "TagAdapter";

        TagAdapter() {
            super(TagEntity.$TYPE);
        }

        @Override
        public Result<TagEntity> performQuery() {
            // This is every tag in the database, sorted by their name.
            // NOTE: this method is executed in a background thread. This could instead be done via
            // RxJava with RxBinding.
            // FIXME: change this to use RxJava + RxBinding instead.
            return data.select(TagEntity.class).orderBy(TagEntity.NAME.lower()).get();
        }

        /**
         * Replaces the contents of a view (invoked by the layout manager).
         * @param item the new item to be placed into the view.
         * @param holder the holder of the binding for the view.
         * @param position the position of the item in the `RecyclerView`.
         */
        @Override
        public void onBindViewHolder(TagEntity item, BindingHolder<TagItemBinding> holder,
                                     int position) {
            Log.v(LOGGING_TAG, "Binding tag item to view holder");
            holder.binding.setTag(item);

            // TODO: determine if this is necessary.
            //holder.binding.executePendingBindings();
        }

        @Override
        public BindingHolder<TagItemBinding> onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.v(LOGGING_TAG, "Creating a tag view holder");
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            TagItemBinding binding = TagItemBinding.inflate(inflater);
            binding.getRoot().setTag(binding);
            binding.getRoot().setOnClickListener(this);
            return new BindingHolder<>(binding);
        }

        @Override
        public void onClick(View v) {
            Log.d(LOGGING_TAG, "Handling tag view handler click");
            // FIXME: uncomment this once `EditTagActivity is implemented.
            /*
            TagItemBinding binding = (TagItemBinding) v.getTag();
            if (binding != null) {
                Intent intent = new Intent(TagActivity.this, EditTagActivity.class);
                intent.putExtra(EditTagActivity.EXTRA_TAG_ID, binding.getTag().getId());
                startActivity(intent);
            }
            */
        }
    }
}