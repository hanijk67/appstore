package com.fanap.midhco.ui.component.selectionpanel;

import com.fanap.midhco.appstore.service.HQLBuilder;

import java.io.Serializable;
import java.util.Collection;

public class SelectionPanelModel<T> implements Serializable {
	private Collection<T> records;
	private HQLBuilder criteria;

	public SelectionPanelModel(Collection<T> objectsList) {
		this.records = objectsList;
	}


	public SelectionPanelModel(HQLBuilder queryBuilder) {

		this.criteria = queryBuilder;
	}

	public SelectionPanelModel(Collection<T> records, HQLBuilder queryBuilder) {
		this.records = records;
		this.criteria = queryBuilder;
	}

	public boolean hasRecords() {
		return records != null && records.size() > 0;
	}

	public boolean hasCriteria() {
		return criteria != null;
	}

	public Collection<T> getRecords() {
		return records;
	}

	public void setRecords(Collection<T> records) {
		this.records = records;
	}

	public HQLBuilder getCriteria() {
		return criteria;
	}

	public void setCriteria(HQLBuilder criteria) {
		this.criteria = criteria;
	}
}
