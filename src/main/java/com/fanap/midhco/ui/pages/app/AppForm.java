package com.fanap.midhco.ui.pages.app;

import com.fanap.midhco.appstore.applicationUtils.ComponentKey;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.UserStatus;
import com.fanap.midhco.appstore.restControllers.vos.PackageVO;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.app.AppElasticService;
import com.fanap.midhco.appstore.service.app.AppPackageService;
import com.fanap.midhco.appstore.service.app.AppService;
import com.fanap.midhco.appstore.service.app.IAPPPackageService;
import com.fanap.midhco.appstore.service.appcategory.AppCategoryService;
import com.fanap.midhco.appstore.service.myException.PageValidationException;
import com.fanap.midhco.appstore.service.os.OSService;
import com.fanap.midhco.appstore.service.osType.OSTypeService;
import com.fanap.midhco.appstore.service.user.RoleService;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.component.ISelectable;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.limitedTextField.LimitedTextField;
import com.fanap.midhco.ui.component.multiAjaxFileUpload2.UploadedFileInfo;
import com.fanap.midhco.ui.component.mydropdown.MyDropDownChoicePanel;
import com.fanap.midhco.ui.component.selectionpanel.SelectionPanel;
import com.fanap.midhco.ui.component.switchbox.SwitchBox;
import com.fanap.midhco.ui.component.tagsinput.TagsInput;
import com.fanap.midhco.ui.component.textareapanel.TextAreaPanel;
import com.fanap.midhco.ui.component.treeview.ITreeNodeProvider;
import com.fanap.midhco.ui.component.treeview.TreeNode;
import com.fanap.midhco.ui.component.treeview.TreeViewPanel;
import com.fanap.midhco.ui.pages.user.UserList;
import org.apache.commons.collections.map.HashedMap;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

import java.util.*;

/**
 * Created by admin123 on 6/30/2016.
 */
@Authorize(views = {Access.APP_ADD, Access.APP_EDIT})
public class AppForm extends BasePanel implements IParentListner {
    Form form;
    FeedbackPanel feedbackPanel;
    MyDropDownChoicePanel osTypeDropDown;
    MyDropDownChoicePanel osDropDown;
    SelectionPanel developerPanel;
    WebMarkupContainer addPackageForm;
    TextField appPackageLable;
    WebMarkupContainer appPackageList;
    Label appCategoryLabel;
    Long selectedAppCategoryId;
    boolean isSelectedAppCategoryIdAssignable;
    boolean isUserDeveloper;
    TagsInput tagsInput;
    String keywordId = "";
    boolean newApp = false;
    SwitchBox deleteSwitchBox;
    Label deleteLbl;
    LimitedTextField titleTextField;
    LimitedTextField shortDescriptionTextField;
    boolean hasId = false;

    protected AppForm(String id, App app) {
        super(id);

        Model<String> selectedParentMdl = Model.of("....");
        hasId = app.getId() != null;

        if (app != null && app.getId() == null) {
            newApp = true;
        }
        feedbackPanel = new FeedbackPanel("feedbackPanel");
        feedbackPanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(feedbackPanel);

        User currentUser = PrincipalUtil.getCurrentUser();
        isUserDeveloper = UserService.Instance.isUserDeveloper(currentUser);

        form = new Form("form", new CompoundPropertyModel(app));
        form.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        List<OSType> osTypeList = OSTypeService.Instance.getEnabledOSTypes();
        osTypeDropDown =
                new MyDropDownChoicePanel("osType", osTypeList, false, false, getString("OSType"), 1, true, new ChoiceRenderer<>()) {
                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        OSType osType = (OSType) getSelectedItem();
                        if (osType == null) {
                            osDropDown.setChoices(new ArrayList(), target);
                            if (addPackageForm instanceof AddPackagePanel) {
                                ((AddPackagePanel) addPackageForm).reset(target);
                            }
                        } else {
                            OSService.OSCriteria osCriteria = new OSService.OSCriteria();
                            osCriteria.osType = new ArrayList<OSType>();
                            osCriteria.osType.add(osType);
                            Session session = HibernateUtil.getCurrentSession();
                            try {
                                List<OS> osList = OSService.Instance.list(osCriteria, 0, -1, null, true, session);
                                osDropDown.setChoices(osList, target);
                            } finally {
                                session.close();
                            }

                            try {
                                if (addPackageForm instanceof AddPackagePanel) {
                                    ((AddPackagePanel) addPackageForm).setOsType(target, osType);
                                    ((AddPackagePanel) addPackageForm).process(target);
                                }
                            } catch (Exception e) {
                                target.appendJavaScript("showMessage('" + AppStorePropertyReader.getString("error.osType.noOS") + "');");
                                return;
                            }
                        }
                    }
                };
        osTypeDropDown.setLabel(new ResourceModel("OSType"));
        if (app.getId() != null) {
            osTypeDropDown.setEnabled(false);
            osTypeDropDown.isEnabled();
        }
        form.add(osTypeDropDown);


        List<OS> osList = new ArrayList<>();
        if (app.getOsType() != null) {
            Session session = HibernateUtil.getCurrentSession();
            OSService.OSCriteria osCriteria = new OSService.OSCriteria();
            osCriteria.osType = new ArrayList<>();
            osCriteria.osType.add(app.getOsType());
            osList.addAll(OSService.Instance.list(osCriteria, 0, -1, null, true, session));
        }
        osDropDown = new MyDropDownChoicePanel("os", osList, false, false, getString("OS"), 1);
        osDropDown.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        osDropDown.setLabel(new ResourceModel("OS"));
//        osDropDown.setRequired(true);
        form.add(osDropDown);

        final Map<String, String> keyWordMap;

        if (app != null && app.getId() != null) {
            keyWordMap = getKeywordMap(app);
        } else {
            keyWordMap = new HashedMap();
        }

        if (keyWordMap != null && keyWordMap.size() != 0) {
            if (keyWordMap != null && keyWordMap.size() != 0) {
                for (Map.Entry<String, String> entry : keyWordMap.entrySet()) {
                    keywordId = keyWordMap.get(entry.getKey());
                    break;
                }
            }
        }
        Model<String> keyWordModel = AppElasticService.Instance.getKeywordModelByKeywordMap(keyWordMap);

        tagsInput = new TagsInput("tagsinput", keyWordModel, null);
        tagsInput.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        tagsInput.setModel(keyWordModel);
        form.add(tagsInput);


        final TreeViewPanel appCategoryTreeViewPanel =
                new TreeViewPanel("appCategory", false, true, new ITreeNodeProvider() {
                    @Override
                    public Set<TreeNode> getNodes(TreeNode parentNode) {
                        Set<TreeNode> treeNodes = new HashSet<>();
                        Map<Long, TreeNode<AppCategory>> appCategoryMap = new HashedMap();
                        List<AppCategory> appCategoryList = AppCategoryService.Instance.listAllEnable();
                        for (AppCategory appCategoryInList : appCategoryList) {
                            if (!appCategoryMap.containsKey(appCategoryInList.getId())) {
                                TreeNode<AppCategory> treeNode = new TreeNode<>();
                                treeNode.setId(appCategoryInList.getId());
                                treeNode.setTitle(appCategoryInList.getCategoryName());
                                treeNode.setSelf(appCategoryInList);
                                appCategoryMap.put(appCategoryInList.getId(), treeNode);
                            }
                        }

                        Session session = HibernateUtil.getCurrentSession();
                        selectedAppCategoryId = null;

                        try {
                            for (AppCategory appCategoryInList : appCategoryList) {
                                AppCategory loadedAppCategory = (AppCategory) session.load(AppCategory.class, appCategoryInList.getId());
                                App tmpApp;
                                if (app.getId() != null) {
                                    tmpApp = (App) session.load(App.class, app.getId());
                                } else {
                                    tmpApp = app;
                                }
                                TreeNode tmpTreeNode = appCategoryMap.get(loadedAppCategory.getId());
                                if (tmpApp.getAppCategory() != null) {
                                    AppCategory loadedTmpAppCategory = (AppCategory) session.load(AppCategory.class, tmpApp.getAppCategory().getId());
                                    if (tmpTreeNode.getId().equals(loadedTmpAppCategory.getId())) {
                                        tmpTreeNode.setSelected(true);
                                        AppCategory appCategory = (AppCategory) session.load(AppCategory.class, tmpTreeNode.getId());
                                        selectedAppCategoryId = tmpTreeNode.getId();
                                        isSelectedAppCategoryIdAssignable =
                                                appCategory.getAssignable() == null ? false : appCategory.getAssignable();
                                    }
                                }
                                if (loadedAppCategory.getParent() != null) {
                                    TreeNode fatherNode = appCategoryMap.get(loadedAppCategory.getParent().getId());
                                    if (fatherNode != null) {
                                        fatherNode.addChild(tmpTreeNode);
                                        tmpTreeNode.setParent(fatherNode);
                                        tmpTreeNode.setSelf(tmpTreeNode);
                                        treeNodes.add(tmpTreeNode);
                                    }
                                } else {
                                    tmpTreeNode.setParent(null);
                                    treeNodes.add(tmpTreeNode);
                                }
                            }
                        } finally {
                        }

                        return treeNodes;
                    }

                }) {
                    @Override
                    public void onUpdate(AjaxRequestTarget target, Collection selectedNodes) {
                        Session session = HibernateUtil.getCurrentSession();
                        try {
                            Iterator selectedNodeIterator = selectedNodes.iterator();
                            if (selectedNodeIterator.hasNext()) {
                                String appCategoryTitle = "";
                                Long appCategorySelectedNode;
                                Object parentNodeObject = selectedNodeIterator.next();
                                if (parentNodeObject instanceof TreeNode || parentNodeObject instanceof AppCategory) {
                                    if (parentNodeObject instanceof TreeNode) {
                                        TreeNode treeNode = (TreeNode) parentNodeObject;
                                        appCategorySelectedNode = treeNode.getId();
                                    } else {
                                        AppCategory appCategoryForParent = (AppCategory) parentNodeObject;
                                        appCategorySelectedNode = appCategoryForParent.getId();
                                    }
                                    AppCategory loadedAppCategory = (AppCategory) session.load(AppCategory.class, appCategorySelectedNode);
                                    appCategoryTitle = "";
                                    if (loadedAppCategory != null) {
                                        appCategoryTitle = loadedAppCategory.getCategoryName();
                                        selectedAppCategoryId = loadedAppCategory.getId();
                                        isSelectedAppCategoryIdAssignable =
                                                loadedAppCategory.getAssignable() == null ? false : loadedAppCategory.getAssignable();
                                    }
                                    selectedParentMdl.setObject(appCategoryTitle);
                                }
                            }
                            target.add(appCategoryLabel);
                            appCategoryLabel.setOutputMarkupId(true);
                        } finally {
                        }
                    }
                };

        appCategoryLabel = new Label("appCategoryLbl", selectedParentMdl);
        appCategoryLabel.setOutputMarkupId(true);
        form.add(appCategoryLabel);
        appCategoryTreeViewPanel.setLabel(new ResourceModel("AppCategory"));
        appCategoryTreeViewPanel.setModel(new Model<>());
//        appCategoryTreeViewPanel.setRequired(true);
        form.add(appCategoryTreeViewPanel);

        appPackageLable = new TextField("packageName");
        appPackageLable.setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true);
        appPackageLable.setEnabled(false);
        appPackageLable.isEnabled();
        appPackageLable.setLabel(new ResourceModel("App.appPackageName"));
        form.add(appPackageLable);

        titleTextField = new LimitedTextField("title", null, false, false, true, false, 40, getString("App.title"));
        titleTextField.setLabel(new ResourceModel("App.title"));
//        titleTextField.setRequired(true);

        form.add(titleTextField);


        shortDescriptionTextField = new LimitedTextField("shortDescription", null, null, true, true, true, 200, getString("App.shortDescription"));
        shortDescriptionTextField.setLabel(new ResourceModel("App.shortDescription"));
//        shortDescriptionTextField.setRequired(true);
        shortDescriptionTextField.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(shortDescriptionTextField);

        developerPanel = new SelectionPanel("developer", SelectionMode.Single) {

            @Override
            public ISelectable getSelectable(String panelId) {
                List<ComponentKey> disabledList = new ArrayList<ComponentKey>();
                UserService.UserCriteria userCriteria = new UserService.UserCriteria();

                Role developerRole = RoleService.Instance.getDeveloperRole();
                disabledList.add(new ComponentKey("roles"));
                disabledList.add(new ComponentKey("statuses"));
                userCriteria.roles = new ArrayList<Role>();
                userCriteria.roles.add(developerRole);
                userCriteria.statuses = new ArrayList<>();
                userCriteria.statuses.add(UserStatus.ENABLED);
                return new UserList(panelId, userCriteria, disabledList, SelectionMode.Single);
            }
        };

        if (isUserDeveloper) {
            developerPanel.setEnabled(false);

            if (!hasId)
                app.setDeveloper(currentUser);

            developerPanel.setConvertedInput(currentUser);
        }

        developerPanel.setLabel(new ResourceModel("App.developer"));
        form.add(developerPanel);

        TextAreaPanel appDescriptionPanel = new TextAreaPanel("description");
        appDescriptionPanel.setLabel(new ResourceModel("App.description"));
        form.add(appDescriptionPanel);

        WebMarkupContainer appPackagesPanel = new WebMarkupContainer("appPackagesPanel");
        appPackagesPanel.setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true);

        if (app.getId() != null) {
            appPackageList = new AppPackageList(
                    "appPackagesList", SelectionMode.None, app.getPackageName(), app.getOsType());
            ((AppPackageList) appPackageList).setParentForm(form);
            appPackageList.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
            appPackagesPanel.add(appPackageList);
        } else {
            appPackageList = new WebMarkupContainer("appPackagesList");
            appPackageList.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
            appPackagesPanel.add(appPackageList);
            appPackagesPanel.setVisible(false);
        }

        form.add(appPackagesPanel);

        WebMarkupContainer addPackagePanel = new WebMarkupContainer("addPackagePanel");
        addPackagePanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        if (!PrincipalUtil.hasPermission(Access.APP_ADD_PACKAGE)) {
            addPackagePanel.setVisible(false);
            addPackageForm = new WebMarkupContainer("addPackageForm");
            addPackageForm.setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true);
            addPackagePanel.add(addPackageForm);
        } else {
            addPackageForm = new AddPackagePanel("addPackageForm", app);
            addPackageForm.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
            ((AddPackagePanel) addPackageForm).setParentListner(AppForm.this);
            addPackagePanel.add(addPackageForm);
        }

        form.add(addPackagePanel);

        Boolean isDeletedApp = null;
        Boolean enabledSwitchBox = false;
        if (app != null) {
            if (app.getDeleted() != null) {
                isDeletedApp = app.getDeleted();
            } else {
                isDeletedApp = false;
            }
            if (app.getId() != null) {
                if (PrincipalUtil.hasPermission(Access.APP_REMOVE) && !isDeletedApp) {
                    enabledSwitchBox = true;
                }
            }
        }

        deleteSwitchBox = new SwitchBox(isDeletedApp, "delete", getString("label.yes"), getString("label.no"));
        deleteSwitchBox.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        deleteSwitchBox.setLabel(new ResourceModel("label.delete"));
        deleteSwitchBox.setModel(new Model<>());

        deleteSwitchBox.setEnabled(enabledSwitchBox);
        deleteSwitchBox.setVisible(enabledSwitchBox);
        form.add(deleteSwitchBox);

        Label deleteLabel = new Label("deleteLbl");
        deleteLabel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        Model deleteModel = new Model(getString("label.delete"));
        deleteLabel.setDefaultModel(deleteModel);
        deleteLabel.setEnabled(enabledSwitchBox);
        deleteLabel.setVisible(enabledSwitchBox);
        form.add(deleteLabel);


        form.add(new AjaxFormButton("save", form) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {
                if (newApp) {
                    if (app != null) {
                        app.setId(null);
                    }
                }

                Session session = HibernateUtil.getNewSession();
                session.flush();
                Transaction tx = null;

                try {
                    String validationString = "";

                    OSType selectedOsType = (OSType) osTypeDropDown.getConvertedInput();
                    OS selectedOs = (OS) osDropDown.getConvertedInput();
                    if (app.getId() != null) {
                        session.refresh(app);
                        selectedOsType = app.getOsType();
//                        selectedOs = app.getOs();
                    }

//                    if (selectedOs == null) {
//                        validationString += " - " +
//                                getString("Required").replace("${label}", osDropDown.getLabel().getObject()) + "<br/>";
//                    }

                    if (selectedOsType == null) {
                        validationString += " - " +
                                getString("Required").replace("${label}", osTypeDropDown.getLabel().getObject()) + "<br/>";
                    } else if (selectedOsType.getDisabled()) {
                        validationString += " - " +
                                getString("Disable").replace("${label}", osTypeDropDown.getLabel().getObject()) + "<br/>";
                    }

                    if (selectedOs != null && selectedOs.getDisabled()) {
                        validationString += " - " +
                                getString("Disable").replace("${label}", osDropDown.getLabel().getObject()) + "<br/>";
                    }

                    if (!isUserDeveloper && (developerPanel == null || developerPanel.getConvertedInput() == null)) {
                        validationString += " - " +
                                getString("Required").replace("${label}", new ResourceModel("App.developer").getObject()) + "<br/>";
                    }

                    if (titleTextField.getConvertedInput() == null || titleTextField.getConvertedInput() == "") {
                        validationString += " - " +
                                getString("Required").replace("${label}", titleTextField.getLabel().getObject()) + "<br/>";
                    }

                    if (shortDescriptionTextField.getConvertedInput() == null) {
                        validationString += " - " +
                                getString("Required").replace("${label}", shortDescriptionTextField.getLabel().getObject()) + "<br/>";
                    }

                    if (selectedAppCategoryId == null) {
                        validationString += " - " +
                                getString("AppCategory.doesNotFound") + "<br/>";
                    } else if (!isSelectedAppCategoryIdAssignable) {
                        validationString += " - " +
                                getString("AppCategory.not.assignable").replace("${label}", appCategoryTreeViewPanel.getLabel().getObject()) + "<br/>";
                    }

                    if (titleTextField != null && titleTextField.getValidatorString() != null && !titleTextField.getValidatorString().isEmpty()) {
                        for (String validationStringInList : titleTextField.getValidatorString()) {
                            validationString += " - " +
                                    validationStringInList + "<br/>";
                        }
                    }

                    if (shortDescriptionTextField != null && shortDescriptionTextField.getValidatorString() != null && !shortDescriptionTextField.getValidatorString().isEmpty()) {
                        for (String validationStringInList : shortDescriptionTextField.getValidatorString()) {
                            validationString += " - " +
                                    validationStringInList + "<br/>";
                        }
                    }


                    AppPackageService.AppPackageModel appPackageModel = ((AddPackagePanel) addPackageForm).getAppPackage();
                    boolean deleted = deleteSwitchBox == null ? false : (deleteSwitchBox.getConvertedInput() == null ? false : (boolean) deleteSwitchBox.getConvertedInput());
                    String iconFileName = null;
                    String iconFileKey = null;
                    Map<String, String> thumbFileInfoMap = null;

                    String description = null;
                    String title = null;
                    String shortDescriptionString = null;
                    String packageFileKey = null;
                    Long packageFileSize = null;
                    app.setDeleted(deleted);

                    if (!deleted) {
                        if (!validationString.isEmpty()) {
                            target.appendJavaScript("showMessage('" + validationString + "');");
                            return;
                        }
                    }

                    tx = session.beginTransaction();

                    if (!deleted) {
                        iconFileName = null;
                        iconFileKey = null;
                        thumbFileInfoMap = new HashedMap();

                        if (isUserDeveloper && !hasId)
                            app.setDeveloper(currentUser);
                        else if (!isUserDeveloper)
                            app.setDeveloper((User) developerPanel.getConvertedInput());

                        if (appPackageModel.usePreviousFile) {
                            PackageVO packageVO = AppPackageService.Instance.getPreviousPackageFiles(app, session);
                            if (packageVO == null || packageVO.getIconFile() == null || packageVO.getThumbFiles() == null || packageVO.getThumbFiles().isEmpty()) {
                                processException(target, new Exception(ResultStatus.NULL_DATA.toString()));
                                return;
                            } else {
                                iconFileKey = packageVO.getIconFile().getFilePath();
                                iconFileName = packageVO.getIconFile().getFileName();
                                for (File thumbFile : packageVO.getThumbFiles()) {
                                    thumbFileInfoMap.put(thumbFile.getFilePath(), thumbFile.getFileName());
                                }
                            }

                        } else if (appPackageModel.getPackFileInfo() != null) {
                            UploadedFileInfo iconFileInfo = appPackageModel.getIconFileInfo();
                            if (iconFileInfo != null) {
                                iconFileName = iconFileInfo.getFileName();
                                iconFileKey = iconFileInfo.getFileId();
                            }

                            Set<UploadedFileInfo> thumbFileInfoList = appPackageModel.getThumbFilesInfo();
                            if (thumbFileInfoList != null) {
                                for (UploadedFileInfo thumbImageFileInfo : thumbFileInfoList) {
                                    File tempFile = new File();
                                    tempFile.setStereoType(StereoType.THUMB_FILE);
                                    thumbFileInfoMap.put(thumbImageFileInfo.getFileId(), thumbImageFileInfo.getFileName());
                                }
                            }
                        }

                        description = (String) appDescriptionPanel.getConvertedInput();
                        title = (String) titleTextField.getConvertedInput();
                        shortDescriptionString = (String) shortDescriptionTextField.getConvertedInput();
                        packageFileKey = appPackageModel != null && appPackageModel.getPackFileInfo() != null ? appPackageModel.getPackFileInfo().getFileId() : null;
                        packageFileSize = appPackageModel != null && appPackageModel.getPackFileInfo() != null ? appPackageModel.getPackFileInfo().getFileSize() : null;
                        app.setOs(selectedOs);
                        String packageName = "";
                        if (packageFileKey != null && !packageFileKey.trim().equals("")) {
                            IAPPPackageService iappPackageService = AppPackageService.Instance.processPackageFile(packageFileKey, selectedOsType);
                            packageName = iappPackageService.getPackage();
                            app.setPackageName(packageName);
                            AppService.AppSearchCriteria appSearchCriteria = new AppService.AppSearchCriteria();
                            appSearchCriteria.setPackageName(app.getPackageName());
                            appSearchCriteria.setDeleted(false);
                            if (app.getOsType() != null) {
                                List<OSType> osTypeList = new ArrayList<>();
                                osTypeList.add(app.getOsType());
                                appSearchCriteria.setOsType(osTypeList);
                            }
                            appSearchCriteria.setId(app.getId());
                            Boolean checkExistUnDeletedApp = AppService.Instance.checkExistUnDeletedApp(appSearchCriteria, session);
                            if (checkExistUnDeletedApp == null) {
                                validationString += " - " + "app is null" + "<br/>";

                            } else if (checkExistUnDeletedApp) {
                                validationString += " - " + ResultStatus.UNDELETED_APP_FOUND.toString() + "<br/>";
                            }
                        }

//                        if (appPackageModel.getPackFileInfo() != null && (iconFileKey == null || iconFileKey.trim().equals("") || iconFileName == null ||
//                                iconFileName.trim().equals("") || thumbFileInfoMap.isEmpty())) {
//                            validationString += " - " + ResultStatus.IMAGES_NOT_FOUND.toString() + "<br/>";
//                        }

                        if (appPackageModel.getPackFileInfo() == null && (appPackageModel.getIconFileInfo() != null || appPackageModel.getThumbFilesInfo() != null || appPackageModel.usePreviousFile)) {
                            validationString += " - " + ResultStatus.PACKAGE_NOT_FOUND.toString() + "<br/>";
                        }

                        if (!validationString.isEmpty()) {
                            target.appendJavaScript("showMessage('" + validationString + "');");
                            return;
                        }

                        AppService.Instance.savePackageForApp(
                                app,
                                appPackageModel != null && appPackageModel.getPackFileInfo() != null ? appPackageModel.getPackFileInfo().getFileName() : null,
                                packageFileKey,
                                iconFileName, iconFileKey, thumbFileInfoMap, selectedOsType,
                                description, title.trim(), shortDescriptionString, packageFileSize, selectedAppCategoryId, session
                        );

                        String tagInputString = (String) tagsInput.getConvertedInput();
                        AppElasticService.AppKeyWordVO appKeyWordVO = new AppElasticService.AppKeyWordVO();
                        appKeyWordVO.setId(keywordId);

                        appKeyWordVO.setAppId(app.getId());
                        appKeyWordVO.setOsId(app.getOs() != null ? app.getOs().getId() : null);
                        appKeyWordVO.setOsTypeId(app.getOsType().getId());
                        appKeyWordVO.setAppPackageName(app.getPackageName().trim());
                        if (tagInputString != null && !tagInputString.trim().isEmpty()) {
                            String elasticKeyWord = AppElasticService.Instance.setElasticKeyword(tagInputString);
                            appKeyWordVO.setKeyword(elasticKeyWord);
                            AppElasticService.Instance.insertAppKeyWordVO(appKeyWordVO);
                        } else {
                            if (app.getId() != null) {
                                AppService.AppSearchCriteria appSearchCriteria = new AppService.AppSearchCriteria();
                                appSearchCriteria.setId(app.getId());

                                User currentUser = PrincipalUtil.getCurrentUser();
                                if (currentUser != null || !PrincipalUtil.hasPermission(Access.APP_REMOVE)) {
                                    appSearchCriteria.setDeleted(false);
                                }
                                List<AppService.AppSearchResultModel> appSearchResultModelList = AppService.Instance.list(appSearchCriteria, 0, 10, "app.id", false, session);
                                if (appSearchResultModelList != null && !appSearchResultModelList.isEmpty()) {
                                    AppService.AppSearchResultModel loadedAppSearchResultModel = appSearchResultModelList.get(0);
                                    AppElasticService.AppKeyWordCriteria appKeyWordCriteria = new AppElasticService.AppKeyWordCriteria();
                                    appKeyWordCriteria.setAppId(loadedAppSearchResultModel.getAppId());
                                    List<AppElasticService.AppKeyWordVO> appKeyWordVOList = AppElasticService.Instance.listAppKeyWord(appKeyWordCriteria, 0, -1);
                                    for (AppElasticService.AppKeyWordVO appKeyWordVOInList : appKeyWordVOList) {
                                        AppElasticService.AppKeyWordCriteria appKeyWordCriteriaForDelete = new AppElasticService.AppKeyWordCriteria();
                                        appKeyWordCriteriaForDelete.setId(appKeyWordVOInList.getId());
                                        AppElasticService.Instance.deleteAppKeyword(appKeyWordCriteriaForDelete);
                                    }
                                }
                            }
                        }

                    } else {
                        AppService.Instance.deleteApp(app, session);
                    }

                    tx.commit();
                    childFinished(target, new Model<>(), this);
                } catch (ConstraintViolationException ex) {
                    if (tx != null)
                        tx.rollback();
                    processException(target, ex);
                } catch (PageValidationException | AppPackageService.APPPackageException ex) {
                    logger.error("Error saving AppForm : ", ex);
                    String message = ex.getMessage();
                    target.appendJavaScript("showMessage('" + message + "');");
                    if (tx != null)
                        tx.rollback();
                } catch (Exception ex) {
                    if (tx != null)
                        tx.rollback();

                    if (ex.getMessage() != null && ex.getMessage().contains("certificationNotValid"))
                        target.appendJavaScript("showMessage('" + getString("App.certificate.not.valid") + "');");
                    else
                        processException(target, ex);
                } finally {
                    if (session != null && session.isOpen()) {
                        session.close();
                    }
                }
            }
        });

        form.add(new AjaxLink("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                childFinished(target, null, this);
            }
        });

        add(form);
    }


    private Map<String, String> getKeywordMap(App app) {
        AppElasticService.AppKeyWordCriteria appKeyWordCriteria = AppElasticService.Instance.buildAppKeyWordCriteriaByApp(app);
        int from = Integer.valueOf(0);
        int count = Integer.valueOf(-1);
        Map<String, String> keyWordMap = AppElasticService.Instance.getKeyWordMap(appKeyWordCriteria, from, count);

        return keyWordMap;
    }


    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {
        if (childModel != null) {
            Object modelObject = childModel.getObject();
            if (modelObject instanceof IAPPPackageService) {
                if (!hasId) {
                    String packageName = ((IAPPPackageService) modelObject).getPackage();
                    appPackageLable.setModel(new Model<>(packageName));
                }
                target.add(appPackageLable);
            }
        }
    }
}
