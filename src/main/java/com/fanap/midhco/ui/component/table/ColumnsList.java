package com.fanap.midhco.ui.component.table;

import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.model.IModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColumnsList implements Serializable {
	private List<IColumn> columns = new ArrayList<IColumn>();

	private List<Object> list = new ArrayList<Object>();

	public void add(IColumn column) {
		columns.add(column);
		list.add(column);
	}

	public void addGroup(IModel groupName, IColumn... columns) {
		List<IColumn> columns_ = Arrays.asList(columns);
		this.columns.addAll(columns_);
		list.add(new ColumnsGroup(groupName, columns_));
	}

	public List<IColumn> getColumns() {
		return columns;
	}

	public List<Object> getList() {
		return list;
	}

	public static class ColumnsGroup implements Serializable {
		private IModel groupName;
		private List<IColumn> columns;

		public ColumnsGroup(IModel groupName, List<IColumn> columns) {
			this.groupName = groupName;
			this.columns = columns;
		}

		public IModel getGroupName() {
			return groupName;
		}

		public List<IColumn> getColumns() {
			return columns;
		}
	}
}
