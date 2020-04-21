package com.fanap.midhco.ui.pages.app;

import com.fanap.midhco.appstore.applicationUtils.AppUtils;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.service.BaseEntityService;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.app.AppPackageService;
import com.fanap.midhco.appstore.service.app.IAPPPackageService;
import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import com.fanap.midhco.appstore.service.myException.AppStoreRuntimeException;
import com.fanap.midhco.appstore.service.os.OSService;
import com.fanap.midhco.appstore.service.osType.IUploadFilter;
import com.fanap.midhco.appstore.service.osType.OSTypeService;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.fileGalleryPanel.FileGalleyPanel;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.component.multiAjaxFileUpload2.MultiAjaxFileUploadPanel2;
import com.fanap.midhco.ui.component.multiAjaxFileUpload2.UploadedFileInfo;
import com.fanap.midhco.ui.component.niceEditor.NiceEditor;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.script.ScriptException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Hamid on 7/6/2016.
 */
@Authorize(view = Access.APP_EDIT)
public class PackageEditForm extends BasePanel {
    BootStrapModal bootStrapModal = new BootStrapModal("modal");

    Form form;
    FileGalleyPanel packGalleyPanel;
    FileGalleyPanel iconGalleyPanel;
    FileGalleyPanel thumbGalleryPanel;
    AppPackage appPackage;
    NiceEditor noteTextArea;
    String changeLogString;
    AjaxEventBehavior textAreaGetBehaviour;
    IUploadFilter uploadFilter;

    List<FileGalleyPanel.ImageDescriptor> iconImageDescriptors;
    List<FileGalleyPanel.ImageDescriptor> thumbDescriptors;

    protected PackageEditForm(String id, AppPackage appPackage, App app) {
        super(id);

        add(bootStrapModal);

        this.appPackage = appPackage;

        form = new Form("form", new Model<>());
        form.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.setModel(new Model<>());

        TextField versionNameTextField = new TextField("versionName");
        versionNameTextField.setEnabled(false);
        versionNameTextField.isEnabled();
        versionNameTextField.setModel(new Model<>(appPackage.getVersionName()));
        form.add(versionNameTextField);

        TextField versionCodeTextField = new TextField("versionCode");
        versionCodeTextField.setEnabled(false);
        versionCodeTextField.isEnabled();
        versionCodeTextField.setModel(new Model<>(appPackage.getVersionName()));
        form.add(versionCodeTextField);

        noteTextArea = new NiceEditor("changeLogArea");
        noteTextArea.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        noteTextArea.setModel(new Model<>(appPackage.getChangeLog()));
        form.add(noteTextArea);
        String downloadURL = FileServerService.FILE_DOWNLOAD_SERVER_PATH;

        if (appPackage != null && appPackage.getPackFile() != null) {
            File packFile = appPackage.getPackFile();
            String packIconURL = packFile.getFileName();
            final List<FileGalleyPanel.ImageDescriptor> packImageDescriptors = new ArrayList<>();
            final FileGalleyPanel.ImageDescriptor packImageDescriptor = new FileGalleyPanel.ImageDescriptor();

            String downPackPath = downloadURL.replace("${key}", packFile.getFilePath());

            packImageDescriptor.setImageId(String.valueOf(packFile.getId()));
            packImageDescriptor.setImageSource(downPackPath);
            packImageDescriptor.setThumbImage(getFileTypeImageSrc(packIconURL));
            packImageDescriptor.setImageTitle(packFile.getFileName());
            packImageDescriptors.add(packImageDescriptor);

            uploadFilter = null;
            try {
                uploadFilter = OSTypeService.Instance.getOSTypeUploadFilter(app.getOsType());
            } catch (ScriptException e) {
                throw new AppStoreRuntimeException("Error loading upload Filter for OSTYpe: " + app.getOsType());
            }
            packGalleyPanel = new FileGalleyPanel("packFile",
                    Arrays.asList(FileGalleyPanel.ACTION_PANEL_VIEW, FileGalleyPanel.ACTION_PANEL_REPLACE), uploadFilter, 1) {
                @Override
                protected void onReplaceButtonClicked(AjaxRequestTarget target) {

                    MultiAjaxFileUploadPanel2 multiAjaxFileUploadPanel2 =
                            new MultiAjaxFileUploadPanel2(bootStrapModal.getContentId(), 1, true, "") {
                                @Override
                                protected void onUploadComplete(AjaxRequestTarget target, UploadedFileInfo uploadedFileInfo) {
                                    Session session = null;
                                    Transaction tx = null;
                                    try {
                                        OSType osType = app.getOsType();

                                        Set<UploadedFileInfo> retSet = (Set) this.getConvertedInput();
                                        UploadedFileInfo uploadedPackage = retSet.iterator().next();
                                        String fileKey = uploadedPackage.getFileId();

                                        IAPPPackageService appPackageService = AppPackageService.Instance.processPackageFile(fileKey, osType);

                                        String packageName = appPackageService.getPackage();
                                        if(packageName == null) {
                                            target.appendJavaScript("showMessage('" + getString("appPackage.replace.package.has.null.packageName") + "');");
                                            return;
                                        } else if(!packageName.equals(app.getPackageName())) {
                                            target.appendJavaScript("showMessage('" + getString("appPackage.replace.package.has.different.name") + "');");
                                            return;
                                        }

                                        String versionCode = appPackageService.getVersionCode();
                                        if(versionCode == null) {
                                            target.appendJavaScript("showMessage('" + getString("appPackage.replace.versionCode.is.null") + "');");
                                            return;
                                        } else if(!versionCode.equals(appPackage.getVersionCode())) {
                                            target.appendJavaScript("showMessage('" + getString("appPackage.replace.versionCode.is.different.from.original") + "');");
                                            return;
                                        }

                                        AppPackageService.Instance.validateNewAppPackage(app, appPackageService, true);

                                        session = HibernateUtil.getCurrentSession();
                                        OSService.OSCriteria osCriteria = new OSService.OSCriteria();
                                        List<OSType> osTypeList = new ArrayList<>();
                                        osTypeList.add(osType);
                                        osCriteria.setOsType(osTypeList);
                                        List<OS> osList = OSService.Instance.list(osCriteria, 0, -1, null, false, session);
                                        session.evict(osList);
                                        for (OS os : osList) {
                                            session.evict(os.getHandlerApps());
                                        }

                                        session.flush();
                                        tx = session.beginTransaction();

                                        FileServerService.Instance.persistFileToServer(uploadedPackage.getFileId());

                                        File newPackFile = new File();
                                        newPackFile.setFileName(uploadedPackage.getFileName());
                                        newPackFile.setFilePath(uploadedPackage.getFileId());
                                        newPackFile.setFileSize(uploadedPackage.getFileSize());
                                        newPackFile.setStereoType(StereoType.MAIN_APP_PACK_FILE);
                                        newPackFile.setCreatorUser(PrincipalUtil.getCurrentUser());
                                        newPackFile.setCreationDate(DateTime.now());
                                        session.save(newPackFile);

                                        appPackage.setPackFile(newPackFile);
                                        if(appPackageService.getPermissions() != null) {
                                            String newPermissionDetail = String.join(",", appPackageService.getPermissions());
                                            appPackage.setPermissionDetail(newPermissionDetail);
                                        }
                                        appPackage.setDeleted(false);
                                        AppPackageService.Instance.saveOrUpdate(appPackage, session);

                                        tx.commit();

                                        packImageDescriptors.clear();

                                        packImageDescriptor.setImageId(String.valueOf(newPackFile.getId()));
                                        String downPackPath = downloadURL.replace("${key}", newPackFile.getFilePath());
                                        packImageDescriptor.setImageSource(downPackPath);
                                        packImageDescriptor.setThumbImage(getFileTypeImageSrc(packIconURL));
                                        packImageDescriptor.setImageTitle(newPackFile.getFileName());
                                        packImageDescriptors.add(packImageDescriptor);

                                        target.appendJavaScript("showMessage('" + getString("appPackage.replace.successfully.completed") + "');");
                                        bootStrapModal.close(target);
                                    } catch (Exception ex) {
                                        if(tx != null)
                                            tx.rollback();

                                        if(ex instanceof AppPackageService.APPPackageException) {
                                            target.appendJavaScript("showMessage('" + ex.getMessage() + "');");
                                        } else
                                            processException(target, ex);
                                    } finally {
                                        if (session != null && session.isOpen()) {
//                                            session.close();
                                        }
                                    }
                                }
                            };
                    multiAjaxFileUploadPanel2.setUploadFilters(Arrays.asList(uploadFilter), target);
                    bootStrapModal.setContent(multiAjaxFileUploadPanel2);
                    bootStrapModal.show(target);
                }
            };
            packGalleyPanel.setReplaceConfirmationMessage("appPackage.replace.confirm");
            packGalleyPanel.setModel(new Model((Serializable) packImageDescriptors));
            form.add(packGalleyPanel);
        } else {
            form.add(new WebMarkupContainer("packFile"));
        }


        iconImageDescriptors = new ArrayList<>();
        FileGalleyPanel.ImageDescriptor iconImageDescriptor = new FileGalleyPanel.ImageDescriptor();
        if (appPackage != null && appPackage.getIconFile() != null) {
            File iconFile = appPackage.getIconFile();
            iconImageDescriptor.setImageId(String.valueOf(iconFile.getId()));
            iconImageDescriptor.setImageTitle(iconFile.getFileName());
            try {
                String iconThumbFileName = AppUtils.getImageThumbNail(iconFile.getFilePath(), iconFile.getFileName());
                iconImageDescriptor.setThumbImage("File:" + iconThumbFileName);
                iconImageDescriptor.setImageSource(downloadURL.replace("${key}", iconFile.getFilePath()));
            } catch (Exception ex) {
                logger.error("file download error with key : " + iconFile.getFilePath() + " file name : " + iconFile.getFileName(), ex);
            }
            iconImageDescriptors.add(iconImageDescriptor);
        } else {
            iconImageDescriptors = new ArrayList<>();
        }
        iconGalleyPanel = new FileGalleyPanel("iconFile",
                Arrays.asList(FileGalleyPanel.ACTION_PANEL_DELETE, FileGalleyPanel.ACTION_PANEL_VIEW,
                        FileGalleyPanel.ACTION_PANEL_ADD),
                IUploadFilter.getImageUploadFilter(), 1);
        iconGalleyPanel.setModel(new Model((Serializable) new ArrayList<>(iconImageDescriptors)));
        iconGalleyPanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(iconGalleyPanel);

        List<File> thumbFiles = appPackage.getThumbImages();
        if (appPackage != null && appPackage.getThumbImages() != null && !appPackage.getThumbImages().isEmpty()) {
            Stream<FileGalleyPanel.ImageDescriptor> thumbGalleryVOStream =
                    thumbFiles.stream().map(thumbFile -> {

                        FileGalleyPanel.ImageDescriptor thumbImageDescriptor = new FileGalleyPanel.ImageDescriptor();
                        thumbImageDescriptor.setImageId(String.valueOf(thumbFile.getId()));
                        try {
                            String thumbFileName = AppUtils.getImageThumbNail(thumbFile.getFilePath(), thumbFile.getFileName());
                            thumbImageDescriptor.setThumbImage("File:" + thumbFileName);
                            thumbImageDescriptor.setImageTitle(thumbFile.getFileName());
                            thumbImageDescriptor.setImageSource(downloadURL.replace("${key}", thumbFile.getFilePath()));
                        } catch (Exception ex) {
                            logger.error("file download error with key : " + thumbFile.getFilePath() + " file name : " + thumbFile.getFileName(), ex);
                        }
                        return thumbImageDescriptor;
                    });

            thumbDescriptors =
                    thumbGalleryVOStream.collect(Collectors.<FileGalleyPanel.ImageDescriptor>toList());
        } else {
            thumbDescriptors = new ArrayList<>();
        }
        thumbGalleryPanel = new FileGalleyPanel("thumbImages",
                Arrays.asList(FileGalleyPanel.ACTION_PANEL_DELETE, FileGalleyPanel.ACTION_PANEL_VIEW
                        , FileGalleyPanel.ACTION_PANEL_ADD),
                IUploadFilter.getImageUploadFilter(), null);
        thumbGalleryPanel.setModel(new Model((Serializable) new ArrayList<>(thumbDescriptors)));
        thumbGalleryPanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(thumbGalleryPanel);


        AjaxFormButton ajaxFormButton = new AjaxFormButton("save", getParentForm()) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("onclick", "var txt = $('#" + noteTextArea.getMarkupId() + "').find('textarea').text();" +
                        "var res = encodeURIComponent(txt);" +
                        "Wicket.Ajax.get({u:'" +
                        textAreaGetBehaviour.getCallbackUrl().toString() +
                        "&changeLog=' + res"
                        + "});");
            }


            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {
                Session session = HibernateUtil.getCurrentSession();
                Transaction tx = null;
                List<String> filesToDeleteKey = new ArrayList<>();
                Map<StereoType, List<FileGalleyPanel.ImageDescriptor>> imageDescriptorMap = getInputs();
                List<FileGalleyPanel.ImageDescriptor> iconImageDescriptors =
                        imageDescriptorMap.get(StereoType.ICON_FILE);
                List<FileGalleyPanel.ImageDescriptor> thumbImageDescriptors =
                        imageDescriptorMap.get(StereoType.THUMB_FILE);

                StringBuffer validationString = new StringBuffer();

//                if (iconImageDescriptors==null ||iconImageDescriptors.isEmpty() ) {
//                    validationString.append("<br>").append("-").append(AppStorePropertyReader.getString("Required").replace("${label}", getString("APPPackage.iconFile")));
//                }
//                if (thumbImageDescriptors==null ||thumbImageDescriptors.isEmpty()) {
//                    validationString.append("<br>").append("-").append(AppStorePropertyReader.getString("Required").replace("${label}", getString("APPPackage.thumbImages")));
//                }
                if(!validationString.toString().trim().equals("")){
                    target.appendJavaScript("showMessage('"+validationString.toString()+"');");
                    return;
                }
                try {
                    OSService.OSCriteria osCriteria = new OSService.OSCriteria();
                    App relatedApp = appPackage.getRelatedApp();
                    List<OS> osList = null;

                    if (relatedApp != null) {
                        List<OSType> osTypeList = new ArrayList<>();
                        osTypeList.add(relatedApp.getOsType());
                        osCriteria.setOsType(osTypeList);

                        osList = OSService.Instance.list(osCriteria, 0, -1, null, false, session);
                    }else {
                        osList = OSService.Instance.listAll(session);
                    }
                    session.evict(osList);
                    for (OS os : osList) {
                        session.evict(os.getHandlerApps());
                    }
                    session.flush();
                    tx = session.beginTransaction();
//                    String appSaveLocation =
//                            ConfigUtil.getProperty(ConfigUtil.APP_APPSTORE_PACKAGE_FILES_SAVE_LOCATION) +
//                                    app.getOsType().getName() + "/" + app.getAppPackageName() + "/" +
//                                    appPackage.getVersionCode() + "/";



                    if (iconImageDescriptors != null && !iconImageDescriptors.isEmpty()) {

                        if (((appPackage.getIconFile() != null &&
                                !appPackage.getIconFile().getId().toString().equals(iconImageDescriptors.get(0).getImageId())))
                                || appPackage.getIconFile() == null) {
                            File iconFile = appPackage.getIconFile();

                            FileGalleyPanel.ImageDescriptor iconImageDescriptor = iconImageDescriptors.get(0);
                            if (iconFile != null)
                                filesToDeleteKey.add(iconFile.getFilePath());

                            if (iconFile == null)
                                iconFile = new File();
                            iconFile.setStereoType(StereoType.ICON_FILE);
                            FileServerService.Instance.persistFileToServer(iconImageDescriptor.getImageId());
                            iconFile.setFilePath(iconImageDescriptor.getImageId());
                            iconFile.setFileName(iconImageDescriptor.getImageTitle());
                            appPackage.setIconFile(iconFile);
                            BaseEntityService.Instance.saveOrUpdate(iconFile, session);

                            iconImageDescriptor.setImageId(iconFile.getId().toString());

                            PackageEditForm.this.iconImageDescriptors = iconImageDescriptors;
                        }
                    }



                    List<File> thumbFiles = appPackage.getThumbImages();
                    Map<String, File> mp = new HashMap<String, File>();
                    for (File file : thumbFiles) {
                        mp.put(file.getId().toString(), file);
                    }
                    /////////////////////////////
                    if (thumbImageDescriptors != null && !thumbImageDescriptors.isEmpty()) {

                        for (FileGalleyPanel.ImageDescriptor thumbDescriptor : thumbImageDescriptors) {
                            if (mp.get(thumbDescriptor.getImageId()) == null) {

                                File thumbFile = new File();
                                thumbFile.setStereoType(StereoType.THUMB_FILE);
                                thumbFile.setFileName(thumbDescriptor.getImageTitle());
                                thumbFile.setFilePath(thumbDescriptor.imageSource);
                                appPackage.getThumbImages().add(thumbFile);
                                BaseEntityService.Instance.saveOrUpdate(thumbFile, session);
                                FileServerService.Instance.persistFileToServer(thumbDescriptor.getImageId());
                                thumbDescriptor.setImageId(thumbFile.getId().toString());

                            } else {
                                mp.remove(thumbDescriptor.getImageId());
                            }
                        }

//                        AppPackageService.Instance.saveOrUpdate(appPackage, session);
                        PackageEditForm.this.thumbDescriptors = thumbImageDescriptors;
                    } else {
                        appPackage.getThumbImages().clear();
                        PackageEditForm.this.thumbDescriptors.clear();
                    }

                    for (File unusedThumbFile : mp.values()) {
                        appPackage.getThumbImages().remove(unusedThumbFile);
                        session.delete(unusedThumbFile);

                        filesToDeleteKey.add(unusedThumbFile.getFilePath());
                    }
                    appPackage.setChangeLog(changeLogString);

//                    AppPackageHistory appPackageHistory = AppPackageHistoryService.Instance.setAppPackageHistoryByAppPackage(appPackage);
//
//                    AppPackageHistoryService.Instance.saveOrUpdate(appPackageHistory, session);


//                    appPackageHistories.add(appPackageHistory);

//                    appPackage.setHistories(appPackageHistories);

                    session.evict(appPackage);
                    appPackage.getHistories();
                    AppPackageService.Instance.saveOrUpdate(appPackage, session);

                    if (!filesToDeleteKey.isEmpty())
                        for (String fileToDeleteKey : filesToDeleteKey) {
                            FileServerService.Instance.deleteFileFromServer(fileToDeleteKey);
                        }

                    tx.commit();
                    childFinished(target, new Model<>(), this);
                } catch (Exception ex) {
                    if (tx != null)
                        tx.rollback();
                    processException(target, ex);
                } finally {
                    if (session!=null && session.isOpen()) {
                        session.close();
                    }
                }
            }
        };
        add(ajaxFormButton);

        textAreaGetBehaviour = new AjaxEventBehavior("dasd") {
            @Override
            protected void onEvent(AjaxRequestTarget target) {
                String changeLog = getRequest().getRequestParameters().getParameterValue("changeLog").toString();
                String convertedChangeLog = null;
                try {
                    convertedChangeLog = URLDecoder.decode(changeLog, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    processException(target, e);
                }

                noteTextArea.setModelObject(convertedChangeLog);
                changeLogString = convertedChangeLog;
            }
        };
        add(textAreaGetBehaviour);


        add(new AjaxLink("reset") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                iconGalleyPanel.setModel(new Model<>(new ArrayList<>(iconImageDescriptors)));
                ajaxRequestTarget.add(iconGalleyPanel);

                thumbGalleryPanel.setModel(new Model<>(new ArrayList<>(thumbDescriptors)));
                ajaxRequestTarget.add(thumbGalleryPanel);
            }
        });

        add(form);
    }

    public Map<StereoType, List<FileGalleyPanel.ImageDescriptor>> getInputs() {
        Map<StereoType, List<FileGalleyPanel.ImageDescriptor>> retMap = new HashMap<>();
        if (iconGalleyPanel != null) {
            File iconFile = appPackage.getIconFile();

            List<FileGalleyPanel.ImageDescriptor> iconDescriptors = (List<FileGalleyPanel.ImageDescriptor>) iconGalleyPanel.getConvertedInput();
            retMap.put(StereoType.ICON_FILE, iconDescriptors);
        }

        if (thumbGalleryPanel != null) {
            List<FileGalleyPanel.ImageDescriptor> thumbDescriptors =
                    (List<FileGalleyPanel.ImageDescriptor>) thumbGalleryPanel.getConvertedInput();
            retMap.put(StereoType.THUMB_FILE, thumbDescriptors);
        }

        return retMap;
    }

    @Override
    public void onAfterRender() {
        super.onAfterRender();
        form.add(new AttributeAppender("style", "width:1000px", ";"));
    }
}
