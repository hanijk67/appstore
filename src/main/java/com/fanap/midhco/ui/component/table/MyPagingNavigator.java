package com.fanap.midhco.ui.component.table;

import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.markup.html.navigation.paging.IPageable;

public class MyPagingNavigator extends AjaxPagingNavigator {
	public MyPagingNavigator(final String id, final IPageable pageable) {
		super(id, pageable);
	}
}
