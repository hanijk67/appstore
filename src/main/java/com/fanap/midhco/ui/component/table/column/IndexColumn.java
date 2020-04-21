package com.fanap.midhco.ui.component.table.column;

import com.fanap.midhco.ui.component.table.MyItem;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Created by admin123 on 6/22/2016.
 */
public class IndexColumn extends AbstractColumn {
    public IndexColumn() {
        super(new Model(""));
    }

    public void populateItem(Item cellItem, String componentId, IModel rowModel) {
        MyItem parentItem = (MyItem) cellItem.findParent(Item.class);
        long page = parentItem.getTable().getCurrentPage();
        long rowsPerPage = parentItem.getTable().getItemsPerPage();
        long indx = page * rowsPerPage + parentItem.getIndex() + 1;
        cellItem.add(new Label(componentId, String.valueOf(indx)));
        cellItem.add(new AttributeAppender("class", new Model("indx"), " "));
    }
}