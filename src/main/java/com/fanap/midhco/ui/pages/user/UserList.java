package com.fanap.midhco.ui.pages.user;

import com.fanap.midhco.appstore.applicationUtils.ComponentKey;
import com.fanap.midhco.appstore.entities.User;
import com.fanap.midhco.appstore.entities.helperClasses.DateType;
import com.fanap.midhco.appstore.entities.helperClasses.UserStatus;
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
import com.fanap.midhco.ui.component.dateTimePanel.DateTimeRangePanel;
import com.fanap.midhco.ui.component.limitedTextField.LimitedTextField;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.component.mydropdown.MyDropDownChoicePanel;
import com.fanap.midhco.ui.component.selectionpanel.SelectionPanel;
import com.fanap.midhco.ui.component.table.MyAjaxDataTable;
import com.fanap.midhco.ui.component.table.column.IndexColumn;
import com.fanap.midhco.ui.pages.role.RoleList;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.hibernate.Session;

import java.io.Serializable;
import java.util.*;

/**
 * Created by admin123 on 6/21/2016.
 */
@Authorize(view = Access.USER_LIST)
public class UserList extends BasePanel implements IParentListner, ISelectable {
    UserSortableDataProvider dp = new UserSortableDataProvider();
    Form form;
    MyAjaxDataTable table;
    LimitedTextField idTextField;
    LimitedTextField userNameTextField;
    FeedbackPanel feedbackPanel;

    BootStrapModal modalWindow = new BootStrapModal("modal");


    public UserList() {
        this(MAIN_PANEL_ID, new UserService.UserCriteria(), SelectionMode.None);
    }

    public UserList(String id, UserService.UserCriteria criteria, final SelectionMode selectionMode) {
        this(id, criteria, null, selectionMode);
    }

    public UserList(String id, UserService.UserCriteria criteria, List<ComponentKey> disabledComponents, final SelectionMode selectionMode) {
        super(id);
        final boolean checkCriteriIsNull = checkAllFieldsNull(criteria);

        setPageTitle(getString("label.list") + " " + getString("User"));

        add(modalWindow);

        form = new Form("searchForm", new CompoundPropertyModel(criteria));
        form.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(form);

        idTextField = new LimitedTextField("id", true, true,false,false,false, 12, getString("label.id"));
        idTextField.setLabel(new ResourceModel("label.id"));
        idTextField.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(idTextField);

        userNameTextField = new LimitedTextField("userName", null, false,true,false,false, 60, getString("User.userName"));

        userNameTextField.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(userNameTextField);

//        TextField firstNameField = new TextField("firstName");
//        firstNameField.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
//        form.add(firstNameField);
//
//        TextField lastNameField = new TextField("lastName");
//        lastNameField.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
//        form.add(lastNameField);
//
//        TextField nationalCodeField = new TextField("nationalCode");
//        nationalCodeField.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
//        form.add(nationalCodeField);

        LimitedTextField lastIpField = new LimitedTextField("lastIp", null, false,true,true,false, 16, getString("User.lastIp"));
        lastIpField.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(lastIpField);

        DateTimeRangePanel lastLoginDateTimeRangePanel = new DateTimeRangePanel("lastLoginDateTime", DateType.DateTime);
        lastLoginDateTimeRangePanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(lastLoginDateTimeRangePanel);

        MyDropDownChoicePanel loggedBox = new MyDropDownChoicePanel("logged",
                Arrays.asList(true, false), false, false, getString("User.logged"), 1, false,
                new ChoiceRenderer() {
                    @Override
                    public Object getDisplayValue(Object o) {
                        if ((Boolean)o) {
                            return getString("label.yes");
                        }
                        return getString("label.no");
                    }

                    @Override
                    public String getIdValue(Object o, int i) {
                        return o.toString();
                    }
                }
        );
        form.add(loggedBox);

        List<UserStatus> allStatus = UserStatus.listAll();
        MyDropDownChoicePanel statusesDropDownChoice =
                new MyDropDownChoicePanel("statuses", allStatus, true, false, getString("UserStatus"), 4);
        statusesDropDownChoice.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(statusesDropDownChoice);

        form.add(new SelectionPanel("roles", SelectionMode.Multiple) {
            @Override
            public ISelectable getSelectable(String panelId) {
                return new RoleList(panelId, new RoleService.RoleCriteria(), SelectionMode.Multiple);
            }
        });

//        List<Gender> allGenders = Gender.listAll();
//        MyDropDownChoicePanel genderDropDown =
//                new MyDropDownChoicePanel("gender", allGenders, false, false, getString("gender"), 2, false, new ChoiceRenderer() {
//                    @Override
//                    public Object getDisplayValue(Object o) {
//                        return o.toString();
//                    }
//
//                    @Override
//                    public String getIdValue(Object o, int i) {
//                        return o.toString();
//                    }
//                } );
//        genderDropDown.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
//        form.add(genderDropDown);

        form.add(new SelectionPanel("creatorUsers", SelectionMode.Multiple) {
            @Override
            public ISelectable getSelectable(String panelId) {
                return new UserList(panelId, new UserService.UserCriteria(), SelectionMode.Multiple);
            }
        });

        DateTimeRangePanel creationDateTimePanel = new DateTimeRangePanel("creationDateTime", DateType.DateTime);
        creationDateTimePanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(creationDateTimePanel);

        AjaxFormSubmitBehavior behave = new AjaxFormSubmitBehavior(form,
                "onclick") {
            protected void onSubmit(AjaxRequestTarget target) {


                String validationString = "";
                if (idTextField!=null && idTextField.getValidatorString()!=null && !idTextField.getValidatorString().isEmpty()) {
                    for (String validationStringInList : idTextField.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }

                if (userNameTextField!=null && userNameTextField.getValidatorString()!=null && !userNameTextField.getValidatorString().isEmpty()) {
                    for (String validationStringInList : userNameTextField.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }
                if (!validationString.isEmpty()) {
                    target.appendJavaScript("showMessage('" + validationString + "');");
                    return;
                }
                try {
                    UserService.UserCriteria cri =
                            (UserService.UserCriteria) form.getModelObject();

                    cri.userNameContain = cri.userName;
                    cri.userName = null;

                    cri.lastNameContain = cri.lastName;
                    cri.lastName = null;
                    dp.setCriteria(cri);
                    table.setVisible(true);
                    target.add(table);
                    if (selectionMode.isSelectable()) {
                        target.add(UserList.this.get("select").setVisible(true));
                    }
                } catch (Exception e) {
                    logger.error("Error:", e);
                }
            }


        };


        Button submitButton = new Button("search");

        submitButton.add(behave);
        form.add(submitButton);


//        form.add(new AjaxFormButton("search", form) {
//            @Override
//            protected void onSubmit(Form form, AjaxRequestTarget target) {
//                String validationString = "";
//                if (idTextField!=null && idTextField.getValidatorString()!=null && !idTextField.getValidatorString().isEmpty()) {
//                    for (String validationStringInList : idTextField.getValidatorString()) {
//                        validationString += " - " +
//                                validationStringInList + "<br/>";
//                    }
//                }
//
//                if (userNameTextField!=null && userNameTextField.getValidatorString()!=null && !userNameTextField.getValidatorString().isEmpty()) {
//                    for (String validationStringInList : userNameTextField.getValidatorString()) {
//                        validationString += " - " +
//                                validationStringInList + "<br/>";
//                    }
//                }
//                if (!validationString.isEmpty()) {
//                    target.appendJavaScript("showMessage('" + validationString + "');");
//                    return;
//                }
//            }
//        });

        AjaxLink searchAjaxLink = new AjaxLink<Object>("reset") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                form.setModel(new CompoundPropertyModel(new UserService.UserCriteria()));
                target.add(form);
                table.setVisible(false);
                target.add(table);
                UserList.this.get("select").setVisible(false);
                target.add(UserList.this.get("select"));
            }
        };

        searchAjaxLink.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(searchAjaxLink);


        feedbackPanel = new FeedbackPanel("feedbackPanel");
        add(feedbackPanel);

        AjaxLink addLink = new AjaxLink<Object>("createUser") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                try {
                    UserForm userForm = new UserForm(modalWindow.getContentId(), new User());
                    userForm.setParentListner(UserList.this);
                    modalWindow.setContent(userForm);
                    modalWindow.setTitle(getString("User.createForm"));
                    modalWindow.show(target);
                } catch (Exception e) {
                    logger.error("Error:", e);
                }
            }
        };
        addLink.setVisible(false);
        if (PrincipalUtil.hasPermission(Access.USER_CREATE))
            addLink.setVisible(true);
        add(addLink);

        table = new MyAjaxDataTable("table", createColumns(), dp, 10);
        table.setSelectionMode(selectionMode != null ? selectionMode : SelectionMode.None);
        table.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        table.setVisible(!checkCriteriIsNull);
        if (selectionMode.isSelectable())
            dp.setCriteria(criteria);
        add(table);

        add(new AjaxLink<Object>("select") {
            public void onClick(AjaxRequestTarget target) {
                childFinished(target, null, null);
            }
        }.setOutputMarkupPlaceholderTag(true).setVisible(false));
        if (selectionMode.isSelectable() && table.getRowCount() > 0)
            get("select").setVisible(true);

        if (disabledComponents != null && !disabledComponents.isEmpty()) {
            FormComponent formComponent = (FormComponent) form.get("roles");
            if (formComponent instanceof SelectionPanel) {
                ((SelectionPanel) formComponent).disable();
            } else {
                formComponent.setEnabled(false);
            }
            form.get("reset").setVisible(false);
        }

        get("createUser").setVisible(!selectionMode.isSelectable());
    }

    protected List<IColumn> createColumns() {
        List<IColumn> columns = new ArrayList<IColumn>();
        columns.add(new IndexColumn());
        columns.add(new PropertyColumn(new ResourceModel("label.id"), "ent.id", "id"));
        columns.add(new PropertyColumn(new ResourceModel("User.username"), "ent.userName", "userName"));
        columns.add(new PropertyColumn(new ResourceModel("Contact.firstName"), "ent.contact.firstName", "contact.firstName"));
        columns.add(new PropertyColumn(new ResourceModel("Contact.lastName"), "ent.contact.lastName", "contact.lastName"));
        columns.add(new PropertyColumn(new ResourceModel("User.userStatus"), "ent.userStatus", "userStatus"));
        columns.add(new PropertyColumn(new ResourceModel("User.roles"), null, "roles"));
                columns.add(new AbstractColumn(new Model("")) {
            @Override
            public void populateItem(Item cellItem, String componentId, final IModel rowModel) {
                if (PrincipalUtil.hasPermission(Access.USER_EDIT)) {
                    User user = (User)rowModel.getObject();
                    if(user.getUserName().equals("admin"))
                        cellItem.add(new Label(componentId, new Model<>("")));
                    else
                    cellItem.add(new AjaxLinkPanel(componentId, AjaxLinkPanel.Image.Edit) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            User user = (User) rowModel.getObject();
                            user = (User) HibernateUtil.getCurrentSession().load(User.class, user.getId());
                            UserForm userForm = new UserForm(modalWindow.getContentId(), user);
                            userForm.setParentListner(UserList.this);
                            modalWindow.setContent(userForm);
                                modalWindow.setTitle(getString("User.editForm"));
                            modalWindow.show(target);
                        }
                    });
                } else {
                    cellItem.add(new Label(componentId, new Model("")));
                }
            }
        });
        return columns;
    }

    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {
        if (eventThrownCmp != null && eventThrownCmp.getId().equals("save"))
            target.add(table);
        modalWindow.close(target);
    }

    public Collection<Object> getSelection() {
        return table.getSelectedObjetcs();
    }

    public static class UserSortableDataProvider extends SortableDataProvider {
        public UserService.UserCriteria criteria;

        public UserService.UserCriteria getCriteria() {
            return criteria;
        }

        public void setCriteria(UserService.UserCriteria criteria) {
            this.criteria = criteria;
        }

        public UserSortableDataProvider() {
            setSort("ent.id", SortOrder.ASCENDING);
        }

        @Override
        public Iterator iterator(long first, long count) {
            Session session = HibernateUtil.getCurrentSession();
            SortParam sortParam = getSort();
            return UserService.Instance.list(criteria, (int) first, (int) count, (String) sortParam.getProperty(), sortParam.isAscending(), session).iterator();
        }

        @Override
        public long size() {
            Session session = HibernateUtil.getCurrentSession();
            SortParam sortParam = getSort();
            return UserService.Instance.count(criteria, (String)sortParam.getProperty(), sortParam.isAscending(), session).intValue();
        }

        @Override
        public IModel model(Object object) {
            return new Model((Serializable) object);
        }
    }
}