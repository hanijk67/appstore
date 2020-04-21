package com.fanap.midhco.ui.component.dateTimePanel;

import com.fanap.midhco.appstore.applicationUtils.MyCalendarUtil;
import com.fanap.midhco.appstore.entities.helperClasses.*;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

/**
 * Created by admin123 on 2/6/15.
 */
public class DateTimePanel extends FormComponentPanel {
    TextField dateField;
    TextField timeField;
    DateType dateType;
    HourMeridianType hourMeridianType = HourMeridianType._12HOUR;
    String AM = "AM";
    String PM = "PM";

    public DateTimePanel(String id, DateType dateType) {
        this(id, dateType, HourMeridianType._12HOUR);
    }

    public DateTimePanel(String id, final DateType dateType, HourMeridianType hourMeridianType) {
        super(id);

        this.hourMeridianType = hourMeridianType;

        dateField = new TextField("date");
        dateField.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        dateField.setModel(new Model());
        add(dateField);

        WebMarkupContainer timeIcon = new WebMarkupContainer("timeIcon");
        add(timeIcon);

        timeField = new TextField("time");
        timeField.setModel(new Model());
        timeField.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(timeField);

        this.dateType = dateType;
        if (dateType.equals(DateType.Date)) {
            timeField.setVisible(false);
            timeIcon.setVisible(false);
        }

        dateField.add(new AttributeModifier("onkeydown",
                new Model("assignToday2(event,'" + dateField.getMarkupId() + "','" + timeField.getMarkupId() + "')")));

        timeField.add(new AttributeModifier("onkeydown",
                new Model("assignToday2(event,'" + dateField.getMarkupId() + "','" + timeField.getMarkupId() + "')")));

        if(hourMeridianType.equals(HourMeridianType._12HOUR))
            timeField.add(new AttributeModifier("meridianType", "_12Hour"));
        else
            timeField.add(new AttributeModifier("meridianType", "_24Hour"));

    }

    @Override
    public void convertInput() {
        try {
            DayDate dayDate = getDate();
            if (dateType.equals(DateType.Date)) {
                setConvertedInput(dayDate);
                setModelObject(dayDate);
                return;
            } else if (dateType.equals(DateType.DateTime)) {
                setConvertedInput(getDateTime());
                setModelObject(getDateTime());
                return;
            }
        } catch (Exception ex) {
        }
        setConvertedInput(null);
    }

    public DayTime getTime() {
        String timeInput = (String) timeField.getConvertedInput();
        String timePart = null;
        String meridianPart = null;

        if (timeInput != null && !timeInput.trim().isEmpty()) {
            try {
                if(hourMeridianType.equals(HourMeridianType._12HOUR)) {
                    String[] splitted = timeInput.trim().split("\\s+");
                    timePart = splitted[0].trim();
                    meridianPart = splitted[1].trim();
                } else if(hourMeridianType.equals(HourMeridianType._24HOUR)) {
                    timePart = timeInput.trim();
                }

                String[] splitted = timePart.split(":");
                int hour = Integer.parseInt(splitted[0]);
                int min = Integer.parseInt(splitted[1]);
                int sec = Integer.parseInt(splitted[2]);

                if(meridianPart != null && meridianPart.equals(PM))
                    hour = 12 + hour;

                return new DayTime(hour, min, sec);
            } catch (Exception ex) {
                return DayTime.UNKNOWN;
            }
        } else
            return DayTime.MIN_DAY_TIME;
    }

    public DateTime getDateTime() {
        try {
            DayDate dayDate = getDate();
            if (!DayDate.isNullOrUnknown(dayDate))
                return new DateTime(dayDate, getTime());
        } catch (Exception ex) {
        }
        return DateTime.UNKNOWN;
    }

    public DayDate getDate() {
        String dateInput = (String) dateField.getConvertedInput();
        if (dateInput != null && !dateInput.isEmpty()) {
            try {
                String[] splitted = dateInput.split("/");
                int year = Integer.parseInt(splitted[0]);
                int month = Integer.parseInt(splitted[1]);
                int day = Integer.parseInt(splitted[2]);
                DayDate persianDayDate = new DayDate(year, month, day);
                return MyCalendarUtil.toGregorian(persianDayDate);
            } catch (Exception ex) {
                return DayDate.UNKNOWN;
            }
        } else {
            return DayDate.MIN_DAY_DATE;
        }
    }

    @Override
    public boolean checkRequired() {
        if (isRequired()) {
            if (dateType.equals(DateType.DateTime))
                return !DateTime.isNullOrUnknown(getDateTime());
            if (dateType.equals(DateType.Date))
                return !DayDate.isNullOrUnknown(getDate());
            return !DayTime.isNullOrUnknown(getTime());
        }
        return true;
    }

    @Override
    public void renderHead(IHeaderResponse response) {

        response.render(OnDomReadyHeaderItem.forScript("$('#" + dateField.getMarkupId() + "')" +
                ".datepicker({isRTL: true,dateFormat: 'yy/mm/dd',zIndexIncrease:1050});"));

        response.render(OnDomReadyHeaderItem.forScript("$('#" + timeField.getMarkupId() + "')" +
                ".timepicker({minuteStep: 1,\n" +
                "                showInputs: true,\n" +
                "                disableFocus: true, " +
                "showMeridian :  " + (hourMeridianType.equals(HourMeridianType._12HOUR) ? "true" : "false") + "," +
                "showSeconds : true,showInputs:false, defaultTime :false});"));
    }

    @Override
    protected void onBeforeRender() {
        if(dateType.equals(DateType.Date)) {
            DayDate dateStruct = (DayDate) getModelObject();
            if(dateStruct != null && !dateStruct.equals(DayDate.MIN_DAY_DATE)) {
                dateField.setModel(new Model(dateStruct.toString()));
            } else {
                dateField.setModel(new Model());
            }
        }

        if(dateType.equals(DateType.DateTime)) {
            DateTime dateTimeStruct = (DateTime) getModelObject();
            if(dateTimeStruct != null && !dateTimeStruct.equals(DateTime.MIN_DATE_TIME)) {
                dateField.setModel(new Model(dateTimeStruct.getDayDate().toString()));
                timeField.setModel(new Model(dateTimeStruct.getDayTime().toString()));
            } else {
                dateField.setModel(new Model());
                timeField.setModel(new Model());
            }
        }

        super.onBeforeRender();
    }
}
