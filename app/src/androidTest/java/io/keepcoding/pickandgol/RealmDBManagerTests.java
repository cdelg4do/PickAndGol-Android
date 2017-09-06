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
import io.keepcoding.pickandgol.manager.db.DBManagerBuilder;
import io.keepcoding.pickandgol.manager.db.DBManagerListener;
import io.keepcoding.pickandgol.model.Pub;
import io.keepcoding.pickandgol.model.User;
import io.keepcoding.pickandgol.util.MainThread;
import io.realm.Realm;

import static io.keepcoding.pickandgol.manager.db.DBManagerBuilder.DatabaseType.REALM;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;


/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class RealmDBManagerTests {

    // Test that, after removing a user, it cannot be retrieved again
    @Test
    public void testThatAfterRemoveAnUserCannotGetItAgain() {

        MainThread.run(new Runnable() {
            @Override
            public void run() {

                final User dummyUser = createDummyUser();
                final DBManager dbMgr = getDBManager();

                // 1- Save the dummy user to the database
                dbMgr.saveUser(dummyUser, new DBManagerListener() {
                    @Override
                    public void onError(Throwable e) {
                        fail("Error while saving a user to the database");
                    }

                    @Override
                    public void onSuccess(@Nullable Object result) {

                        // 2- Remove the user from the database
                        dbMgr.removeUser(dummyUser.getId(), new DBManagerListener() {
                            @Override
                            public void onError(Throwable e) {
                                fail("Error while removing a user from the database");
                            }

                            @Override
                            public void onSuccess(@Nullable Object result) {

                                // 3- Try to retrieve the removed user from the database
                                dbMgr.getUser(dummyUser.getId(), new DBManagerListener() {
                                    @Override
                                    public void onError(Throwable e) {
                                        fail("Error while retrieving a removed user from the database");
                                    }

                                    @Override
                                    public void onSuccess(@Nullable Object result) {

                                        // 4- Make sure the result is null
                                        assertNull("The removed user is still there!", result);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }


    // Test that, after removing a pub, it cannot be retrieved again
    @Test
    public void testThatAfterRemoveAPubCannotGetItAgain() {

        MainThread.run(new Runnable() {
            @Override
            public void run() {

                final Pub dummyPub = createDummyPub();
                final DBManager dbMgr = getDBManager();

                // 1- Save the dummy pub to the database
                dbMgr.savePub(dummyPub, new DBManagerListener() {
                    @Override
                    public void onError(Throwable e) {
                        fail("Error while saving a pub to the database");
                    }

                    @Override
                    public void onSuccess(@Nullable Object result) {

                        // 2- Remove the pub from the database
                        dbMgr.removePub(dummyPub.getId(), new DBManagerListener() {
                            @Override
                            public void onError(Throwable e) {
                                fail("Error while removing a pub from the database");
                            }

                            @Override
                            public void onSuccess(@Nullable Object result) {

                                // 3- Try to retrieve the removed pub from the database
                                dbMgr.getPub(dummyPub.getId(), new DBManagerListener() {
                                    @Override
                                    public void onError(Throwable e) {
                                        fail("Error while retrieving a removed pub from the database");
                                    }

                                    @Override
                                    public void onSuccess(@Nullable Object result) {

                                        // 4- Make sure the result is null
                                        assertNull("The removed pub is still there!", result);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }


    /*************************************************************
       Auxiliary methods to prepare the tests:
     ************************************************************/

    private DBManager getDBManager() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        Realm.init(appContext);

        return new DBManagerBuilder().type(REALM).build();
    }

    @NonNull
    private User createDummyUser() {
        String[] favorites = {"1111", "2222"};
        final User user = new User("58b2aef6d9f0163f6eee636e");

        user.setName("Irene")
        .setEmail("irene@gmail.com")
        .setPhotoUrl("http://images.com/irene.jpg")
        .setFavorites(Arrays.asList(favorites));
        return user;
    }

    @NonNull
    private Pub createDummyPub() {
        List<String> events = new ArrayList<>();
        events.add("58a058b633674f1e95cd411f");
        events.add("58a0288b8b00070cab093c62");

        List<String> photos = new ArrayList<>();
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
