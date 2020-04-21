package com.fanap.midhco.ui.pages.os;

import com.fanap.midhco.appstore.entities.OSType;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.osType.OSTypeService;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.access.WebAction;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.ajaxLinkPanel.AjaxLinkPanel;
import com.fanap.midhco.ui.component.limitedTextField.LimitedTextField;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.component.switchbox.SwitchBox;
import com.fanap.midhco.ui.component.table.MyAjaxDataTable;
import com.fanap.midhco.ui.component.table.column.IndexColumn;
import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by admin123 on 6/28/2016.
 */
@Authorize(view = Access.OSTYPE_LIST)
public class OSTypeList extends BasePanel implements IParentListner {
    static final Logger logger = Logger.getLogger(OSTypeList.class);
    Form form;
    OSTypeSortableDataProvider dp = new OSTypeSortableDataProvider();
    MyAjaxDataTable table;
    BootStrapModal modalWindow = new BootStrapModal("modal");
    LimitedTextField idTextField;
    LimitedTextField nameTextField;

    public OSTypeList() {
        this(MAIN_PANEL_ID, new OSTypeService.OSTypeCriteria(), SelectionMode.None);
    }

    public OSTypeList(String id, OSTypeService.OSTypeCriteria criteria, final SelectionMode selectionMode) {
        super(id);

        add(modalWindow);

        setPageTitle(getString("label.list") + " " + getString("OSType"));

        form = new Form("searchForm", new CompoundPropertyModel(criteria));
        form.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        idTextField = new LimitedTextField("id", true, true,false,false,false, 12, getString("OSType.id"));
        idTextField.setLabel(new ResourceModel("OSType.id"));
        form.add(idTextField);

        nameTextField = new LimitedTextField("name", true, false,false,false,false, 40, getString("OSType.name"));

        nameTextField.setLabel(new ResourceModel("OSType.name"));
        form.add(nameTextField);

        form.add(new AjaxFormButton("search", form) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {


                String validationString = "";
                if (nameTextField != null && nameTextField.getValidatorString() != null && !nameTextField.getValidatorString().isEmpty()) {
                    for (String validationStringInList : nameTextField.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }

                if (idTextField != null && idTextField.getValidatorString() != null && !idTextField.getValidatorString().isEmpty()) {
                    for (String validationStringInList : idTextField.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }


                if (!validationString.isEmpty()) {
                    target.appendJavaScript("showMessage('" + validationString + "');");
                    return;
                }
                try {
                    OSTypeService.OSTypeCriteria cri =
                            (OSTypeService.OSTypeCriteria) form.getModelObject();
                    dp.setCriteria(cri);
                    table.setVisible(true);
                    target.add(table);
                    if (selectionMode.isSelectable()) {
                        target.add(OSTypeList.this.get("select").setVisible(true));
                    }
                } catch (Exception e) {
                    logger.error("Error:", e);
                }
            }
        });

        form.add(new AjaxLink("reset") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                form.setModel(new CompoundPropertyModel(new OSTypeService.OSTypeCriteria()));
                target.add(form);
                table.setVisible(false);
                target.add(table);
                OSTypeList.this.get("select").setVisible(false);
                target.add(OSTypeList.this.get("select"));
            }
        });

        add(authorize(new AjaxLink("add") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                OSTypeForm osTypeForm = new OSTypeForm(modalWindow.getContentId(), new OSType());
                osTypeForm.setParentListner(OSTypeList.this);
                modalWindow.setContent(osTypeForm);
                modalWindow.setTitle(getString("OSType.createForm"));
                modalWindow.show(ajaxRequestTarget);
            }
        }, WebAction.RENDER, Access.OSTYPE_ADD));

        table = new MyAjaxDataTable("table", getColumns(), dp, 10);
        table.setSelectionMode(selectionMode);
        table.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(table);

        boolean allFieldsNull = checkAllFieldsNull(criteria);

        if (allFieldsNull)
            table.setVisible(false);

        add(new AjaxLink("select") {
            public void onClick(AjaxRequestTarget target) {
                childFinished(target, null, null);
            }
        }.setOutputMarkupPlaceholderTag(true).setVisible(false));
        if (selectionMode.isSelectable() && table.getRowCount() > 0)
            get("select").setVisible(true);

        add(form);
    }

    private List<IColumn> getColumns() {
        List<IColumn> columnList = new ArrayList<IColumn>();
        columnList.add(new IndexColumn());

        columnList.add(new PropertyColumn(new ResourceModel("OSType.id"), "ent.id", "id"));
        columnList.add(new PropertyColumn(new ResourceModel("OSType.name"), "ent.name", "name"));

        columnList.add(new AbstractColumn(new ResourceModel("label.disabled.verb")) {
                @Override
                public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                    final OSType osType = (OSType) rowModel.getObject();
                if(PrincipalUtil.hasPermission(Access.OSTYPE_EDIT)) {
                    SwitchBox switchBox = new SwitchBox(componentId, new ResourceModel("label.yes").getObject(), new ResourceModel("label.no").getObject(), true) {
                        @Override
                        protected void onChange(AjaxRequestTarget target, Boolean currentState) {
                                Session session = HibernateUtil.getCurrentSession();
                                Transaction tx = null;

                            osType.setDisabled(currentState);
                                try {
                                    tx = session.beginTransaction();
                                    OSTypeService.Instance.saveOrUpdate(osType, session);
                                    tx.commit();
                                } catch (Exception ex) {
                                    logger.error("Error commiting transaction : ", ex);
                                    if (tx != null && tx.isActive()) {
                                        logger.debug("Rolling back transaction!");
                                        tx.rollback();
                                    }
                                    processException(target, ex);
                                } finally {
                                    session.close();
                                }

                                target.add(table);
                            }
                    };
                    switchBox.setModel(new Model<>(osType.getDisabled()));
                    cellItem.add(switchBox);
                    } else {

                    cellItem.add(new Label(componentId, new Model<>(
                            (osType.getDisabled()!=null && osType.getDisabled())?getString("label.yes"):getString("label.no") )));
                }
            }
        });

        if (PrincipalUtil.hasPermission(Access.OSTYPE_EDIT)) {
            columnList.add(new AbstractColumn(new Model<String>("")) {
                @Override
                public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                    final OSType osType = (OSType) rowModel.getObject();
                    Session session = HibernateUtil.getNewSession();
                    final OSType loadedOsType = (OSType) session.get(OSType.class, osType.getId());
                    session.close();
                    cellItem.add(new AjaxLinkPanel(componentId, AjaxLinkPanel.Image.Edit) {
                        @Override
                            public void onClick(AjaxRequestTarget target) {
                            OSTypeForm osTypeForm = new OSTypeForm(modalWindow.getContentId(), loadedOsType);
                            osTypeForm.setParentListner(OSTypeList.this);
                            modalWindow.setContent(osTypeForm);
                            modalWindow.setTitle(getString("OSType.editForm"));
                            modalWindow.show(target);
                                    }
                    });
                                }
            });

        }


        return columnList;
    }

    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {
        if (eventThrownCmp != null && eventThrownCmp.getId().equals("save"))
            target.add(table);
        modalWindow.close(target);
    }

    public static class OSTypeSortableDataProvider extends SortableDataProvider {
        OSTypeService.OSTypeCriteria criteria;

        public OSTypeSortableDataProvider() {
            setSort("id", SortOrder.ASCENDING);
        }

        public void setCriteria(OSTypeService.OSTypeCriteria criteria) {
            this.criteria = criteria;
        }

        @Override
        public Iterator iterator(long first, long count) {
            Session session = HibernateUtil.getCurrentSession();
            SortParam sortParam = getSort();
            return OSTypeService.Instance.list(criteria, (int) first, (int) count, (String) sortParam.getProperty(), sortParam.isAscending(), session).iterator();
        }

        @Override
        public long size() {
            Session session = HibernateUtil.getCurrentSession();
            return OSTypeService.Instance.count(criteria, session);
        }

        @Override
        public IModel model(Object o) {
            return new Model((Serializable) o);
        }
    }
}
