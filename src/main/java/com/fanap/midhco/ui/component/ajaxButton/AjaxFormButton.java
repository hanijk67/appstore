package com.fanap.midhco.ui.component.ajaxButton;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

import java.util.List;

public abstract class AjaxFormButton extends AjaxButton {
    private FeedbackPanel feedback;
    protected AjaxFormButton(String id, Form form) {
        this(id, form, null);
    }

    // Main Constructor
    protected AjaxFormButton(String id, Form form, FeedbackPanel feedback) {
        super(id, form);
        setOutputMarkupId(true);
        this.feedback = feedback;
        if (feedback != null)
            feedback.setOutputMarkupId(true);
    }

    @Override
    protected void onError(AjaxRequestTarget target, Form form) {
        if (feedback != null)
            target.add(feedback);
        else {
            List list = Session.get().getFeedbackMessages().toList();
//            if(list.isEmpty()) {
//                onSubmit(target, form);
//                return;
//            }
            StringBuilder builder = new StringBuilder();
            for (Object msg : list) {
                FeedbackMessage message = (FeedbackMessage) msg;
                builder.append(message.getMessage().toString().replace("'", "\\'")).append("<br>");
            }
            target.appendJavaScript(String.format("showMessage('%s');", builder.toString()));
        }
    }

    protected void onSubmit(AjaxRequestTarget target, Form form) {
        onSubmit(form, target);
        if (feedback != null)
            target.add(feedback);
    }

    protected abstract void onSubmit(Form form, AjaxRequestTarget target);
}
