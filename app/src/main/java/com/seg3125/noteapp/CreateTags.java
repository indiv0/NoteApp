package com.seg3125.noteapp;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.requery.Persistable;
import io.requery.reactivex.ReactiveEntityStore;

class CreateTags implements Callable<Observable<Iterable<Tag>>> {

    private final ReactiveEntityStore<Persistable> data;

    CreateTags(ReactiveEntityStore<Persistable> data) {
        this.data = data;
    }

    @Override
    public Observable<Iterable<Tag>> call() {
        // TODO: extract default tag strings to the localization files.
        String[] names = new String[] {
                "tutorial",
                "groceries",
        };

        final Set<Tag> tags = new TreeSet<>(new Comparator<Tag>() {
            @Override
            public int compare(Tag lhs, Tag rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });

        // Create the example notes.
        for (int i = 0; i < 2; i++) {
            TagEntity tag = new TagEntity();
            String name = names[i];
            tag.setName(name);
            tags.add(tag);
        }

        return data.insert(tags).toObservable();
    }
}
