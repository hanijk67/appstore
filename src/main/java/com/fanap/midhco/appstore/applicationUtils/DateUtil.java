package com.fanap.midhco.appstore.applicationUtils;

import com.fanap.midhco.appstore.entities.helperClasses.DateTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by A.Moshiri on 11/11/2017.
 */
public class DateUtil {

    public static String getWeekStartDay() {
        Date currentDate = new Date();
        return getWeekStartDay(currentDate);
    }

    public static String getWeekStartDay(Date gregDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(gregDate);
        cal.setFirstDayOfWeek(7);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - cal.getFirstDayOfWeek();
        if(dayOfWeek<0)
            dayOfWeek = Calendar.SATURDAY +dayOfWeek;
        cal.add(Calendar.DAY_OF_MONTH, -dayOfWeek);

        Date weekStart = cal.getTime();
        return String.valueOf(weekStart.getTime());
    }
    public static Long getBaseTimeForInputDate(DateTime dateTime){
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateTime.toDate());
        cal.setFirstDayOfWeek(7);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Date dayDate = cal.getTime();
        return  dayDate.getTime();
    }

    public static Date localToGMT(DateTime dateTime) {
        Date date = new Date(dateTime.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date gmt = new Date(sdf.format(date));
        return gmt;
    }
}