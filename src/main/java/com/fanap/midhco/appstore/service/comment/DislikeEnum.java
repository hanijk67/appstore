package com.fanap.midhco.appstore.service.comment;

import com.fanap.midhco.appstore.wicketApp.EnumCaptionHelper;

import java.util.Arrays;
import java.util.List;

/**
 * Created by admin123 on 9/13/2017.
 */
public class DislikeEnum {
    public static final DislikeEnum NOT_USEFUL = new DislikeEnum(0);
    public static final DislikeEnum NOT_SUITABLE = new DislikeEnum(1);
    public static final DislikeEnum SPAM = new DislikeEnum(2);

    int state;

    private DislikeEnum(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DislikeEnum that = (DislikeEnum) o;

        return state == that.state;

    }

    @Override
    public int hashCode() {
        return state;
    }

    public List<DislikeEnum> listAll() {
        return Arrays.asList(NOT_USEFUL, NOT_SUITABLE, SPAM);
    }

    @Override
    public String toString() {
        return EnumCaptionHelper.DISLIKEENUM.get(this);
    }
}
