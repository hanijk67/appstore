package com.fanap.midhco.ui.component.table;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;

import java.util.Iterator;
import java.util.List;

public abstract class CLSortableDataProvider extends SortableDataProvider {
    private boolean hasNext;

    public abstract List list(long first, long count);

    @Override
    public final Iterator iterator(long first, long count) {
        List list = list(first, count + 1);
        hasNext = list.size() > count;
        if (hasNext)
            return list.subList(0, (int)count).iterator();
        else
            return list.iterator();
    }

    @Override
    public final long size() {
        return Integer.MAX_VALUE;
    }

    public long count() {
        return 0;
    }

    public boolean isHasNext() {
        return hasNext;
    }
}
