package com.fanap.midhco.ui.component.imageGalleryPanel;

import com.fanap.midhco.appstore.applicationUtils.AppUtils;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.ui.component.imagePanel.ImagePanel;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.DynamicImageResource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hamid on 7/3/2016.
 */
public class FileGalleyPanel extends FormComponentPanel {
    List<ImageDescriptor> imageDescriptors;
    WebMarkupContainer masterContainer;
    List<ActionPanel> actionPanels;
    public String confirmationMessage;
    final AjaxEventBehavior asyncEvenBehaviour;

    public static final ActionPanel ACTION_PANEL_VIEW = new ActionPanel(1, AppStorePropertyReader.getString("label.view"));
    public static final ActionPanel ACTION_PANEL_DELETE = new ActionPanel(2, AppStorePropertyReader.getString("label.delete"));

    private static CssResourceReference CSS_EFFECTS = new CssResourceReference(FileGalleyPanel.class, "res/effects.css");
    private static CssResourceReference CSS_EFFECTS_PRE = new CssResourceReference(FileGalleyPanel.class, "res/effectspre.css");

    BootStrapModal modal = new BootStrapModal("modal");

    public static class ImageDescriptor {
        public String imageTitle;
        public String thumbImage;
        public String imageSource;


        public String getImageTitle() {
            return imageTitle;
        }

        public void setImageTitle(String imageTitle) {
            this.imageTitle = imageTitle;
        }

        public String getThumbImage() {
            return thumbImage;
        }

        public void setThumbImage(String thumbImage) {
            this.thumbImage = thumbImage;
        }

        public String getImageSource() {
            return imageSource;
        }

        public void setImageSource(String imageSource) {
            this.imageSource = imageSource;
        }
    }

    public static class ActionPanel {
        private Integer id;
        private String actionMessage;

        private ActionPanel(Integer id, String title) {
            this.id = id;
            this.actionMessage = title;
        }

        public String getActionTitle() {
            return actionMessage;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ActionPanel))
                return false;
            return id.equals(((ActionPanel) o).id);
        }
    }


    private Loop constructImageDescriptors(List<ImageDescriptor> imageDescriptors) {
        this.imageDescriptors = imageDescriptors;
        Loop imageContainerLoop = new Loop("imageContainerLoop", imageDescriptors.size()) {
            int counter = 0;

            @Override
            protected void populateItem(LoopItem loopItem) {
                final ImageDescriptor currentImageDescriptor = imageDescriptors.get(counter);
                WebMarkupContainer imageItem = new WebMarkupContainer("imageItem");

                NonCachingImage image = new NonCachingImage("image", imageResources.get(counter));
                imageItem.add(image);
                image.setVisible(true);

                imageItem.add(new Label("title", new Model<>(currentImageDescriptor.getImageTitle())));

                AjaxLink viewLink = new AjaxLink("viewBtn") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {

                        ImagePanel imagePanel = new ImagePanel(
                                modal.getContentId(),
                                currentImageDescriptor.getImageSource(),
                                new Model<>(currentImageDescriptor.getImageTitle()));
                        modal.setContent(imagePanel);

                        modal.show(target);
                    }
                };
                imageItem.add(viewLink);
                if (!actionPanels.contains(ACTION_PANEL_VIEW)) {
                    viewLink.setVisible(false);
                }

                AjaxLink deleteLink = new AjaxLink("deleteBtn") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        onDeleteButtonClicked(target);
                    }

                    @Override
                    protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                        super.updateAjaxAttributes(attributes);

                        attributes.setEventPropagation(AjaxRequestAttributes.EventPropagation.STOP);

                        String str = "";
                        if (FileGalleyPanel.this.confirmationMessage != null) {

                            AjaxCallListener ajaxCallListener = new AjaxCallListener();
                            attributes.getAjaxCallListeners().add(ajaxCallListener);

                            str = asyncEvenBehaviour.getCallbackUrl().toString();

                            str = "Wicket.Ajax.get({u:'" +
                                    str
                                    + "'});";

                                str = String.format("launchConfirmDialog('%s', '', \"" + str + "\");",
                                        getString(confirmationMessage)) + ";";


                            ajaxCallListener.onPrecondition(str + "return false;");
                        }
                    }
                };
                imageItem.add(deleteLink);
                if (!actionPanels.contains(ACTION_PANEL_DELETE)) {
                    deleteLink.setVisible(false);
                }

                loopItem.add(imageItem);

                counter++;
            }
        };
        imageContainerLoop.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        return imageContainerLoop;
    }

    protected void onDeleteButtonClicked(AjaxRequestTarget target) {
    }

    List<DynamicImageResource> imageResources;
    public FileGalleyPanel(String id, List<ImageDescriptor> imageDescriptors, List<ActionPanel> actionPanels) {
        super(id);

        asyncEvenBehaviour = new AjaxEventBehavior("salam") {
            @Override
            protected void onEvent(AjaxRequestTarget target) {
                FileGalleyPanel.this.onDeleteButtonClicked(target);
            }
        };
        add(asyncEvenBehaviour);

        add(modal);

        this.actionPanels = actionPanels;

        masterContainer = new WebMarkupContainer("masterContainer");
        masterContainer.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        this.imageDescriptors = imageDescriptors;

        imageResources = new ArrayList<>();
        for (int i = 0; i < imageDescriptors.size(); i++) {
            final int j = i;
            imageResources.add(new DynamicImageResource() {
                @Override
                protected byte[] getImageData(Attributes attributes) {
                    return AppUtils.getImageAsBytes(imageDescriptors.get(j).getImageSource());
                }
            });
        }

        Loop imageContainerLoop = constructImageDescriptors(imageDescriptors);
        masterContainer.add(imageContainerLoop);

        add(masterContainer);
    }

    public void setImageDescriptors(AjaxRequestTarget target, List<ImageDescriptor> imageDescriptors) {
        Loop loop = constructImageDescriptors(imageDescriptors);
        masterContainer.get("imageContainerLoop").replaceWith(loop);
        target.add(masterContainer);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(CssHeaderItem.forReference(CSS_EFFECTS));
        response.render(CssHeaderItem.forReference(CSS_EFFECTS_PRE));

        super.renderHead(response);
    }

    public void setConfirmationMessage(String confirmationMessage) {
        this.confirmationMessage = confirmationMessage;
    }
}
