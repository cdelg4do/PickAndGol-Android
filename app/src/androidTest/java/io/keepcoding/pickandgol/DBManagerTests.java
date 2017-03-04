package io.keepcoding.pickandgol;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import io.keepcoding.pickandgol.model.User;
import io.keepcoding.pickandgol.model.db.DBManager;
import io.keepcoding.pickandgol.model.db.realmmanager.DBRealmManager;

import static org.junit.Assert.assertEquals;


/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class DBManagerTests {
    @Test
    public void testSaveAUserAndRecoverTheSameUser() {
        String[] favorites = {"1111", "2222"};
        User user = new User("58b2aef6d9f0163f6eee636e");
        user.setName("Irene")
                .setEmail("irene@gmail.com")
                .setPhotoUrl("http://images.com/irene.jpg")
                .setFavorites(Arrays.asList(favorites));

        Context appContext = InstrumentationRegistry.getTargetContext();
        DBManager manager = DBRealmManager.getDefaultInstance(appContext);
        manager.saveUser(user);
        User userRead = manager.getUser(user.getId());

        assertEquals("The user read is not the same as the user written", user, userRead);
    }
}
