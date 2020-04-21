package com.fanap.midhco.appstore.entities;

import com.fanap.midhco.appstore.wicketApp.EnumCaptionHelper;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by A.Moshiri on 6/20/2017.
 */
@Embeddable
public class ApprovalState implements Serializable {

    public static final ApprovalState APPROVED = new ApprovalState((byte)2);
    public static final ApprovalState DISAPPROVED = new ApprovalState((byte)1);
    public static final ApprovalState CANCELED = new ApprovalState((byte)3);

    private byte state;

    public ApprovalState(byte state) {
        this.state = state;
    }

    public ApprovalState() {
    }

    @Override
    public int hashCode() {
        return state;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null)
            return false;
        if (!(obj instanceof ApprovalState))
            return false;
        ApprovalState other = (ApprovalState) obj;
        if (state != other.state)
            return false;
        return true;
    }

    public static List<ApprovalState> listAll() {
        return Arrays.asList(APPROVED, DISAPPROVED ,CANCELED);
    }

    @Override
    public String toString() {
        return EnumCaptionHelper.APPROVAL_STATE.get(this);
    }
}