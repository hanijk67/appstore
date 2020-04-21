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
public class TestPriority implements Serializable{

    public static final TestPriority LOW = new TestPriority((byte) 3);
    public static final TestPriority MEDIUM = new TestPriority((byte) 2);
    public static final TestPriority HIGH = new TestPriority((byte) 1);

    private byte state;

    public TestPriority(byte state) {
        this.state = state;
    }

    public TestPriority() {
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
        if (!(obj instanceof TestPriority))
            return false;
        TestPriority other = (TestPriority) obj;
        if (state != other.state)
            return false;
        return true;
    }

    public static List<TestPriority> listAll() {
        return Arrays.asList(LOW,MEDIUM,HIGH);
    }

    @Override
    public String toString() {
        return EnumCaptionHelper.PRIORITY.get(this);
    }
}