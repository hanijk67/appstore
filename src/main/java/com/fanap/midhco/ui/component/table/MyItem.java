package com.fanap.midhco.ui.component.table;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

public class MyItem extends Item {
    private static final String CLASS_EVEN = "even";
    private static final String CLASS_ODD = "odd";
    private static final String CLASS_EVEN_SELECTABLE = "evenSelectable";
    private static final String CLASS_ODD_SELECTABLE = "oddSelectable";
    private static final String CLASS_SELECTED = "selected";

    private MyDataTable table;
    private boolean selected = false;
    private boolean selectable = false;

    public MyItem(String id, int index, IModel model, MyDataTable table) {
        this(id, index, model, table, false);
    }

    public MyItem(String id, int index, IModel model, MyDataTable table, boolean selectable) {
        super(id, index, model);
        this.table = table;
        this.selectable = selectable;
        setOutputMarkupId(true);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public MyDataTable getTable() {
        return table;
    }

    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        if (selected)
            tag.put("class", CLASS_SELECTED);
        else if (selectable)
            tag.put("class", (getIndex() % 2 == 0) ? CLASS_EVEN_SELECTABLE : CLASS_ODD_SELECTABLE);
        else
            tag.put("class", (getIndex() % 2 == 0) ? CLASS_EVEN : CLASS_ODD);
    }

    public void selectItem(AjaxRequestTarget target) {
        selected = true;
        MyItem selectedItem = table.getSelectedItem();
        if (selectedItem != null && !selectedItem.equals(this)) {
            selectedItem.selected = false;
            target.add(selectedItem);
        }
        table.setSelectedItem(this);
        target.add(this);
    }
}
