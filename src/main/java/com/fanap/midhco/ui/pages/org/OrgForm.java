package com.fanap.midhco.ui.pages.org;

import com.fanap.midhco.appstore.applicationUtils.AppUtils;
import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.File;
import com.fanap.midhco.appstore.entities.Organization;
import com.fanap.midhco.appstore.entities.StereoType;
import com.fanap.midhco.appstore.service.BaseEntityService;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import com.fanap.midhco.appstore.service.org.OrgService;
import com.fanap.midhco.appstore.service.osType.IUploadFilter;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.limitedTextField.LimitedTextField;
import com.fanap.midhco.ui.component.multiAjaxFileUpload2.MultiAjaxFileUploadPanel2;
import com.fanap.midhco.ui.component.multiAjaxFileUpload2.UploadedFileInfo;
import com.fanap.midhco.ui.component.switchbox.SwitchBox;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.Form;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by A.Moshiri on 2/26/2018.
 */
@Authorize(views = {Access.ORGANIZATION_ADD, Access.ORGANIZATION_EDIT})
public class OrgForm extends BasePanel {
    Form form;
    FeedbackPanel feedbackPanel;
    MultiAjaxFileUploadPanel2 orgLogo;
    NonCachingImage tmpIconFile;
    String fileKey;
    LimitedTextField nickName;
    LimitedTextField fullNameTextField;
    LimitedTextField englishFullNameTextField;
    SwitchBox isDefaultSwitchBox;

    protected OrgForm(String id, Organization organization) {
        super(id);


        if (organization != null) {
            fileKey = organization.getIconFile() != null ? organization.getIconFile().getFilePath() : null;
        } else {
            fileKey = null;
        }

        feedbackPanel = new FeedbackPanel("feedbackPanel");
        feedbackPanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(feedbackPanel);

        form = new Form("form", new CompoundPropertyModel(organization));
        form.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        nickName = new LimitedTextField("nickName",null , null ,false,true,false,40,getString("organization.nickName"));
        nickName.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        nickName.setRequired(true);
        nickName.setLabel(new ResourceModel("organization.nickName"));
        form.add(nickName);

        fullNameTextField = new LimitedTextField("fullName",false , null ,false,true,false,40,getString("organization.fullName"));
        fullNameTextField.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        fullNameTextField.setRequired(true);
        fullNameTextField.setLabel(new ResourceModel("organization.fullName"));
        form.add(fullNameTextField);

        englishFullNameTextField = new LimitedTextField("englishFullName",true , null ,false,true,false,40,getString("organization.englishFullName"));
        englishFullNameTextField.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        englishFullNameTextField.setRequired(true);
        englishFullNameTextField.setLabel(new ResourceModel("organization.englishFullName"));
        form.add(englishFullNameTextField);

        isDefaultSwitchBox = new SwitchBox(false, "isDefault", getString("label.yes"), getString("label.no"));
        isDefaultSwitchBox.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        isDefaultSwitchBox.setLabel(new ResourceModel("organization.default"));
        form.add(isDefaultSwitchBox);

        List<IUploadFilter> imageFilters = new ArrayList<>();
        imageFilters.add(IUploadFilter.getImageUploadFilter());

        orgLogo = new MultiAjaxFileUploadPanel2("logoFile", imageFilters, 1, false, getString("organization.logo"));
        orgLogo.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        orgLogo.setLabel(new ResourceModel("organization.logo"));
        orgLogo.setModel(new Model<>(organization.getIconFile()));
        form.add(orgLogo);

        tmpIconFile = new NonCachingImage("tmpLogoFile", new DynamicImageResource() {
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
        if (tmpIconFile == null || organization == null || organization.getId() == null) {
            tmpIconFile.setVisible(false);
        }

        form.add(tmpIconFile);


        form.add(new AjaxFormButton("save", form, feedbackPanel) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {
                String validationString = "";
                Session session = HibernateUtil.getCurrentSession();

                try {

                    Collection<UploadedFileInfo> appCatIconFileList = (Collection<UploadedFileInfo>) orgLogo.getConvertedInput();
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
                        int orgIconMaxPixel = Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.ORG_ICON_MAX_PIXEL));
                        int orgIconMinPixel = Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.ORG_ICON_MIN_PIXEL));
                        if(orgIconMinPixel > width || orgIconMaxPixel<width){
                            validationString += " - " +
                                    getString("error.appCategory.icon.width.pixel").replace("${min}", String.valueOf(orgIconMinPixel))
                                            .replace("${max}", String.valueOf(orgIconMaxPixel)) + " <br/>";
                        }
                        if(orgIconMinPixel > height || orgIconMaxPixel<height){
                            validationString += " - " +
                                    getString("error.appCategory.icon.height.pixel").replace("${min}", String.valueOf(orgIconMinPixel))
                                            .replace("${max}", String.valueOf(orgIconMaxPixel)) + " <br/>";
                        }

                        File iconFile = new File();
                        iconFile.setFileName(appCatIconFile.getFileName());
                        iconFile.setFilePath(appCatIconFile.getFileId());

                        iconFile.setStereoType(StereoType.THUMB_FILE);
                        iconFile.setFileName(AppUtils.dateTagFileName(appCatIconFile.getFileName()));
                        FileServerService.Instance.persistFileToServer(appCatIconFile.getFileId());
                        iconFile.setFilePath(appCatIconFile.getFileId());
                        organization.setIconFile(iconFile);
                        BaseEntityService.Instance.saveOrUpdate(iconFile, session);
                        fileKey = iconFile.getFilePath();
                    } else {
                        if (organization.getId() == null) {
                            validationString += " - " +
                                    getString("Required").replace("${label}", getString("organization.logo")) + "<br/>";
                        } else {
                            Organization loadedOrganization = (Organization) session.load(Organization.class, organization.getId());
                            if (loadedOrganization.getIconFile() == null) {
                                validationString += " - " +
                                        getString("Required").replace("${label}", getString("organization.logo")) + "<br/>";
                            }
                        }
                    }

                    if (organization.getNickName() == null || organization.getNickName().trim().equals("")) {
                        validationString += " - " +
                                getString("Required").replace("${label}", getString("organization.nickName")) + "<br/>";
                    }

                    if (organization.getFullName() == null || organization.getFullName().trim().equals("")) {
                        validationString += " - " +
                                getString("Required").replace("${label}", getString("organization.fullName")) + "<br/>";
                    }

                    if (organization.getEnglishFullName() == null || organization.getEnglishFullName().trim().equals("")) {
                        validationString += " - " +
                                getString("Required").replace("${label}", getString("organization.englishFullName")) + "<br/>";
                    }

                    if (nickName != null && nickName.getValidatorString() != null && !nickName.getValidatorString().isEmpty()) {
                        for (String validationStringInList : nickName.getValidatorString()) {
                            validationString += " - " +
                                    validationStringInList + "<br/>";
                        }
                    }

                    if (fullNameTextField != null && fullNameTextField.getValidatorString() != null && !fullNameTextField.getValidatorString().isEmpty()) {
                        for (String validationStringInList : fullNameTextField.getValidatorString()) {
                            validationString += " - " +
                                    validationStringInList + "<br/>";
                        }
                    }

                    if (englishFullNameTextField != null && englishFullNameTextField.getValidatorString() != null && !englishFullNameTextField.getValidatorString().isEmpty()) {
                        for (String validationStringInList : englishFullNameTextField.getValidatorString()) {
                            validationString += " - " +
                                    validationStringInList + "<br/>";
                        }
                    }

                    OrgService.OrgCriteria orgCriteria = new OrgService.OrgCriteria();
                    orgCriteria.setEnglishFullName((String) englishFullNameTextField.getConvertedInput());
                    List<Organization> organizationList = OrgService.Instance.list(orgCriteria, 0 , -1 , null , false, session);
                    if(organizationList!=null && !organizationList.isEmpty()){
                        if(organization==null || organization.getId()==null){
                            validationString += " - " +
                                    ResultStatus.ORGANIZATION_EXIST.toString() + "<br/>";
                        }else {
                            for(Organization orgInLis : organizationList){
                                if(!organization.getId().equals(orgInLis.getId())){
                                    validationString += " - " +
                                            ResultStatus.ORGANIZATION_EXIST.toString() + "<br/>";
                                    target.appendJavaScript("showMessage('" + validationString + "');");
                                    return;
                                }
                            }
                        }
                    }



                } catch (Exception e) {
                    logger.error("Error in validation in ORGFORM ", e);
                    validationString += " - " +
                            getString("error.generalErr") + "<br/>";
                }

                if (!validationString.isEmpty()) {
                    target.appendJavaScript("showMessage('" + validationString + "');");
                    return;
                }


                try {
                    Transaction tx = null;
                    try {
                        tx = session.beginTransaction();
                        boolean isDefault = isDefaultSwitchBox == null ? false : (boolean) isDefaultSwitchBox.getConvertedInput();
                        Organization orgToSave = organization.getId() != null ? (Organization) session.load(Organization.class, organization.getId()) : new Organization();

                        if (isDefault) {
                            OrgService.OrgCriteria orgCriteria = new OrgService.OrgCriteria();
                            orgCriteria.setIsDefault(isDefault);
                            List<Organization> organizationList = OrgService.Instance.list(orgCriteria, 0, -1, null, false, session);
                            for (Organization orgInList : organizationList) {
                                orgInList.setDefault(false);
                                BaseEntityService.Instance.saveOrUpdate(orgInList, session);
                            }
                        }
                        orgToSave.setDefault(isDefault);

                        session.evict(organization);
                        //todo comit on database without use orgToSave;
                        orgToSave.setFullName(organization.getFullName());
                        orgToSave.setIconFile(organization.getIconFile());
                        orgToSave.setEnglishFullName(organization.getEnglishFullName());
                        orgToSave.setNickName(organization.getNickName());
                        BaseEntityService.Instance.saveOrUpdate(orgToSave, session);
                        tx.commit();

                        childFinished(target, new Model<>(orgToSave), form.get("save"));

                    } catch (Exception ex) {
                        processException(target, ex);
                    }

                } catch (Exception ex) {
                    logger.error("Error occured saving orgForm ", ex);
                    processException(target, ex);
                }   finally {
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
