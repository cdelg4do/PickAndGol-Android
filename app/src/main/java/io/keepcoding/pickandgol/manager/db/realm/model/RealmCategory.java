package io.keepcoding.pickandgol.manager.db.realm.model;

import android.support.annotation.Nullable;

import io.keepcoding.pickandgol.model.Category;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


/**
 * This class is the equivalent to the Category class, managed by Realm
 */
public class RealmCategory extends RealmObject {

    @PrimaryKey
    private String id;
    private String name;

    // An empty public constructor is mandatory for Realm when using customized constructors
    public RealmCategory() {
    }

    public RealmCategory(String id, String name) {

        this.id = id;
        this.name = name;
    }

    // Getters:

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    // Setters:

    public RealmCategory setId(String id) {

        this.id = id;
        return this;
    }

    public RealmCategory setName(String name) {

        this.name = name;
        return this;
    }


    // Mapping methods (memory <-> database):

    public static @Nullable RealmCategory mapFromModel(Category category) {

        if (category == null)
            return null;

        RealmCategory realmCategory = new RealmCategory()
                .setId( category.getId() )
                .setName( category.getName() );

        return realmCategory;
    }

    public Category mapToModel() {

        Category category = new Category(
                this.getId(),
                this.getName()
        );

        return category;
    }
}
