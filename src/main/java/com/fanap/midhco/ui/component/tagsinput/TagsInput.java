package com.fanap.midhco.ui.component.tagsinput;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * Created by A.Moshiri on 8/27/2017.
 */
public class TagsInput extends FormComponentPanel {

    private static CssResourceReference BOOTSTRAP_TAGS_INPUT_CSS = new CssResourceReference(TagsInput.class, "res/bootstrap-tagsinput.css");
    private static ResourceReference BOOTSTRAP_TAGS_INPUT_JS = new JavaScriptResourceReference(TagsInput.class, "res/bootstrap-tagsinput.js");
    private static ResourceReference BOOTSTRAP_TAGS_INPUT_ANGULAR_JS = new JavaScriptResourceReference(TagsInput.class, "res/bootstrap-tagsinput-angular.js");
    private static ResourceReference ANGULAR_JS = new JavaScriptResourceReference(TagsInput.class, "res/angular-1.6.1.js");

    TextField keyWordTextField;
    Model tagsModel;
    Integer maxTags;

    public TagsInput(String id) {
        this(id, null, null);
    }

    public TagsInput(String id, Model<String> keyWordModel, Integer maxTags) {
        super(id);

        tagsModel = keyWordModel;
        keyWordTextField = new TextField("keyWords");
        keyWordTextField.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        keyWordTextField.setModel(tagsModel);
        add(keyWordTextField);
    }


    @Override
    protected void onBeforeRender() {
        String model = (String) getModelObject();
        if (model != null && !model.trim().equals("")) {
            keyWordTextField.setModel(new Model<>(model));
        } else {
            keyWordTextField.setModel(new Model());
        }
                super.onBeforeRender();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.render(JavaScriptHeaderItem.forReference(ANGULAR_JS));
        response.render(JavaScriptHeaderItem.forReference(BOOTSTRAP_TAGS_INPUT_ANGULAR_JS));
        response.render(JavaScriptHeaderItem.forReference(BOOTSTRAP_TAGS_INPUT_JS));
        response.render(CssHeaderItem.forReference(BOOTSTRAP_TAGS_INPUT_CSS));

        if(maxTags!=null) {
            response.render(OnDomReadyHeaderItem.forScript(
                    "$('#" + keyWordTextField.getMarkupId() + "').tagsinput({\n" +
                            "  maxTags:"+ maxTags+" \n" +
                            "});"
            ));
        }else {
            response.render(OnDomReadyHeaderItem.forScript(
                    "$('#" + keyWordTextField.getMarkupId() + "').tagsinput({\n" +
                            " \n" +
                            "});"
            ));
        }
    }

    @Override
    public void convertInput() {
        super.convertInput();
        setConvertedInput(keyWordTextField.getConvertedInput());
//        keyWordTextField.setConvertedInput(keyWordTextField.getConvertedInput());
    }
}
