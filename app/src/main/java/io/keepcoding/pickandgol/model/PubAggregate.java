package io.keepcoding.pickandgol.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;


/**
 * This class is an aggregate of Pub objects
 */
public class PubAggregate implements Iterable<Pub>, Updatable<Pub>, Searchable<Pub> {

    private List<Pub> pubList;

    // Constructor is private, use the static build...() methods instead
    private PubAggregate() {
        pubList = new ArrayList<>();
    }

    @Override
    public int size() {
        return pubList.size();
    }

    @Override
    public @Nullable Pub get(int index) {
        if (index <0 || index >= pubList.size())
            return null;

        return pubList.get(index);
    }

    @Override
    public @NonNull List<Pub> getAll() {
        return pubList;
    }

    @Override
    public void add(Pub element) {
        pubList.add(element);
    }

    @Override
    public void delete(Pub element) {
        pubList.remove(element);
    }

    @Override
    public void update(Pub element, int index) {
        pubList.set(index, element);
    }

    @Override
    public void setAll(List<Pub> list) {

        for (Pub element : list)
            pubList.add(element);
    }

    @Override
    public @Nullable Pub search(String id) {

        Pub foundPub = null;
        boolean keepSearching = true;
        int i = 0;

        while (keepSearching && i < pubList.size()) {

            if (pubList.get(i).getId().equals(id)) {
                foundPub = pubList.get(i);
                keepSearching = false;
            }

            i++;
        }

        return foundPub;
    }


    // Static builders (create aggregates from other source objects):

    public static PubAggregate buildEmpty() {
        return new PubAggregate();
    }

    public static PubAggregate buildFromList(List<Pub> list) {

        PubAggregate pubAggregate = new PubAggregate();
        pubAggregate.setAll(list);

        return pubAggregate;
    }
}
