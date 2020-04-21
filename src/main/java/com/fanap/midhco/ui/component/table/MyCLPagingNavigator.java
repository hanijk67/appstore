package com.fanap.midhco.ui.component.table;

import com.fanap.midhco.ui.BasePanel;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

public class MyCLPagingNavigator extends BasePanel {
	private MyDataTable table;
	private AjaxLink first, prev, next;
	private Label currentPage = new Label("currentPage", "");

	public MyCLPagingNavigator(String id, final MyDataTable table) {
		super(id);
		this.table = table;
		setOutputMarkupId(true);

		add(first = new AjaxLink("first") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				table.setCurrentPage(0);
				target.add(table);
				target.add(MyCLPagingNavigator.this);
			}
		});

		add(prev = new AjaxLink("prev") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				long page = table.getCurrentPage();
				table.setCurrentPage(page - 1);
				target.add(table);
				target.add(MyCLPagingNavigator.this);
			}
		});

		add(next = new AjaxLink("next") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				long page = table.getCurrentPage();
				table.setCurrentPage(page + 1);
				target.add(table);
				target.add(MyCLPagingNavigator.this);
			}
		});

		add(currentPage);
	}

	@Override
	protected void onBeforeRender() {
		CLSortableDataProvider provider = (CLSortableDataProvider) table.getDataProvider();
		if (table.getCurrentPage() == 0) {
			first.setEnabled(false);
			prev.setEnabled(false);
			first.add(new AttributeModifier("style", new Model("opacity:0.5")));
			prev.add(new AttributeModifier("style", new Model("opacity:0.5")));
		} else {
			first.setEnabled(true);
			prev.setEnabled(true);
			first.add(new AttributeModifier("style", new Model("")));
			prev.add(new AttributeModifier("style", new Model("")));
		}

		if (provider.isHasNext()) {
			next.setEnabled(true);
			next.add(new AttributeModifier("style", new Model("")));
		} else {
			next.setEnabled(false);
			next.add(new AttributeModifier("style", new Model("opacity:0.5")));
		}

		currentPage.setDefaultModel(new Model(table.getCurrentPage() + 1));

		super.onBeforeRender();
	}
}
