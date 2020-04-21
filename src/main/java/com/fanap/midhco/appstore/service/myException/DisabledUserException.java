package com.fanap.midhco.appstore.service.myException;

import com.fanap.midhco.appstore.entities.helperClasses.UserStatus;

public class DisabledUserException extends AppStoreRuntimeException {
    private UserStatus userStatus;

    public DisabledUserException(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }
}
