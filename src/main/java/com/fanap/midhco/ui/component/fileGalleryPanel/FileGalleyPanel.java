package com.fanap.midhco.ui.component.fileGalleryPanel;

import com.fanap.midhco.appstore.applicationUtils.AppUtils;
import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.applicationUtils.ImageFormat;
import com.fanap.midhco.appstore.service.myException.AppStoreRuntimeException;
import com.fanap.midhco.appstore.service.osType.IUploadFilter;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.component.ajaxDownload.AjaxDownload;
import com.fanap.midhco.ui.component.ajaxLinkPanel.AjaxLinkPanel;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.component.multiAjaxFileUpload2.UploadedFileInfo;
import com.fanap.midhco.ui.pages.uploadPanel.UploadPanel;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.util.resource.UrlResourceStream;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Hamid on 7/3/2016.
 */
public class FileGalleyPanel extends FormComponentPanel implements IParentListner {

    WebMarkupContainer masterContainer;
    List<ActionPanel> actionPanels;
    public String confirmationMessage;
    public String replaceConfirmationMessage;
    final AjaxEventBehavior asyncEvenBehaviour;
    final AjaxEventBehavior replaceAsyncEventBehaviour;
    AjaxDownload fileAjaxDownload;
    String fileDownloadTitle;
    String fileToDownloadPath;
    UploadPanel uploadPanel;
    Integer maxUploadCount;
    Integer imageSize;

    public static final ActionPanel ACTION_PANEL_VIEW = new ActionPanel(1, AppStorePropertyReader.getString("label.view"));
    public static final ActionPanel ACTION_PANEL_DELETE = new ActionPanel(2, AppStorePropertyReader.getString("label.delete"));
    public static final ActionPanel ACTION_PANEL_ADD = new ActionPanel(3, AppStorePropertyReader.getString("label.add"));
    public static final ActionPanel ACTION_PANEL_REPLACE = new ActionPanel(4, AppStorePropertyReader.getString("label.replace"));

    private static CssResourceReference CSS_EFFECTS = new CssResourceReference(FileGalleyPanel.class, "res/effects.css");
    private static CssResourceReference CSS_EFFECTS_PRE = new CssResourceReference(FileGalleyPanel.class, "res/effectspre.css");

    BootStrapModal modal = new BootStrapModal("modal");
    IUploadFilter uploadFilter;

    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {
        try {
            if (childModel != null && eventThrownCmp != null && eventThrownCmp.getId().equals("ok")) {
                Collection<UploadedFileInfo> uploadedFileInfos = (Collection<UploadedFileInfo>) childModel.getObject();
                if (uploadedFileInfos != null && !uploadedFileInfos.isEmpty()) {

                    for (UploadedFileInfo uploadedFileInfo : uploadedFileInfos) {
                        ImageDescriptor imageDescriptor = new ImageDescriptor();
                        imageDescriptor.setImageSource(uploadedFileInfo.getPhysicalLocation());
                        String emblemPath = getFileTypeImageSrc(uploadedFileInfo.getFileName());
                        if (emblemPath == null) {
                            int thumb_width = Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.APP_APPSTORE_THUMBIMAGE_WIDTH));
                            int thumb_height = Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.APP_APPSTORE_THUMBIMAGE_HEIGHT));
                            ImageFormat imageFormat = AppUtils.getImageFormat(uploadedFileInfo.getFileName());
                            if (imageFormat.equals(ImageFormat.UNKNOWN)) {
                                target.appendJavaScript("showMessage('" + getString("UNKNOWN.image.type") + " " +
                                        getString("label.file") + " :" + uploadedFileInfo.getFileName() + "');");
                                break;
                            }

                            String thumbFileName = AppUtils.getImageThumbNail(uploadedFileInfo.getPhysicalLocation(), uploadedFileInfo.getFileName());
                            emblemPath = "File:" + thumbFileName;
                        }
                        imageDescriptor.setThumbImage(emblemPath);

                        imageDescriptor.setImageTitle(uploadedFileInfo.getFileName());
                        imageDescriptor.setImageId(uploadedFileInfo.getFileId());

                        imageDescriptors.add(imageDescriptor);
                        if (maxUploadCount != null) {
                            if (imageDescriptors.size() > maxUploadCount)
                                for (int i = 0; i < imageDescriptors.size() - maxUploadCount; i++)
                                    imageDescriptors.remove(0);
                        }
                    }

                    Loop imageContainerLoop = constructImageDescriptors();
                    masterContainer.get("imageContainerLoop").replaceWith(imageContainerLoop);
                    target.add(masterContainer);
                    modal.close(target);
                }
            }
        } catch (Exception ex) {
            throw new AppStoreRuntimeException(ex.getMessage(), ex);
        }
    }

    public static class ImageDescriptor implements Serializable {
        public String imageId;
        public String imageTitle;
        public String thumbImage;
        public String imageSource;
        public boolean isImagePathRelative;
        public boolean isNew;

        public String getImageId() {
            return imageId;
        }

        public void setImageId(String imageId) {
            this.imageId = imageId;
        }

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

        public boolean isImagePathRelative() {
            return isImagePathRelative;
        }

        public void setImagePathRelative(boolean isImagePathRelative) {
            this.isImagePathRelative = isImagePathRelative;
        }

        public boolean isNew() {
            return isNew;
        }

        public void setNew(boolean isNew) {
            this.isNew = isNew;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ImageDescriptor))
                return false;
            return ((ImageDescriptor) o).getImageId().equals(this.getImageId());
        }
    }

    public static class ActionPanel implements Serializable {
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


    private Loop constructImageDescriptors() {
        imageResources = new ArrayList<>();

        for (int i = 0; i < imageDescriptors.size(); i++) {
            final int j = i;
            imageResources.add(new DynamicImageResource() {
                @Override
                protected byte[] getImageData(Attributes attributes) {
                    ImageDescriptor imageDescriptor = imageDescriptors.get(j);
                    return AppUtils.getImageAsBytes(imageDescriptor.getThumbImage());
                }
            });
        }

        Loop imageContainerLoop = new Loop("imageContainerLoop", imageDescriptors.size()) {
            int counter = 0;

            @Override
            protected void populateItem(LoopItem loopItem) {
                if (counter >= imageDescriptors.size())
                    counter = 0;
                final ImageDescriptor currentImageDescriptor = imageDescriptors.get(counter);
                WebMarkupContainer imageItem = new WebMarkupContainer("imageItem");

                int width = Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.APP_APPSTORE_THUMBIMAGE_WIDTH));
                int height = Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.APP_APPSTORE_THUMBIMAGE_HEIGHT));

                WebMarkupContainer hoverDiv = new WebMarkupContainer("hoverDiv");
                hoverDiv.add(new AttributeAppender("style", "width:" + width + "px;height:" + height + "px"));
                imageItem.add(hoverDiv);

                if (!currentImageDescriptor.isImagePathRelative()) {
                    NonCachingImage image = new NonCachingImage("image", imageResources.get(counter));
                    image.add(new AttributeAppender("width", width + "px"));
                    image.add(new AttributeAppender("height", height + "px"));
                    hoverDiv.add(image);
                    image.setVisible(true);
                } else {
                    ContextImage image = new ContextImage("image");
                    image.add(new AttributeAppender("src", currentImageDescriptor.getImageSource()));
                    image.add(new AttributeAppender("width", width + "px"));
                    image.add(new AttributeAppender("height", height + "px"));
                    hoverDiv.add(image);
                    image.setVisible(true);
                }

                hoverDiv.add(new Label("title", new Model<>(currentImageDescriptor.getImageTitle())));

                AjaxLink viewLink = new AjaxLink("viewBtn") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        fileDownloadTitle = currentImageDescriptor.getImageTitle();
                        fileToDownloadPath = currentImageDescriptor.getImageSource();
                        fileAjaxDownload.initiate(target);
                    }
                };
                hoverDiv.add(viewLink);
                if (!actionPanels.contains(ACTION_PANEL_VIEW)) {
                    viewLink.setVisible(false);
                }

                AjaxLink deleteLink = new AjaxLink("deleteBtn") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        imageDescriptors.remove(currentImageDescriptor);
                        counter = 0;
                        Loop imageContainerLoop = constructImageDescriptors();
                        masterContainer.get("imageContainerLoop").replaceWith(imageContainerLoop);
                        target.add(masterContainer);
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
                hoverDiv.add(deleteLink);
                if (!actionPanels.contains(ACTION_PANEL_DELETE)) {
                    deleteLink.setVisible(false);
                }

                AjaxLink replaceLink = new AjaxLink("replaceBtn") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                    }

                    @Override
                    protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                        super.updateAjaxAttributes(attributes);
                        attributes.setEventPropagation(AjaxRequestAttributes.EventPropagation.STOP);

                        String str = "";
                        if (FileGalleyPanel.this.replaceConfirmationMessage != null) {

                            AjaxCallListener ajaxCallListener = new AjaxCallListener();
                            attributes.getAjaxCallListeners().add(ajaxCallListener);

                            str = replaceAsyncEventBehaviour.getCallbackUrl().toString();

                            str = "Wicket.Ajax.get({u:'" +
                                    str
                                    + "'});";

                            str = String.format("launchConfirmDialog('%s', '', \"" + str + "\");",
                                    getString(replaceConfirmationMessage)) + ";";


                            ajaxCallListener.onPrecondition(str + "return false;");
                        }
                    }
                };
                hoverDiv.add(replaceLink);

                if(!actionPanels.contains(ACTION_PANEL_REPLACE)) {
                    replaceLink.setVisible(false);
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

    protected void onAddButtonClicked(AjaxRequestTarget target) {
    }

    protected void onReplaceButtonClicked(AjaxRequestTarget target) {
    }

    List<DynamicImageResource> imageResources;
    List<ImageDescriptor> imageDescriptors = new ArrayList<>();

    public FileGalleyPanel(String id, List<ActionPanel> actionPanels,
                           IUploadFilter uploadFilter, Integer maxUploadCount) {
        super(id);

        this.maxUploadCount = maxUploadCount;

        this.uploadFilter = uploadFilter;
        uploadFilter.getFilterList();

        asyncEvenBehaviour = new AjaxEventBehavior("salam") {
            @Override
            protected void onEvent(AjaxRequestTarget target) {
                FileGalleyPanel.this.onDeleteButtonClicked(target);
            }
        };
        add(asyncEvenBehaviour);

        replaceAsyncEventBehaviour = new AjaxEventBehavior("salam") {
            @Override
            protected void onEvent(AjaxRequestTarget target) {
                FileGalleyPanel.this.onReplaceButtonClicked(target);
            }
        };
        add(replaceAsyncEventBehaviour);



        add(modal);

        this.actionPanels = actionPanels;

        masterContainer = new WebMarkupContainer("masterContainer");
        masterContainer.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        Loop imageContainerLoop = constructImageDescriptors();
        masterContainer.add(imageContainerLoop);

        fileAjaxDownload = new AjaxDownload() {

            @Override
            protected String getFileName() {
                return fileDownloadTitle;
            }

            @Override
            protected IResourceStream getResourceStream() {
                try {
                    if (fileToDownloadPath.toUpperCase().startsWith("HTTP:")
                            || fileToDownloadPath.toUpperCase().startsWith("FILE:"))
                        return new UrlResourceStream(new URL(fileToDownloadPath));
                    else
                        return new FileResourceStream(new File(new URL(fileToDownloadPath).getFile()));

                } catch (Exception ex) {
                }
                return null;
            }
        };
        add(fileAjaxDownload);

        WebMarkupContainer addPanel = new AjaxLinkPanel("addPanel", AjaxLinkPanel.Image.Add) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                uploadPanel =
                        new UploadPanel(modal.getContentId(), Arrays.asList(uploadFilter), maxUploadCount, getString("label.add"));
                uploadPanel.setParentListner(FileGalleyPanel.this);
                modal.setContent(uploadPanel);
                modal.show(target);
            }
        };
        add(addPanel);
        if (!actionPanels.contains(ACTION_PANEL_ADD))
            addPanel.setVisible(false);

        add(masterContainer);
    }

    public void setImageDescriptors(AjaxRequestTarget target, List<ImageDescriptor> imageDescriptors) {
        if (imageDescriptors != null) {
            setModelObject(imageDescriptors);
            Loop loop = constructImageDescriptors();
            masterContainer.get("imageContainerLoop").replaceWith(loop);
            target.add(masterContainer);
        }
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

    public void setReplaceConfirmationMessage(String replaceConfirmationMessage) { this.replaceConfirmationMessage = replaceConfirmationMessage;}

    @Override
    public void convertInput() {
        setConvertedInput(imageDescriptors);
    }

    protected String getFileTypeImageSrc(String fileName) {
        String fileExtension = AppUtils.getFileExtension(fileName);
        String basePath = "http://" + getRequest().getOriginalUrl().getHost() + ":" +
                getRequest().getOriginalUrl().getPort() + getRequest().getContextPath();
        if (fileExtension != null) {
            if (fileExtension.equals("apk")) {
                return basePath + "/images/fileTypes/apk.png";
            } else if (fileExtension.toUpperCase().equals("JPG") ||
                    fileExtension.toUpperCase().equals("PNG")) {
                return null;
            }
        }

        return basePath + "/images/fileTypes/unknown.png";
    }

    @Override
    public void onBeforeRender() {
        Object o = getModelObject();
        if (o != null) {
            imageDescriptors = (List) o;
            masterContainer.get("imageContainerLoop").replaceWith(constructImageDescriptors());
        }
        super.onBeforeRender();
    }
}
