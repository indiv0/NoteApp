package com.seg3125.noteapp;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.requery.Persistable;
import io.requery.reactivex.ReactiveEntityStore;

class CreateNotes implements Callable<Observable<Iterable<Note>>> {

    private final ReactiveEntityStore<Persistable> data;

    CreateNotes(ReactiveEntityStore<Persistable> data) {
        this.data = data;
    }

    @Override
    public Observable<Iterable<Note>> call() {
        // TODO: extract default note strings to the localization files.
        String[] titles = new String[] {
                "How to Use",
                "Alphabet",
                "Numbers",
        };
        String[] contents = new String[] {
                "Create a new note by clicking the + icon in the main menu!",
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
                "0123456789",
        };

        final Set<Note> notes = new TreeSet<>(new Comparator<Note>() {
            @Override
            public int compare(Note lhs, Note rhs) {
                return lhs.getTitle().compareTo(rhs.getTitle());
            }
        });

        // Create the example notes.
        for (int i = 0; i < 3; i++) {
            NoteEntity note = new NoteEntity();
            String title = titles[i];
            String content = contents[i];
            note.setTitle(title);
            note.setContent(content);
            notes.add(note);
        }

        return data.insert(notes).toObservable();
    }
}
