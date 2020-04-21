package com.fanap.midhco.ui.pages.test;

import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.app.AppPackageHistoryService;
import com.fanap.midhco.appstore.service.app.AppPackageService;
import com.fanap.midhco.appstore.service.test.TestIssueHistoryService;
import com.fanap.midhco.appstore.service.test.TestIssueServices;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.mydropdown.MyDropDownChoicePanel;
import com.fanap.midhco.ui.component.niceEditor.NiceEditor;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by A.Moshiri on 6/21/2017.
 */
public class TestIssueForm extends BasePanel {

    FeedbackPanel feedbackPanel;
    Form form;
    TextField titleTextField;
    MyDropDownChoicePanel approvalState;
    MyDropDownChoicePanel priority;
    NiceEditor description;
    String descriptionString;
    AjaxEventBehavior descriptionGetBehaviour;
    Label testIssueFormName;

    protected TestIssueForm(String id, TestIssue testIssue, AppPackage appPackage) {
        super(id);

        feedbackPanel = new FeedbackPanel("feedbackPanel");
        add(feedbackPanel);

        testIssueFormName = new Label("testIssueFormName");
        testIssueFormName.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        testIssueFormName.setDefaultModel(new ResourceModel("label.test.create.test.Issue"));
        add(testIssueFormName);

        form = new Form("form", new CompoundPropertyModel(testIssue));

        titleTextField = new TextField("title");
        titleTextField.setLabel(new ResourceModel("TestIssue.title"));
        titleTextField.setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true);
        form.add(titleTextField);

        description = new NiceEditor("description", "50");
        description.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        description.setLabel(new ResourceModel("TestIssue.description"));

        form.add(description);

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

        AjaxFormButton ajaxFormButton = new AjaxFormButton("save", getParentForm()) {
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

                if (appPackage == null || appPackage.getId() == null) {
                    validationString += " - " +
                            getString("Required").replace("${label}", new ResourceModel("APPPackage").getObject()) + "<br/>";
                }

                String titleString = titleTextField.getConvertedInput() == null ? null : titleTextField.getConvertedInput().toString();
                if (titleString == null || titleString.trim().equals("")) {
                    validationString += " - " +
                            getString("Required").replace("${label}", titleTextField.getLabel().getObject()) + "<br/>";
                }

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
                boolean needToUpdateAppPackage = false;
                Session session = HibernateUtil.getNewSession();
                Transaction tx = null;
                AppPackage loadedAppPackage = (AppPackage) session.load(AppPackage.class, appPackage.getId());
                loadedAppPackage.getTestIssues();
                loadedAppPackage.getTestGroups();
                loadedAppPackage.getThumbImages();
                try {
                    tx = session.beginTransaction();
                    formTestIssue.setTitle(titleString);
                    formTestIssue.setDescription(descriptionString);
                    formTestIssue.setApprovalState(selectedApprovalState);
                    formTestIssue.setPriority(selectedPriority);

                    TestIssueHistory testIssueHistory = TestIssueHistoryService.Instance.setTestIssueHistoryByTestIssue(formTestIssue);
                    TestIssueHistoryService.Instance.saveOrUpdate(testIssueHistory, session);

                    List<TestIssueHistory> testIssueHistoryList = null;
                    testIssueHistoryList = formTestIssue.getHistories();

                    if (testIssueHistoryList == null)
                        testIssueHistoryList = new ArrayList<TestIssueHistory>();

                    testIssueHistoryList.add(testIssueHistory);

                    formTestIssue.setHistories(testIssueHistoryList);

                    if (testIssue == null || testIssue.getId() == null) {
                        loadedAppPackage.getTestIssues().add(formTestIssue);
                        needToUpdateAppPackage = true;
                    }

                    TestIssueServices.Instance.saveOrUpdate(formTestIssue, session);
                    AppPackageHistory appPackageHistory = AppPackageHistoryService.Instance.setAppPackageHistoryByAppPackage(loadedAppPackage, session);
                    if (appPackageHistory!=null) {
                    if (testIssue != null && testIssue.getId() != null) {
                        appPackageHistory.setHasTestIssueChange(true);
                        appPackageHistory.setChangedTestIssue(testIssue.getId());
                    }
                    if (needToUpdateAppPackage) {
                        AppPackageHistoryService.Instance.saveOrUpdate(appPackageHistory, session);

                        List<AppPackageHistory> appPackageHistories = null;
                        appPackageHistories = loadedAppPackage.getHistories();

                        if (appPackageHistories == null)
                            appPackageHistories = new ArrayList<AppPackageHistory>();

                        appPackageHistories.add(appPackageHistory);

                        loadedAppPackage.setHistories(appPackageHistories);
                        AppPackageService.Instance.saveOrUpdate(loadedAppPackage, session);
                    }
                    }
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
        };
        form.add(ajaxFormButton);

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

        form.add(new AjaxLink("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                childFinished(target, null, this);
            }
        });
        add(form);
    }
}
