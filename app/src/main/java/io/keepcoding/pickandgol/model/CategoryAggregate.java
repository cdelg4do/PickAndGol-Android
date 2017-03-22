package io.keepcoding.pickandgol.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CategoryAggregate implements Iterable<Category>, Updatable<Category>, Searchable<Category> {
    private List<Category> categories;

    public CategoryAggregate() {
        categories = new ArrayList<>();
    }

    @Override
    public int size() {
        return categories.size();
    }

    @Nullable
    @Override
    public Category get(int index) {
        return categories.get(index);
    }

    @NonNull
    @Override
    public List<Category> getAll() {
        return categories;
    }

    @Override
    public void add(Category element) {
        categories.add(element);
    }

    @Override
    public void delete(Category element) {
        categories.remove(element);
    }

    @Override
    public void update(Category element, int index) {
        categories.set(index, element);
    }

    @Override
    public void setAll(List<Category> list) {
        categories.addAll(list);
    }

    @Override
    public void addElements(Iterable<Category> moreElements) {
        for (int i = 0; i < moreElements.size(); i++) {
            categories.add(moreElements.get(i));
        }
    }

    @Nullable
    @Override
    public Category search(String id) {
        for (Category category: categories) {
            if (category.getId().equals(id)) {
                return category;
            }
        }

        return null;
    }
}
