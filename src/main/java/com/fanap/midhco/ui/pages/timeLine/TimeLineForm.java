package com.fanap.midhco.ui.pages.timeLine;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.applicationUtils.MyCalendarUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.*;
import com.fanap.midhco.appstore.restControllers.vos.FileVO;
import com.fanap.midhco.appstore.restControllers.vos.OrganizationVO;
import com.fanap.midhco.appstore.restControllers.vos.TimeLineVO;
import com.fanap.midhco.appstore.service.BaseEntityService;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.engine.EngineOrganizationService;
import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import com.fanap.midhco.appstore.service.org.OrgService;
import com.fanap.midhco.appstore.service.osType.IUploadFilter;
import com.fanap.midhco.appstore.service.timeLine.TimeLineElasticService;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.dateTimePanel.DateTimePanel;
import com.fanap.midhco.ui.component.display.DisplayForm;
import com.fanap.midhco.ui.component.limitedTextField.LimitedTextField;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.component.multiAjaxFileUpload2.MultiAjaxFileUploadPanel2;
import com.fanap.midhco.ui.component.multiAjaxFileUpload2.UploadedFileInfo;
import com.fanap.midhco.ui.component.mydropdown.MyDropDownChoicePanel;
import com.fanap.midhco.ui.component.niceEditor.NiceEditor;
import com.fanap.midhco.ui.component.switchbox.SwitchBox;
import com.fanap.midhco.ui.component.tagsinput.TagsInput;
import com.fanap.midhco.ui.component.treeview.ITreeNodeProvider;
import com.fanap.midhco.ui.component.treeview.TreeNode;
import com.fanap.midhco.ui.component.treeview.TreeViewPanel;
import io.searchbox.client.JestResult;
import org.apache.commons.collections.map.HashedMap;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * Created by A.Moshiri on 9/16/2018.
 */
@Authorize(views = {Access.TIME_LINE_ADD, Access.TIME_LINE_EDIT})

public class TimeLineForm extends BasePanel {
    Form insertForm;
    BootStrapModal modal = new BootStrapModal("modal");

    FeedbackPanel feedbackPanel;
    MyDropDownChoicePanel timeLineFileTypeDropDown;
    Label selectedParentLabel;
    OrganizationVO selectedOrganizationVO = null;

    DateTimePanel startShowTimeLineDateTimePanel;
    NiceEditor descriptionComponent;
    NiceEditor timeLineDescriptionComponent;
    DisplayForm displayForm;

    MultiAjaxFileUploadPanel2 timeLineFileUploadPanel;
    Label selectedTimeLineFilePath;
    WebMarkupContainer selectedTimeLIneFilePathContainer;

    LimitedTextField timeLineTitle;
    SwitchBox isActiveSwitchBox;
    SwitchBox showInChildSwitchBox;
    Boolean isLoaded;
    Label descriptionLbl;
    Label timeLineFileLbl;
    Label timeLineDownloadPathLbl;
    AjaxEventBehavior textAreaGetBehaviour;
    AjaxEventBehavior timeLineDescriptionBehaviour;
    String descriptionStr;
    String timeLineDescriptionStr;
    TimeLine timeLine;
    List<IUploadFilter> imageFilters;
    Long selectedFileType;
    TagsInput keywordsTagsInput;

    List<OrganizationVO> organizationVOList = null;
    Map<Long, TreeNode<OrganizationVO>> organizationVOTreeNodeMap = new HashedMap();

//    TextAreaPanel timeLineDescriptionPanel;

    protected TimeLineForm(String id, TimeLine inputTimeLine) {
        super(id);
        if (inputTimeLine == null) {
            return;
        }
        imageFilters = new ArrayList<>();
        isLoaded = false;
        descriptionStr = "";
        timeLineDescriptionStr = "";
        Model<String> selectedTimeLineFilePathMdl = new Model<>(null);
        timeLine = inputTimeLine;
        insertForm = new Form("form", new CompoundPropertyModel(inputTimeLine));
        Model<String> selectedParentMdl = Model.of("....");

        List<String> imageAndVideoExtensionList = IUploadFilter.getImageAndVideoUploadFilter().getFilterList();

        insertForm.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        if (inputTimeLine != null && inputTimeLine.getId() != null) {
            try {
                TimeLineElasticService.TimeLineCriteria criteria = new TimeLineElasticService.TimeLineCriteria();
                criteria.setId(inputTimeLine.getId());
                List<TimeLineVO> loadedTimeLineVOs = TimeLineElasticService.Instance.searchTimeLine(criteria, 0, -1, null, true);
                if (loadedTimeLineVOs != null && !loadedTimeLineVOs.isEmpty() && loadedTimeLineVOs.size() < 2) {
                    timeLine = TimeLine.getTimeLineFromTimeLineVO(loadedTimeLineVOs.get(0));
                    isLoaded = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        feedbackPanel = new FeedbackPanel("feedbackPanel");
        add(feedbackPanel);
        add(modal);

        Model<String> descriptionLblMdl = new Model<>(null);
        Model<String> timeLineFileLblMdl = new Model<>(null);
        Model<String> timeLineDownloadPathLblMdl = new Model<>(null);
        descriptionLblMdl.setObject(AppStorePropertyReader.getString("timeLine.description"));

        timeLineFileLblMdl.setObject(AppStorePropertyReader.getString("timeLine.file"));
        timeLineDownloadPathLblMdl.setObject(AppStorePropertyReader.getString("timeLine.downloadPath"));

        List<TimeLineFileType> timeLineFileTypes = new ArrayList<>();
        timeLineFileTypes.add(TimeLineFileType.IMAGE_AND_VIDEO);
        timeLineFileTypes.add(TimeLineFileType.HTML);
        //this part was additional about user request but has problem
//        timeLineFileTypes.add(TimeLineFileType.ANY_FILE);
        timeLineFileTypeDropDown = new MyDropDownChoicePanel("timeLineFileType", timeLineFileTypes, false, false, getString("timeLine.fileType"), 3, true, new ChoiceRenderer<>()) {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {

                TimeLineFileType selectedItems = (TimeLineFileType) getSelectedItem();

                if (imageFilters == null) {
                    imageFilters = new ArrayList<>();
                } else if (imageFilters != null && !imageFilters.isEmpty()) {
                    imageFilters.clear();
                }
                timeLineDownloadPathLbl.setVisible(false);

                if (selectedItems != null) {
                    selectedFileType = selectedItems.getState();
                    if (selectedItems.equals(TimeLineFileType.TEXT)) {
                        selectedTimeLineFilePath.setVisible(false);

                        descriptionComponent.setVisible(true);
                        descriptionLbl.setVisible(true);
//                        timeLineFileUploadPanel.setVisible(false);
                        timeLineDownloadPathLbl.setVisible(false);
                        timeLineFileLbl.setVisible(false);

                    } else {
//                        timeLineFileUploadPanel.setVisible(true);
                        timeLineFileLbl.setVisible(true);
                        descriptionComponent.setVisible(false);
                        descriptionLbl.setVisible(false);
                        selectedTimeLineFilePath.setVisible(true);

                        if (selectedItems.equals(TimeLineFileType.VIDEO)) {
                            imageFilters.add(IUploadFilter.getVideoUploadFilter());
                        } else if (selectedItems.equals(TimeLineFileType.IMAGE)) {
                            imageFilters.add(IUploadFilter.getImageUploadFilter());
                        } else if (selectedItems.equals(TimeLineFileType.IMAGE_AND_VIDEO)) {
                            imageFilters.add(IUploadFilter.getImageAndVideoUploadFilter());
                        } else if (selectedItems.equals(TimeLineFileType.HTML)) {
                            timeLineDownloadPathLbl.setVisible(true);

                            imageFilters.add(IUploadFilter.getHTMLUploadFilter());
                        } else if (selectedItems.equals(TimeLineFileType.ANY_FILE)) {
                            timeLineDownloadPathLbl.setVisible(true);

                            imageFilters.clear();
                            imageFilters = null;
//                            imageFilters.add(IUploadFilter.getAnyUploadFilter());
                        }
                    }
                } else {
//                    timeLineFileUploadPanel.setVisible(false);
                    timeLineDownloadPathLbl.setVisible(false);
                    timeLineFileLbl.setVisible(false);
                    descriptionComponent.setVisible(false);
                    descriptionLbl.setVisible(false);
                }
                target.add(descriptionLbl);
                target.add(timeLineFileLbl);
                target.add(timeLineDownloadPathLbl);
                target.add(descriptionComponent);
                target.add(timeLineFileUploadPanel);
                target.add(selectedTimeLineFilePath);
            }
        };
        timeLineFileTypeDropDown.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        timeLineFileTypeDropDown.setLabel(new ResourceModel("timeLine.fileType"));


        selectedFileType = null;
        if (timeLine != null) {
            if (timeLine.getTimeLineFileType() != null) {
                selectedFileType = timeLine.getTimeLineFileType().getState();
            } else if (timeLine.getFileType() != null) {
                selectedFileType = timeLine.getFileType();
            }
        }
        if (selectedFileType != null) {
            timeLineFileTypeDropDown.setDefaultModel(new Model<>(new TimeLineFileType(selectedFileType)));
        } else {
            timeLineFileTypeDropDown.setModel(new Model<>());
        }
        insertForm.add(timeLineFileTypeDropDown);


        displayForm = new DisplayForm("displayForm", timeLine.getFileVOList(), AppStorePropertyReader.getString("label.delete"));
        if (isLoaded && timeLine.getFileVOList() != null && !timeLine.getFileVOList().isEmpty()) {
            displayForm.setModel(new Model((Serializable) timeLine.getFileVOList()));
            displayForm.setVisible(true);
        } else {
            displayForm.setModel(new Model());
            displayForm.setVisible(false);
        }
        displayForm.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        insertForm.add(displayForm);

        Map<Long, OrganizationVO> organizationVOMap = new HashedMap();

        final TreeViewPanel parentTreeViewPanel;

        try {
            organizationVOList = EngineOrganizationService.Instance.getAllOrganization();
            if (organizationVOList != null) {
                for (OrganizationVO organizationVOInList : organizationVOList) {
                    if (!organizationVOTreeNodeMap.containsKey(organizationVOInList.getId())) {
                        TreeNode<OrganizationVO> treeNode = new TreeNode<>();
                        treeNode.setId(organizationVOInList.getId());
                        treeNode.setSelf(organizationVOInList);
                        treeNode.setTitle(organizationVOInList.getTitleFa());
                        if (organizationVOInList.getTitleFa() == null || organizationVOInList.getTitleFa().trim().equals("")) {
                            organizationVOInList.setTitleFa("بدون عنوان");
                        }
                        organizationVOTreeNodeMap.put(organizationVOInList.getId(), treeNode);

                        organizationVOMap.put(organizationVOInList.getId(), organizationVOInList);
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        try {
            parentTreeViewPanel = new TreeViewPanel("organization", false, true, new ITreeNodeProvider() {
                @Override
                public Set<TreeNode> getNodes(TreeNode parentNode) {
                    try {
                        Set<TreeNode> treeNodes = new HashSet<>();


                        if (organizationVOList != null) {
                            for (OrganizationVO organizationVOInList : organizationVOList) {
                                TreeNode tmpTreeNode = organizationVOTreeNodeMap.get(organizationVOInList.getId());
                                if (tmpTreeNode != null) {
                                    if (organizationVOInList.getParent() != null) {
                                        TreeNode parentOrgNode = organizationVOTreeNodeMap.get(organizationVOInList.getParent().getId());
                                        if (parentOrgNode != null) {
                                            parentOrgNode.addChild(tmpTreeNode);

                                            if (inputTimeLine.getOrganizationId() != null) {
                                                OrganizationVO loadedOrganizationVo = organizationVOMap.get(Long.valueOf(inputTimeLine.getOrganizationId()));
                                                if (loadedOrganizationVo != null) {
                                                    if (tmpTreeNode.getId().equals(loadedOrganizationVo.getId())) {
                                                        tmpTreeNode.setSelected(true);
                                                    }
                                                }
                                            }
                                            tmpTreeNode.setParent(parentOrgNode);
                                            tmpTreeNode.setSelf(tmpTreeNode);
                                            treeNodes.add(tmpTreeNode);
                                        }


                                    } else {
                                        tmpTreeNode.setParent(null);
                                        treeNodes.add(tmpTreeNode);
                                    }
                                }


                            }
                        } else {
                            error(AppStorePropertyReader.getString("error.getOrganization"));
                        }
                        return treeNodes;
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }) {
                @Override
                public void onUpdate(AjaxRequestTarget target, Collection selectedNodes) {
                    Session session = HibernateUtil.getCurrentSession();
                    try {
                        Iterator selectedNodeIterator = selectedNodes.iterator();
                        if (selectedNodeIterator.hasNext()) {
                            selectedOrganizationVO = null;
                            String parentNodeTitle = "";
                            Long parentNodeId;
                            Object parentNodeObject = selectedNodeIterator.next();

                            if (parentNodeObject instanceof TreeNode) {
                                TreeNode treeNode = (TreeNode) parentNodeObject;
                                parentNodeId = treeNode.getId();
                            } else {
                                OrganizationVO organizationVOForParent = (OrganizationVO) parentNodeObject;
                                parentNodeId = organizationVOForParent.getId();
                            }
                            OrganizationVO loadedParentOrganizationVO = organizationVOMap.get(parentNodeId);
                            parentNodeTitle = "";
                            if (loadedParentOrganizationVO != null) {
                                parentNodeTitle = loadedParentOrganizationVO.getTitleFa();
                                selectedOrganizationVO = loadedParentOrganizationVO;
                            }
                            selectedParentMdl.setObject(parentNodeTitle);

                        }
                        target.add(selectedParentLabel);
                        selectedParentLabel.setOutputMarkupId(true);
                    } finally {
                        if (session.isOpen()) {
                            session.close();
                        }
                    }
                }
            };
            parentTreeViewPanel.setModel(new Model<>());

            if (timeLine.getOrganizationId() != null) {
                TreeNode<OrganizationVO> treeNode = organizationVOTreeNodeMap.get(Long.parseLong(timeLine.getOrganizationId()));
                if(treeNode != null) {
                    parentTreeViewPanel.setModelObject(treeNode.getSelf());
                    selectedOrganizationVO = treeNode.getSelf();
                    selectedParentMdl.setObject(treeNode.getTitle());
                }
            }
        } catch (Exception e) {
            throw e;
        }

        selectedParentLabel = new Label("selectedOrganization", selectedParentMdl);
        selectedParentLabel.setOutputMarkupId(true);
        insertForm.add(selectedParentLabel);


        parentTreeViewPanel.setRequired(true);
        parentTreeViewPanel.setLabel(new ResourceModel("organization"));
        insertForm.add(parentTreeViewPanel);

        Organization selectedOrg = null;
        if (timeLine != null) {
            if (timeLine.getOrganizationId() != null) {
                OrgService.OrgCriteria orgCriteria = new OrgService.OrgCriteria();
                orgCriteria.setId(Long.valueOf(timeLine.getOrganizationId()));
                Session session = HibernateUtil.getNewSession();
                List<Organization> organizationList = OrgService.Instance.list(orgCriteria, 0, -1, null, true, session);
                if (organizationList != null && !organizationList.isEmpty() && organizationList.size() < 2) {
                    selectedOrg = organizationList.get(0);
                }
            }
        }

        startShowTimeLineDateTimePanel = new DateTimePanel("startDateTime", DateType.Date, HourMeridianType._24HOUR);
        startShowTimeLineDateTimePanel.setLabel(new ResourceModel("timeLine.startShowTimeLine"));
        startShowTimeLineDateTimePanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        if (isLoaded && timeLine != null && timeLine.getStartShowTimeLine() != null) {
            DateTime persianDateTime = MyCalendarUtil.toPersian(new DateTime(timeLine.getStartShowTimeLine()));
            startShowTimeLineDateTimePanel.setModel(new Model(new DayDate(persianDateTime.toDate())));
        } else {
            startShowTimeLineDateTimePanel.setModel(new Model<>());
        }
        insertForm.add(startShowTimeLineDateTimePanel);

        descriptionComponent = new NiceEditor("description");
        descriptionComponent.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        if (timeLine != null) {
            descriptionComponent.setModel(new Model<>(timeLine.getDescription()));
        } else {
            descriptionComponent.setModel(new Model<>(""));
        }
        descriptionComponent.setVisible(false);
        insertForm.add(descriptionComponent);


        timeLineDescriptionComponent = new NiceEditor("timeLineDescription");
        timeLineDescriptionComponent.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        if (timeLine != null) {
            timeLineDescriptionComponent.setModel(new Model<>(timeLine.getTimeLineDescription()));
        } else {
            timeLineDescriptionComponent.setModel(new Model<>(""));
        }
        insertForm.add(timeLineDescriptionComponent);

        timeLineFileUploadPanel = new MultiAjaxFileUploadPanel2("fileVOList", imageFilters, null, true, getString("timeLine.file")) {
            @Override
            protected void onUploadComplete(AjaxRequestTarget ajaxRequestTarget, UploadedFileInfo uploadedFileInfo) {
                super.onUploadComplete(ajaxRequestTarget, uploadedFileInfo);
                Collection<UploadedFileInfo> timeLineFileList = (Collection<UploadedFileInfo>) timeLineFileUploadPanel.getConvertedInput();
                Iterator<UploadedFileInfo> uploadedFileInfoIterator;
                if (timeLineFileList != null && timeLineFileList.size() > 0) {
                    uploadedFileInfoIterator = timeLineFileList.iterator();
                } else {
                    uploadedFileInfoIterator = null;
                }

                List<UploadedFileInfo> uploadedFileInfoList = new ArrayList();
                if (uploadedFileInfoIterator != null) {
                    while (uploadedFileInfoIterator.hasNext()) {
                        UploadedFileInfo tmpUploadedFileInfo = uploadedFileInfoIterator.next();
                        uploadedFileInfoList.add(tmpUploadedFileInfo);
                    }
                }
                if (uploadedFileInfoList != null && uploadedFileInfoList.size() > 0) {
                    UploadedFileInfo timeLineFile = uploadedFileInfoList.get(0);
                    String iconDownloadPath = ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH).replace("${key}", timeLineFile.getFileId());
                    String fileExtension = "";

                    fileExtension = timeLineFile.getFileName().substring(timeLineFile.getFileName().lastIndexOf(".") + 1, timeLineFile.getFileName().length());


                    if (!imageAndVideoExtensionList.contains(fileExtension)) {
                        selectedTimeLineFilePathMdl.setObject(iconDownloadPath);
                    }
                }
                selectedTimeLineFilePath.setDefaultModelObject(selectedTimeLineFilePathMdl.getObject());
                if (selectedFileType != null && !selectedFileType.equals(TimeLineFileType.IMAGE_AND_VIDEO)) {
                    selectedTimeLineFilePath.setVisible(true);
                    selectedTimeLIneFilePathContainer.setVisible(true);
                } else {
                    selectedTimeLineFilePath.setVisible(false);
                    selectedTimeLIneFilePathContainer.setVisible(false);
                }
                if (insertForm.get("timeLineFileLink") != null) {
                    insertForm.get("timeLineFileLink").setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
                    ajaxRequestTarget.add(insertForm.get("timeLineFileLink"));
                } else {
                    ajaxRequestTarget.add(selectedTimeLineFilePath);
                }
                selectedTimeLIneFilePathContainer.add(new AttributeModifier("href", selectedTimeLineFilePathMdl));
                ajaxRequestTarget.add(selectedTimeLIneFilePathContainer);
            }
        };

        timeLineFileUploadPanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        timeLineFileUploadPanel.setLabel(new ResourceModel("timeLine.file"));

        timeLineFileUploadPanel.setModel(new Model());

//        if (isLoaded && (timeLine.getFileType().equals(TimeLineFileType.IMAGE.getState()) || timeLine.getFileType().equals(TimeLineFileType.VIDEO.getState()) || timeLine.getFileType().equals(TimeLineFileType.IMAGE_AND_VIDEO.getState()))) {
//            timeLineFileUploadPanel.setVisible(true);
//        } else {
//            timeLineFileUploadPanel.setVisible(false);
//        }

        insertForm.add(timeLineFileUploadPanel);

        if (imageFilters.isEmpty()) {
            imageFilters.add(IUploadFilter.getAnyUploadFilter());
        }


        selectedTimeLineFilePath = new Label("timeLineFileLink", selectedTimeLineFilePathMdl);
        selectedTimeLineFilePath.setDefaultModelObject(selectedTimeLineFilePathMdl.getObject());

        selectedTimeLineFilePath.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        selectedTimeLIneFilePathContainer = new WebMarkupContainer("timeLineDownloadPath");
        selectedTimeLIneFilePathContainer.add(selectedTimeLineFilePath);

        selectedTimeLIneFilePathContainer.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        selectedTimeLIneFilePathContainer.add(new AttributeModifier("href", selectedTimeLineFilePathMdl));


        insertForm.add(selectedTimeLIneFilePathContainer);


        timeLineTitle = new LimitedTextField("title", null, false, false, true, true, 40, getString("timeLine.title"));
        timeLineTitle.setLabel(new ResourceModel("timeLine.title"));
        timeLineTitle.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        if (isLoaded && timeLine.getTitle() != null) {
            timeLineTitle.setModel(new Model(timeLine.getTitle()));
        } else {
            timeLineTitle.setModel(new Model(""));
        }
        insertForm.add(timeLineTitle);

        Boolean hasDefaultState = null;
        if (timeLine != null && timeLine.getActive() != null) {
            hasDefaultState = timeLine.getActive();
        }

        isActiveSwitchBox = new SwitchBox(hasDefaultState, "isActive", getString("label.yes"), getString("label.no"));
        isActiveSwitchBox.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        isActiveSwitchBox.setLabel(new ResourceModel("label.activation.verb"));
        isActiveSwitchBox.setModel(new Model<>());
        insertForm.add(isActiveSwitchBox);

        showInChildSwitchBox = new SwitchBox(hasDefaultState, "showInChild", getString("label.yes"), getString("label.no"));
        showInChildSwitchBox.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        showInChildSwitchBox.setLabel(new ResourceModel("timeLine.showInChild"));
        showInChildSwitchBox.setModel(new Model<>());
        insertForm.add(showInChildSwitchBox);


        descriptionLbl = new Label("descriptionLbl", descriptionLblMdl);
        descriptionLbl.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        descriptionLbl.setVisible(false);
        insertForm.add(descriptionLbl);


        timeLineFileLbl = new Label("timeLineFileLbl", timeLineFileLblMdl);
        timeLineFileLbl.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        timeLineFileLbl.setVisible(false);
        insertForm.add(timeLineFileLbl);


        timeLineDownloadPathLbl = new Label("timeLineDownloadPathLbl", timeLineDownloadPathLblMdl);
        timeLineDownloadPathLbl.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        if (isLoaded && !selectedFileType.equals(TimeLineFileType.IMAGE_AND_VIDEO.getState())) {
            selectedTimeLineFilePath.setVisible(true);
            String fileDownloadPath = ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH).replace("${key}", timeLine.getFileVOList().get(0).getFileKey());
            selectedTimeLineFilePathMdl.setObject(fileDownloadPath);

            timeLineDownloadPathLbl.setVisible(true);
            selectedTimeLIneFilePathContainer.setVisible(true);
        } else {
            selectedTimeLineFilePathMdl.setObject("");
            selectedTimeLineFilePath.setVisible(false);
            selectedTimeLIneFilePathContainer.setVisible(false);
            timeLineDownloadPathLbl.setVisible(false);
        }
        insertForm.add(timeLineDownloadPathLbl);

        final Map<String, String> keyWordMap;

        Model<String> keywordModel = Model.of("");
        if (timeLine != null && timeLine.getKeywords() != null && !timeLine.getKeywords().isEmpty()) {
            StringBuilder keywordBuilder = new StringBuilder("");
            for (String keyword : timeLine.getKeywords()) {
                keywordBuilder.append(keyword);
                keywordBuilder.append(",");
            }
            String keyword = keywordBuilder.toString();
            keyword = keyword.substring(0, keyword.lastIndexOf(","));
            keywordModel = Model.of(keyword);
        }
        keywordsTagsInput = new TagsInput("keywords", keywordModel, null);
        keywordsTagsInput.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        keywordsTagsInput.setModel(keywordModel);
        insertForm.add(keywordsTagsInput);

        insertForm.add(
                new AjaxFormButton("save", insertForm, feedbackPanel) {

                    @Override
                    protected void onComponentTag(ComponentTag tag) {
                        super.onComponentTag(tag);
//                tag.put("onclick", "var txt = $('#" + descriptionComponent.getMarkupId() + "').find('textarea').text();" +
//                        "var res = encodeURIComponent(txt);" +
//                        "Wicket.Ajax.get({u:'" +
//                        textAreaGetBehaviour.getCallbackUrl().toString() +
//                        "&changeLog=' + res"
//                        + "});");

                        tag.put("onclick", "var txt = $('#" + timeLineDescriptionComponent.getMarkupId() + "').find('textarea').text();" +
                                "var res = encodeURIComponent(txt);" +
                                "Wicket.Ajax.get({u:'" +
                                timeLineDescriptionBehaviour.getCallbackUrl().toString() +
                                "&changeLog=' + res"
                                + "});");
                    }

                    @Override
                    protected void onSubmit(Form form, AjaxRequestTarget target) {
                        TimeLine timelineInForm = (TimeLine) form.getModelObject();
                        TimeLineFileType selectedTimeLineFileType = (TimeLineFileType) timeLineFileTypeDropDown.getConvertedInput();

                        TimeLineVO timeLineVO = null;
                        String validationString = "";
                        File uploadFile = null;
                        List<FileVO> fileVOList = new ArrayList<>();
                        List<FileVO> finalFileVOList = new ArrayList<>();
                        List<Long> selectedToDeleteFiles = (List<Long>) displayForm.getConvertedInput();
                        List<String> imageExtensionList = IUploadFilter.getImageUploadFilter().getFilterList();
                        List<String> videoExtensionList = IUploadFilter.getVideoUploadFilter().getFilterList();
                        List<String> htmlExtensionList = IUploadFilter.getHTMLUploadFilter().getFilterList();
                        Session session = null;
                        StringBuffer fileExtension = new StringBuffer("");

                        try {
                            Transaction tx = null;
                            if (timelineInForm == null) {
                                validationString += " - " +
                                        ResultStatus.NULL_DATA.toString()
                                        + "<br/>";
                                target.appendJavaScript("showMessage('" + validationString + "');");
                                return;
                            } else {
                                session = HibernateUtil.getCurrentSession();

                                Set<UploadedFileInfo> thumbsUploadedFileInfoSet = (Set<UploadedFileInfo>) timeLineFileUploadPanel.getConvertedInput();
                                if (thumbsUploadedFileInfoSet != null && !thumbsUploadedFileInfoSet.isEmpty()) {
                                    tx = session.beginTransaction();

                                    for (UploadedFileInfo thumbImageFileInfo : thumbsUploadedFileInfoSet) {
                                        fileExtension.delete(0, fileExtension.length());
                                        String tempFileLocation = null;
                                        tempFileLocation = FileServerService.Instance.copyFileFromServerToTemp(thumbImageFileInfo.getFileId());
                                        java.io.File file = new java.io.File(tempFileLocation);

                                        File thumbFile = new File();
                                        thumbFile.setFileName(thumbImageFileInfo.getFileName());

                                        fileExtension.append(thumbFile.getFileName().substring(thumbFile.getFileName().lastIndexOf(".") + 1, thumbFile.getFileName().length()));
                                        if (fileExtension == null || fileExtension.toString().trim().equals("")) {

                                        }
                                        thumbFile.setFilePath(thumbImageFileInfo.getFileId());
                                        thumbFile.setStereoType(StereoType.THUMB_FILE);
                                        FileServerService.Instance.persistFileToServer(thumbImageFileInfo.getFileId());
                                        BaseEntityService.Instance.saveOrUpdate(thumbFile, session);
                                        if (selectedTimeLineFileType != null) {
                                            if (selectedTimeLineFileType.equals(TimeLineFileType.IMAGE_AND_VIDEO) &&
                                                    !imageAndVideoExtensionList.contains(fileExtension.toString())) {
                                                validationString += " - " +
                                                        ResultStatus.INVALID_DATA.toString()
                                                        + "<br/>";
                                                target.appendJavaScript("showMessage('" + validationString + "');");
                                                return;

                                            } else if (selectedTimeLineFileType.equals(TimeLineFileType.HTML) &&
                                                    !htmlExtensionList.contains(fileExtension.toString())) {
                                                validationString += " - " +
                                                        ResultStatus.INVALID_DATA.toString()
                                                        + "<br/>";
                                                target.appendJavaScript("showMessage('" + validationString + "');");
                                                return;

                                            }
                                        }


                                        FileVO fileVO = FileVO.convertFileToFileVo(thumbFile);
                                        fileVOList.add(fileVO);
                                    }
                                } else {
                                    if (timeLine.getId() == null &&
                                            (timeLine.getFileVOList() == null || timeLine.getFileVOList().isEmpty())) {
                                        validationString += " - " +
                                                getString("Required").replace("${label}", new ResourceModel("timeLine.file").getObject())
                                                + "<br/>";
                                    }
                                }

                                if (isLoaded) {
                                    TimeLineElasticService.TimeLineCriteria timeLineCriteria = new TimeLineElasticService.TimeLineCriteria();
                                    timeLineCriteria.setId(timeLine.getId());
                                    TimeLineVO loadedTimeLine = TimeLineElasticService.Instance.searchTimeLine(timeLineCriteria, 0, -1, null, true).get(0);
                                    if (loadedTimeLine.getFileType().equals(selectedTimeLineFileType.getState())) {
                                        List<FileVO> loadedFileVoList = loadedTimeLine.getFileVOList();
                                        for (FileVO loadedFileVO : loadedFileVoList) {
                                            String fileKey = null;
                                            fileKey = loadedFileVO.getFileKey().substring(loadedFileVO.getFileKey().lastIndexOf("=") + 1, loadedFileVO.getFileKey().length());
                                            loadedFileVO.setFileKey(fileKey);
                                        }

                                        fileVOList.addAll(loadedFileVoList);
//                                fileVOList.addAll(loadedTimeLine.getFileVOList());
                                    } else {
                                        for (FileVO loadedFileVo : loadedTimeLine.getFileVOList()) {
                                            selectedToDeleteFiles.add(loadedFileVo.getFileId());
                                        }
                                    }
                                }

                                if (selectedToDeleteFiles != null && !selectedToDeleteFiles.isEmpty()) {
                                    for (FileVO fileVo : fileVOList) {
                                        if (fileVo.getFileId() != null && !selectedToDeleteFiles.contains(fileVo.getFileId())) {
                                            finalFileVOList.add(fileVo);
                                        }
                                    }
                                    for (Long deletedId : selectedToDeleteFiles) {
                                        File loadedFileToDelete = (File) session.load(File.class, deletedId);
                                        if (loadedFileToDelete != null) {
                                            FileServerService.Instance.deleteFileFromServer(loadedFileToDelete.getFilePath());
                                        }
                                    }
                                } else {
                                    finalFileVOList = fileVOList;
                                }


                                timelineInForm.setFileVOList(finalFileVOList);

                                try {
                                    OrganizationVO organization = selectedOrganizationVO;

                                    if (organization != null) {
                                        if (organization.getTitleFa().equals("root")) {
                                            validationString += " - " +
                                                    getString("error.unSelectable.organization")
                                                    + "<br/>";
                                        }
                                        timelineInForm.setOrganizationId(String.valueOf(organization.getId()));
                                        timelineInForm.setOrganizationNickName(String.valueOf(organization.getTitleFa()));
                                    } else {
                                        validationString += " - " +
                                                getString("Required").replace("${label}", new ResourceModel("timeLine.organization").getObject())
                                                + "<br/>";
                                    }

                                    if (selectedTimeLineFileType == null || selectedTimeLineFileType.getState() == null) {
                                        validationString += " - " +
                                                getString("Required").replace("${label}", new ResourceModel("timeLine.fileType").getObject())
                                                + "<br/>";
                                    }
                                    if (descriptionStr != null && !descriptionStr.trim().equals("")) {
                                        timelineInForm.setDescription(descriptionStr);
                                    }

                                    if (timeLineDescriptionStr != null && !timeLineDescriptionStr.trim().equals("")) {
                                        timelineInForm.setTimeLineDescription(timeLineDescriptionStr);
                                    }
                                    Boolean isActive = (Boolean) isActiveSwitchBox.getConvertedInput();
                                    if (isActive != null) {
                                        timelineInForm.setActive(isActive);
                                    }

                                    Boolean showInChild = (Boolean) showInChildSwitchBox.getConvertedInput();
                                    if (showInChild != null) {
                                        timelineInForm.setShowInChild(showInChild);
                                    }
                                    if (timelineInForm.getCreatorUserId() == null) {
                                        timelineInForm.setCreationDate(DateTime.now().getTime());
                                        timelineInForm.setCreatorUserId(PrincipalUtil.getCurrentUser().getUserId());
                                        timelineInForm.setCreatorUserName(PrincipalUtil.getCurrentUser().getUserName());
                                    }
                                    if (timelineInForm.getId() != null) {
                                        timelineInForm.setLastModificationDate(DateTime.now().getTime());
                                        timelineInForm.setLastModificationUserId(PrincipalUtil.getCurrentUser().getUserId());
                                        timelineInForm.setLastModificationUserName(PrincipalUtil.getCurrentUser().getUserName());
                                    }
                                    String title = (String) timeLineTitle.getConvertedInput();
                                    timelineInForm.setTitle(title);
                                    timelineInForm.setDescription(descriptionStr);
                                    timelineInForm.setTimeLineDescription(timeLineDescriptionStr);
                                    if (selectedTimeLineFileType != null && selectedTimeLineFileType.getState() != null) {
                                        timelineInForm.setFileType(selectedTimeLineFileType.getState());
                                    }
                                    DateTime startDateTime = new DateTime(((DayDate) startShowTimeLineDateTimePanel.getConvertedInput()).toDate().getTime());
                                    timelineInForm.setStartDateTime(startDateTime);
                                    timelineInForm.setStartShowTimeLine(startDateTime.getTime());
                                    timeLineVO = TimeLineVO.buildTimeLineVoByTimeLine(timelineInForm);

                                } catch (Exception e) {
                                    validationString += " - " +
                                            ResultStatus.INVALID_DATA.toString()
                                            + "<br/>";

                                }
                            }
                            if (timeLineVO.getTitle() == null || timeLineVO.getTitle().trim().equals("")) {
                                validationString += " - " +
                                        getString("Required").replace("${label}", new ResourceModel("timeLine.title").getObject())
                                        + "<br/>";
                            }

                            if (timeLineVO.getTimeLineDescription() == null || timeLineVO.getTimeLineDescription().trim().equals("")) {
                                validationString += " - " +
                                        getString("Required").replace("${label}", new ResourceModel("timeLine.timeLineDescription").getObject())
                                        + "<br/>";
                            }

                            if (timeLineVO.getStartShowTimeLine() == null) {
                                validationString += " - " +
                                        getString("Required").replace("${label}", new ResourceModel("timeLine.startShowTimeLine").getObject())
                                        + "<br/>";
                            }


                            if (startShowTimeLineDateTimePanel != null && startShowTimeLineDateTimePanel.getConvertedInput() != null) {
                                DateTime startDateTime = new DateTime(((DayDate) startShowTimeLineDateTimePanel.getConvertedInput()).toDate().getTime());
                                DateTime minDateTime = new DateTime(DayDate.MIN_DAY_DATE, DayTime.MIN_DAY_TIME);
                                DateTime currentDateTime = new DateTime((DateTime.now().getDayDate().toDate().getTime()) - DateTime.ONE_HOUR_MILLIS);

                                if (startDateTime.compareTo(minDateTime) == 0) {
                                    validationString += " - " +
                                            getString("Required").replace("${label}", startShowTimeLineDateTimePanel.getLabel().getObject()) + "<br/>";
                                } else if (startDateTime.compareTo(currentDateTime) < 0) {
                                    validationString += " - " +
                                            getString("error.dayAndTime.validation.less").replace("${first}", startShowTimeLineDateTimePanel.getLabel().getObject()).replace("${second}", getString("label.current.date")) + "<br/>";
                                }

                            }

                            if (timeLineVO.getFileType() != null) {
                                TimeLineFileType timeLineFileType = new TimeLineFileType(timeLineVO.getFileType());
                                if (timeLineFileType == null) {
                                    validationString += " - " +
                                            ResultStatus.INVALID_DATA
                                            + "<br/>";
                                } else if ((timeLineVO.getDescription() == null || timeLineVO.getDescription().trim().equals("") || timeLineVO.getDescription().trim().equals("<br/>")) &&
                                        timeLineFileUploadPanel.getConvertedInput() == null && !isLoaded) {
                                    validationString += " - " +
                                            getString("RequiredOneOf").replace("${label}", getString("timeLine.file"))
                                                    .replace("${label1}", getString("timeLine.description"))
                                            + "<br/>";
                                } else if (timelineInForm.getFileVOList() == null && !timelineInForm.getFileVOList().isEmpty() && timelineInForm.getDescription() == null) {
                                    validationString += " - " +
                                            getString("RequiredOneOf").replace("${label}", getString("timeLine.file"))
                                                    .replace("${label1}", getString("timeLine.description"))
                                            + "<br/>";
                                }
                            }

                            if (!validationString.isEmpty()) {
                                target.appendJavaScript("showMessage('" + validationString + "');");
                                return;
                            }

                            if (keywordsTagsInput != null && keywordsTagsInput.getConvertedInput() != null) {

                                String[] keywordsString = keywordsTagsInput.getConvertedInput().toString().split(",");
                                List<String> keywords = new ArrayList<String>();
                                for (String string : keywordsString) {
                                    keywords.add(string.trim());
                                }
                                timeLineVO.setKeywords(keywords);
                            } else {
                                timeLineVO.setKeywords(null);
                            }

                            if (!isLoaded) {
                                timeLineVO.setId(TimeLineVO.getTimeLineIdByTimeLine(timelineInForm));
                            }

                            JestResult jestResult = TimeLineElasticService.Instance.insertTimeLine(timeLineVO);
                            if (jestResult.isSucceeded()) {
                                form.setModelObject(new TimeLine());
                                target.add(form);
                            } else {
                                throw new Exception("commit data to elastic error with this code : " + jestResult.getErrorMessage());
                            }

                            if (tx != null)
                                tx.commit();
                            childFinished(target, new Model<>(), this);


                        } catch (Exception e) {
                            logger.error("Error occured saving timeline ", e);
                            validationString += " - " +
                                    getString("error.generalErr")
                                    + "<br/>";
                        } finally {
                            if (session != null && session.isOpen()) {
                                session.close();
                            }
                        }

                        if (!validationString.isEmpty()) {
                            target.appendJavaScript("showMessage('" + validationString + "');");

                            return;
                        }
                    }


                });


        textAreaGetBehaviour = new AjaxEventBehavior("dasd") {
            @Override
            protected void onEvent(AjaxRequestTarget target) {

                String description = getRequest().getRequestParameters().getParameterValue("changeLog").toString();
                String convertedDescription = null;
                try {
                    convertedDescription = URLDecoder.decode(description, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    processException(target, e);
                }

                descriptionComponent.setModelObject(convertedDescription);
                descriptionStr = convertedDescription;
                System.out.println("");

            }

        };
        add(textAreaGetBehaviour);

        timeLineDescriptionBehaviour = new AjaxEventBehavior("dasd") {
            @Override
            protected void onEvent(AjaxRequestTarget target) {
                String description = getRequest().getRequestParameters().getParameterValue("changeLog").toString();
                String convertedDescription = null;
                try {
                    convertedDescription = URLDecoder.decode(description, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    processException(target, e);
                }

                timeLineDescriptionComponent.setModelObject(convertedDescription);
                timeLineDescriptionStr = convertedDescription;
                System.out.println("");
            }

        };
        add(timeLineDescriptionBehaviour);


        insertForm.add(new AjaxLink("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                childFinished(target, null, this);
            }
        });

        add(insertForm);
    }


    public static void main(String[] args) {
        DateTime dateTime = DateTime.now();
        Date date = new Date();
        System.out.println("date is : " + date);
        System.out.println("date.getTime() is : " + date.getTime());
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);
        System.out.println("changedDate is : " + date);
        System.out.println("changedDate.getTime() is : " + date.getTime());

        System.out.println("date Time is : " + dateTime);
        System.out.println("dateTime.getTime() is : " + dateTime.getTime());
        System.out.println("dateTime.getDayDate() is : " + dateTime.getDayDate());
        System.out.println("dateTime.getDayDate() is : " + dateTime.getDayDate());
        Date convertedDate = new Date(dateTime.getDayDate().toDate().getTime());
        System.out.println("dateTime.getDayDate().toDate() is : " + convertedDate);
        System.out.println("dateTime.getDayDate().toDate().getTime() is : " + convertedDate.getTime());

        System.out.println("dateTime.getDayTime() is : " + dateTime.getDayTime());
        System.out.println("dateTime.getDateTimeLong() is : " + dateTime.getDateTimeLong());
        System.out.println("dateTime.getDayDate().getDateTimeLong() is : " + dateTime.getDayDate().getDateTimeLong());
        System.out.println("dateTime.getDayDate().getDateTimeLong() is : " + dateTime.getDayDate().getDateTimeLong());
        System.out.println("dateTime is : " + dateTime);
        System.out.println("dateTime is : " + dateTime);
    }


}
