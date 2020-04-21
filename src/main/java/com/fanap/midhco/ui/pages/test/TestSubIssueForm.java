package com.fanap.midhco.ui.pages.test;

import com.fanap.midhco.appstore.entities.ApprovalState;
import com.fanap.midhco.appstore.entities.Device;
import com.fanap.midhco.appstore.entities.TestIssue;
import com.fanap.midhco.appstore.entities.TestSubIssue;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.device.DeviceService;
import com.fanap.midhco.appstore.service.test.TestIssueServices;
import com.fanap.midhco.appstore.service.test.TestSubIssueService;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.mydropdown.MyDropDownChoicePanel;
import com.fanap.midhco.ui.component.niceEditor.NiceEditor;
import com.fanap.midhco.ui.component.table.MyAjaxDataTable;
import com.fanap.midhco.ui.pages.device.DeviceList;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by A.Moshiri on 6/28/2017.
 */
public class TestSubIssueForm extends BasePanel implements IParentListner {
    private static ResourceReference TEST_ISSUE_SCRIPT =
            new JavaScriptResourceReference(TestSubIssueForm.class, "res/testIssue.js");
    Form form;
    MyAjaxDataTable myAjaxDataTable;
    MyDropDownChoicePanel approvalState;
    Label priority;
    Label titleLbl;

    NiceEditor subIssueDescription;
    String descriptionString;
    AjaxEventBehavior descriptionGetBehaviour;
    DeviceList myDeviceList;
    List<Device> selectedDevice;
    Label deviceList;
    Label testSubIssueDescriptionLbl;
    Label testIssueDescriptionLbl;
    String testIssueDescription;

    public TestSubIssueForm(String id, TestIssueServices.TestIssueSearchModel testSearchModel) {
        super(id);
        selectedDevice = new ArrayList<>();
        testIssueDescription = (testSearchModel==null? "" :testSearchModel.getDescription());

        form = new Form("form", new CompoundPropertyModel(testSearchModel));
        form.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        testSubIssueDescriptionLbl = new Label("testSubIssueDescriptionLbl");
        testSubIssueDescriptionLbl.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        testSubIssueDescriptionLbl.setDefaultModel(new ResourceModel("label.testSubIssue.description"));
        form.add(testSubIssueDescriptionLbl);

        testIssueDescriptionLbl = new Label("testIssueDescriptionLbl");
        testIssueDescriptionLbl.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        testIssueDescriptionLbl.setDefaultModel(new ResourceModel("label.testIssue.description"));
        form.add(testIssueDescriptionLbl);

        titleLbl = new Label("title");
        StringBuilder titleStr = new StringBuilder(getString("TestIssue.title"));
        if (testSearchModel != null && testSearchModel.getTitle() != null) {
            titleStr.append(" : ");
            titleStr.append(testSearchModel.getTitle());
            titleLbl.setDefaultModel(new Model<>(titleStr));
        }
        form.add(titleLbl);

        priority = new Label("priority");
        StringBuilder priorityStr = new StringBuilder(getString("TestIssue.priority"));

        if (testSearchModel != null && testSearchModel.getPriority() != null) {
            priorityStr.append(" : ");
            priorityStr.append(testSearchModel.getPriority());
            priority.setDefaultModel(new Model<>(priorityStr));
        }
        form.add(priority);

        DeviceService.DeviceCriteria deviceCriteria = new DeviceService.DeviceCriteria();
        deviceCriteria.setActive(true);


        WebMarkupContainer appDevicePanel = new WebMarkupContainer("appDevicePanel");
        appDevicePanel.setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true);

        myDeviceList = new DeviceList("deviceList", deviceCriteria, SelectionMode.Multiple);
        myDeviceList.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        myDeviceList.setParentListner(this);
        appDevicePanel.add(myDeviceList);

        add(appDevicePanel);

        subIssueDescription = new NiceEditor("subIssueDescription", "50");
        subIssueDescription.setModel(new Model<>(""));
        subIssueDescription.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        subIssueDescription.setLabel(new ResourceModel("TestSubIssue.description"));

        form.add(subIssueDescription);

        List<ApprovalState> approvalStates = new ArrayList<>();
        approvalStates.add(ApprovalState.APPROVED);
        approvalStates.add(ApprovalState.DISAPPROVED);

        approvalState =
                new MyDropDownChoicePanel("approvalState", approvalStates, false, false, getString("TestIssue.approvalState"), 3, true, new ChoiceRenderer<>());
        approvalState.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        approvalState.setLabel(new ResourceModel("TestSubIssue.approvalState"));

        form.add(approvalState);

        AjaxFormButton ajaxFormButton = new AjaxFormButton("save", form) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("onclick", "var txt = $('#" + subIssueDescription.getMarkupId() + "').find('textarea').text();" +
                        "var res = encodeURIComponent(txt);" +
                        "Wicket.Ajax.get({u:'" +
                        descriptionGetBehaviour.getCallbackUrl().toString() +
                        "&changeLog=' + res"
                        + "});");
            }

            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {

                TestIssueServices.TestIssueSearchModel testSearchModel1 = (TestIssueServices.TestIssueSearchModel) form.getModelObject();
                ApprovalState selectedApprovalState = (ApprovalState) approvalState.getConvertedInput();
                String validationString = "";

                if (selectedApprovalState == null) {
                    validationString += " - " +
                            getString("Required").replace("${label}", approvalState.getLabel().getObject()) + "<br/>";
                }

                if (descriptionString == null || descriptionString.trim().equals("") || descriptionString.equals("<br>")) {
                    validationString += " - " +
                            getString("Required").replace("${label}", subIssueDescription.getLabel().getObject()) + "<br/>";
                }

                if (selectedDevice == null || selectedDevice.size() == 0) {
                    validationString += " - " +
                            getString("Required").replace("${label}", new ResourceModel("Device").getObject()) + "<br/>";
                } else {
                    for (Device device : selectedDevice) {
                        if (device.getActive() == null || !device.getActive()) {
                            validationString += " - " +
                                    getString("Device.invalid.selected.device") + "<br/>";
                            break;
                        }
                    }
                }

                if (!validationString.trim().isEmpty()) {
                    target.appendJavaScript("showMessage('" + validationString + "');");
                    return;
                }


                Session session = HibernateUtil.getNewSession();
                TestIssue testIssue = (TestIssue) session.load(TestIssue.class, testSearchModel1.getId());
                TestSubIssue testSubIssue = new TestSubIssue();
                testSubIssue.setDescription(descriptionString);
                testSubIssue.setTestUser(PrincipalUtil.getCurrentUser());
                testSubIssue.setDevices(selectedDevice);
                testSubIssue.setApprovalState(selectedApprovalState);
                Transaction tx = null;
                try {
                    tx = session.beginTransaction();
                    TestSubIssueService.Instance.saveOrUpdate(testSubIssue, session);
                    testIssue.getSubIssues().add(testSubIssue);
                    TestIssueServices.Instance.saveOrUpdate(testIssue, session);
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
        add(ajaxFormButton);

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

                subIssueDescription.setModelObject(convertedChangeLog);
                descriptionString = convertedChangeLog;
            }
        };
        form.add(descriptionGetBehaviour);

        add(new AjaxLink("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                childFinished(target, null, this);
            }
        });


        add(form);

    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(TEST_ISSUE_SCRIPT));
        response.render(OnDomReadyHeaderItem.forScript("showHistoryMessage('" + testIssueDescription +"');"));
    }

    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {
        if (eventThrownCmp != null && eventThrownCmp.getId().equals("setSelected")) {
            Set<DeviceService.DeviceSearchModel> deviceSearchModels = (Set<DeviceService.DeviceSearchModel>) childModel.getObject();
            if (deviceSearchModels != null && deviceSearchModels.size() != 0) {
                List<DeviceService.DeviceSearchModel> deviceSearchModelList = new ArrayList<DeviceService.DeviceSearchModel>(deviceSearchModels);
                Session session = HibernateUtil.getCurrentSession();
                for (DeviceService.DeviceSearchModel deviceSearchModel : deviceSearchModelList) {
                    // in this loop we have selected Devices
                    Device device = (Device) session.get(Device.class, deviceSearchModel.getId());
                    selectedDevice.add(device);
                }
            }

        }
    }
}
