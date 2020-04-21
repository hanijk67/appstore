package com.fanap.midhco.appstore.restControllers.vos.componentVos;

/**
 * Created by A.Moshiri on 1/8/2018.
 */
public class CalendarType {
    public static CalendarType GREGORIAN = new CalendarType(0);
    public static CalendarType PERSIAN = new CalendarType(1);

    int state;

    public CalendarType(int state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CalendarType that = (CalendarType) o;

        return state == that.state;
    }

    @Override
    public int hashCode() {
        return state;
    }
}
