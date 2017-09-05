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


    // Static builders (create aggregates from other source objects):

    public static CategoryAggregate buildEmpty() {
        return new CategoryAggregate();
    }

    public static CategoryAggregate buildFromList(List<Category> list) {

        CategoryAggregate categoryAggregate = new CategoryAggregate();
        categoryAggregate.setAll(list);

        return categoryAggregate;
    }


    // Auxiliary methods:

    // Outputs the contents as a String (for debugging purposes)
    public String debugString() {

        if (size() == 0)
            return "< The aggregate contains 0 categories >";

        StringBuilder str = new StringBuilder();

        for (Category category: categoryList) {

            String id = category.getId();
            String name = category.getName();

            str.append("["+ id +"] "+ name +"\n");
        }

        return str.toString();
    }
}
