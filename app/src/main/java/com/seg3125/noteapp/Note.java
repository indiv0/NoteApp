package com.seg3125.noteapp;

import android.databinding.Bindable;
import android.databinding.Observable;
import android.os.Parcelable;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.Persistable;

@Entity
public interface Note extends Observable, Parcelable, Persistable {
    @Key @Generated
    int getId();

    @Bindable
    String getTitle();
    void setTitle(String title);

    @Bindable
    String getContent();
    void setContent(String content);

    @Bindable
    boolean getLock();
    void setLock(boolean Lock);

    @Bindable
    String getLockKey();
    void setLockKey(String Key);
}
