package io.keepcoding.pickandgol.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;


/**
 * This class is an aggregate of User objects
 */
public class UserAggregate implements Iterable<User>, Updatable<User>, Searchable<User> {

    private List<User> userList;

    // Constructor is private, use the static build...() methods instead
    private UserAggregate() {
        userList = new ArrayList<>();
    }

    @Override
    public int size() {
        return userList.size();
    }

    @Override
    public @Nullable User get(int index) {
        if (index <0 || index >= userList.size())
            return null;

        return userList.get(index);
    }

    @Override
    public @NonNull List<User> getAll() {
        return userList;
    }

    @Override
    public void add(User element) {
        userList.add(element);
    }

    @Override
    public void delete(User element) {
        userList.remove(element);
    }

    @Override
    public void update(User element, int index) {
        userList.set(index, element);
    }

    @Override
    public void setAll(List<User> list) {

        for (User element : list)
            userList.add(element);
    }

    @Override
    public void addElements(Iterable<User> moreElements) {

        for (int i=0; i<moreElements.size(); i++)
            userList.add( moreElements.get(i) );
    }

    @Override
    public @Nullable User search(String id) {

        User foundUser = null;
        boolean keepSearching = true;
        int i = 0;

        while (keepSearching && i < userList.size()) {

            if (userList.get(i).getId().equals(id)) {
                foundUser = userList.get(i);
                keepSearching = false;
            }

            i++;
        }

        return foundUser;
    }


    // Static builders (create aggregates from other source objects):

    public static UserAggregate buildEmpty() {
        return new UserAggregate();
    }

    public static UserAggregate buildFromList(List<User> list) {

        UserAggregate userAggregate = new UserAggregate();
        userAggregate.setAll(list);

        return userAggregate;
    }
}
