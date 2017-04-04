package com.seg3125.noteapp;

import android.app.Application;
import android.os.StrictMode;

import net.danlew.android.joda.JodaTimeAndroid;

import io.requery.Persistable;
import io.requery.android.sqlite.DatabaseSource;
import io.requery.reactivex.ReactiveEntityStore;
import io.requery.reactivex.ReactiveSupport;
import io.requery.sql.Configuration;
import io.requery.sql.EntityDataStore;
import io.requery.sql.TableCreationMode;

public class NoteApplication extends Application {

    private static final int DB_Version = 3;

    // The data store, which holds all entities used by this application.
    private ReactiveEntityStore<Persistable> dataStore;

    @Override
    public void onCreate() {
        super.onCreate();
        // Enable Android Strict Mode with the default settings, in order to monitor for
        // unintentional disk or network I/O on the main thread.
        StrictMode.enableDefaults();

        // Initialize the Joda-Time Android library.
        JodaTimeAndroid.init(this);
    }

    /**
     * @return {@link EntityDataStore} single instance for the application.
     * <p/>
     * Note if you're using Dagger you can make this part of your application level module returning
     * {@code @Provides @Singleton}.
     */
    ReactiveEntityStore<Persistable> getData() {
        if (dataStore == null) {
            // In the future, override onUpgrade to handle migrating to a new version.
            DatabaseSource source = new DatabaseSource(this, Models.DEFAULT, DB_Version);
            if (BuildConfig.DEBUG) {
                // In development mode, set the table creation mode to recreate the tables on every
                // upgrade.
                // This allows you to simply re-populate with default data every time instead of
                // having to handle migrations during the development process.
                source.setTableCreationMode(TableCreationMode.DROP_CREATE);
            }
            // Load the configuration for the database source, and use that configuration to create
            // the reactive store.
            Configuration configuration = source.getConfiguration();
            dataStore = ReactiveSupport.toReactiveStore(
                    new EntityDataStore<Persistable>(configuration));
        }

        return dataStore;
    }
}
