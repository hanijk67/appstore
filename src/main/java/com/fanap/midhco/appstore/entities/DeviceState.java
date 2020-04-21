package com.fanap.midhco.appstore.entities;

import com.fanap.midhco.appstore.wicketApp.EnumCaptionHelper;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by admin123 on 7/4/2016.
 */
@Embeddable
public class DeviceState implements Serializable {
    public static final DeviceState FREE = new DeviceState((byte) 2);
    public static final DeviceState INUSED = new DeviceState((byte) 1);

    private byte state;

    public DeviceState(byte state) {
        this.state = state;
    }

    public DeviceState() {
    }

    @Override
    public int hashCode() {
        return state;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof DeviceState))
            return false;
        DeviceState other = (DeviceState) obj;
        if (state != other.state)
            return false;
        return true;
    }

    public static List<DeviceState> listAll() {
        return Arrays.asList(FREE,INUSED);
    }

    @Override
    public String toString() {
        return EnumCaptionHelper.DEVICE_STATE.get(this);
    }
}