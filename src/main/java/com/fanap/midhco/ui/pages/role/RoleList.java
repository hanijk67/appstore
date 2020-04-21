package com.fanap.midhco.ui.pages.role;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.Role;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.user.RoleService;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.component.ISelectable;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.ajaxLinkPanel.AjaxLinkPanel;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.component.table.MyAjaxDataTable;
import com.fanap.midhco.ui.component.table.column.IndexColumn;
import com.fanap.midhco.ui.pages.user.UserList;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
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
 * Created by admin123 on 6/22/2016.
 */
@Authorize(view = Access.ROLE_LIST)
public class RoleList extends BasePanel implements IParentListner, ISelectable {
    RoleSortableDataProvider dp = new RoleSortableDataProvider();
    Form form;
    MyAjaxDataTable table;
    BootStrapModal modalWindow = new BootStrapModal("modal");

    public RoleList() {
        this(MAIN_PANEL_ID, new RoleService.RoleCriteria(), SelectionMode.None);
    }

    public RoleList(String id, RoleService.RoleCriteria roleCriteria, final SelectionMode selectionMode) {
        super(id);

        setPageTitle(getString("label.list") + " " + getString("Role"));

        add(modalWindow);

        boolean allFieldsNull = checkAllFieldsNull(roleCriteria);

        form = new Form("searchForm", new CompoundPropertyModel(roleCriteria));
        form.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        final TextField roleCodeTextField = new TextField("roleId");
        roleCodeTextField.setLabel(new ResourceModel("Role.code"));
        form.add(roleCodeTextField);

        final TextField roleNameTextField = new TextField("roleName");
        roleNameTextField.setLabel(new ResourceModel("Role.name"));
        form.add(roleNameTextField);

        form.add(new AjaxButton("search") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                RoleService.RoleCriteria criteria = (RoleService.RoleCriteria) form.getModelObject();
                if(criteria != null) {
                    dp.setCriteria(criteria);
                    table.setVisible(true);
                    target.add(table);
                    if (selectionMode.isSelectable())
                        target.add(RoleList.this.get("select").setVisible(true));
                }
            }
        });

        form.add(new AjaxLink("reset") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                RoleService.RoleCriteria criteria = new RoleService.RoleCriteria();
                form.setModelObject(criteria);
                target.add(form);
//comment by Moshiri for hide table
//                if (selectionMode.isSelectable())
//                    target.add(RoleList.this.get("select").setVisible(true));
//                else
//                    RoleList.this.get("select").setVisible(false);

                table.setVisible(false);
                target.add(table);
                    RoleList.this.get("select").setVisible(false);

                target.add(RoleList.this.get("select"));
            }
        });

        add(form);

        AjaxLink addRoleButton = new AjaxLink("createRole") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                RoleForm roleForm = new RoleForm(modalWindow.getContentId(), new Role());
                modalWindow.setContent(roleForm);
                roleForm.setParentListner(RoleList.this);
                modalWindow.show(target);
            }
        };
        add(addRoleButton);

//        if(!PrincipalUtil.hasPermission(Access.ROLE_CREATE) || !selectionMode.equals(SelectionMode.None))
            addRoleButton.setVisible(false);

        int rowsPerPage =
                Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.APP_LIST_ROWS_PER_PAGE));
        table = new MyAjaxDataTable("table", getColumns(), dp, rowsPerPage);
        table.setSelectionMode(selectionMode);
        table.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(table);

        table.setVisible(false);
/*

        if(allFieldsNull)
            table.setVisible(false);

        if(!selectionMode.equals(SelectionMode.None))
            table.setVisible(true);
*/

        add(new AjaxLink("select") {
            public void onClick(AjaxRequestTarget target) {
                childFinished(target, null, null);
            }
        }.setOutputMarkupPlaceholderTag(true).setVisible(false));
        if (selectionMode.isSelectable() && table.getRowCount() > 0)
            get("select").setVisible(true);
    }

    private List<IColumn> getColumns() {
        List<IColumn> columnList = new ArrayList<IColumn>();
        columnList.add(new IndexColumn());

        columnList.add(new PropertyColumn(new ResourceModel("Role.code"), "role.id", "id"));
        columnList.add(new PropertyColumn(new ResourceModel("Role.name"), "role.name", "name"));
        if(PrincipalUtil.hasPermission(Access.ROLE_EDIT))
            columnList.add(new AbstractColumn(new Model<String>("")) {
                @Override
                public void populateItem(Item cellItem, String componentId, final IModel rowModel) {
                    cellItem.add(new AjaxLinkPanel(componentId, AjaxLinkPanel.Image.Edit) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            Role role = (Role)rowModel.getObject();
                            Session session = HibernateUtil.getNewSession();
                            Role loadedRole = (Role) session.get(Role.class , role.getId());
                            session.close();
                            RoleForm roleForm = new RoleForm(modalWindow.getContentId(), loadedRole);
                            roleForm.setParentListner(RoleList.this);
                            modalWindow.setContent(roleForm);
                            modalWindow.setTitle(getString("Role.editForm"));
                            modalWindow.show(target);
                        }
                    });
                }
            });

        return columnList;
    }

    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {
        if(eventThrownCmp != null && eventThrownCmp.getId().equals("save"))
            target.add(table);
        modalWindow.close(target);
    }

    @Override
    public Collection<Object> getSelection() {
        return table.getSelectedObjetcs();
    }

    public static class RoleSortableDataProvider extends SortableDataProvider {
        RoleService.RoleCriteria criteria;

        public RoleSortableDataProvider() {
            setSort("id", SortOrder.ASCENDING);
        }

        public void setCriteria(RoleService.RoleCriteria criteria) {
            this.criteria = criteria;
        }

        @Override
        public Iterator iterator(long first, long count) {
            Session session = HibernateUtil.getCurrentSession();
            SortParam sortParam = getSort();
            return RoleService.Instance.list(criteria, (int)first, (int)count, (String)sortParam.getProperty(), sortParam.isAscending(), session).iterator();
        }

        @Override
        public long size() {
            Session session = HibernateUtil.getCurrentSession();
            return RoleService.Instance.count(criteria, session);
        }

        @Override
        public IModel model(Object o) {
            return new Model((Serializable) o);
        }
    }
}
