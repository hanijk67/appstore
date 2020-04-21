package com.fanap.midhco.ui.pages.timeLine;

import com.fanap.midhco.appstore.entities.AppCategory;
import com.fanap.midhco.appstore.entities.TimeLine;
import com.fanap.midhco.appstore.entities.TimeLineFileType;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.entities.helperClasses.DateType;
import com.fanap.midhco.appstore.entities.helperClasses.DayDate;
import com.fanap.midhco.appstore.restControllers.vos.OrganizationVO;
import com.fanap.midhco.appstore.restControllers.vos.TimeLineVO;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.engine.EngineOrganizationService;
import com.fanap.midhco.appstore.service.timeLine.TimeLineElasticService;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.access.WebAction;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.ajaxLinkPanel.AjaxLinkPanel;
import com.fanap.midhco.ui.component.dateTimePanel.DateTimeRangePanel;
import com.fanap.midhco.ui.component.limitedTextField.LimitedTextField;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.component.mydropdown.MyDropDownChoicePanel;
import com.fanap.midhco.ui.component.switchbox.SwitchBox;
import com.fanap.midhco.ui.component.table.MyAjaxDataTable;
import com.fanap.midhco.ui.component.table.column.IndexColumn;
import com.fanap.midhco.ui.component.table.column.PersianDateColumn;
import com.fanap.midhco.ui.component.tagsinput.TagsInput;
import com.fanap.midhco.ui.component.textareapanel.TextAreaPanel;
import com.fanap.midhco.ui.component.treeview.ITreeNodeProvider;
import com.fanap.midhco.ui.component.treeview.TreeNode;
import com.fanap.midhco.ui.component.treeview.TreeViewPanel;
import org.apache.commons.collections.map.HashedMap;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.hibernate.Session;

import java.io.Serializable;
import java.util.*;

/**
 * Created by A.Moshiri on 9/16/2018.
 */
@Authorize(views = {Access.TIME_LINE_LIST})

public class TimeLineList extends BasePanel implements IParentListner {

    TimeLineListSortableDataProvider dp = new TimeLineListSortableDataProvider();

    MyAjaxDataTable table;
    BootStrapModal modal = new BootStrapModal("modal");
    Form form;
    FeedbackPanel feedbackPanel;
    MyDropDownChoicePanel timeLineFileTypeDropDown;
    DateTimeRangePanel startShowTimeLineDateTime;
    LimitedTextField text;
    MyDropDownChoicePanel isActiveDropDown;
    MyDropDownChoicePanel showInChildDropDown;
    MyDropDownChoicePanel searchInParentsDropDown;
    OrganizationVO selectedOrganizationVO = null;
    Label selectedParentLabel;

    List<Long> selectedOrgIds = new ArrayList<>();
    TimeLineElasticService.TimeLineCriteria tableCriteria = null;
    TextAreaPanel timeLineDescriptionPanel;
    Model<String> selectedParentMdl;
    TagsInput keywordsTagsInput;
    TreeViewPanel parentTreeViewPanel;
    Boolean checkOrg = false;

    public TimeLineList() {
        this(MAIN_PANEL_ID, new TimeLineElasticService.TimeLineCriteria(), SelectionMode.None);
    }


    protected TimeLineList(String id, final TimeLineElasticService.TimeLineCriteria criteria, final SelectionMode selectionMode) {
        super(id);
        setPageTitle(getString("timeLine"));

        String defaultStringForLabel = "....";
        add(modal);
        feedbackPanel = new FeedbackPanel("feedbackPanel");
        add(feedbackPanel);
        form = new Form("form", new CompoundPropertyModel(criteria));
        selectedParentMdl = Model.of(defaultStringForLabel);

        text = new LimitedTextField("title", null, false, false, true, true, 40, getString("timeLine.title"));
        text.setLabel(new ResourceModel("timeLine.title"));
        text.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(text);
        checkOrg = !checkOrg;

        List<TimeLineFileType> timeLineFileTypes = new ArrayList<>();
        timeLineFileTypes.add(TimeLineFileType.IMAGE_AND_VIDEO);
        timeLineFileTypes.add(TimeLineFileType.HTML);
        //this part was additional about user request but has problem
//        timeLineFileTypes.add(TimeLineFileType.ANY_FILE);

        timeLineFileTypeDropDown = new MyDropDownChoicePanel("timeLineFileTypes", timeLineFileTypes, true, false, getString("timeLine.fileType"), 3);
        timeLineFileTypeDropDown.setLabel(new ResourceModel("timeLine.fileType"));
        timeLineFileTypeDropDown.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(timeLineFileTypeDropDown);

        Map<Long, OrganizationVO> organizationVOMap = new HashedMap();

        try {
            parentTreeViewPanel =
                    new TreeViewPanel("organization", false, true, new ITreeNodeProvider() {
                        @Override
                        public Set<TreeNode> getNodes(TreeNode parentNode) {
                            try {
                                Set<TreeNode> treeNodes = new HashSet<>();
                                Map<Long, TreeNode<OrganizationVO>> organizationVOTreeNodeMap = new HashedMap();

                                List<OrganizationVO> organizationVOList = null;
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

                                    for (OrganizationVO organizationVOInList : organizationVOList) {
                                        TreeNode tmpTreeNode = organizationVOTreeNodeMap.get(organizationVOInList.getId());
                                        if (tmpTreeNode != null) {
                                            if (organizationVOInList.getParent() != null) {
                                                TreeNode parentOrgNode = organizationVOTreeNodeMap.get(organizationVOInList.getParent().getId());
                                                if (parentOrgNode != null) {
                                                    parentOrgNode.addChild(tmpTreeNode);
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
                        public void validate() {
                        }

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
                                    //if (parentNodeObject instanceof TreeNode || parentNodeObject instanceof AppCategory) {
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
                                //}
                                target.add(selectedParentLabel);
                                selectedParentLabel.setOutputMarkupId(true);
                            } finally {
                                if (session.isOpen()) {
                                    session.close();
                                }
                            }
                        }
                    };
        } catch (Exception e) {
            throw e;
        }

        parentTreeViewPanel.setModel(new Model<>());
        parentTreeViewPanel.setRequired(true);
        parentTreeViewPanel.setLabel(new ResourceModel("organization"));
        form.add(parentTreeViewPanel);


        selectedParentLabel = new Label("selectedOrganization", selectedParentMdl);
        selectedParentLabel.setOutputMarkupId(true);
        form.add(selectedParentLabel);

        startShowTimeLineDateTime = new DateTimeRangePanel("startShowTimeLineDateTime", DateType.Date);
        startShowTimeLineDateTime.setLabel(new ResourceModel("Anouncement.startDateTime"));
        startShowTimeLineDateTime.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(startShowTimeLineDateTime);

        isActiveDropDown = new MyDropDownChoicePanel("isActive",
                Arrays.asList(true, false), false, false, getString("timeLine.isActive"), 1, false,
                new ChoiceRenderer() {
                    @Override
                    public Object getDisplayValue(Object o) {
                        if ((Boolean) o) {
                            return getString("label.yes");
                        }
                        return getString("label.no");
                    }

                    @Override
                    public String getIdValue(Object o, int i) {
                        return o.toString();
                    }
                }
        );
        form.add(isActiveDropDown);

        showInChildDropDown = new MyDropDownChoicePanel("showInChild",
                Arrays.asList(true, false), false, false, getString("timeLine.showInChild"), 1, false,
                new ChoiceRenderer() {
                    @Override
                    public Object getDisplayValue(Object o) {
                        if ((Boolean) o) {
                            return getString("label.yes");
                        }
                        return getString("label.no");
                    }

                    @Override
                    public String getIdValue(Object o, int i) {
                        return o.toString();
                    }
                }
        );
        form.add(showInChildDropDown);

        searchInParentsDropDown = new MyDropDownChoicePanel("searchInParents",
                Arrays.asList(true, false), false, false, getString("timeLine.searchInParents"), 1, false,
                new ChoiceRenderer() {
                    @Override
                    public Object getDisplayValue(Object o) {
                        if ((Boolean) o) {
                            return getString("label.yes");
                        }
                        return getString("label.no");
                    }

                    @Override
                    public String getIdValue(Object o, int i) {
                        return o.toString();
                    }
                }
        );
        form.add(searchInParentsDropDown);

        timeLineDescriptionPanel = new TextAreaPanel("timeLineDescription");
        timeLineDescriptionPanel.setLabel(new ResourceModel("timeLine.timeLineDescription"));
        form.add(timeLineDescriptionPanel);


        Model<String> keywordModel = Model.of("");
        if (criteria != null && criteria.keywords != null) {
            int count = 0;
            StringBuilder keywordBuilder = new StringBuilder("");
            for (String keyword : criteria.keywords) {
                keywordBuilder.append(keyword);
                if (count != criteria.keywords.size()) {
                    keywordBuilder.append(",");
                }
                count++;
            }
            keywordModel = Model.of(keywordBuilder.toString());
        }
        keywordsTagsInput = new TagsInput("keywords", keywordModel, null);
        keywordsTagsInput.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        keywordsTagsInput.setModel(keywordModel);
        form.add(keywordsTagsInput);


        form.add(new AjaxFormButton("search", form) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {

                String validationString = "";
                if (text != null && text.getValidatorString() != null && !text.getValidatorString().isEmpty()) {
                    for (String validationStringInList : text.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }

                selectedOrgIds.clear();
                OrganizationVO organizationVO = (OrganizationVO) parentTreeViewPanel.getConvertedInput();

                if (organizationVO != null) {
                    if (organizationVO.getTitleFa().equals("root")) {
                        validationString += " - " +
                                getString("error.unSelectable.organization")
                                + "<br/>";
                    }
                }

                if (!validationString.isEmpty()) {
                    target.appendJavaScript("showMessage('" + validationString + "');");
                    return;
                }

                TimeLineElasticService.TimeLineCriteria timeLineCriteria = (TimeLineElasticService.TimeLineCriteria) form.getModelObject();
                DayDate[] dayDates = ((DayDate[]) startShowTimeLineDateTime.getConvertedInput());
                DateTime firstDateTime = (dayDates[0].compareTo(new DayDate(0, 0, 0)) > 0) ? new DateTime(dayDates[0].toDate().getTime()) : DateTime.MIN_DATE_TIME;
                DateTime secondDateTime = (dayDates[1].compareTo(new DayDate(0, 0, 0)) > 0) ? new DateTime(dayDates[1].toDate().getTime()) : DateTime.MAX_DATE_TIME;
                DateTime[] inputDateTime = new DateTime[2];
                inputDateTime[0] = firstDateTime;
                inputDateTime[1] = secondDateTime;
                timeLineCriteria.setStartShowTimeLineDateTime(inputDateTime);
                selectedOrgIds.clear();

                Set<OrganizationVO> selectedNodes = (Set<OrganizationVO>) parentTreeViewPanel.getSelection();
                for (OrganizationVO org : selectedNodes) {
                    selectedOrgIds.add(org.getId());
                }

                if (selectedOrgIds.size() > 0) {
                    timeLineCriteria.setOrganizationIds(selectedOrgIds);
                }
                if (keywordsTagsInput != null && keywordsTagsInput.getConvertedInput() != null) {

                    String[] keywordsString = keywordsTagsInput.getConvertedInput().toString().split(",");
                    List<String> keywords = new ArrayList<String>();
                    for (String string : keywordsString) {
                        keywords.add(string.trim());
                    }
                    timeLineCriteria.keywords = keywords;
                } else {
                    timeLineCriteria.keywords = null;
                }


                if (timeLineCriteria != null) {
                    dp.setCriteria(timeLineCriteria);
                    tableCriteria = timeLineCriteria;
                    table.setVisible(true);
                    target.add(table);
                    if (selectionMode.isSelectable())
                        target.add(TimeLineList.this.get("select").setVisible(true));
                }
            }
        });

        form.add(new AjaxLink("reset") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                TimeLineElasticService.TimeLineCriteria criteria = new TimeLineElasticService.TimeLineCriteria();
                form.setModelObject(criteria);
                timeLineDescriptionPanel.setModelObject(String.valueOf(""));
                selectedParentMdl.setObject(defaultStringForLabel);

                Map<Long, OrganizationVO> organizationVOMap = new HashedMap();

                try {
                    parentTreeViewPanel =
                            new TreeViewPanel("organization", false, true, new ITreeNodeProvider() {
                                @Override
                                public Set<TreeNode> getNodes(TreeNode parentNode) {
                                    try {
                                        Set<TreeNode> treeNodes = new HashSet<>();
                                        Map<Long, TreeNode<OrganizationVO>> organizationVOTreeNodeMap = new HashedMap();

                                        List<OrganizationVO> organizationVOList = null;
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

                                            for (OrganizationVO organizationVOInList : organizationVOList) {
                                                TreeNode tmpTreeNode = organizationVOTreeNodeMap.get(organizationVOInList.getId());
                                                if (tmpTreeNode != null) {
                                                    if (organizationVOInList.getParent() != null) {
                                                        TreeNode parentOrgNode = organizationVOTreeNodeMap.get(organizationVOInList.getParent().getId());
                                                        if (parentOrgNode != null) {
                                                            parentOrgNode.addChild(tmpTreeNode);
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
                                public void validate() {
                                    //                        super.validate();
                                    System.out.println("validate On tree view");
                                }

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
                                            if (parentNodeObject instanceof TreeNode || parentNodeObject instanceof AppCategory) {
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
                } catch (Exception e) {
                    throw e;
                }
                form.remove("organization");
                form.remove("selectedOrganization");
                target.add(form);


                parentTreeViewPanel.setModel(new Model<>());
                parentTreeViewPanel.setRequired(true);
                parentTreeViewPanel.setLabel(new ResourceModel("organization"));
                form.add(parentTreeViewPanel);

                selectedParentLabel = new Label("selectedOrganization", selectedParentMdl);
                selectedParentLabel.setOutputMarkupId(true);
                form.add(selectedParentLabel);

                target.add(parentTreeViewPanel);
                form.add(timeLineDescriptionPanel);
                keywordsTagsInput.setModel(new Model());
                form.add(parentTreeViewPanel);

                target.add(form);

                if (selectionMode.isSelectable())
                    target.add(TimeLineList.this.get("select").setVisible(true));
                else
                    TimeLineList.this.get("select").setVisible(false);

                target.add(TimeLineList.this.get("select"));
                table.setVisible(false);

                target.add(table);
            }
        });


        add(authorize(new AjaxLink("add") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                if (!selectionMode.equals(SelectionMode.WithoutAdd)) {

                    TimeLineForm timeLineForm = new TimeLineForm(modal.getContentId(), new TimeLine());
                    timeLineForm.setParentListner(TimeLineList.this);
                    modal.setContent(timeLineForm);
                    modal.show(target);
                }
            }
        }, WebAction.RENDER, Access.TIME_LINE_ADD).setVisible(!selectionMode.equals(SelectionMode.WithoutAdd)));


        add(new AjaxLink("select") {
            public void onClick(AjaxRequestTarget target) {
                childFinished(target, null, null);
            }
        }.setOutputMarkupPlaceholderTag(true).setVisible(false));
        if (selectionMode.isSelectable() && table.getRowCount() > 0)
            get("select").setVisible(true);

        table = new MyAjaxDataTable("table", getColumns(), dp, 10);
        table.setSelectionMode(selectionMode);
        table.setVisible(true);
        table.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        boolean allFieldsNull = checkAllFieldsNull(criteria);
        if (allFieldsNull)
            table.setVisible(false);
        add(table);


        add(form);

    }

    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {
        if (eventThrownCmp != null && eventThrownCmp.getId().equals("save")) {
            if (tableCriteria != null) {
                dp.setCriteria(tableCriteria);
                table.setVisible(true);
            }
            target.add(table);
        }
        modal.close(target);
    }


    public static class TimeLineListSortableDataProvider extends SortableDataProvider {
        public TimeLineElasticService.TimeLineCriteria criteria;

        public TimeLineListSortableDataProvider() {
            setSort("", SortOrder.ASCENDING);
        }

        public void setCriteria(TimeLineElasticService.TimeLineCriteria criteria) {
            this.criteria = criteria;
        }

        @Override
        public Iterator iterator(long first, long count) {
            SortParam sortParam = getSort();
            String[] sortParams = new String[2];
            sortParams[1] = (String) sortParam.getProperty();
            try {
                List<TimeLineVO> timeLineList = TimeLineElasticService.Instance.searchTimeLine(criteria, (int) first, (int) count, sortParams[1], sortParam.isAscending());
                return timeLineList.iterator();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public long size() {
            try {
                return TimeLineElasticService.Instance.count(criteria, 0, -1, null, true);
            } catch (Exception e) {
                return 0;
            }
        }

        @Override
        public IModel model(Object object) {
            return new Model((Serializable) object);
        }
    }

    private List<IColumn> getColumns() {
        List<IColumn> columnList = new ArrayList<IColumn>();
        columnList.add(new IndexColumn());

        // todo correct and use this line for sorting data
/*
        columnList.add(new PropertyColumn(new ResourceModel("timeLine.title"), "timeLine.title", "title"));

        columnList.add(new PersianDateColumn(new ResourceModel("timeLine.startShowTimeLineDateTime"), "timeLine.startShowTimeLineDateTime", "startDateTime"));

        columnList.add(new PersianDateColumn(new ResourceModel("timeLine.endShowTimeLineDateTime"), "timeLine.endShowTimeLineDateTime", "endDateTime"));
        */


        columnList.add(new PropertyColumn(new ResourceModel("timeLine.title"), "title"));
        columnList.add(new PropertyColumn(new ResourceModel("timeLine.organization"), "organizationNickName"));

        columnList.add(new AbstractColumn(new ResourceModel("timeLine.fileType", "fileType")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                TimeLineVO timeLineVO = (TimeLineVO) rowModel.getObject();
                if (timeLineVO.getFileType() != null) {
                    Long fileType = timeLineVO.getFileType();
                    if (fileType.equals(TimeLineFileType.VIDEO.getState())) {
                        cellItem.add(new Label(componentId, getString("file.type.video")));
                    } else if (fileType.equals(TimeLineFileType.IMAGE.getState())) {
                        cellItem.add(new Label(componentId, getString("file.type.image")));
                    } else if (fileType.equals(TimeLineFileType.HTML.getState())) {
                        cellItem.add(new Label(componentId, getString("file.type.text")));
                    } else {
                        cellItem.add(new Label(componentId, getString("timeLine.file")));
                    }
                }
            }
        });


        columnList.add(new PersianDateColumn(new ResourceModel("timeLine.startShowTimeLine"), "startDateTime"));

        columnList.add(new AbstractColumn(new ResourceModel("timeLine.showInChild")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                TimeLineVO timeLineVO = (TimeLineVO) rowModel.getObject();

                if (PrincipalUtil.hasPermission(Access.TIME_LINE_EDIT)) {
                    SwitchBox switchBox = new SwitchBox(componentId, new ResourceModel("label.yes").getObject(), new ResourceModel("label.no").getObject(), true) {
                        @Override
                        protected void onChange(AjaxRequestTarget target, Boolean currentState) {

                            try {

                                timeLineVO.setShowInChild(currentState);

                                TimeLineElasticService.Instance.insertTimeLine(timeLineVO);

                            } catch (Exception ex) {
                                processException(target, ex);

                            }
                        }
                    };
                    switchBox.setModel(new Model<>(timeLineVO.getShowInChild()));
                    cellItem.add(switchBox);
                } else {
                    cellItem.add(new Label(componentId, new Model<>(
                            (timeLineVO.getShowInChild() != null && timeLineVO.getShowInChild()) ? getString("label.yes") : getString("label.no"))));
                }
            }
        });

        columnList.add(new AbstractColumn(new ResourceModel("label.activation.verb")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                TimeLineVO timeLineVO = (TimeLineVO) rowModel.getObject();

                if (PrincipalUtil.hasPermission(Access.TIME_LINE_EDIT)) {
                    SwitchBox switchBox = new SwitchBox(componentId, new ResourceModel("label.yes").getObject(), new ResourceModel("label.no").getObject(), true) {
                        @Override
                        protected void onChange(AjaxRequestTarget target, Boolean currentState) {

                            try {

                                timeLineVO.setActive(currentState);

                                TimeLineElasticService.Instance.insertTimeLine(timeLineVO);

                            } catch (Exception ex) {
                                processException(target, ex);

                            }
                        }
                    };
                    switchBox.setModel(new Model<>(timeLineVO.getActive()));
                    cellItem.add(switchBox);
                } else {
                    cellItem.add(new Label(componentId, new Model<>(
                            (timeLineVO.getActive() != null && timeLineVO.getActive()) ? getString("label.yes") : getString("label.no"))));
                }
            }
        });

        columnList.add(new AbstractColumn(new Model("")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                if (PrincipalUtil.hasPermission(Access.TIME_LINE_EDIT)) {
                    TimeLineVO timeLineVO = (TimeLineVO) rowModel.getObject();
                    TimeLine timeLine = TimeLine.getTimeLineFromTimeLineVO(timeLineVO);
                    cellItem.add(new AjaxLinkPanel(componentId, AjaxLinkPanel.Image.Edit) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {

                            TimeLineForm timeLineForm = new TimeLineForm(modal.getContentId(), timeLine);
                            timeLineForm.setParentListner(TimeLineList.this);
                            modal.setContent(timeLineForm);
                            modal.show(target);

                        }
                    });
                } else {
                    cellItem.add(new Label(componentId, new Model<>("")));
                }
            }
        });


        return columnList;
    }


}
