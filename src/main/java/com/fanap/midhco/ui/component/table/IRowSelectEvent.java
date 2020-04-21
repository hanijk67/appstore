package com.fanap.midhco.ui.component.table;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.io.IClusterable;

public interface IRowSelectEvent extends IClusterable {
    public void onClick(AjaxRequestTarget target, IModel rowModel, MyItem cellItem);
}
