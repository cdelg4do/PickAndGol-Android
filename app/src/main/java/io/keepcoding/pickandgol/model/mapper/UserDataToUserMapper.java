package io.keepcoding.pickandgol.model.mapper;

import io.keepcoding.pickandgol.manager.net.response.UserResponse;
import io.keepcoding.pickandgol.model.User;


/**
 * This class is used to map a UserResponse.UserData object to a User model object.
 */
public class UserDataToUserMapper {

    public User map(UserResponse.UserData data) {

        User user = new User(
                data.getId(),
                data.getEmail(),
                data.getName(),
                data.getFavoritePubs(),
                data.getPhotoUrl()
        );

        return user;
    }
}
