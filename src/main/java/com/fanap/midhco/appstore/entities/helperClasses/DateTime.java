package com.fanap.midhco.appstore.entities.helperClasses;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

@Embeddable
@org.hibernate.annotations.Entity(mutable = false)
public class DateTime implements Comparable, Serializable, Cloneable {
    private DayDate dayDate = DayDate.UNKNOWN;
    private DayTime dayTime = DayTime.UNKNOWN;

    public static final DateTime UNKNOWN = new DateTime(DayDate.UNKNOWN, DayTime.UNKNOWN);
    public static final DateTime MIN_DATE_TIME = new DateTime(DayDate.MIN_DAY_DATE, DayTime.MIN_DAY_TIME);
    public static final DateTime MAX_DATE_TIME = new DateTime(DayDate.MAX_DAY_DATE, DayTime.MAX_DAY_TIME);

    public static final long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000;
    public static final long ONE_HOUR_MILLIS = 60 * 60 * 1000;
    public static final long ONE_MINUTE_MILLIS = 60 * 1000;

    public DateTime() {
    }

    public DateTime(Date date) {
        this.dayDate = new DayDate(date);
        this.dayTime = new DayTime(date);
    }

    public DateTime(Long dateTimeLong) {
        Date date = new Date( dateTimeLong);
        this.dayDate = new DayDate(date);
        this.dayTime = new DayTime(date);
    }

    public DateTime(int year , int month , int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month-1, day);

        Date date = calendar.getTime();

        this.dayDate = new DayDate(date);
        this.dayTime = new DayTime(date);
    }

    public DateTime(DayDate dayDate, DayTime dayTime) {
        this.dayDate = dayDate;
        this.dayTime = dayTime;
    }

    public DayDate getDayDate() {
        return dayDate;
    }

    public void setDayDate(DayDate dayDate) {
        this.dayDate = dayDate;
    }

    public DayTime getDayTime() {
        return dayTime;
    }

    public void setDayTime(DayTime dayTime) {
        this.dayTime = dayTime;
    }

    public boolean isLessThan(DateTime dateTime) {
        return this.compareTo(dateTime) < 0;
    }

    public boolean isGreaterThan(DateTime dateTime) {
        return this.compareTo(dateTime) > 0;
    }

    public boolean isGreaterThanOrEquals(DateTime dateTime) {
        return this.compareTo(dateTime) >= 0;
    }

    public void decrease(int minutes) {
        increase(-minutes);
    }

    public void increase(int minutes) {
        Date date = toDate();
        long time = date.getTime() + minutes * 60 * 1000;
        DateTime dateTime = toDateTime(time);
        this.dayDate = dateTime.getDayDate();
        this.dayTime = dateTime.getDayTime();
    }

    public static boolean between(DateTime sourcedate, int smallDate, int bigDate) {
//		DateTime d= new DateTime(MyDateFormatNew.parse("MMddHHmmss", localDate + localTime));
        DateTime tenDayBefore = DateTime.toDateTime(DateTime.now().getTime() - smallDate * DateTime.ONE_DAY_MILLIS);
        DateTime tenDayAfter = DateTime.toDateTime(DateTime.now().getTime() + bigDate * DateTime.ONE_DAY_MILLIS);
        if (tenDayBefore.getDateTimeLong() < sourcedate.getDateTimeLong() &&
                sourcedate.getDateTimeLong() < tenDayAfter.getDateTimeLong()) {
            return true;
        }

        return false;
    }

    public int compareTo(Object o) {
        DateTime dateTime = (DateTime) o;
        int dayDateCompare = dayDate.compareTo(dateTime.dayDate);
        if (dayDateCompare == 0) {
            return dayTime.compareTo(dateTime.dayTime);
        } else {
            return dayDateCompare;
        }
    }

    public boolean before(Date other) {
        return before(new DateTime(other));
    }

    public boolean after(Date other) {
        return after(new DateTime(other));
    }

    public boolean after(DateTime other) {
        if (dayDate.after(other.dayDate))
            return true;
        else if (dayDate.before(other.dayDate))
            return false;
        return dayTime.after(other.dayTime);
    }

    public boolean afterEquals(DateTime other) {
        if (dayDate.after(other.dayDate))
            return true;
        else if (dayDate.before(other.dayDate))
            return false;
        return dayTime.afterEquals(other.dayTime);
    }

    public boolean before(DateTime other) {
        if (dayDate.before(other.dayDate))
            return true;
        else if (dayDate.after(other.dayDate))
            return false;
        return dayTime.before(other.dayTime);
    }

    public boolean beforeEquals(DateTime other) {
        if (dayDate.before(other.dayDate))
            return true;
        else if (dayDate.after(other.dayDate))
            return false;
        return dayTime.beforeEquals(other.dayTime);
    }

    public static DateTime toDateTime(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        return new DateTime(new DayDate(calendar.getTime()), new DayTime(hour, minute, second));
    }

    public static DateTime now() {
        return toDateTime(System.currentTimeMillis());
    }

    public static DateTime fromNow(long milis) {
        return toDateTime(System.currentTimeMillis() + milis);
    }

    public static DateTime beforeNow(int days) {
        return toDateTime(System.currentTimeMillis() - days * ONE_DAY_MILLIS);
    }

    public static DateTime beforeFrom(DateTime baseDateTime, int days) {
        return toDateTime(baseDateTime.getTime() - days * ONE_DAY_MILLIS);
    }

    public static DateTime afterNow(int days) {
        return toDateTime(System.currentTimeMillis() + days * ONE_DAY_MILLIS);
    }

    public static DateTime afterFrom(DateTime baseDateTime, int days) {

        return toDateTime(baseDateTime.getTime() + days * ONE_DAY_MILLIS);
    }

    public DateTime nextDay() {
        return new DateTime(dayDate.nextDay(), dayTime);
    }

    public boolean equals(Date date) {
        DateTime other = new DateTime(date);
        return this.equals(other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof DateTime))
            return false;

        final DateTime dateTime = (DateTime) o;

        if (dayDate != null ? !dayDate.equals(dateTime.dayDate) : dateTime.dayDate != null)
            return false;
        if (dayTime != null ? !dayTime.equals(dateTime.dayTime) : dateTime.dayTime != null)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = (dayDate != null ? dayDate.hashCode() : 0);
        result = 29 * result + (dayTime != null ? dayTime.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return (dayDate != null ? dayDate.toString() : "-") + ", " + (dayTime != null ? dayTime.toString() : "-");
    }

    public Date toDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, getDayDate().getYear());
        calendar.set(Calendar.MONTH, getDayDate().getMonth() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, getDayDate().getDay());
        calendar.set(Calendar.HOUR_OF_DAY, getDayTime().getHour());
        calendar.set(Calendar.MINUTE, getDayTime().getMinute());
        calendar.set(Calendar.SECOND, getDayTime().getSecond());
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public long getTime() {
        return toDate().getTime();
    }

    public boolean isNull() {
        return dayDate == null || dayTime == null;
    }

    @Override
    public DateTime clone() {
        return new DateTime(dayDate.clone(), dayTime.clone());
    }

    public static boolean isNullOrUnknown(DateTime dateTime) {
        return dateTime == null || dateTime.equals(UNKNOWN) || dateTime.equals(MAX_DATE_TIME);
    }

    public long getDateTimeLong() {
        return dayDate.getDate() * 1000000L + dayTime.getDayTime();
    }
}
