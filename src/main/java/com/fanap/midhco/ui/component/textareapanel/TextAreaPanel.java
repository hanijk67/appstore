package com.fanap.midhco.ui.component.textareapanel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.StringValue;

/**
 * Created by admin123 on 6/25/2016.
 */
public class TextAreaPanel extends FormComponentPanel {
    TextArea textArea;
    String content;
    AjaxEventBehavior ajaxEventBehavior;
    Boolean isAjaxy;

    public TextAreaPanel(String id, String content, boolean isReadonly, String minWidth) {
        this(id, content, isReadonly, false, minWidth);
    }

    public TextAreaPanel(String id, String content, boolean isReadonly) {
        this(id, content, isReadonly, false);
    }

    public TextAreaPanel(String id, String content, boolean isReadonly, boolean isAjaxy) {
        this(id, content, isReadonly, isAjaxy, null);
    }

    public TextAreaPanel(String id, String content, boolean isReadonly, boolean isAjaxy, String minWidth) {
        super(id);

        this.isAjaxy = isAjaxy;

        textArea = new TextArea("content", new Model(content));
        if (isReadonly) {
            textArea.add(new AttributeModifier("readonly", new Model("readonly")));
            textArea.add(new AttributeAppender("class", new Model("readonly"), " "));
            textArea.add(new AttributeAppender("style", new Model("background-color:transparent"), ";"));
        }

        textArea.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(textArea);

        if (isAjaxy) {
            ajaxEventBehavior = new AjaxEventBehavior("rwrwrwer") {
                @Override
                protected void onEvent(AjaxRequestTarget target) {
                    StringValue contentValue = getRequest().getRequestParameters().getParameterValue("content");
                    TextAreaPanel.this.setConvertedInput(contentValue.toString());
                    TextAreaPanel.this.onUpdate(target);
                }
            };

            add(ajaxEventBehavior);


        }

        if(minWidth != null && !minWidth.isEmpty()) {
            textArea.add(new AttributeAppender("style", new Model("min-width:" + minWidth + "px"), ";"));
        }

    }

    public TextAreaPanel(String id, String content) {
        this(id, content, false);
    }

    public TextAreaPanel(String id) {
        this(id, "", false);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        if(isAjaxy) {
            response.render(OnDomReadyHeaderItem.forScript("$('#" + textArea.getMarkupId() + "').change(function() {" +
                    "Wicket.Ajax.post({'u':'" + ajaxEventBehavior.getCallbackUrl() + "' + '&content=' + $(this).val()});" +
                    "});"));
        }
        response.render(OnDomReadyHeaderItem.forScript("$('#" + textArea.getMarkupId() + "').autosize();"));
    }

    @Override
    public void convertInput() {
        setConvertedInput(textArea.getConvertedInput());
    }

    @Override
    public void onBeforeRender() {
        try {
            String content = (String) getModelObject();
            if (content != null) {
                textArea.setModel(new Model(content));
            }
        } catch (Exception ex) {
            setModel(new Model());
        }
        super.onBeforeRender();
    }

    public void onUpdate(AjaxRequestTarget target) {
    }
}