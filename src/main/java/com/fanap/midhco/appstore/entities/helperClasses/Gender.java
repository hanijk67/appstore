package com.fanap.midhco.appstore.entities.helperClasses;

import com.fanap.midhco.appstore.wicketApp.EnumCaptionHelper;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by admin123 on 6/25/2016.
 */
@Embeddable
public class Gender implements Serializable {
    public static Gender MALE = new Gender((byte)0);
    public static Gender FEMALE = new Gender((byte)1);

    private byte type;

    public Gender(byte type) {
        this.type = type;
    }

    public Gender() {}

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
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
        if (!(obj instanceof Gender))
            return false;
        Gender other = (Gender) obj;
        if (type != other.getType())
            return false;
        return true;
    }

    public static List<Gender> listAll() {
        return Arrays.asList(MALE, FEMALE);
    }

    @Override
    public String toString() {
        return EnumCaptionHelper.GENDER.get(this);
    }
}
