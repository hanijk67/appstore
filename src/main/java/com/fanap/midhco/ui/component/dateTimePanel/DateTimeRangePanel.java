package com.fanap.midhco.ui.component.dateTimePanel;

import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.entities.helperClasses.DateType;
import com.fanap.midhco.appstore.entities.helperClasses.DayDate;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

/**
 * Created by admin123 on 2/6/15.
 */
public class DateTimeRangePanel extends FormComponentPanel {
    DateTimePanel fromDatePanel;
    DateTimePanel toDatePanel;
    DateType dateType;

    public DateTimeRangePanel(String id, final DateType dateType) {
        super(id);
        this.dateType = dateType;
        fromDatePanel = new DateTimePanel("fromDate", dateType);
        fromDatePanel.setModel(new Model());
        toDatePanel = new DateTimePanel("toDate", dateType);
        toDatePanel.setModel(new Model());
        add(fromDatePanel);
        add(toDatePanel);

        add(new IValidator() {
                @Override
                public void validate(IValidatable iValidatable) {
                    if (dateType.equals(DateType.Date)) {
                        DayDate fromDayDate = (DayDate) fromDatePanel.getConvertedInput();
                        DayDate toDayDate = (DayDate) toDatePanel.getConvertedInput();
                        if (!fromDayDate.equals(DayDate.UNKNOWN) && !toDayDate.equals(DayDate.UNKNOWN)) {
                            if (toDayDate.before(fromDayDate)) {
                                ValidationError error = new ValidationError();
                                error.addKey("error.range");
                                iValidatable.error(error);
                            }
                        }
                    } else if (dateType.equals(DateType.DateTime)) {
                        DateTime fromDateTime = (DateTime) fromDatePanel.getConvertedInput();
                        DateTime toDateTime = (DateTime) toDatePanel.getConvertedInput();
                        if (!fromDateTime.equals(DateTime.UNKNOWN) &&
                                !toDateTime.equals(DateTime.UNKNOWN)) {
                            if (toDateTime.before(fromDateTime)) {
                                ValidationError error = new ValidationError();
                                error.addKey("error.range");
                                iValidatable.error(error);
                            }
                        }
                    }
                }
            });
    }

    @Override
    public void convertInput() {
        if(dateType.equals(DateType.Date))
            setConvertedInput(new DayDate[]{(DayDate)fromDatePanel.getConvertedInput(), (DayDate)toDatePanel.getConvertedInput()});
        if(dateType.equals(DateType.DateTime))
            setConvertedInput(new DateTime[]{(DateTime)fromDatePanel.getConvertedInput(), (DateTime)toDatePanel.getConvertedInput()});
    }

    @Override
    protected void onBeforeRender() {
        Object input = getModelObject();
        if (input == null) {
            fromDatePanel.setModel(new Model());
            toDatePanel.setModel(new Model());
        } else {
            if (input instanceof DayDate[] && ((DayDate[]) input).length == 2) {
                fromDatePanel.setModel(new Model(((DayDate[]) input)[0]));
                toDatePanel.setModel(new Model(((DayDate[]) input)[1]));
            } else if (input instanceof DateTime[] && ((DateTime[]) input).length == 2) {
                fromDatePanel.setModel(new Model(((DateTime[]) input)[0]));
                toDatePanel.setModel(new Model(((DateTime[]) input)[1]));
            }
        }

        super.onBeforeRender();
    }
}
