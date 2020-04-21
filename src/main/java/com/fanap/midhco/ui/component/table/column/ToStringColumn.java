package com.fanap.midhco.ui.component.table.column;

import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Created by admin123 on 6/22/2016.
 */
public class ToStringColumn extends AbstractColumn {
    public ToStringColumn() {
        this(new Model());
    }

    public ToStringColumn(IModel model) {
        super(model);
    }

    public void populateItem(Item cellItem, String componentId, IModel rowModel) {
        String str = "";
        if (rowModel.getObject() != null)
            str = rowModel.getObject().toString();
        cellItem.add(new Label(componentId, str));
    }
}
