package com.fanap.midhco.ui.pages.test;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.app.AppPackageHistoryService;
import com.fanap.midhco.appstore.service.app.AppPackageService;
import com.fanap.midhco.appstore.service.test.TestIssueHistoryService;
import com.fanap.midhco.appstore.service.test.TestIssueServices;
import com.fanap.midhco.appstore.service.test.TestSubIssueService;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.component.mydropdown.MyDropDownChoicePanel;
import com.fanap.midhco.ui.component.niceEditor.NiceEditor;
import com.fanap.midhco.ui.component.table.IRowSelectEvent;
import com.fanap.midhco.ui.component.table.MyAjaxDataTable;
import com.fanap.midhco.ui.component.table.MyItem;
import com.fanap.midhco.ui.component.table.column.IndexColumn;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by A.Moshiri on 7/1/2017.
 */
public class TestIssueResultForm extends BasePanel {
    Form form;
    BootStrapModal modal;
    MyAjaxDataTable table;
    Label titleLabel;
    MyDropDownChoicePanel priority;
    MyDropDownChoicePanel approvalState;
    NiceEditor description;
    Long testIssueId;
    TestSubIssueSortableDataProvider dp;
    String descriptionString;
    AjaxEventBehavior descriptionGetBehaviour;
    WebMarkupContainer masterContainer;
    String selectedCell ="";
    Label formName;
    Label testDescription;
    Label testResult;

    protected TestIssueResultForm(String id, AppPackage appPackage, TestIssue testIssue) {
        super(id);

        formName = new Label("formName");
        formName.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        formName.setDefaultModel(new ResourceModel("label.testIssue.details"));
        add(formName);

        testIssueId = (testIssue == null || testIssue.getId() == null) ? null : testIssue.getId();
        dp = new TestSubIssueSortableDataProvider();
        modal = new BootStrapModal("modal");
        add(modal);

        form = new Form("form", new CompoundPropertyModel(testIssue));
        add(form);

        testDescription = new Label("testDescription");
        testDescription.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        testDescription.setDefaultModel(new ResourceModel("label.testIssue.description"));
        form.add(testDescription);

        testResult = new Label("testResult");
        testResult.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        testResult.setDefaultModel(new ResourceModel("label.testIssue.tester"));
        form.add(testResult);

        masterContainer = new WebMarkupContainer("masterContainer");
        masterContainer.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        TestIssueDetailsFrom testIssueDetailsFrom = new TestIssueDetailsFrom("content",new TestSubIssue());
        masterContainer.add(testIssueDetailsFrom).setVisible(false);
        form.add(masterContainer);


        titleLabel = new Label("title");
        if (testIssue != null && testIssue.getTitle() != null) {
            titleLabel.setDefaultModel(new Model<>(testIssue.getTitle()));
        }
        titleLabel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(titleLabel);

        List<ApprovalState> approvalStates = new ArrayList<>();
        approvalStates.add(ApprovalState.APPROVED);
        approvalStates.add(ApprovalState.DISAPPROVED);
        approvalStates.add(ApprovalState.CANCELED);
        approvalState =
                new MyDropDownChoicePanel("approvalState", approvalStates, false, false, getString("TestIssue.approvalState"), 3, false, new ChoiceRenderer());
        approvalState.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        approvalState.setLabel(new ResourceModel("TestIssue.approvalState"));

        form.add(approvalState);


        List<TestPriority> priorities = new ArrayList<>();
        priorities.add(TestPriority.HIGH);
        priorities.add(TestPriority.LOW);
        priorities.add(TestPriority.MEDIUM);
        priority =
                new MyDropDownChoicePanel("priority", priorities, false, false, getString("TestIssue.priority"), 3, false, new ChoiceRenderer());
        priority.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        priority.setLabel(new ResourceModel("TestIssue.priority"));
        form.add(priority);

        description = new NiceEditor("description");
        description.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        description.setLabel(new ResourceModel("TestIssue.description"));
        form.add(description);

        int rowsPerPage =
                Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.TEST_ISSUE_LIST_ROWS_PER_PAGE));
        TestSubIssueService.TestSubIssueCriteria criteria = new TestSubIssueService.TestSubIssueCriteria();

        if (testIssueId != null) {
            criteria.setTestIssueId(testIssueId);
        }
        dp.setCriteria(criteria);
        table = new MyAjaxDataTable("table", getColumns(), dp, rowsPerPage);
        table.setSelectionMode(SelectionMode.Single);
        table.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        table.setRowSelectEvent(new IRowSelectEvent() {
            @Override
            public void onClick(AjaxRequestTarget target, IModel rowModel, MyItem cellItem) {
                TestSubIssueService.TestSubIssueSearchModel testSubIssueSearchModel = (TestSubIssueService.TestSubIssueSearchModel) rowModel.getObject();
                Session session = HibernateUtil.getCurrentSession();

                TestSubIssue testSubIssue = (TestSubIssue) session.load(TestSubIssue.class, testSubIssueSearchModel.getId());
                TestIssueDetailsFrom testIssueDetailsFrom = new TestIssueDetailsFrom(modal.getContentId(), testSubIssue);
                masterContainer.setVisible(true);
                masterContainer.get("content").setVisible(true);
                if (selectedCell.equals(cellItem.getId())){
                    masterContainer.get("content").replaceWith(new WebMarkupContainer("content"));
                    selectedCell = "";
                }
                else {
                    masterContainer.get("content").replaceWith(testIssueDetailsFrom);
                    selectedCell = cellItem.getId();
                }
                target.add(masterContainer);

                cellItem.setSelected(true);
            }
        });
        form.add(table);

        add(new AjaxFormButton("save", form) {

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("onclick", "var txt = $('#" + description.getMarkupId() + "').find('textarea').text();" +
                        "var res = encodeURIComponent(txt);" +
                        "Wicket.Ajax.get({u:'" +
                        descriptionGetBehaviour.getCallbackUrl().toString() +
                        "&changeLog=' + res"
                        + "});");
            }

            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {

                TestIssue formTestIssue = (TestIssue) form.getModelObject();
                ApprovalState selectedApprovalState = (ApprovalState) approvalState.getConvertedInput();
                TestPriority selectedPriority = (TestPriority) priority.getConvertedInput();

                String validationString = "";

                if (selectedApprovalState == null) {
                    validationString += " - " +
                            getString("Required").replace("${label}", approvalState.getLabel().getObject()) + "<br/>";
                }

                if (selectedPriority == null) {
                    validationString += " - " +
                            getString("Required").replace("${label}", priority.getLabel().getObject()) + "<br/>";
                }

                if (descriptionString == null || descriptionString.trim().equals("")) {
                    validationString += " - " +
                            getString("Required").replace("${label}", description.getLabel().getObject()) + "<br/>";
                }
                if (!validationString.trim().isEmpty()) {
                    target.appendJavaScript("showMessage('" + validationString + "');");
                    return;
                }
                Session session = HibernateUtil.getCurrentSession();
                AppPackage loadedAppPackage = (appPackage != null && appPackage.getId() != null) ? (AppPackage) session.load(AppPackage.class, appPackage.getId()) : new AppPackage();
                TestIssue formTestIssueToSave = (TestIssue) session.get(TestIssue.class , formTestIssue.getId());

                Transaction tx = null;
                try {
                    tx = session.beginTransaction();
                    formTestIssueToSave.setDescription(descriptionString);
                    formTestIssueToSave.setApprovalState(selectedApprovalState);
                    formTestIssueToSave.setPriority(selectedPriority);
                    TestIssueHistory testIssueHistory = TestIssueHistoryService.Instance.setTestIssueHistoryByTestIssue(formTestIssueToSave);
                    TestIssueHistoryService.Instance.saveOrUpdate(testIssueHistory, session);

                    List<TestIssueHistory> testIssueHistoryList = null;
                    testIssueHistoryList = formTestIssueToSave.getHistories();

                    if (testIssueHistoryList == null)
                        testIssueHistoryList = new ArrayList<TestIssueHistory>();

                    testIssueHistoryList.add(testIssueHistory);

                    formTestIssueToSave.setHistories(testIssueHistoryList);


//                    AppPackageHistory appPackageHistory = AppPackageHistoryService.Instance.setAppPackageHistoryByAppPackage(loadedAppPackage, session);

                    if (formTestIssue != null && formTestIssue.getId() == null) {
                        loadedAppPackage.getTestIssues().add(formTestIssueToSave);
                    }

//                    if (appPackageHistory!=null) {
//                        if (formTestIssue != null && formTestIssue.getId() != null) {
//                            appPackageHistory.setHasTestIssueChange(true);
//                            appPackageHistory.setChangedTestIssue(testIssue.getId());
//                        }
//                        AppPackageHistoryService.Instance.saveOrUpdate(appPackageHistory, session);

                    List<AppPackageHistory> appPackageHistories = null;
                    appPackageHistories = loadedAppPackage.getHistories();

                    if (appPackageHistories == null)
                        appPackageHistories = new ArrayList<AppPackageHistory>();

//                        appPackageHistories.add(appPackageHistory);

//                        loadedAppPackage.setHistories(appPackageHistories);
//                    }
                    AppPackageService.Instance.saveOrUpdate(loadedAppPackage, session);


                    TestIssueServices.Instance.saveOrUpdate(formTestIssueToSave, session);
                    tx.commit();
                    childFinished(target, new Model<>(), this);
                } catch (Exception ex) {
                    if (tx != null)
                        tx.rollback();
                    processException(target, ex);
                } finally {
                    session.close();
                }
            }
        });

        add(new AjaxLink("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                childFinished(target, null, this);
            }
        });

        descriptionGetBehaviour = new AjaxEventBehavior("dasd") {
            @Override
            protected void onEvent(AjaxRequestTarget target) {
                String changeLog = getRequest().getRequestParameters().getParameterValue("changeLog").toString();
                String convertedChangeLog = null;
                try {
                    convertedChangeLog = URLDecoder.decode(changeLog, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    processException(target, e);
                }

                description.setModelObject(convertedChangeLog);
                descriptionString = convertedChangeLog;
            }
        };
        form.add(descriptionGetBehaviour);

    }


    public List<IColumn> getColumns() {
        List<IColumn> columnList = new ArrayList<IColumn>();
        columnList.add(new IndexColumn());

        columnList.add(new PropertyColumn(new ResourceModel("TestSubIssue.user"), "ent.testUser", "testUser"));
        columnList.add(new PropertyColumn(new ResourceModel("TestIssue.approvalState"), "ent.approvalState", "approvalState"));

        return columnList;

    }

    public static class TestSubIssueSortableDataProvider extends SortableDataProvider {
        TestSubIssueService.TestSubIssueCriteria criteria;

        public TestSubIssueSortableDataProvider() {
            setSort("ent.id", SortOrder.ASCENDING);
        }

        public void setCriteria(TestSubIssueService.TestSubIssueCriteria criteria) {
            this.criteria = criteria;
        }

        @Override
        public Iterator iterator(long first, long count) {
            Session session = HibernateUtil.getCurrentSession();
            SortParam sortParam = getSort();
            return TestSubIssueService.Instance.list(criteria, (int) first, (int) count, (String) sortParam.getProperty(), sortParam.isAscending(), session).iterator();
        }

        @Override
        public long size() {
            Session session = HibernateUtil.getCurrentSession();
            return TestSubIssueService.Instance.count(criteria, session);
        }

        @Override
        public IModel model(Object o) {
            return new Model((Serializable) o);
        }
    }
}
