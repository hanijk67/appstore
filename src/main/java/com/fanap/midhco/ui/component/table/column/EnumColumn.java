package com.fanap.midhco.ui.component.table.column;

import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import java.util.Collection;
import java.util.Map;

/**
 * Created by admin123 on 6/22/2016.
 */

public class EnumColumn extends PropertyColumn {
    private Map<?, ?> enumLiterals;
    private boolean sameValueIfNull = false;

    public EnumColumn(IModel displayModel, String propertyExpression, Map<?, ?> enumLiterals) {
        this(displayModel, null, propertyExpression, enumLiterals);
    }

    public EnumColumn(IModel displayModel, String propertyExpression, Map<?, ?> enumLiterals, boolean sameValueIfNull) {
        this(displayModel, null, propertyExpression, enumLiterals);
        this.sameValueIfNull = sameValueIfNull;
    }

    public EnumColumn(IModel displayModel, String sortProperty, String propertyExpression, Map<?, ?> enumLiterals) {
        super(displayModel, sortProperty, propertyExpression);
        this.enumLiterals = enumLiterals;
    }

    public void populateItem(Item cellItem, String componentId, IModel rowModel) {
        Object enumLiteral = PropertyResolver.getValue(getPropertyExpression(), rowModel.getObject());
        String display = getValue(enumLiteral);
        if (display == null && sameValueIfNull && enumLiteral != null)
            display = enumLiteral.toString();
        cellItem.add(new Label(componentId, display != null ? display.toString() : ""));
    }

    private String getValue(Object enumLiteral) {
        String display = null;
        if (enumLiteral != null) {
            if (enumLiteral instanceof Collection) {
                Collection col = (Collection) enumLiteral;
                StringBuilder builder = new StringBuilder();
                for (Object lit : col)
                    builder.append("[").append(enumLiterals.get(lit)).append("] ");
                display = builder.toString();
            } else if (enumLiterals.containsKey(enumLiteral))
                display = enumLiterals.get(enumLiteral).toString();
        }
        return display;
    }

    public String getEnumCaption(Object obj) {
        String res = getValue(obj);
        return res != null ? res.toString() : "";
    }
}
