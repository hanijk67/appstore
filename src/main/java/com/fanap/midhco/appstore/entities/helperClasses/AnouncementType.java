package com.fanap.midhco.appstore.entities.helperClasses;

import com.fanap.midhco.appstore.wicketApp.EnumCaptionHelper;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by admin123 on 8/28/2017.
 */
@Embeddable
public class AnouncementType implements Serializable{
    public static final AnouncementType VOID = new AnouncementType(0);
    public static final AnouncementType PRODUCTLISTTYPE = new AnouncementType(1);

    int type;

    public AnouncementType(int type) {
        this.type = type;
    }

    public AnouncementType() {
    }

    public int getType() {
        return type;
    }

    public void setStatus(byte status) {
        this.type = status;
    }

    @Override
    public int hashCode() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof AnouncementType))
            return false;
        AnouncementType other = (AnouncementType) obj;
        if (type != other.type)
            return false;
        return true;
    }

    public static List<UserStatus> listAll() {
        return Arrays.asList();
    }

    @Override
    public String toString() {
        return EnumCaptionHelper.ANOUNCEMENTTYPE.get(this);
    }
}
