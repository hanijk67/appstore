package com.fanap.midhco.appstore.applicationUtils;

import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.entities.helperClasses.DayDate;
import com.fanap.midhco.appstore.restControllers.vos.componentVos.CalendarType;
import org.devocative.adroit.CalendarUtil;
import org.devocative.adroit.vo.DateFieldVO;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by admin123 on 6/22/2016.
 */
public class MyCalendarUtil {

    public static int gregDaysInMonth[] = {31, 28, 31, 30, 31, 30, 31, 31, 30,
            31, 30, 31};
    public static int jalaliDaysInMonth[] = {31, 31, 31, 31, 31, 31, 30, 30, 30,
            30, 30, 29};

    public static DateTime toPersian(DateTime gregorianDateTime) {
        if (gregorianDateTime!=null) {
        Date date = gregorianDateTime.toDate();
        DateFieldVO persianDateFieldVO = CalendarUtil.toPersianDateField(date);
        int year = persianDateFieldVO.getYear();
        int month = persianDateFieldVO.getMonth();
        int day = persianDateFieldVO.getDay();
        return new DateTime(new DayDate(year, month, day), gregorianDateTime.getDayTime());
        }else {
            return null;
        }
    }

    public static DateTime toGregorian(DateTime persianDateTime) {
        DayDate persianDayDate = persianDateTime.getDayDate();
        DateFieldVO persianDateFieldVO = new DateFieldVO();

        persianDateFieldVO.setYear(persianDayDate.getYear());
        persianDateFieldVO.setMonth(persianDayDate.getMonth());
        persianDateFieldVO.setDay(persianDayDate.getDay());
        Date gregorianDate = CalendarUtil.toGregorian(persianDateFieldVO);

        return new DateTime(new DayDate(gregorianDate), persianDateTime.getDayTime());
    }

    public static DayDate toPersian(DayDate gregorianDayDate) {
        Date date = gregorianDayDate.toDate();
        DateFieldVO persianDateFieldVO = CalendarUtil.toPersianDateField(date);
        int year = persianDateFieldVO.getYear();
        int month = persianDateFieldVO.getMonth();
        int day = persianDateFieldVO.getDay();
        return new DayDate(year, month, day);
    }

    public static DayDate toGregorian(DayDate persianDayDate) {
        DateFieldVO persianDateFieldVO = new DateFieldVO();

        persianDateFieldVO.setYear(persianDayDate.getYear());
        persianDateFieldVO.setMonth(persianDayDate.getMonth());
        persianDateFieldVO.setDay(persianDayDate.getDay());
        Date gregorianDate = CalendarUtil.toGregorian(persianDateFieldVO);

        return new DayDate(gregorianDate);
    }

    public static boolean isGregorianLeapYear(int year) {
        if (year % 4 != 0) {
            return false;
        } else if (year % 400 == 0) {
            return true;
        } else if (year % 100 == 0) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isPersianLeapYear(int year) {
        int remnant = year % 33;
        if (Arrays.asList(1, 5, 9, 13, 17, 22, 26, 30).contains(remnant))
            return true;
        return false;
    }


    public static Calendar getDateByInputData(int year, int month, int day, CalendarType calendarType) {
        if (calendarType != null) {
            if (calendarType.equals(CalendarType.PERSIAN)) {
                return MyCalendarUtil.getGregorianCalendar(year, month, day);
            } else if (calendarType.equals(CalendarType.GREGORIAN)) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month - 1, day, 0, 0, 0);
                return calendar;
            }
        }
        return null;
}

    /**
     * Converts Persian date to gregorian date
     */
    public static Calendar getGregorainCalendar(int year, int month, int day, int hour, int minute, int second) {
        GregorianCalendar gregorian = (GregorianCalendar) getGregorianCalendar(year, month, day);

        gregorian.set(Calendar.HOUR_OF_DAY, hour);
        gregorian.set(Calendar.MINUTE, minute);
        gregorian.set(Calendar.SECOND, second);

        return gregorian;
    }

    public static Calendar getGregorianCalendar(int year, int month, int day) {

        int gy, gm, gd;
        int jy, jm, jd;
        long g_day_no, j_day_no;
        boolean leap;

        int i;

        jy = year - 979;
        jm = month - 1;
        jd = day - 1;

        j_day_no = 365 * jy + (jy / 33) * 8 + (jy % 33 + 3) / 4;
        for (i = 0; i < jm; ++i)
            j_day_no += jalaliDaysInMonth[i];

        j_day_no += jd;

        g_day_no = j_day_no + 79;

        gy = (int) (1600 + 400 * (g_day_no / 146097)); /*
                                                         * 146097 = 365*400 +
														 * 400/4 - 400/100 +
														 * 400/400
														 */
        g_day_no = g_day_no % 146097;

        leap = true;
        if (g_day_no >= 36525) /* 36525 = 365*100 + 100/4 */ {
            g_day_no--;
            gy += 100 * (g_day_no / 36524); /* 36524 = 365*100 + 100/4 - 100/100 */
            g_day_no = g_day_no % 36524;

            if (g_day_no >= 365)
                g_day_no++;
            else
                leap = false;
        }

        gy += 4 * (g_day_no / 1461); /* 1461 = 365*4 + 4/4 */
        g_day_no %= 1461;

        if (g_day_no >= 366) {
            leap = false;

            g_day_no--;
            gy += g_day_no / 365;
            g_day_no = g_day_no % 365;
        }

        for (i = 0; g_day_no >= gregDaysInMonth[i]
                + parsBooleanToInt(i == 1 && leap); i++)
            g_day_no -= gregDaysInMonth[i] + parsBooleanToInt(i == 1 && leap);

        gm = i + 1;
        gd = (int) (g_day_no + 1);

        GregorianCalendar gregorian = new GregorianCalendar(gy, gm - 1, gd);
        return gregorian;
    }


    private static int parsBooleanToInt(Boolean sample) {
        if (sample)
            return 1;
        else
            return 0;
    }


    public static Date getDateFromCalendar(Calendar gregorianCalendar) {
        if (gregorianCalendar == null) {
            return null;
        } else {
            return gregorianCalendar.getTime();
        }
    }

    public static String getPersianMonthName(int monthOfJalaliMonth) {
        switch (monthOfJalaliMonth) {
            case 1:
                return "فروردین";
            case 2:
                return "اردیبهشت";
            case 3:
                return "خرداد";
            case 4:
                return "تیر";
            case 5:
                return "مرداد";
            case 6:
                return "شهریور";
            case 7:
                return "مهر";
            case 8:
                return "آبان";
            case 9:
                return "آذر";
            case 10:
                return "دی";
            case 11:
                return "بهمن";
            case 12:
                return "اسفند";
        }
        return null;

    }

    public static String getGregorianMonthName(int monthOfGregorianMonth) {
        switch (monthOfGregorianMonth) {
            case 1:
                return "Jan";
            case 2:
                return "Feb";
            case 3:
                return "March";
            case 4:
                return "April";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "Agust";
            case 9:
                return "Sept";
            case 10:
                return "Oct";
            case 11:
                return "Nov";
            case 12:
                return "Des";

        }
        return null;

    }

    public static int getDaysInMonth(CalendarType calendarType, Integer currentMonthIndex) {
        int monthLength = 0;
        if (calendarType != null || currentMonthIndex == null || currentMonthIndex > 11 || currentMonthIndex < 0) {
            if (calendarType.equals(CalendarType.GREGORIAN)) {
                monthLength = MyCalendarUtil.gregDaysInMonth[currentMonthIndex];
            } else {
                monthLength = MyCalendarUtil.jalaliDaysInMonth[currentMonthIndex];
            }
        }
        return monthLength;
    }

    public static String getMonthName(CalendarType calendarType, int currentMonthIndex) {
        String monthName = "";
        if (calendarType != null) {
            if (calendarType.equals(CalendarType.GREGORIAN)) {
                monthName = MyCalendarUtil.getGregorianMonthName(currentMonthIndex);
            } else {
                monthName = MyCalendarUtil.getPersianMonthName(currentMonthIndex);
            }
        }
        return monthName;
    }

    public static Integer getMonthIndexFromDateTime(DateTime dateTime) {
        return Integer.valueOf(dateTime.toString().substring(5, 7));
    }

    public static DateTime generateGregorianDateTime(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        Date date = calendar.getTime();
        return new DateTime(date);
    }

    public static Integer getDateIndexFromDateTime(DateTime inputDateTime) {
        return Integer.valueOf(inputDateTime.toString().substring(8, 10));
    }

    public static Integer getYearIndexFromDateTime(DateTime inputDateTime) {
        return Integer.valueOf(inputDateTime.toString().substring(0, 4));
    }

    public static int getMontIndexByMonthName(CalendarType calendarType, String monthName) {
        if (calendarType != null || monthName != null && !monthName.trim().equals("")) {
            if (calendarType.equals(CalendarType.GREGORIAN)) {
                return getGregorianMonthIndexByMonthName(monthName);
            } else if (calendarType.equals(CalendarType.PERSIAN)) {
                return getJalaliMonthIndexByMonthName(monthName);
            }
        }
        return 0;
    }

    private static int getJalaliMonthIndexByMonthName(String monthName) {
        switch (monthName) {
            case "فروردین":
                return 1;
            case "اردیبهشت":
                return 2;
            case "خرداد":
                return 3;
            case "تیر":
                return 4;
            case "مرداد":
                return 5;
            case "شهریور":
                return 6;
            case "مهر":
                return 7;
            case "آبان":
                return 8;
            case "آذر":
                return 9;
            case "دی":
                return 10;
            case "بهمن":
                return 11;
            case "اسفند":
                return 12;
            default:
                return 0;
        }
    }

    private static int getGregorianMonthIndexByMonthName(String monthName) {
        switch (monthName) {
            case "Jan":
                return 1;
            case "Feb":
                return 2;
            case "March":
                return 3;
            case "April":
                return 4;
            case "May":
                return 5;
            case "June":
                return 6;
            case "July":
                return 7;
            case "Agust":
                return 8;
            case "Sept":
                return 9;
            case "Oct":
                return 10;
            case "Nov":
                return 11;
            case "Des":
                return 12;
            default:
                return 0;
        }
    }
}