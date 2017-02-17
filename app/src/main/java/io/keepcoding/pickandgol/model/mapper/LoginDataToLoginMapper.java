package io.keepcoding.pickandgol.model.mapper;

import io.keepcoding.pickandgol.manager.net.response.LoginResponse;
import io.keepcoding.pickandgol.model.Login;


/**
 * This class is used to map a LoginResponse.LoginData object to a Login model object.
 */
public class LoginDataToLoginMapper {

    public Login map(LoginResponse.LoginData data) {

        Login login = new Login(
                data.getId(),
                data.getEmail(),
                data.getName(),
                data.getToken(),
                data.getPhotoUrl()
        );

        return login;
    }
}
