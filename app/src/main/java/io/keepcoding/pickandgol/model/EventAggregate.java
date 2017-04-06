package io.keepcoding.pickandgol.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * This class is an aggregate of Event objects, obtained from an event search.
 */
public class EventAggregate implements Iterable<Event>, Updatable<Event>, Searchable<Event>, Serializable {

    // The list of events contained in this aggregate
    private List<Event> eventList;

    // The total number of search matches (there can be more events than those contained here)
    private int totalResults;


    // Constructor is private, use the static build...() methods instead
    private EventAggregate() {
        eventList = new ArrayList<>();
        totalResults = 0;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int total) {
        this.totalResults = total;
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
    public void addElements(Iterable<Event> moreElements) {

        for (int i=0; i<moreElements.size(); i++)
            eventList.add( moreElements.get(i) );
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

    public static EventAggregate buildFromList(List<Event> list, int total) {

        EventAggregate eventAggregate = new EventAggregate();
        eventAggregate.setAll(list);
        eventAggregate.setTotalResults(total);

        return eventAggregate;
    }
}
