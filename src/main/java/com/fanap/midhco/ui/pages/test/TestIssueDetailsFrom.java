package com.fanap.midhco.ui.pages.test;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.Device;
import com.fanap.midhco.appstore.entities.TestSubIssue;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.table.MyAjaxDataTable;
import com.fanap.midhco.ui.component.table.column.IndexColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by A.Moshiri on 7/2/2017.
 */
public class TestIssueDetailsFrom extends BasePanel {
    private static ResourceReference TEST_ISSUE_SCRIPT =
            new JavaScriptResourceReference(TestSubIssueForm.class, "res/testIssue.js");
    Form form;
    MyAjaxDataTable table;
    Label userNameLabel;
    Label approvalStateLabel;
    Label formName;
    Label testSubIssueDescriptionLbl;
    Label testSubIssueDevices;
    String testSubIssueDescription;


    public TestIssueDetailsFrom(String id, TestSubIssue testSubIssue) {
        super(id);
        form = new Form("form", new CompoundPropertyModel(testSubIssue));

        formName = new Label("formName");
        formName.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        formName.setDefaultModel(new ResourceModel("label.testSubIssue.details"));
        add(formName);

        testSubIssueDescription = (testSubIssue == null ? "" : testSubIssue.getDescription());

        testSubIssueDescriptionLbl = new Label("testSubIssueDescriptionLbl");
        testSubIssueDescriptionLbl.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        testSubIssueDescriptionLbl.setDefaultModel(new ResourceModel("label.testSubIssue.description"));
        form.add(testSubIssueDescriptionLbl);

        testSubIssueDevices = new Label("testSubIssueDevices");
        testSubIssueDevices.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        testSubIssueDevices.setDefaultModel(new ResourceModel("label.testSubIssue.devices"));
        form.add(testSubIssueDevices);

        userNameLabel = new Label("testUser");
        if (testSubIssue != null && testSubIssue.getTestUser() != null) {
            userNameLabel.setDefaultModel(new Model<>(testSubIssue.getTestUser() != null ?
                    testSubIssue.getTestUser().toString() : ""));
        }
        userNameLabel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(userNameLabel);

        approvalStateLabel = new Label("approvalState");
        if (testSubIssue != null && testSubIssue.getApprovalState() != null) {
            approvalStateLabel.setDefaultModel(new Model<>(testSubIssue.getApprovalState()));
        }
        approvalStateLabel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(approvalStateLabel);

        List<Device> deviceList = testSubIssue.getDevices();

        int rowsPerPage =
                Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.TEST_ISSUE_LIST_ROWS_PER_PAGE));
        table = new MyAjaxDataTable("table", getColumns(), deviceList, rowsPerPage);
        table.setSelectionMode(SelectionMode.None);
        table.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        form.add(table);
        add(form);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(TEST_ISSUE_SCRIPT));
        response.render(OnDomReadyHeaderItem.forScript("showHistoryMessage('" + testSubIssueDescription +"');"));
    }

    private List<IColumn> getColumns() {
        List<IColumn> columnList = new ArrayList<IColumn>();
        columnList.add(new IndexColumn());
        columnList.add(new PropertyColumn(new ResourceModel("Device.title"), "ent.title", "title"));
        columnList.add(new PropertyColumn(new ResourceModel("Device.imei"), "ent.imei", "imei"));

        return columnList;
    }
}
