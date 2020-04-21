package com.fanap.midhco.ui.pages.appcategory;

import com.fanap.midhco.appstore.applicationUtils.AppUtils;
import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.AppCategory;
import com.fanap.midhco.appstore.entities.File;
import com.fanap.midhco.appstore.entities.StereoType;
import com.fanap.midhco.appstore.service.BaseEntityService;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.appcategory.AppCategoryService;
import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import com.fanap.midhco.appstore.service.osType.IUploadFilter;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.limitedTextField.LimitedTextField;
import com.fanap.midhco.ui.component.multiAjaxFileUpload2.MultiAjaxFileUploadPanel2;
import com.fanap.midhco.ui.component.multiAjaxFileUpload2.UploadedFileInfo;
import com.fanap.midhco.ui.component.treeview.ITreeNodeProvider;
import com.fanap.midhco.ui.component.treeview.TreeNode;
import com.fanap.midhco.ui.component.treeview.TreeViewPanel;
import org.apache.commons.collections.map.HashedMap;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Created by admin123 on 2/12/2017.
 */
@Authorize(views = {Access.APPCATEGORY_ADD, Access.APPCATEGORY_EDIT})
public class AppCategoryForm extends BasePanel {
    Form form;
    FeedbackPanel feedbackPanel;
    Label selectedParentLabel;
    AppCategory selectedParentAppCategory = null;
    Long appCategoryId = null;
    Long needToEditAppCategoryId = null;
    MultiAjaxFileUploadPanel2 appCategoryIconMultiAjaxFileUploadPanel2;
    NonCachingImage tmpIconFile;
    int categoryIconSize;
    String fileKey;
    LimitedTextField categoryNameTextField;
    String parentNodeTitle = "";


    public AppCategoryForm(String id, AppCategory appCategory) {
        super(id);
        if (appCategory != null && appCategory.getId() != null) {
            needToEditAppCategoryId = appCategory.getId();
        }

        if (appCategory != null) {
            fileKey = appCategory.getIconFile() != null ? appCategory.getIconFile().getFilePath() : null;
        } else {
            fileKey = null;
        }
        categoryIconSize = Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.APP_CATEGORY_ICON_SIZE));

        Model<String> selectedParentMdl = Model.of("....");
        feedbackPanel = new FeedbackPanel("feedbackPabel");
        feedbackPanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(feedbackPanel);

        form = new Form("form", new CompoundPropertyModel(appCategory));
        form.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        categoryNameTextField = new LimitedTextField("categoryName", null, false, true, true,false, 30, getString("AppCategory.categoryName"));
        categoryNameTextField.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        categoryNameTextField.setRequired(true);
        categoryNameTextField.setLabel(new ResourceModel("AppCategory.categoryName"));
        form.add(categoryNameTextField);

        CheckBox isEnabledCheckBox = new CheckBox("isEnabled");
        isEnabledCheckBox.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        isEnabledCheckBox.setLabel(new ResourceModel("AppCategory.isEnabled"));
        isEnabledCheckBox.setRequired(true);
        form.add(isEnabledCheckBox);

        CheckBox isAssignableCheckBox = new CheckBox("isAssignable");
        isAssignableCheckBox.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        isAssignableCheckBox.setLabel(new ResourceModel("AppCategory.isAssignable"));
        isAssignableCheckBox.setRequired(true);
        form.add(isAssignableCheckBox);

        AppCategoryService.AppCategoryCriteria appCategoryCriteria = new AppCategoryService.AppCategoryCriteria();
        appCategoryCriteria.setAssignable(false);

        final TreeViewPanel parentTreeViewPanel =
                new TreeViewPanel("parent", false, true, new ITreeNodeProvider() {
                    @Override
                    public Set<TreeNode> getNodes(TreeNode parentNode) {
                        Set<TreeNode> treeNodes = new HashSet<>();
                        Map<Long, TreeNode<AppCategory>> appCategoryMap = new HashedMap();
                        List<AppCategory> appCategoryList = AppCategoryService.Instance.listAll();

                        for (AppCategory appCategoryInList : appCategoryList) {
                            if (!appCategoryMap.containsKey(appCategoryInList.getId())) {
                                TreeNode<AppCategory> treeNode = new TreeNode<>();
                                treeNode.setId(appCategoryInList.getId());
                                treeNode.setSelf(appCategoryInList);
                                treeNode.setTitle(appCategoryInList.getCategoryName());
                                appCategoryMap.put(appCategoryInList.getId(), treeNode);
                            }
                        }
                        Session session = HibernateUtil.getCurrentSession();
                        try {
                            if (needToEditAppCategoryId!=null) {
                                if (!appCategoryMap.containsKey(needToEditAppCategoryId)) {
                                    TreeNode<AppCategory> treeNode = new TreeNode<>();
                                    AppCategory needToEditAppCategory = (AppCategory) session.load(AppCategory.class, needToEditAppCategoryId);
                                    treeNode.setId(needToEditAppCategoryId);
                                    treeNode.setSelf(needToEditAppCategory);
                                    treeNode.setTitle(needToEditAppCategory.getCategoryName());
                                    appCategoryMap.put(needToEditAppCategoryId, treeNode);
                                    appCategoryList.add(needToEditAppCategory);
                                }
                            }
                            for (AppCategory appCategoryInList : appCategoryList) {
                                TreeNode tmpTreeNode = appCategoryMap.get(appCategoryInList.getId());
                                if (appCategoryInList.getParent() != null) {
                                    AppCategory loadedAppCategoryInList = (AppCategory) session.load(AppCategory.class, appCategoryInList.getId());
                                    TreeNode fatherNode = appCategoryMap.get(loadedAppCategoryInList.getParent().getId());
                                    if (fatherNode != null) {
                                    fatherNode.addChild(tmpTreeNode);
                                    if (appCategory.getId() != null) {
                                            appCategoryId = appCategory.getId();
                                        AppCategory loadedAppCategory = (AppCategory) session.load(AppCategory.class, appCategory.getId());
                                        if (loadedAppCategory.getParent() != null) {
                                            if (fatherNode.getId().equals(loadedAppCategory.getParent().getId())) {
                                                fatherNode.setSelected(true);
                                            }
                                        }
                                    }
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
                            session.close();
                        }
                        return treeNodes;
                    }
                })

                {
                    @Override
                    public void onUpdate(AjaxRequestTarget target, Collection selectedNodes) {
                        Session session = HibernateUtil.getCurrentSession();
                        try {
                            Iterator selectedNodeIterator = selectedNodes.iterator();
                            if (selectedNodeIterator.hasNext()) {
                                selectedParentAppCategory = null;
                                String parentNodeTitle = "";
                                Long parentNodeId;
                                Object parentNodeObject = selectedNodeIterator.next();
                                if (parentNodeObject instanceof TreeNode || parentNodeObject instanceof AppCategory) {
                                    if (parentNodeObject instanceof TreeNode) {
                                        TreeNode treeNode = (TreeNode) parentNodeObject;
                                        parentNodeId = treeNode.getId();
                                    } else {
                                        AppCategory appCategoryForParent = (AppCategory) parentNodeObject;
                                        parentNodeId = appCategoryForParent.getId();
                                    }
                                    AppCategory loadedParentAppCategory = (AppCategory) session.load(AppCategory.class, parentNodeId);
                                    parentNodeTitle = "";
                                    if (loadedParentAppCategory != null) {
                                        parentNodeTitle = loadedParentAppCategory.getCategoryName();
                                        selectedParentAppCategory = loadedParentAppCategory;
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

        selectedParentLabel = new Label("selectedParent", selectedParentMdl);
        selectedParentLabel.setOutputMarkupId(true);
        form.add(selectedParentLabel);

        parentTreeViewPanel.setModel(new Model<>());
        parentTreeViewPanel.setRequired(true);
        parentTreeViewPanel.setLabel(new ResourceModel("AppCategory.parent"));
        form.add(parentTreeViewPanel);

        List<IUploadFilter> imageFilters = new ArrayList<>();
        imageFilters.add(IUploadFilter.getImageUploadFilter());

        appCategoryIconMultiAjaxFileUploadPanel2 = new MultiAjaxFileUploadPanel2("iconFile", imageFilters, 1, false, getString("APPPackage.iconFile"), categoryIconSize);
        appCategoryIconMultiAjaxFileUploadPanel2.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        appCategoryIconMultiAjaxFileUploadPanel2.setLabel(new ResourceModel("APPPackage.iconFile"));
        appCategoryIconMultiAjaxFileUploadPanel2.setModel(new Model<>(appCategory.getIconFile()));
        form.add(appCategoryIconMultiAjaxFileUploadPanel2);

        tmpIconFile = new NonCachingImage("tmpIconFile", new DynamicImageResource() {
            @Override
            protected byte[] getImageData(Attributes attributes) {
                if (fileKey != null) {
                    return AppUtils.getImageAsBytes(FileServerService.FILE_DOWNLOAD_SERVER_PATH.replace("${key}", fileKey));
                } else {
                    return null;
                }
            }
        });

        tmpIconFile.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        tmpIconFile.setVisible(true);
        if (tmpIconFile == null ||appCategory==null|| appCategory.getId()==null) {
            tmpIconFile.setVisible(false);
        }

        form.add(tmpIconFile);

        form.add(new AbstractFormValidator() {
            @Override
            public FormComponent[] getDependentFormComponents() {
                return new FormComponent[0];
            }

            @Override
            public void validate(Form form) {
                AppCategory appCategoryToSave = (AppCategory) form.getModelObject();
                Collection<UploadedFileInfo> appCatIconFileList = (Collection<UploadedFileInfo>) appCategoryIconMultiAjaxFileUploadPanel2.getConvertedInput();
                Iterator<UploadedFileInfo> uploadedFileInfoIterator;
                if (appCatIconFileList != null && appCatIconFileList.size() > 0) {
                    uploadedFileInfoIterator = appCatIconFileList.iterator();
                } else {
                    uploadedFileInfoIterator = null;
                }
                Session session = HibernateUtil.getCurrentSession();
                try {
                    AppCategory rootCategory = AppCategoryService.Instance.loadRootAppCategory(session);
                    if(rootCategory==null){
                        error(parentTreeViewPanel, "AppCategory.root.doesNotFound");
                        return;
                    }
                    if (selectedParentAppCategory != null && selectedParentAppCategory.getId() != null) {
                        if (selectedParentAppCategory.getId().equals(appCategoryToSave.getId())) {
                            error(parentTreeViewPanel, "AppCategory.parent.same.appCategory");
                        } else if (!rootCategory.getId().equals(selectedParentAppCategory.getId())) {
                            AppCategory tmpSelectedParentAppCategory = (AppCategory) session.load(AppCategory.class, selectedParentAppCategory.getId());
                            AppCategory parentCategory = tmpSelectedParentAppCategory.getParent();
                            if (!rootCategory.getId().equals(parentCategory.getId())) {
                            error(parentTreeViewPanel, "AppCategory.invalid.parent");
                        }
                    }
                    } else {
                        if (parentNodeTitle!=null && !parentNodeTitle.trim().equals("")) {
                        error(parentTreeViewPanel, "AppCategory.parent.doesNotFound");
                    }
                    }

                    if (rootCategory.getId().equals(appCategoryId)) {
                        error(parentTreeViewPanel, "AppCategory.parent.of.root.category.notEditable");
                    }

                } catch (Exception e) {
                    error(parentTreeViewPanel, "error.generalErr");
                } finally {
                    session.close();
                }
            }
        });
        form.add(new AjaxFormButton("save", form, feedbackPanel) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {
                String validationString = "";
                Session session = HibernateUtil.getCurrentSession();

                try {

                    Collection<UploadedFileInfo> appCatIconFileList = (Collection<UploadedFileInfo>) appCategoryIconMultiAjaxFileUploadPanel2.getConvertedInput();
                    Iterator<UploadedFileInfo> uploadedFileInfoIterator;
                    if (appCatIconFileList != null && appCatIconFileList.size() > 0) {
                        uploadedFileInfoIterator = appCatIconFileList.iterator();
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
                        UploadedFileInfo appCatIconFile = uploadedFileInfoList.get(0);
                        String tempFileLocation = null;
                        tempFileLocation = FileServerService.Instance.copyFileFromServerToTemp(appCatIconFile.getFileId());
                        java.io.File file = new java.io.File(tempFileLocation);

                        BufferedImage bufferedImage = ImageIO.read(new java.io.File(file.getPath()));
                        int width = bufferedImage.getWidth();
                        int height = bufferedImage.getHeight();
                        int categoryIconMaxPixel = Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.APP_CATEGORY_ICON_MAX_PIXEL));
                        int categoryIconMinPixel = Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.APP_CATEGORY_ICON_MIN_PIXEL));
                        if(categoryIconMinPixel > width || categoryIconMaxPixel<width){
                            validationString += " - " +
                                    getString("error.appCategory.icon.width.pixel").replace("${min}", String.valueOf(categoryIconMinPixel))
                                                                                   .replace("${max}", String.valueOf(categoryIconMaxPixel)) + " <br/>";
                        }
                         if(categoryIconMinPixel > height || categoryIconMaxPixel<height){
                            validationString += " - " +
                                    getString("error.appCategory.icon.height.pixel").replace("${min}", String.valueOf(categoryIconMinPixel))
                                                                                   .replace("${max}", String.valueOf(categoryIconMaxPixel)) + " <br/>";
                        }

                        if (appCategoryIconMultiAjaxFileUploadPanel2.getSizeInBytes() > categoryIconSize *1000) {
                            validationString += " - " +
                                    getString("error.appCategory.icon.size").replace("${data}", String.valueOf(categoryIconSize)) + "  <br/>";
                        }
                    }else {
                        if(appCategory.getId()==null){
                            validationString += " - " +
                                    getString("Required").replace("${label}", getString("AppCategory.icon")) + "<br/>";
                        }else {
                            AppCategory loadedAppCategory = (AppCategory) session.load(AppCategory.class , appCategory.getId());
                            if (loadedAppCategory.getIconFile()==null) {
                                validationString += " - " +
                                        getString("Required").replace("${label}", getString("AppCategory.icon")) + "<br/>";
                            }
                        }
                    }

                    if (categoryNameTextField != null && categoryNameTextField.getValidatorString() != null && !categoryNameTextField.getValidatorString().isEmpty()) {
                        for (String validationStringInList : categoryNameTextField.getValidatorString()) {
                            validationString += " - " +
                                    validationStringInList + "<br/>";
                        }
                    }

                } catch (Exception e) {
                    validationString += " - " +
                            getString("error.generalErr") + "<br/>";
                }

                if (!validationString.isEmpty()) {
                    target.appendJavaScript("showMessage('" + validationString + "');");
                    return;
                }

                AppCategory appCategoryToSave = (AppCategory) form.getModelObject();
                List<AppCategory> allParentFromSelectedParent = new ArrayList<AppCategory>();
                //get selected parent and all it's parent till parent is null and set them to the allParentFromSelectedParent
                AppCategory loadedParent = (AppCategory) session.load(AppCategory.class, selectedParentAppCategory.getId()); // if I use selectedParentCategory I will receive laze Error
                allParentFromSelectedParent.add(loadedParent);
                AppCategory parentOfLoadedParent = (loadedParent.getParent() != null ? (AppCategory) session.load(AppCategory.class, loadedParent.getParent().getId()) : null);
                while (parentOfLoadedParent != null) {
                    AppCategory tmpParentAppCategory = (AppCategory) session.load(AppCategory.class, parentOfLoadedParent.getId());
                    allParentFromSelectedParent.add(tmpParentAppCategory);
                    parentOfLoadedParent = tmpParentAppCategory.getParent();
                }

                AppCategory tmpAppCategoryToSave = (appCategoryToSave.getId() != null ? (AppCategory) session.load(AppCategory.class, appCategoryToSave.getId()) : appCategoryToSave);
                try {
                    //if one child has been selected as parent
                    boolean hasChildAsParent = false;
                    AppCategory childAsParentCategory = null;
                    if (selectedParentAppCategory.getParent() != null && tmpAppCategoryToSave != null && allParentFromSelectedParent.contains(tmpAppCategoryToSave)) { //this means that selected parent is in the series of selected node Children
                        hasChildAsParent = true;
                        childAsParentCategory = (AppCategory) session.load(AppCategory.class, selectedParentAppCategory.getId());
                        childAsParentCategory.setParent((AppCategory) session.load(AppCategory.class, tmpAppCategoryToSave.getParent().getId()));
                    }
                    tmpAppCategoryToSave.setParent(selectedParentAppCategory);
                    tmpAppCategoryToSave.setEnabled(isEnabledCheckBox.getConvertedInput());
                    tmpAppCategoryToSave.setAssignable(isAssignableCheckBox.getConvertedInput());
                    tmpAppCategoryToSave.setCategoryName(appCategoryToSave.getCategoryName());


                    Collection<UploadedFileInfo> appCatIconFileList = (Collection<UploadedFileInfo>) appCategoryIconMultiAjaxFileUploadPanel2.getConvertedInput();
                    if (appCatIconFileList!=null) {
                        Iterator<UploadedFileInfo> uploadedFileInfoIterator = appCatIconFileList.iterator();
                        List<UploadedFileInfo> uploadedFileInfoList = new ArrayList();
                        if (uploadedFileInfoIterator != null) {
                            while (uploadedFileInfoIterator.hasNext()) {
                                UploadedFileInfo tmpUploadedFileInfo = uploadedFileInfoIterator.next();
                                uploadedFileInfoList.add(tmpUploadedFileInfo);
                            }
                        }

                        if (uploadedFileInfoList != null && uploadedFileInfoList.size() > 0) {
                            UploadedFileInfo appCatIconFile = uploadedFileInfoList.get(0);
                            File iconFile = new File();
                            iconFile.setStereoType(StereoType.THUMB_FILE);
                            iconFile.setFileName(AppUtils.dateTagFileName(appCatIconFile.getFileName()));
                            FileServerService.Instance.persistFileToServer(appCatIconFile.getFileId());
                            iconFile.setFilePath(appCatIconFile.getFileId());
                            tmpAppCategoryToSave.setIconFile(iconFile);
                            BaseEntityService.Instance.saveOrUpdate(iconFile, session);
                            fileKey = iconFile.getFilePath();
                        }
                    }

                    Transaction tx = null;
                    try {
                        tx = session.beginTransaction();
                        if (hasChildAsParent) {
                            BaseEntityService.Instance.saveOrUpdate(childAsParentCategory, session);
                        }
                        BaseEntityService.Instance.saveOrUpdate(tmpAppCategoryToSave, session);
                        tx.commit();
                        childFinished(target, new Model<>(tmpAppCategoryToSave), form.get("save"));

                    } catch (Exception ex) {
                        processException(target, ex);
                    }

                } finally {
                    if (session.isOpen()) {
                        session.close();
                    }
                }
            }
        });

        form.add(new AjaxLink("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                childFinished(target, null, form.get("cancel"));
            }
        });

        add(form);

    }
}
