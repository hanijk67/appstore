package com.fanap.midhco.appstore.entities.helperClasses;


import javax.persistence.Embeddable;
import javax.persistence.PostLoad;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

@Embeddable
@org.hibernate.annotations.Entity(mutable = false)
public class DayTime implements Comparable, Serializable {

    public static final DayTime UNKNOWN = new DayTime(0, 0, 0);
    public static final DayTime MIN_DAY_TIME = new DayTime(0, 0, 0);
    public static final DayTime MAX_DAY_TIME = new DayTime(23, 59, 59);
    private int dayTime;

    @Transient
    private int hour;

    @PostLoad
    private void onLoad() {
        this.hour = getHour();
    }

    public DayTime() {
    }

    public DayTime(Date date) {
        if (date == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MILLISECOND, 0);
        setHour(calendar.get(Calendar.HOUR_OF_DAY));
        setMinute(calendar.get(Calendar.MINUTE));
        setSecond(calendar.get(Calendar.SECOND));

    }

    public DayTime(int hour, int minute) {
        setDayTime(hour, minute);
    }

    public DayTime(int hour, int minute, int second) {
        setDayTime(hour, minute, second);
    }

    private void setDayTime(int hour, int minute) {
        this.dayTime = hour * 10000 + minute * 100 + getSecond();
    }

    private void setDayTime(int hour, int minute, int second) {
        this.dayTime = hour * 10000 + minute * 100 + second;
    }

    public int getDayTime() {
        return dayTime;
    }

    public void setDayTime(int dayTime) {
        this.dayTime = dayTime;
    }

    public void setHour(int hour) {
        setDayTime(hour, getMinute(), getSecond());
    }

    @Transient
    public int getHour() {
        return dayTime / 10000;
    }

    public void setMinute(int minute) {
        setDayTime(getHour(), minute, getSecond());
    }

    @Transient
    public int getMinute() {
        return (dayTime % 10000) / 100;
    }


    public int getSecond() {
        return dayTime % 100;
    }

    public void setSecond(int second) {
        setDayTime(getHour(), getMinute(), second);
    }

    public boolean between(DayTime time1, DayTime time2) {
        if (this.before(time2) && this.after(time1)) {
            return true;
        }
        return false;
    }

    public boolean after(DayTime time) {
        return this.compareTo(time) > 0;
    }

    public boolean afterEquals(DayTime time) {
        return this.compareTo(time) >= 0;
    }

    public boolean before(DayTime time) {
        return this.compareTo(time) < 0;
    }

    public boolean beforeEquals(DayTime time) {
        return this.compareTo(time) <= 0;
    }

    public int compareTo(Object o) {
        if (o == null)
            return 1;
        DayTime anotherTime = (DayTime) o;
        return dayTime - anotherTime.dayTime;
    }

    public void increase(int minutes) {
        int sec = this.getHour() * 3600 + (this.getMinute() + minutes) * 60 + getSecond();
        int dayTime = (sec / 3600) * 10000 + ((sec % 3600) / 60) * 100 + sec % 60;
        this.dayTime = dayTime;
    }

    @Override
    public String toString() {
        return (getHour() > 9 ? getHour() : "0" + getHour()) + ":" + (getMinute() > 9 ? getMinute() : "0" + getMinute()) + ":" + (getSecond() > 9 ? getSecond() : "0" + getSecond());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DayTime dayTime = (DayTime) o;

        if (this.dayTime != dayTime.dayTime) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return dayTime;
    }

    /*
        * return diffrent between two time bar hasbe minute
        * */
    public int diff(DayTime time) {
        return (getHour() - time.getHour()) * 3600 + (getMinute() - time.getMinute()) * 60 + (getSecond() - time.getSecond());
    }

    @Override
    public DayTime clone() {
        return new DayTime(getHour(), getMinute(), getSecond());
    }

    public static DayTime getUnknownInstance() {
        return UNKNOWN.clone();
    }

    public static boolean isNullOrUnknown(DayTime dayTime) {
        return dayTime == null || dayTime.equals(UNKNOWN);
    }

}
