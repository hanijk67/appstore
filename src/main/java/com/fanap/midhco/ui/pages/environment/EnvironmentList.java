package com.fanap.midhco.ui.pages.environment;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.OSEnvironment;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.environment.EnvironmentService;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.component.ISelectable;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.ajaxLinkPanel.AjaxLinkPanel;
import com.fanap.midhco.ui.component.limitedTextField.LimitedTextField;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.component.table.MyAjaxDataTable;
import com.fanap.midhco.ui.component.table.column.IndexColumn;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by A.Moshiri on 2/26/2018.
 */
@Authorize(view = Access.ENVIRONMENT_LIST)
public class EnvironmentList extends BasePanel implements IParentListner, ISelectable {
    Form searchForm;
    envSortableDataProvider dp = new envSortableDataProvider();
    MyAjaxDataTable table;
    BootStrapModal modalWindow = new BootStrapModal("modal");
    LimitedTextField envName;


    public EnvironmentList() {
        this(MAIN_PANEL_ID, new EnvironmentService.EnvironmentCriteria(), SelectionMode.None);
    }

    protected EnvironmentList(String id, EnvironmentService.EnvironmentCriteria criteria, SelectionMode selectionMode) {
        super(id);

        setPageTitle(getString("osEnvironment"));

        add(modalWindow);

        boolean allFieldsNull = checkAllFieldsNull(criteria);

        searchForm = new Form("searchForm", new CompoundPropertyModel(criteria));
        searchForm.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(searchForm);

        envName = new LimitedTextField("envName", true, false, false, false,false, 30, getString("osEnvironment.name"));
        envName.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        searchForm.add(envName);


        int rowsPerPage =
                Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.APP_LIST_ROWS_PER_PAGE));
        table = new MyAjaxDataTable("table", getColumns(), dp, rowsPerPage);
        table.setSelectionMode(selectionMode);
        table.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(table);

        if (allFieldsNull)
            table.setVisible(false);


        add(new AjaxLink("select") {
            public void onClick(AjaxRequestTarget target) {
                childFinished(target, null, null);
            }
        }.setOutputMarkupPlaceholderTag(true).setVisible(false));
        if (selectionMode.isSelectable() && table.getRowCount() > 0)
            get("select").setVisible(true);


        add(new AjaxLink("createEnvironment") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                OSEnvironment environment = new OSEnvironment();
                EnvironmentForm envForm = new EnvironmentForm(modalWindow.getContentId(), environment);
                envForm.setParentListner(EnvironmentList.this);
                modalWindow.setContent(envForm);
                modalWindow.setTitle(getString("osEnvironment.createForm"));
                modalWindow.show(target);
            }
        });

        searchForm.add(new AjaxFormButton("search", searchForm) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {

                String validationString = "";
                if (envName != null && envName.getValidatorString() != null && !envName.getValidatorString().isEmpty()) {
                    for (String validationStringInList : envName.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }

                if (!validationString.isEmpty()) {
                    target.appendJavaScript("showMessage('" + validationString + "');");
                    return;
                }


                EnvironmentService.EnvironmentCriteria criteria = (EnvironmentService.EnvironmentCriteria) form.getModelObject();
                if (criteria != null) {
                    dp.setCriteria(criteria);
                    table.setVisible(true);
                    target.add(table);
                    if (selectionMode.isSelectable())
                        target.add(EnvironmentList.this.get("select").setVisible(true));
                }
            }
        });

        searchForm.add(new AjaxLink("reset") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                searchForm.setModel(new CompoundPropertyModel(new EnvironmentService.EnvironmentCriteria()));
                target.add(searchForm);
                table.setVisible(false);
                target.add(table);
                EnvironmentList.this.get("select").setVisible(false);
                target.add(EnvironmentList.this.get("select"));
                target.add(EnvironmentList.this.get("select"));
            }
        });
    }

    private List<IColumn> getColumns() {
        List<IColumn> columnList = new ArrayList<IColumn>();
        columnList.add(new IndexColumn());

        columnList.add(new PropertyColumn(new ResourceModel("osEnvironment.id"), "ent.id", "id"));
        columnList.add(new PropertyColumn(new ResourceModel("osEnvironment.name"), "ent.envName", "envName"));

        if (PrincipalUtil.hasPermission(Access.ENVIRONMENT_EDIT))
            columnList.add(new AbstractColumn(new ResourceModel("label.edit", "EDIT")) {
                @Override
                public void populateItem(Item cellItem, String componentId, final IModel rowModel) {
                    OSEnvironment osEnvironment = (OSEnvironment) rowModel.getObject();

                    if (!osEnvironment.getEnvName().equals("windows")) {
                        cellItem.add(new AjaxLinkPanel(componentId, AjaxLinkPanel.Image.Edit) {
                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                Session session = HibernateUtil.getNewSession();
                                OSEnvironment loadedOsEnvironment = (OSEnvironment) session.get(OSEnvironment.class, osEnvironment.getId());
                                session.close();
                                EnvironmentForm envForm = null;
                                if (loadedOsEnvironment!=null) {
                                    envForm = new EnvironmentForm(modalWindow.getContentId(), loadedOsEnvironment);
                                }else {
                                    envForm = new EnvironmentForm(modalWindow.getContentId(), osEnvironment);
                                }
                                envForm.setParentListner(EnvironmentList.this);
                                modalWindow.setContent(envForm);
                                modalWindow.setTitle(getString("osEnvironment.editForm"));
                                modalWindow.show(target);
                            }
                        });
                    } else {
                        cellItem.add(new Label(componentId, new Model<>()));
                    }
                }
            });

        return columnList;
    }


    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {
        if (eventThrownCmp != null && eventThrownCmp.getId().equals("save")) {
            target.add(table);
        }
        modalWindow.close(target);
    }

    @Override
    public Collection<Object> getSelection() {
        return table.getSelectedObjetcs();
    }


    public static class envSortableDataProvider extends SortableDataProvider {
        EnvironmentService.EnvironmentCriteria criteria;

        public envSortableDataProvider() {
            setSort("ent.id", SortOrder.ASCENDING);
        }

        public void setCriteria(EnvironmentService.EnvironmentCriteria criteria) {
            this.criteria = criteria;
        }

        @Override
        public Iterator iterator(long first, long count) {
            Session session = HibernateUtil.getCurrentSession();
            SortParam sortParam = getSort();
            return EnvironmentService.Instance.list(criteria, (int) first, (int) count, (String) sortParam.getProperty(), sortParam.isAscending(), session).iterator();
        }

        @Override
        public long size() {
            Session session = HibernateUtil.getCurrentSession();
            return EnvironmentService.Instance.count(criteria, session);
        }

        @Override
        public IModel model(Object o) {
            return new Model((Serializable) o);
        }
    }

}
