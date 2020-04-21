package com.fanap.midhco.appstore.entities;

import com.fanap.midhco.appstore.wicketApp.EnumCaptionHelper;

import java.io.Serializable;

/**
 * Created by admin123 on 7/31/2017.
 */

public class RatingIndex implements Serializable {
    public static RatingIndex Excellent = new RatingIndex(5);
    public static RatingIndex VeryGood = new RatingIndex(4);
    public static RatingIndex Good = new RatingIndex(3);
    public static RatingIndex intermediate = new RatingIndex(2);
    public static RatingIndex bad = new RatingIndex(1);

    int state;

    public RatingIndex(int state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RatingIndex that = (RatingIndex) o;

        return state == that.state;
    }

    public int getState() {
        return state;
    }

    @Override
    public int hashCode() {
        return state;
    }

    public String toString() {
        return EnumCaptionHelper.RATING_INDEX.get(this);
    }
}
