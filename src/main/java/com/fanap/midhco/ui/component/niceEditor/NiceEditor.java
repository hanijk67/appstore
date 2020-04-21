package com.fanap.midhco.ui.component.niceEditor;

import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextArea;

import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;


/**
 * Created by A.Moshiri on 4/26/2017.
 */
public class NiceEditor extends FormComponentPanel {

    TextArea textArea;
    String minHeight;

    public String getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(String minHeight) {
        this.minHeight = minHeight;
    }

    private static ResourceReference JAVASCRIPT = new JavaScriptResourceReference(
            NiceEditor.class, "res/nicEdit.js");


    public NiceEditor(String id) {
        this(id, "");
    }

    public NiceEditor(String id, String minHeight) {
        super(id);

        textArea = new TextArea("content");
        textArea.setModel(new Model<>());
        textArea.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        if (minHeight != null && !minHeight.isEmpty()) {
            textArea.add(new AttributeAppender("style", new Model("min-height:" + minHeight + "px"), ";"));
            this.minHeight = minHeight;
        }
        add(textArea);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(JAVASCRIPT));
        response.render(OnDomReadyHeaderItem.forScript(

                " var myEditor = new nicEditor({fullPanel : true ,iconsPath:'/appStore2/nicEditorIcons.gif', maxHeight : 250})" +
                        ".panelInstance('" + textArea.getMarkupId() + "');" +
                        "myEditor.addEvent('blur', function() {" +
                        "nicEditors.findEditor('" + textArea.getMarkupId() + "').saveContent();" +
                        "$('#" + textArea.getMarkupId() + "').text($('#" + textArea.getMarkupId() + "').val());" +
                        "$('#" + textArea.getMarkupId() + "').change();" +
                        "try {" +
                        "nicEditors.findEditor('" + textArea.getMarkupId() + "').setContent($('#" + textArea.getMarkupId() + "').val());" +
                        "nicEditors.findEditor('" + textArea.getMarkupId() + "').saveContent();" +
                        "if (typeof('#" + textArea.getMarkupId() + "') != 'undefined') {" +
                        "$('#" + textArea.getMarkupId() + "').text($('#" + textArea.getMarkupId() + "').val());" +
                        "$('#" + textArea.getMarkupId() + "').change();" +
                        "} else {" +
                        "console.log('undefined variable in nice Editor');" +
                        "}" +
                        "}" +
                        "catch(err) {" +
                        "console.log('in catch in nice Editor');" +
                        "}" +
                        "finally {" +
                        "$('#" + textArea.getMarkupId() + "').text($('#" + textArea.getMarkupId() + "').val());" +
                        "$('#" + textArea.getMarkupId() + "').change();" +
                        "}" +

                        "});"
                        + "var niceDiv = $('#" + textArea.getMarkupId() + "').closest('div[jid=\"niceEditorParentDiv\"]');" +
                        getNicDivInfo()

        ));
    }

    private String getNicDivInfo() {
        StringBuffer nicDivInfo = new StringBuffer("");

        nicDivInfo.append("$(niceDiv).find('.nicEdit-main').width('100%');");
        nicDivInfo.append("$(niceDiv).find('.nicEdit-panelContain').parent().width('100%');");
        nicDivInfo.append("$(niceDiv).find('.nicEdit-panelContain').parent().next().width('100%');");
        return nicDivInfo.toString();
    }

    @Override
    public void convertInput() {
//        super.convertInput();
        textArea.setConvertedInput(textArea.getConvertedInput());
    }

    @Override
    public void onBeforeRender() {
        if (getModel() != null) {
            String content = (String) getModelObject();
            if (content != null) {
                textArea.setModelObject(content);
            }
        }
        super.onBeforeRender();
    }

}