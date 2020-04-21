package com.fanap.midhco.ui.pages.app;

import com.fanap.midhco.appstore.entities.AppPackage;
import com.fanap.midhco.appstore.service.app.AppPackageHistoryService;
import com.fanap.midhco.ui.BasePanel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * Created by A.Moshiri on 7/18/2017.
 */
public class AppPackageHistoryForm extends BasePanel {
    Form form;
    String htmlHistoryMessage;
    private static ResourceReference APP_PACKAGE_HISTORY_SCRIPT =
            new JavaScriptResourceReference(AppHistoryForm.class, "res/appHistory.js");


    public AppPackageHistoryForm(String id, AppPackage appPackage, boolean fromApp) {
        super(id);
        form = new Form("form");
        form.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : AppPackageHistoryService.Instance.createHistoryMessage(appPackage, fromApp)) {
            stringBuilder.append(str);
        }
        htmlHistoryMessage = stringBuilder.toString();
        form.add(new AjaxLink("cancel") {
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
        response.render(JavaScriptHeaderItem.forReference(APP_PACKAGE_HISTORY_SCRIPT));
        response.render(OnDomReadyHeaderItem.forScript("showHistoryMessage('" + htmlHistoryMessage + "');"));
    }
}
