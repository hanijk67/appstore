package com.fanap.midhco.ui.component.table;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;

import java.util.List;

public class SimpleHeader extends AbstractToolbar {
	public SimpleHeader(DataTable table, ColumnsList columnsList) {
		super(table);
		RepeatingView headersTop = new RepeatingView("headersTop");
		add(headersTop);

		RepeatingView headersDown = new RepeatingView("headersDown");
		add(headersDown);

		for (Object o : columnsList.getList()) {
			if (o instanceof IColumn) {
				IColumn column = (IColumn) o;
				WebMarkupContainer item = new WebMarkupContainer(headersTop.newChildId());
				item.setRenderBodyOnly(true);
				headersTop.add(item);
				WebMarkupContainer td = new WebMarkupContainer("headerTop");
				item.add(td);
				td.add(column.getHeader("labelTop"));
				td.add(new AttributeAppender("rowspan", new Model(2), " "));
			}
			else {
				ColumnsList.ColumnsGroup group = (ColumnsList.ColumnsGroup) o;
				WebMarkupContainer topItem = new WebMarkupContainer(headersTop.newChildId());
				topItem.setRenderBodyOnly(true);
				headersTop.add(topItem);
				WebMarkupContainer topTd = new WebMarkupContainer("headerTop");
				topTd.add(new AttributeAppender("colspan", new Model(group.getColumns().size()), " "));
				topTd.add(new AttributeAppender("class", new Model("group"), " "));
				topTd.add(new Label("labelTop", group.getGroupName()));
				topItem.add(topTd);

				for (IColumn column : group.getColumns()) {
					WebMarkupContainer downItem = new WebMarkupContainer(headersDown.newChildId());
					downItem.setRenderBodyOnly(true);
					headersDown.add(downItem);
					WebMarkupContainer downCell = new WebMarkupContainer("headerDown");
					downCell.add(column.getHeader("labelDown"));
					downItem.add(downCell);
				}
			}
		}
	}

	public SimpleHeader(DataTable table, List<IColumn> columns) {
		super(table);
		RepeatingView headersTop = new RepeatingView("headersTop");
		add(headersTop);

		RepeatingView headersDown = new RepeatingView("headersDown");
		add(headersDown);
		for (IColumn column : columns) {
			WebMarkupContainer item = new WebMarkupContainer(headersTop.newChildId());
			item.setRenderBodyOnly(true);
			headersTop.add(item);
			WebMarkupContainer td = new WebMarkupContainer("headerTop");
			item.add(td);
			td.add(column.getHeader("labelTop"));
//			td.add(new AttributeAppender("rowspan", new Model(2), " "));
		}
	}
}
