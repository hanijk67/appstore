package com.fanap.midhco.ui.component.table;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

public class MyAjaxDataTable extends MyDataTable {

    public MyAjaxDataTable(String id, List<IColumn> columns, final List list, int rowsPerPage) {
        this(id, columns, new IDataProvider() {

            public Iterator iterator(long first, long count) {
                long toIndex = first + count;
                if (toIndex > list.size())
                    toIndex = list.size();
                return list.subList((int)first, (int)toIndex).listIterator();
            }

            public long size() {
                return list.size();
            }

            public IModel model(Object object) {
                return new Model((Serializable) object);
            }

            public void detach() {
            }
        }, rowsPerPage);
    }

    public MyAjaxDataTable(String id, List<IColumn> columns, IDataProvider dataProvider, int rowsPerPage) {
        this(id, columns, dataProvider, rowsPerPage, false);
    }

    // Main Constructor 1
    public MyAjaxDataTable(String id, List<IColumn> columns, IDataProvider dataProvider, int rowsPerPage, boolean countLess) {
        super(id, columns, dataProvider, rowsPerPage, false);
        addTopToolbar(new MyAjaxNavigationToolbar(this, countLess, 1000));
        addTopToolbar(new SimpleHeader(this, columns));
        if (!countLess)
            addBottomToolbar(new NoRecordsToolbar(this));
    }

    public MyAjaxDataTable(String id, List<IColumn> columns, ISortableDataProvider dataProvider, int rowsPerPage) {
        this(id, columns, dataProvider, rowsPerPage, false);
    }

    // Main Constructor 2
    public MyAjaxDataTable(String id, List<IColumn> columns, ISortableDataProvider dataProvider, int rowsPerPage, boolean countLess) {
        super(id, columns, dataProvider, rowsPerPage, false);
        addTopToolbar(new MyAjaxNavigationToolbar(this, countLess, 1000));
        addTopToolbar(new AjaxFallbackHeadersToolbar(this, dataProvider) {
            @Override
            public boolean isVisible() {
                return MyAjaxDataTable.this.getPageCount() > 0;
            }
        });

        if (!countLess)
            addBottomToolbar(new NoRecordsToolbar(this));
    }

    // Main Constructor 3
    public MyAjaxDataTable(String id, List<IColumn> columns, ISortableDataProvider dataProvider, int rowsPerPage, boolean countLess, int maxRows) {
        super(id, columns, dataProvider, rowsPerPage, false);
        addTopToolbar(new MyAjaxNavigationToolbar(this, countLess, maxRows));
        addTopToolbar(new AjaxFallbackHeadersToolbar(this, dataProvider) {
            @Override
            public boolean isVisible() {
                return MyAjaxDataTable.this.getPageCount() > 0;
            }
        });

        if (!countLess)
            addBottomToolbar(new NoRecordsToolbar(this));
    }

    public void _999Called() {
    }
}
