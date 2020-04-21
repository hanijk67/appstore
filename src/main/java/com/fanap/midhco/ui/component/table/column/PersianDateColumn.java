package com.fanap.midhco.ui.component.table.column;

import com.fanap.midhco.appstore.applicationUtils.ModelUtil;
import com.fanap.midhco.appstore.entities.helperClasses.DateType;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

/**
 * Created by admin123 on 6/22/2016.
 */
public class PersianDateColumn extends PropertyColumn {
    public PersianDateColumn(IModel displayModel, String propertyExpression) {
        this(displayModel, null, propertyExpression);
    }

    public PersianDateColumn(IModel displayModel, String sortProperty, String propertyExpression) {
        this(displayModel, sortProperty, propertyExpression, DateType.DateTime);
    }

    public PersianDateColumn(IModel displayModel, String sortProperty, String propertyExpression, DateType dateType) {
        super(displayModel, sortProperty != null && dateType == DateType.DateTime ? String.format("%1$s.dayDate,%1$s.dayTime", sortProperty) : sortProperty, propertyExpression);
    }

    public void populateItem(Item cellItem, String componentId, IModel rowModel) {
        cellItem.add(new Label(componentId, ModelUtil.getCalendar(rowModel.getObject(), getPropertyExpression())));
    }
}

