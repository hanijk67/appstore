package com.fanap.midhco.ui;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import java.io.Serializable;

public interface IParentListner extends Serializable {
	public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp);
}
