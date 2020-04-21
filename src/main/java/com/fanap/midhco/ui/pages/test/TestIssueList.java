package com.fanap.midhco.ui.pages.test;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.AppPackage;
import com.fanap.midhco.appstore.entities.ApprovalState;
import com.fanap.midhco.appstore.entities.TestIssue;
import com.fanap.midhco.appstore.entities.TestPriority;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.test.TestIssueServices;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.WebAction;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.component.table.IRowSelectEvent;
import com.fanap.midhco.ui.component.table.MyAjaxDataTable;
import com.fanap.midhco.ui.component.table.MyItem;
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
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.hibernate.Session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by A.Moshiri on 6/21/2017.
 */
public class TestIssueList extends BasePanel implements IParentListner {
    FeedbackPanel feedbackPanel;
    BootStrapModal modal;
    MyAjaxDataTable table;
    AppPackage inputAppPackage;
    Form form;
    Label formName;

    TestIssueSortableDataProvider dp = new TestIssueSortableDataProvider();

    public TestIssueList() {
        this(MAIN_PANEL_ID, new AppPackage(), SelectionMode.Single);
    }

    public TestIssueList(String id, AppPackage appPackage, SelectionMode selectionMode) {
        super(id);
        form = new Form("form");
        inputAppPackage = appPackage;
        formName = new Label("formName");
        formName.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        formName.setDefaultModel(new ResourceModel("label.testIssue.list"));
        add(formName);

        feedbackPanel = new FeedbackPanel("feedbackPanel");
        add(feedbackPanel);

        modal = new BootStrapModal("modal");
        add(modal);

        int rowsPerPage =
                Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.TEST_ISSUE_LIST_ROWS_PER_PAGE));
        TestIssueServices.TestIssueCriteria criteria = new TestIssueServices.TestIssueCriteria();

        if (appPackage != null && appPackage.getId() != null) {
            criteria.setPackageId(appPackage.getId());
        }
        dp.setCriteria(criteria);
        table = new MyAjaxDataTable("table", getColumns(), dp, rowsPerPage);
        table.setSelectionMode(selectionMode);

        table.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        table.setRowSelectEvent(new IRowSelectEvent() {
            @Override
            public void onClick(AjaxRequestTarget target, IModel rowModel, MyItem cellItem) {
                cellItem.setSelected(false);
                TestIssueServices.TestIssueSearchModel testIssueSearchModel = (TestIssueServices.TestIssueSearchModel) rowModel.getObject();
                Session session = HibernateUtil.getCurrentSession();
                TestIssue testIssue = (TestIssue) session.get(TestIssue.class, testIssueSearchModel.getId());
                TestIssueResultForm testIssueResultForm = new TestIssueResultForm(modal.getContentId(), appPackage, testIssue);
                testIssueResultForm.setParentListner(TestIssueList.this);
                modal.setContent(testIssueResultForm);
                target.appendJavaScript("$('.modal-body').css('overflow-x', 'hidden')");
                modal.show(target);
                cellItem.setSelected(true);
            }
        });
        form.add(table);


        form.add(authorize(new AjaxLink("add") {
                               @Override
                               public void onClick(AjaxRequestTarget target) {
                                   TestIssueForm testIssueForm = new TestIssueForm(modal.getContentId(), new TestIssue(), appPackage);
                                   testIssueForm.setParentListner(TestIssueList.this);
                                   modal.setContent(testIssueForm);
                                   modal.setTitle(new ResourceModel("APPPackage.testIssue"));
                                   target.appendJavaScript("$('.modal-body').css('overflow-x', 'hidden')");
                                   modal.show(target);
                               }
                           },
                WebAction.RENDER, Access.TEST_ADD));

        add(form);
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

        return columnList;
    }

    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {
        if (eventThrownCmp != null && eventThrownCmp.getId().equals("save")) {
            target.add(table);
        }
        modal.close(target);
    }

    public static class TestIssueSortableDataProvider extends SortableDataProvider {
        TestIssueServices.TestIssueCriteria criteria;

        public TestIssueSortableDataProvider() {
            setSort("ent.id", SortOrder.ASCENDING);
        }

        public void setCriteria(TestIssueServices.TestIssueCriteria criteria) {
            this.criteria = criteria;
        }

        @Override
        public Iterator iterator(long first, long count) {
            Session session = HibernateUtil.getCurrentSession();
            SortParam sortParam = getSort();
            criteria.setFromSizeSearch(false);
            return TestIssueServices.Instance.list(criteria, (int) first, (int) count, (String) sortParam.getProperty(), sortParam.isAscending(), session).iterator();
        }

        @Override
        public long size() {
            Session session = HibernateUtil.getCurrentSession();
            criteria.setFromSizeSearch(true);
            return TestIssueServices.Instance.count(criteria, session);
        }

        @Override
        public IModel model(Object o) {
            return new Model((Serializable) o);
        }
    }
}
