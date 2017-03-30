package com.seg3125.noteapp;

import android.databinding.Bindable;
import android.databinding.Observable;
import android.os.Parcelable;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToMany;
import io.requery.Persistable;
import io.requery.query.MutableResult;

@Entity
public interface Tag extends Observable, Parcelable, Persistable {
    @Key @Generated
    int getId();

    @Bindable
    String getName();
    void setName(String name);

    @ManyToMany
    MutableResult<Note> getNotes();
}