package com.fanap.midhco.ui.component.table.column;

import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.model.IModel;

/**
 * Created by admin123 on 6/22/2016.
 */
public abstract class MyAbstractColumnWithDataTag extends AbstractColumn {

    public MyAbstractColumnWithDataTag(IModel displayModel, String sortProperty) {
        super(displayModel, sortProperty);
    }

    public MyAbstractColumnWithDataTag(IModel displayModel) {
        super(displayModel);
    }

    public abstract Object getDataTag(Object o) ;
}
