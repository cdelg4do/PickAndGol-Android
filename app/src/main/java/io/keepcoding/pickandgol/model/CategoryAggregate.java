package io.keepcoding.pickandgol.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;


/**
 * This class is an aggregate of Category objects.
 */
public class CategoryAggregate implements Iterable<Category>, Updatable<Category>, Searchable<Category> {

    // The list of categories contained in this aggregate
    private List<Category> categoryList;


    public CategoryAggregate() {
        categoryList = new ArrayList<>();
    }

    @Override
    public int size() {
        return categoryList.size();
    }

    @Override
    public @Nullable Category get(int index) {
        if (index <0 || index >= categoryList.size())
            return null;

        return categoryList.get(index);
    }

    @Override
    public @NonNull List<Category> getAll() {
        return categoryList;
    }

    @Override
    public void add(Category element) {
        categoryList.add(element);
    }

    @Override
    public void delete(Category element) {
        categoryList.remove(element);
    }

    @Override
    public void update(Category element, int index) {
        categoryList.set(index, element);
    }

    @Override
    public void setAll(List<Category> list) {
        categoryList.addAll(list);
    }

    @Override
    public void addElements(Iterable<Category> moreElements) {

        for (int i = 0; i < moreElements.size(); i++)
            categoryList.add(moreElements.get(i));
    }

    @Override
    public @Nullable Category search(String id) {

        for (Category category: categoryList) {

            if (category.getId().equals(id))
                return category;
        }

        return null;
    }
}
