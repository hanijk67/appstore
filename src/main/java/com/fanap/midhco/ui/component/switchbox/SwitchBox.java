package com.fanap.midhco.ui.component.switchbox;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.StringValue;

/**
 * Created by admin123 on 9/25/2015.
 */
public class SwitchBox extends FormComponentPanel {
    CheckBox checkBox;
    AjaxEventBehavior changeAjaxBehaviour;
    WebMarkupContainer surronding;
    boolean isAjax;

    private static ResourceReference JAVASCRIPT = new JavaScriptResourceReference(
            SwitchBox.class, "res/bootstrap-switch.js");

    private static CssResourceReference CSS = new CssResourceReference(SwitchBox.class, "res/bootstrap-switch.css");

    public SwitchBox(String id, String onText, String offText) {
        this(id, onText, offText, false);
    }


    public SwitchBox(Boolean defaultState ,String id, String onText, String offText) {

        this( defaultState, id, onText, offText, null ,false);
    }

    public SwitchBox(String id, String onText, String offText, Model model) {
        this(null , id, onText, offText, model, false);
    }

    public SwitchBox(String id, String onText, String offText, boolean isAjax) {
        this(null , id, onText, offText, null, isAjax);
    }



    public SwitchBox(Boolean defaultState ,  String id, String onText, String offText, Model model, boolean isAjax) {
        super(id);

        setModel(model);

        this.isAjax = isAjax;

        surronding = new WebMarkupContainer("surronding");

        add(surronding);

        checkBox = new CheckBox("checkBox");
        checkBox.setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true);
        if (defaultState==null) {
        checkBox.setModel(new Model());
        }else {
            checkBox.setModel(new Model<>(defaultState));
        }
        checkBox.add(new AttributeModifier("data-on-text",  new Model(onText)));
        checkBox.add(new AttributeModifier("data-off-text", new Model(offText)));
        surronding.add(checkBox);

        changeAjaxBehaviour = new AjaxEventBehavior("onChangeBehaviour") {
            @Override
            protected void onEvent(AjaxRequestTarget ajaxRequestTarget) {
                StringValue currentStateValue = getRequest().getRequestParameters().getParameterValue("state");
                String currentStringValue = currentStateValue.toString();
                if(currentStringValue.equals("true")) {
                    onChange(ajaxRequestTarget, true);
                } else {
                    onChange(ajaxRequestTarget, false);
                }
            }
        };

        add(changeAjaxBehaviour);
    }

    @Override
    protected void onBeforeRender() {
        Object object = getModelObject();
        if(object != null) {
            checkBox.setModelObject((Boolean) object);
        }
        super.onBeforeRender();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(JavaScriptHeaderItem.forReference(JAVASCRIPT));
        response.render(CssHeaderItem.forReference(CSS));

        if (isAjax) {
            response.render(OnDomReadyHeaderItem.forScript("$('#" + checkBox.getMarkupId() + "').bootstrapSwitch({onSwitchChange : function() {" +
                        "Wicket.Ajax.post({'u': '" + changeAjaxBehaviour.getCallbackUrl() + "&state=' + this.checked});"
                    +"}});"));
        } else {
        response.render(OnDomReadyHeaderItem.forScript("$('#" + checkBox.getMarkupId() + "').bootstrapSwitch();"));
        }

        super.renderHead(response);
    }

    protected void onChange(AjaxRequestTarget target, Boolean currentState) {
    }

    @Override
    public void convertInput() {
        setConvertedInput(checkBox.getConvertedInput());
    }
}
