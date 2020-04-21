package com.fanap.midhco.appstore.applicationUtils;

import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.entities.helperClasses.DayDate;
import com.fanap.midhco.appstore.entities.helperClasses.DayTime;
import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.Collection;
import java.util.Map;

/**
 * Created by admin123 on 6/22/2016.
 */
public class ModelUtil {
    public static final Format DEFAULT = new DecimalFormat();

    public static IModel getProperty(Object obj, String property) {
        return getProperty(obj, property, null);
    }

    public static IModel getProperty(Object obj, String property, Format format) {
        Model model = new Model();
        if (obj != null) {
            Object prop = PropertyResolver.getValue(property, obj);
            if (prop != null) {
                if (format != null && prop instanceof Number)
                    model.setObject(format.format(prop));
                else if (prop instanceof Collection) {
                    Collection col = (Collection) prop;
                    StringBuilder builder = new StringBuilder();
                    if (col.size() > 0) {
                        builder.append("<ul>");
                        for (Object item : col)
                            builder.append("<li>").append(item).append("</li>");
                        builder.append("</ul>");
                    }
                    model.setObject(builder.toString());
                } else
                    model.setObject((Serializable)prop);
            }
        }
        return model;
    }

    public static IModel getLiteralCaption(Object obj, String prop, Map<Object, String> captions) {
        Model model = new Model("-");
        if (obj != null) {
            Object literal = PropertyResolver.getValue(prop, obj);
            if (literal != null) {
                if (literal instanceof Collection) {
                    Collection col = (Collection) literal;
                    StringBuilder builder = new StringBuilder();
                    for (Object lit : col)
                        builder.append("[").append(captions.get(lit)).append("] ");
                    model.setObject(builder.toString());
                } else
                    model.setObject(captions.get(literal));
            }
        }
        return model;
    }

    public static IModel getCalendar(Object obj, String property) {
        String result = "";
        if (obj != null) {
            Object value = PropertyResolver.getValue(property, obj);
            if (value != null) {
                if (value instanceof DateTime) {
                    DateTime grDateTime = (DateTime) value;
                    if (!grDateTime.equals(DateTime.UNKNOWN) && !grDateTime.equals(DateTime.MAX_DATE_TIME)) {
                        DateTime persianDateTime = MyCalendarUtil.toPersian(grDateTime);
                        result = String.format("%s %s", persianDateTime.getDayTime(), persianDateTime.getDayDate());
                    }
                } else if (value instanceof DayDate) {
                    DayDate grDayDate = (DayDate) value;
                    if (!grDayDate.equals(DayDate.UNKNOWN) && !grDayDate.equals(DayDate.MAX_DAY_DATE))
                        result = MyCalendarUtil.toPersian(grDayDate).toString();
                } else if (value instanceof DayTime)
                    result = value.toString();
            }
        }
        return new Model(result);
    }
}
