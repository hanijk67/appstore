package com.fanap.midhco.ui.pages;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.ApprovalState;
import com.fanap.midhco.appstore.entities.TestPriority;
import com.fanap.midhco.appstore.entities.Role;
import com.fanap.midhco.appstore.entities.User;
import com.fanap.midhco.appstore.service.test.TestIssueServices;
import com.fanap.midhco.appstore.service.user.RoleService;
import com.fanap.midhco.ui.AppStoreSession;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.table.IRowSelectEvent;
import com.fanap.midhco.ui.component.table.MyAjaxDataTable;
import com.fanap.midhco.ui.component.table.MyItem;
import com.fanap.midhco.ui.component.table.column.IndexColumn;
import com.fanap.midhco.ui.pages.security.AuthenticatedUserPanel;
import com.fanap.midhco.ui.pages.test.TestIssueList;
import com.fanap.midhco.ui.pages.test.TestSubIssueForm;
import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.ArrayList;
import java.util.List;

public class Index extends WebPage implements IParentListner{
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(BasePage.class);
    TestIssueList.TestIssueSortableDataProvider dp = new TestIssueList.TestIssueSortableDataProvider();
    MyAjaxDataTable table;
    WebMarkupContainer masterContainer;
    WebMarkupContainer testListContainer;
    String selectedCell ="";

    public Index(final PageParameters parameters) {
        super(parameters);

        AppStoreSession appStoreSession = (AppStoreSession) AppStoreSession.get();
        masterContainer = new WebMarkupContainer("masterContainer");
        masterContainer.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        WebMarkupContainer contentWebMarkupContainer = new WebMarkupContainer("content");
        masterContainer.add(contentWebMarkupContainer);
        add(masterContainer);


        boolean isAuth = appStoreSession.isAuthenticated();
        if (isAuth) {
            add(new AuthenticatedUserPanel("authenticatedUserPanel"));
            com.fanap.midhco.ui.MenuPanel menuPanel = new com.fanap.midhco.ui.MenuPanel("menuPanel");
            menuPanel.setMarkupId("main-menu");
            menuPanel.setOutputMarkupId(true);
            add(menuPanel);
        } else {
            add(new Label("authenticatedUserPanel", ""));
            add(new Label("menuPanel", ""));
        }

        int rowsPerPage =
                Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.TEST_ISSUE_LIST_ROWS_PER_PAGE));
        TestIssueServices.TestIssueCriteria criteria = new TestIssueServices.TestIssueCriteria();
        criteria.setShowAllTest(true);
        List<ApprovalState> approvalStates = new ArrayList<>();
        approvalStates.add(ApprovalState.DISAPPROVED);
        criteria.setApprovalState(approvalStates);
        dp.setCriteria(criteria);
        User currentUser = PrincipalUtil.getCurrentUser();
        Role testRole = RoleService.Instance.getTesterRole();
        table = new MyAjaxDataTable("table", getColumns(), dp, rowsPerPage);
        table.setSelectionMode(SelectionMode.Single);
        table.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        table.setVisible(false);

        table.setRowSelectEvent(new IRowSelectEvent() {
            @Override
            public void onClick(AjaxRequestTarget target, IModel rowModel, MyItem cellItem) {
                TestIssueServices.TestIssueSearchModel testSearchModel = (TestIssueServices.TestIssueSearchModel) rowModel.getObject();
                TestSubIssueForm testSubIssueForm = new TestSubIssueForm("content",testSearchModel);
                testSubIssueForm.setParentListner(Index.this);

                masterContainer.setVisible(true);
                masterContainer.get("content").setVisible(true);
                if (selectedCell.equals(cellItem.getId())) {
                    masterContainer.get("content").replaceWith(new WebMarkupContainer("content"));
                    selectedCell = "";
                } else {
                    masterContainer.get("content").replaceWith(testSubIssueForm);
                    selectedCell = cellItem.getId();
                }

                target.add(masterContainer);
            }
        });

        testListContainer = new WebMarkupContainer("testListContainer");
        testListContainer.setVisible(false);
        testListContainer.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        if (currentUser.getRoles().contains(testRole)) {
            table.setVisible(true);
            testListContainer.setVisible(true);
        }

        testListContainer.add(table);
        add(testListContainer);
    }

    private List<IColumn> getColumns() {
        List<IColumn> columnList = new ArrayList<IColumn>();
        columnList.add(new IndexColumn());

        columnList.add(new PropertyColumn(new ResourceModel("TestIssue.id"), "ent.id", "id"));
        columnList.add(new PropertyColumn(new ResourceModel("TestIssue.title"), "ent.title", "title"));

        columnList.add(new AbstractColumn(new ResourceModel("TestIssue.approvalState", "approvalState")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                TestIssueServices.TestIssueSearchModel testSearchModel = (TestIssueServices.TestIssueSearchModel) rowModel.getObject();
                if (testSearchModel.getApprovalState() != null) {
                    if (testSearchModel.getApprovalState().equals(ApprovalState.DISAPPROVED)) {
                        cellItem.add(new Label(componentId, new ResourceModel("ApprovalState.disApproved", "disApproved")));
                    } else if (testSearchModel.getApprovalState().equals(ApprovalState.APPROVED)) {
                        cellItem.add(new Label(componentId, new ResourceModel("ApprovalState.approved", "approved")));
                    } else {
                        cellItem.add(new Label(componentId, new ResourceModel("ApprovalState.canceled", "canceled")));
                    }
                } else {
                    cellItem.add(new Label(componentId, new ResourceModel("", "")));
                }
            }
        });

        columnList.add(new AbstractColumn(new ResourceModel("TestIssue.priority", "priority")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                TestIssueServices.TestIssueSearchModel testSearchModel = (TestIssueServices.TestIssueSearchModel) rowModel.getObject();
                if (testSearchModel.getPriority() != null) {
                    if (testSearchModel.getPriority().equals(TestPriority.LOW)) {
                        cellItem.add(new Label(componentId, new ResourceModel("Priority.low", "low")));
                    } else if (testSearchModel.getPriority().equals(TestPriority.MEDIUM)) {
                        cellItem.add(new Label(componentId, new ResourceModel("Priority.medium", "medium")));
                    } else {
                        cellItem.add(new Label(componentId, new ResourceModel("Priority.high", "high")));
                    }
                } else {
                    cellItem.add(new Label(componentId, new ResourceModel("", "")));
                }
            }
        });

        columnList.add(new PropertyColumn(new ResourceModel("App.title"), "app.title", "appTitle"));
        columnList.add(new PropertyColumn(new ResourceModel("APPPackage.versionCode"), "pack.versionCode", "versionCode"));
        columnList.add(new PropertyColumn(new ResourceModel("APPPackage.versionName"), "pack.versionName", "versionName"));

        return columnList;
    }

    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {
        if (eventThrownCmp != null && eventThrownCmp.getId().equals("save")){
            masterContainer.get("content").replaceWith(new WebMarkupContainer("content"));
            target.add(masterContainer);
        }

    }
}
