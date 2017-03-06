package io.keepcoding.pickandgol;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import io.keepcoding.pickandgol.manager.db.DBManager;
import io.keepcoding.pickandgol.manager.db.DBManagerListener;
import io.keepcoding.pickandgol.manager.db.realm.RealmDBManager;
import io.keepcoding.pickandgol.model.User;
import io.keepcoding.pickandgol.util.MainThread;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class RealmDBManagerTests {

    @Test
    public void testSaveAnUserAndRecoverTheSameUser() {

        MainThread.run(new Runnable() {
            @Override
            public void run() {

                String[] favorites = {"1111", "2222"};
                final User user = new User("58b2aef6d9f0163f6eee636e");

                user.setName("Irene")
                .setEmail("irene@gmail.com")
                .setPhotoUrl("http://images.com/irene.jpg")
                .setFavorites(Arrays.asList(favorites));

                Context appContext = InstrumentationRegistry.getTargetContext();
                final DBManager manager = RealmDBManager.getDefaultInstance(appContext);

                manager.saveUser(user, new DBManagerListener() {
                    @Override
                    public void onError(Throwable e) {
                        assertNotEquals("saveUser() failed!", null, null);
                    }

                    @Override
                    public void onSuccess(@Nullable Object result) {
                        User userRead = manager.getUser(user.getId());
                        assertEquals("The user read is not the same as the user written", user, userRead);
                    }
                });

            }
        });



    }
}
