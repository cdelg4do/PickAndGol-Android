package io.keepcoding.pickandgol;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.keepcoding.pickandgol.manager.db.DBManager;
import io.keepcoding.pickandgol.manager.db.DBManagerListener;
import io.keepcoding.pickandgol.manager.db.realm.RealmDBManager;
import io.keepcoding.pickandgol.model.Pub;
import io.keepcoding.pickandgol.model.User;
import io.keepcoding.pickandgol.util.MainThread;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;


/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class RealmDBManagerTests {
    @Test
    public void testThatAfterRemoveAnUserCannotGetItAgain() {
        MainThread.run(new Runnable() {
            @Override
            public void run() {
                final User user = createUser();
                final DBManager manager = getDBManager();
                manager.saveUser(user, new DBManagerListener() {
                    @Override
                    public void onError(Throwable e) {
                        fail("Error while saving object");
                    }

                    @Override
                    public void onSuccess(@Nullable Object result) {
                        final User userSaved = manager.getUser(user.getId());
                        assertEquals("The object recovered is not the same as the object saved", user, userSaved);

                        manager.removeUser(user.getId(), new DBManagerListener() {
                            @Override
                            public void onError(Throwable e) {
                                fail("Error while removing a object");
                            }

                            @Override
                            public void onSuccess(@Nullable Object result) {
                                final User userRemoved = manager.getUser(user.getId());
                                assertNull("The object removed was recovered!", userRemoved);
                            }
                        });
                    }
                });
            }
        });
    }

    @Test
    public void testThatAfterRemoveAPubCannotGetItAgain() {
        MainThread.run(new Runnable() {
            @Override
            public void run() {
                final Pub pub = createPub();
                final DBManager manager = getDBManager();
                manager.savePub(pub, new DBManagerListener() {
                    @Override
                    public void onError(Throwable e) {
                        fail("Error while saving a pub");
                    }

                    @Override
                    public void onSuccess(@Nullable Object result) {
                        final Pub pubSaved = manager.getPub(pub.getId());
                        assertEquals("The pub recovered is not the same as the pub saved", pub, pubSaved);

                        manager.removePub(pub.getId(), new DBManagerListener() {
                            @Override
                            public void onError(Throwable e) {
                                fail("Error while removing a pub");
                            }

                            @Override
                            public void onSuccess(@Nullable Object result) {
                                final Pub pubRemoved = manager.getPub(pub.getId());
                                assertNull("The pub removed was recovered!", pubRemoved);
                            }
                        });
                    }
                });
            }
        });
    }

    private DBManager getDBManager() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        return RealmDBManager.getDefaultInstance();
    }

    @NonNull
    private User createUser() {
        String[] favorites = {"1111", "2222"};
        final User user = new User("58b2aef6d9f0163f6eee636e");

        user.setName("Irene")
        .setEmail("irene@gmail.com")
        .setPhotoUrl("http://images.com/irene.jpg")
        .setFavorites(Arrays.asList(favorites));
        return user;
    }

    @NonNull
    private Pub createPub() {
        List<String> events = new ArrayList<String>();
        events.add("58a058b633674f1e95cd411f");
        events.add("58a0288b8b00070cab093c62");

        List<String> photos = new ArrayList<String>();
        photos.add("https://pickandgol.s3.amazonaws.com/67e19952-2226-4718-bd83-cb62d89ff3cb.jpg");

        return new Pub("58b2aef6d9f0163f6eee656h",
                "Casa Paco",
                true,
                40.41665,
                -3.70381,
                "www.casapaco.es",
                "589a12e6448fec0896cb49fa",
                events,
                photos);
    }
}
