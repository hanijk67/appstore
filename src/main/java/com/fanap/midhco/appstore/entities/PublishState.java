package com.fanap.midhco.appstore.entities;

import com.fanap.midhco.appstore.wicketApp.EnumCaptionHelper;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin123 on 7/1/2016.
 */
@Embeddable
public class PublishState implements Serializable {
     //!!!!!!!!!!!!!any changes should be added to listAll() function; !!!!!!!!!!!!!!!!!
    public static final PublishState PUBLISHED = new PublishState((byte)2);
    public static final PublishState UNPUBLISHED = new PublishState((byte)1);

    private byte state;

    public byte getState() {
        return state;
    }

    public PublishState(byte state) {
        this.state = state;
    }

    public PublishState() {
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
        if (!(obj instanceof PublishState))
            return false;
        PublishState other = (PublishState) obj;
        if (state != other.state)
            return false;
        return true;
    }

    public static List<PublishState> listAll() {
        List<PublishState> publishStates = new ArrayList<>();
        publishStates.add(PUBLISHED);
        publishStates.add(UNPUBLISHED);
        return publishStates;
    }

    @Override
    public String toString() {
        return EnumCaptionHelper.PUBLISH_STATE.get(this);
    }
}
