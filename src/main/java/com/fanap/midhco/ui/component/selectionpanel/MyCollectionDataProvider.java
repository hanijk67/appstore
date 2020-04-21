package com.fanap.midhco.ui.component.selectionpanel;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

public class MyCollectionDataProvider implements IDataProvider {
    private Collection collection;

    public MyCollectionDataProvider(Collection collection) {
        this.collection = collection;
    }

    public Iterator iterator(long first, long count) {
        return collection.iterator();
    }

    public long size() {
        return 1000;
    }

    public IModel model(Object object) {
        return new Model((Serializable) object);
    }

    public void detach() {
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }
}
