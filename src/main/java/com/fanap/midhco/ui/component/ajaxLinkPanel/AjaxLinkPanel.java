package com.fanap.midhco.ui.component.ajaxLinkPanel;

import com.fanap.midhco.ui.component.HasLabel;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

import java.util.Map;

public abstract class AjaxLinkPanel extends Panel implements HasLabel {
    public enum Image {
        Add("images/table/add.png", new ResourceModel("label.add")),
        Minus("images/table/minus.png", new ResourceModel("label.delete")),
        Edit("images/table/edit.png", new ResourceModel("label.edit")),
        View("images/table/view.png", new ResourceModel("label.view")),
        Delete("images/table/cross.png", new ResourceModel("label.delete")),
        Version("images/history.png", new ResourceModel("label.version")),
        History("images/history.png", new ResourceModel("label.history")),
        Download("images/download.png", new ResourceModel("label.download")),
        Refresh("images/table/refresh-icon.png", new ResourceModel("label.refresh")),
        SetScheduler("images/setScheduler.png", new ResourceModel("label.setScheduler")),
        UnSetScheduler("images/unSetScheduler.png", new ResourceModel("label.unSetScheduler")),
        Test("images/test.png", new ResourceModel("label.test"));

        private String url;
        private IModel title;

        private Image(String url, IModel title) {
            this.url = url;
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public IModel getTitle() {
            return title;
        }
    }

    private ContextImage linkImage = new ContextImage("linkImage");
    private Label linkLabel = new Label("linkLabel");
    private AjaxLink link;
    private IModel label;
    private String confirmationMessage;
    private Map<String, String> parameters;


    {
        final AjaxEventBehavior asyncEvenBehaviour = new AjaxEventBehavior("salam") {
            @Override
            protected void onEvent(AjaxRequestTarget target) {
                AjaxLinkPanel.this.onClick(target);
            }
        };
        add(asyncEvenBehaviour);

        link = new AjaxLink("link") {
            public void onClick(AjaxRequestTarget target) {
                AjaxLinkPanel.this.onClick(target);
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);

                attributes.setEventPropagation(AjaxRequestAttributes.EventPropagation.STOP);

                String str = "";
                if (confirmationMessage != null) {

                    AjaxCallListener ajaxCallListener = new AjaxCallListener();
                    attributes.getAjaxCallListeners().add(ajaxCallListener);

                    str = asyncEvenBehaviour.getCallbackUrl().toString();

                    str = "Wicket.Ajax.get({u:'" +
                            str
                            + "'});";

                    if (parameters == null) {
                        str = String.format("launchConfirmDialog('%s', '', \"" + str + "\");",
                                getString(confirmationMessage)) + ";";

                    } else {
                        String confirmationText = getString(confirmationMessage);
                        for (String paramName : parameters.keySet()) {
                            confirmationText =
                                    confirmationText.replace("${" + paramName + "}", parameters.get(paramName));
                        }
                        str = String.format("launchConfirmDialog('%s', '', '" + str + "');",
                                getString(confirmationMessage)) + ";";
                    }

                    ajaxCallListener.onPrecondition(str + "return false;");
                }
            }
        };
        link.add(linkImage);
        link.add(linkLabel);
        add(link);
    }

    protected AjaxLinkPanel(String id, Image image) {
        this(id, image.getUrl(), image.getTitle());
    }

    protected AjaxLinkPanel(String id, String url, IModel title) {
        super(id);
        linkImage.add(new AttributeAppender("title", title, " "));
        linkImage.add(new AttributeAppender("src", url, " "));
        linkLabel.setVisible(false);
    }

    protected AjaxLinkPanel(String id, IModel label) {
        super(id);
        linkImage.setVisible(false);
        linkLabel.setDefaultModel(label);
    }

    public abstract void onClick(AjaxRequestTarget target);

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        link.setEnabled(isEnabled());
    }

    public IModel getLabel() {
        return label;
    }

    public Component setLabel(IModel model) {
        label = model;
        return this;
    }

    public Component setConfirmationMessage(String confirmationMessage) {
        this.confirmationMessage = confirmationMessage;
        return this;
    }

    public Component setConfirmationMessage(String confirmationMessage, Map<String, String> parameters) {
        this.confirmationMessage = confirmationMessage;
        this.parameters = parameters;
        return this;
    }
}
