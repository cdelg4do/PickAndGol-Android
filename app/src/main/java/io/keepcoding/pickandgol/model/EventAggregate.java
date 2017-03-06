package io.keepcoding.pickandgol.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;


/**
 * This class is an aggregate of Event objects
 */
public class EventAggregate implements Iterable<Event>, Updatable<Event>, Searchable<Event> {

    private List<Event> eventList;

    // Constructor is private, use the static build...() methods instead
    private EventAggregate() {
        eventList = new ArrayList<>();
    }

    @Override
    public int size() {
        return eventList.size();
    }

    @Override
    public @Nullable Event get(int index) {
        if (index <0 || index >= eventList.size())
            return null;

        return eventList.get(index);
    }

    @Override
    public @NonNull List<Event> getAll() {
        return eventList;
    }

    @Override
    public void add(Event element) {
        eventList.add(element);
    }

    @Override
    public void delete(Event element) {
        eventList.remove(element);
    }

    @Override
    public void update(Event element, int index) {
        eventList.set(index, element);
    }

    @Override
    public void setAll(List<Event> list) {

        for (Event element : list)
            eventList.add(element);
    }

    @Override
    public @Nullable Event search(String id) {

        Event foundEvent = null;
        boolean keepSearching = true;
        int i = 0;

        while (keepSearching && i < eventList.size()) {

            if (eventList.get(i).getId().equals(id)) {
                foundEvent = eventList.get(i);
                keepSearching = false;
            }

            i++;
        }

        return foundEvent;
    }


    // Static builders (create aggregates from other source objects):

    public static EventAggregate buildEmpty() {
        return new EventAggregate();
    }

    public static EventAggregate buildFromList(List<Event> list) {

        EventAggregate pubAggregate = new EventAggregate();
        pubAggregate.setAll(list);

        return pubAggregate;
    }
}
