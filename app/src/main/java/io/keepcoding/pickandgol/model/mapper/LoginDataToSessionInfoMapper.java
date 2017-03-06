package io.keepcoding.pickandgol.model.mapper;

import io.keepcoding.pickandgol.manager.net.response.LoginResponse;
import io.keepcoding.pickandgol.model.SessionInfo;


/**
 * This class is used to map a LoginResponse.LoginData object to a SessionInfo model object.
 */
public class LoginDataToSessionInfoMapper {

    public SessionInfo map(LoginResponse.LoginData data) {

        SessionInfo sessionInfo = new SessionInfo(
                data.getId(),
                data.getEmail(),
                data.getName(),
                data.getToken(),
                data.getPhotoUrl()
        );

        return sessionInfo;
    }
}
