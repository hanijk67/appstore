package com.fanap.midhco.ui.component;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

/**
 * Created by admin123 on 6/22/2016.
 */
public interface HasLabel {
    public IModel getLabel();

    public Component setLabel(IModel model);
}
