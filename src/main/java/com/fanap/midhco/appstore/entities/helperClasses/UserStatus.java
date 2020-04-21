package com.fanap.midhco.appstore.entities.helperClasses;

import com.fanap.midhco.appstore.wicketApp.EnumCaptionHelper;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by admin123 on 6/19/2016.
 */
@Embeddable
public class UserStatus implements Serializable {
    public static final UserStatus ENABLED = new UserStatus((byte) 1);
    public static final UserStatus DISABLED = new UserStatus((byte) 2);
    public static final UserStatus SECURITY_BLOCKED = new UserStatus((byte) 3);
    public static final UserStatus ADMIN_BLOCKED = new UserStatus((byte) 4);

    private byte status;

    public UserStatus(byte state) {
        this.status = state;
    }

    public UserStatus() {
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        return status;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof UserStatus))
            return false;
        UserStatus other = (UserStatus) obj;
        if (status != other.status)
            return false;
        return true;
    }

    public static List<UserStatus> listAll() {
        return Arrays.asList(ENABLED, DISABLED, SECURITY_BLOCKED, ADMIN_BLOCKED);
    }

    @Override
    public String toString() {
        return EnumCaptionHelper.USER_STATUS.get(this);
    }

}
