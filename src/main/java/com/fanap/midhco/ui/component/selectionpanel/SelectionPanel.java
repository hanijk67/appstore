package com.fanap.midhco.ui.component.selectionpanel;

import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.component.ISelectable;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.ajaxLinkPanel.AjaxLinkPanel;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.component.table.MyDataTable;
import com.fanap.midhco.ui.component.table.column.IndexColumn;
import com.fanap.midhco.ui.component.table.column.ToStringColumn;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public abstract class SelectionPanel extends FormComponentPanel<Object> implements IParentListner {
    private Collection<Object> objectsList = new HashSet<Object>();
    private ISelectable selectable;
    private BootStrapModal modalWindow = new BootStrapModal("modal");
    private MyDataTable selection;
    private SelectionMode selectionMode;
    private WebMarkupContainer holder = new WebMarkupContainer("holder");
    private Label head;
    private WebMarkupContainer opener;
    private Serializable criteria;
    private List<IColumn> columns;
    private IParentListner quickFindListener;
    private IParentListner selectListener;

    public SelectionPanel(String id) {
        this(id, (String) null);
    }

    public SelectionPanel(String id, String windowCaption) {
        this(id, SelectionMode.Single, windowCaption);
    }

    public SelectionPanel(String id, final SelectionMode selectionMode) {
        this(id, selectionMode, null);
    }

    public SelectionPanel(String id, final SelectionMode selectionMode, final String windowCaption) {
        this(id, null, selectionMode, windowCaption);
    }

    public SelectionPanel(String id, IModel model, final SelectionMode selectionMode) {
        this(id, model, selectionMode, null);
    }

    // Main Constructor
    public SelectionPanel(String id, IModel model, final SelectionMode selectionMode, final String windowCaption) {
        super(id, model);
        this.selectionMode = selectionMode;
        add(modalWindow);

        createColumns();
        selection = new MyDataTable("selection", columns, new MyCollectionDataProvider(objectsList), 1000, false);
        selection.setOutputMarkupId(true);
        holder.add(selection);
        holder.setOutputMarkupId(true);
        add(holder.setOutputMarkupId(true));

        add(new AjaxLink<Object>("open") {
            public void onClick(AjaxRequestTarget target) {
                String caption = windowCaption;
                selectable = getSelectable(modalWindow.getContentId());
                if (selectable instanceof BasePanel)
                    ((BasePanel) selectable).setParentListner(SelectionPanel.this);
                else if (selectable instanceof BasePanel) {
                    BasePanel basePanel = (BasePanel) selectable;
                    basePanel.setParentListner(SelectionPanel.this);
                    if (caption == null)
                        caption = basePanel.getPageTitle();
                }

                modalWindow.setContent((Component) selectable);
                modalWindow.setTitle(caption != null ? caption : getLocalizer().getString("label.select", this));
                modalWindow.show(target);

            }
        });
        add(new AjaxLink<Object>("clearAll") {
            public void onClick(AjaxRequestTarget target) {
                objectsList.clear();
                updateSelection(target);
            }
        }.setVisible(selectionMode == SelectionMode.Multiple || selectionMode == SelectionMode.MultipleOrQuery));
        final WebComponent quickFind = new WebComponent("quickFind");
        quickFind.setVisible(false);
        quickFind.add(new AbstractDefaultAjaxBehavior() {
            protected void respond(AjaxRequestTarget target) {
                IRequestParameters requestParameters = getRequest().getRequestParameters();
                String _id = requestParameters.getParameterValue("id").toString();
                if (_id != null && _id.length() > 0) {
                    List list = quickFind(_id);
                    if (list != null && list.size() > 0) {
                        if (selectionMode == SelectionMode.Multiple || selectionMode == SelectionMode.MultipleOrQuery) {
                            objectsList.addAll(list);
                            updateSelection(target);
                            onAfterSelect(target);
                            if (quickFindListener != null)
                                quickFindListener.onChildFinished(target, null, quickFind);
                        } else {
                            if (list.size() == 1) {
                                objectsList.clear();
                                objectsList.add(list.get(0));
                                Object obj = list.get(0);
                                boolean isSelectable = true;

                                if (isSelectable) {
                                    updateSelection(target);
                                    onAfterSelect(target);
                                    if (quickFindListener != null)
                                        quickFindListener.onChildFinished(target, new Model((Serializable) getDefaultModelObject()), quickFind);
                                }
                            } else {
                                String message = getString("error.quickFind.multiple");
                                target.appendJavaScript(String.format("showMessage('%s');", message));
                            }
                        }
                    } else {
                        String message = "error.quickFind.notFount";
                        target.appendJavaScript(String.format("showMessage('%s');", message));
                    }
                }
            }

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                Component cmp = getComponent();
                String onclick = String.format("quickFind('%s')", getCallbackUrl());
                if (cmp.isEnabled() && cmp.isEnableAllowed())
                    tag.put("onclick", onclick);
            }
        });
        add(quickFind);

        opener = new WebMarkupContainer("opener");
        head = new Label("head", new Model());
        opener.add(head.setOutputMarkupId(true));
        add(opener.setOutputMarkupId(true));

        add(new IValidator() {
            @Override
            public void validate(IValidatable iValidatable) {
                if ((SelectionPanel.this.selectionMode == SelectionMode.Multiple) && objectsList.size() > 500) {
                    ValidationError validationError = new ValidationError();
                    validationError.addKey("error.selection.outOfLimit");
                    error(validationError);
                }
            }
        });
    }


    public abstract ISelectable getSelectable(String panelId);


    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component childComponent) {
        String childName = "";
        if (childComponent != null)
            childName = childComponent.getId();
        if (!childName.equals("searchSelect")) {
            Collection<Object> result = selectable.getSelection();
            if (result != null) {
                if (selectionMode == SelectionMode.Single)
                    objectsList.clear();
                else if (result.size() > 500) {
                    String message = "error.selection.outOfLimit2";
                    target.appendJavaScript(String.format("showMessage('%s');", message));
                    return;
                }
                for (Object obj : result) {
                    if (!objectsList.contains(obj))
                        objectsList.add(obj);
                }
            }
        } else {
            objectsList.clear();
        }
        updateSelection(target);

//        modalWindow.remove((Component) selectable);
        modalWindow.close(target);
        target.add(modalWindow);

        onAfterSelect(target);
    }


    protected void onAfterSelect(AjaxRequestTarget target) {
    }

    protected void onAfterRemove(AjaxRequestTarget target) {
    }

    public Object getSelection() {
        Object result = null;
        switch (selectionMode) {
            case Single:
                if (objectsList.size() == 1)
                    result = objectsList.iterator().next();
                break;
            case Multiple:
                if (objectsList.size() > 0) {
                    if(objectsList instanceof ArrayList)
                        result = new ArrayList<Object>(objectsList);
                    else if(objectsList instanceof HashSet)
                        result = new HashSet<Object>(objectsList);
                }
                break;
            case MultipleOrQuery:
                if (objectsList.size() > 0)
                    result = new SelectionPanelModel(objectsList);
                break;
        }
        return result;
    }

    @Override
    protected void onBeforeRender() {
        Object model = getDefaultModelObject();
        if (model == null || selectionMode == SelectionMode.Single) {
            objectsList.clear();
        }
        if (model != null) {
            switch (selectionMode) {
                case Multiple:
                    objectsList.addAll((Collection) model);
                    break;

                case MultipleOrQuery:
                    objectsList.add(((SelectionPanelModel) model).getRecords());
                    break;

                default:
                    objectsList.add(model);
                    break;
            }
        }
        criteria = null;
        super.onBeforeRender();

        opener.add(new AttributeModifier("onclick", new Model(String.format("showHide(event, '%s');", holder.getMarkupId()))));

        if (selectionMode == SelectionMode.Single)
            opener.setVisible(false);
        else if (selectionMode == SelectionMode.MultipleOrQuery || selectionMode == SelectionMode.Multiple) {
            holder.add(new AttributeModifier("class", new Model("dropDownDiv")));
            if (objectsList.size() == 0) {
                head.setDefaultModel(new Model("-"));
                opener.add(new AttributeModifier("style", new Model("background-color:#cccccc;")));
            } else {
                head.setDefaultModel(new Model(String.format("%s %s", objectsList.size(), getLocalizer().getString("label.selected", this))));
                opener.add(new AttributeModifier("style", new Model("background-color:white;")));
            }
        }

    }

    @Override
    public void convertInput() {
        setConvertedInput(getSelection());
    }

    @Override
    public void updateModel() {
        if (selectionMode == SelectionMode.Single) {
            if (objectsList.size() == 1) {
                updateModelObject(objectsList.iterator().next());
            } else
                updateModelObject(null);
        } else {
            if (selectionMode == SelectionMode.Multiple)
                updateModelObject(new HashSet<Object>(objectsList));
            else if (selectionMode == SelectionMode.MultipleOrQuery) {
                SelectionPanelModel selectedObj = null;
                if (objectsList.size() > 0)
                    selectedObj = new SelectionPanelModel(objectsList);
                updateModelObject(selectedObj);
            }
        }
    }

    private void updateModelObject(Object obj) {
        modelChanging();
        getModel().setObject(obj);
        modelChanged();
    }

    private void updateSelection(AjaxRequestTarget target) {
        updateModel();
        target.add(selection);
        if (selectionMode == SelectionMode.Multiple || selectionMode == SelectionMode.MultipleOrQuery) {
            if (objectsList.size() == 0) {
                head.setDefaultModel(new Model("-"));
                opener.add(new AttributeModifier("style", new Model("background-color:#cccccc;")));
            } else if (objectsList.size() != 0) {
                head.setDefaultModel(new Model(String.format("%s %s", objectsList.size(), getLocalizer().getString("label.selected", this))));
                opener.add(new AttributeModifier("style", new Model("background-color:white;")));
            }
            target.add(opener);
            target.add(head);
        }

        if (selectListener != null) {
            selectListener.onChildFinished(target, new Model((Serializable) this.getModelObject()), null);
        }
    }

    protected List quickFind(String str) {
        throw new RuntimeException("Not Implemented!");
    }

    public void createColumns() {
        columns = new ArrayList<IColumn>();
        if (selectionMode == SelectionMode.Multiple || selectionMode == SelectionMode.MultipleOrQuery)
            columns.add(new IndexColumn());
        columns.add(new ToStringColumn(new ResourceModel("label.title")));
        columns.add(new AbstractColumn(new Model("")) {
            public void populateItem(Item cellItem, String componentId, final IModel rowModel) {
                cellItem.add(new AjaxLinkPanel(componentId, AjaxLinkPanel.Image.Delete) {
                    public void onClick(AjaxRequestTarget target) {
                        objectsList.remove(rowModel.getObject());
                        updateSelection(target);
                        SelectionPanel.this.onAfterRemove(target);
                    }
                });
            }
        });
    }

    public Serializable getCriteria() {
        return criteria;
    }

    public void disable() {
        get("open").setVisible(false);
        get("quickFind").setVisible(false);
        get("clearAll").setVisible(false);

        selection.getColumns().remove(2);
        selection.getColumns().add(2, new AbstractColumn(new Model("")) {
            @Override
            public void populateItem(Item item, String s, IModel iModel) {
                item.add(new Label(s, ""));
            }
        });
    }

    public void enable() {
        get("open").setVisible(true);
        get("quickFind").setVisible(true);
    }


    public void setSelectable(ISelectable selectable) {
        this.selectable = selectable;
    }

    public SelectionPanel setQuickFindListener(IParentListner quickFindListener) {
        this.quickFindListener = quickFindListener;
        return this;
    }

    public void setSelectListener(IParentListner selectListener) {
        this.selectListener = selectListener;
    }
}
