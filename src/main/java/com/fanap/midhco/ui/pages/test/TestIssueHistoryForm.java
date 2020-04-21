package com.fanap.midhco.ui.pages.test;

import com.fanap.midhco.appstore.entities.TestIssue;
import com.fanap.midhco.appstore.service.test.TestIssueHistoryService;
import com.fanap.midhco.ui.BasePanel;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * Created by A.Moshiri on 7/23/2017.
 */
public class TestIssueHistoryForm extends BasePanel {
    Form form;
    String htmlHistoryMessage;
    private static ResourceReference TEST_HISTORY_SCRIPT =
            new JavaScriptResourceReference(TestIssueHistoryForm.class, "res/testIssue.js");

    protected TestIssueHistoryForm(String id, TestIssue testIssue, boolean requestFromAppPackage) {
        super(id);
        form = new Form("form");
        form.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        StringBuilder stringBuilder = new StringBuilder();
        for(String str :  TestIssueHistoryService.Instance.createHistoryMessage(testIssue, requestFromAppPackage)){
            stringBuilder.append(str);
        }
        htmlHistoryMessage =stringBuilder.toString();

        add(form);

    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(TEST_HISTORY_SCRIPT));
        response.render(OnDomReadyHeaderItem.forScript("showHistoryMessage('" + htmlHistoryMessage + "');"));
    }
}
