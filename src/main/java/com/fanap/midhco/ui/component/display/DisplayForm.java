package com.fanap.midhco.ui.component.display;

import com.fanap.midhco.appstore.applicationUtils.AppUtils;
import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.TimeLineFileType;
import com.fanap.midhco.appstore.restControllers.vos.FileVO;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.media.video.Video;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.DynamicImageResource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by A.Moshiri on 10/9/2018.
 */
public class DisplayForm extends FormComponentPanel implements IParentListner {
    Form form;
    FeedbackPanel feedbackPanel;
    BootStrapModal modal;
    NonCachingImage tmpIconFile;
    PageableListView listView;
    Model fileListModel = new Model();
    List<FileVO> fileVOList;
    List<FileVO> finalFileVOList;
    List<FileVO> selectedFileVOS = new ArrayList<>();
    NonCachingImage nonCachingImage;
    AjaxCheckBox selectedAjaxCheckBox;
    Label selectedLbl;
    Video video;

    private static CssResourceReference CSS = new CssResourceReference(DisplayForm.class, "res/DisplayForm.css");


    public DisplayForm(String id, List<FileVO> inputFileVOList, String selectedLblStr) {
        super(id);
        fileVOList = inputFileVOList;
        feedbackPanel = new FeedbackPanel("feedbackPanel");
        feedbackPanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(feedbackPanel);

        modal = new BootStrapModal("modal");
        add(modal);
        Model selectedMdl = new Model(String.valueOf("..."));


        form = new Form("form");

        form.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);


        int itemPerPage = Integer.valueOf(ConfigUtil.getProperty(ConfigUtil.ITEM_PER_PAGE));

        listView = new PageableListView<FileVO>("rows", fileListModel, itemPerPage) {
            @Override
            protected void populateItem(ListItem<FileVO> item) {
                Boolean showImageData = false;
                Boolean showMovieData = false;
                FileVO fileVOInList = item.getModelObject();

                if (fileVOInList.getFileType() != null) {
                    if (fileVOInList.getFileType().equals(TimeLineFileType.IMAGE.getState())) {
                        showImageData = true;
                    } else if (fileVOInList.getFileType().equals(TimeLineFileType.VIDEO.getState())) {
                        showMovieData = true;
                    }
                }
                Label tmpPathLabel = new Label("fileKey", new PropertyModel(item.getModel(), "fileKey"));

                selectedAjaxCheckBox = new AjaxCheckBox("selectedItem", new PropertyModel(item.getModel(), "selected")) {
                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        boolean isSelected = getConvertedInput();
                        FileVO FileVoInList = item.getModelObject();
                        if (isSelected) {
                            selectedFileVOS.add(FileVoInList);
                        } else {
                            if (selectedFileVOS.contains(FileVoInList)) {
                                selectedFileVOS.remove(FileVoInList);
                            }
                        }
                    }
                };
                Model checkboxTextModel = Model.of("Default text");
                if (selectedLblStr != null && !selectedLblStr.trim().equals("")) {
                    checkboxTextModel.setObject(selectedLblStr);
                }
                Label checkboxText = new Label("checkboxText", checkboxTextModel);
                selectedAjaxCheckBox.add(checkboxText);

                video = new Video("video", fileVOInList.getFileKey());
                video.setWidth(500);
                video.setHeight(400);

                nonCachingImage = new NonCachingImage("image", new DynamicImageResource() {
                    @Override
                    protected byte[] getImageData(Attributes attributes) {
                        if (fileVOInList.getFileType() != null &&
                                (fileVOInList.getFileType().equals(TimeLineFileType.IMAGE.getState()) || fileVOInList.getFileType().equals(TimeLineFileType.IMAGE_AND_VIDEO.getState()))) {
                            return AppUtils.getImageAsBytes(tmpPathLabel.getDefaultModelObjectAsString());
                        } else {
                            return null;
                        }
                    }
                });
                if (showImageData) {
                    nonCachingImage.setVisible(true);
                } else {
                    nonCachingImage.setVisible(false);
                }

                if (showMovieData) {
                    video.setVisible(true);
                } else {
                    video.setVisible(false);
                }

                if (showImageData || showMovieData) {
                    selectedAjaxCheckBox.setVisible(true);
                } else {
                    selectedAjaxCheckBox.setVisible(false);
                }

                item.add(selectedAjaxCheckBox);

                item.add(nonCachingImage);
                item.add(video);


            }
        };
        listView.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(listView);
        add(form);

    }


    @Override
    protected void onBeforeRender() {
        finalFileVOList = new ArrayList<>();
        for (FileVO fileVO : fileVOList) {
            if (fileVO != null && fileVO.getFileType() != null &&
                    (fileVO.getFileType().equals(TimeLineFileType.IMAGE.getState()) || fileVO.getFileType().equals(TimeLineFileType.VIDEO.getState()) || fileVO.getFileType().equals(TimeLineFileType.VIDEO.getState()))) {
                finalFileVOList.add(fileVO);
            }
        }

        fileListModel.setObject((Serializable) finalFileVOList);

        super.onBeforeRender();
    }


    @Override
    public void convertInput() {
        List<Long> selectedFileVoIds = new ArrayList<>();
        for (FileVO fileVO : selectedFileVOS) {
            Long fileId = fileVO.getFileId();
            selectedFileVoIds.add(fileId);
        }
        setConvertedInput(selectedFileVoIds);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(CSS));
    }

    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {
        if (eventThrownCmp != null && eventThrownCmp.getId().equals("save"))
            target.add(this);
        modal.close(target);
    }
}
