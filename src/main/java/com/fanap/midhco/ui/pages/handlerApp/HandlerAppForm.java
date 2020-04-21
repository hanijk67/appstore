package com.fanap.midhco.ui.pages.handlerApp;

import com.fanap.midhco.appstore.applicationUtils.AppUtils;
import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.service.BaseEntityService;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.environment.EnvironmentService;
import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import com.fanap.midhco.appstore.service.handlerApp.HandlerAppService;
import com.fanap.midhco.appstore.service.org.OrgService;
import com.fanap.midhco.appstore.service.os.OSService;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.limitedTextField.LimitedTextField;
import com.fanap.midhco.ui.component.multiAjaxFileUpload2.MultiAjaxFileUploadPanel2;
import com.fanap.midhco.ui.component.multiAjaxFileUpload2.UploadedFileInfo;
import com.fanap.midhco.ui.component.mydropdown.MyDropDownChoicePanel;
import com.fanap.midhco.ui.component.switchbox.SwitchBox;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
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
import org.hibernate.exception.ConstraintViolationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by A.Moshiri on 8/14/2017.
 */
@Authorize(views = {Access.HANDLERAPP_ADD, Access.HANDLERAPP_EDIT})
public class HandlerAppForm extends BasePanel {
    Form form;
    LimitedTextField versionCodeTextField;
    FeedbackPanel feedbackPanel;
    WebMarkupContainer selectedFileHandlerAppPathContainer;
    WebMarkupContainer selectedFileHandlerAppPathContainer32Bit;
    Label selectedFileHandlerAppPathTitle;
    Label selectedFileHandlerAppPathTitle32Bit;
    Label testFileHandlerApp32BitLbl;
    Label fileHandlerApp32BitLbl;

    WebMarkupContainer selectedTestFileHandlerAppPathContainer;
    WebMarkupContainer selectedTestFileHandlerAppPathContainer32Bit;
    Label selectedTestFileHandlerAppPathTitle;
    Label selectedTestFileHandlerAppPathTitle32Bit;
    Long osId;
    boolean hasError = false;
    MyDropDownChoicePanel organizationDropDown;
    MyDropDownChoicePanel osEnvironmentDropDown;

    MultiAjaxFileUploadPanel2 handlerFileMultiAjaxFileUploadPanel2;
    MultiAjaxFileUploadPanel2 testHandlerFileMultiAjaxFileUploadPanel2;
    MultiAjaxFileUploadPanel2 handlerFileMultiAjaxFileUploadPanel32Bit;
    MultiAjaxFileUploadPanel2 testHandlerFileMultiAjaxFileUploadPanel32Bit;
    SwitchBox isActiveSwitchBox;
    SwitchBox isDefaultSwitchBox;
    Boolean hasId;
    OSEnvironment windowsEnv ;


    public HandlerAppForm(String id, HandlerApp handlerApp, Long inputOsId) {

        super(id);
        if (handlerApp != null && handlerApp.getId() != null) {
            hasId = true;
        } else {
            hasId = false;
        }
        windowsEnv = EnvironmentService.Instance.getDefaultEnvironment();
        Model<String> selectedFileHandlerAppPathMdl = new Model<>(null);
        Model<String> selectedTestFileHandlerAppPathMdl = new Model<>(null);
        Model<String> selectedFileHandlerAppPathMdl32Bit = new Model<>(null);
        Model<String> selectedTestFileHandlerAppPathMdl32Bit = new Model<>(null);
        Model<String> testFileHandlerAppLblMdl32Bit = new Model<>(null);

        Model<String> fileHandlerAppLblMdl32Bit = new Model<>(null);
        fileHandlerAppLblMdl32Bit.setObject(AppStorePropertyReader.getString("HandlerApp.handlerAppDownloadPath32Bit"));
        testFileHandlerAppLblMdl32Bit.setObject(AppStorePropertyReader.getString("HandlerApp.testHandlerAppDownloadPath32Bit"));

        feedbackPanel = new FeedbackPanel("feedbackPanel");
        add(feedbackPanel);
        osId = inputOsId;

        form = new Form("form", new CompoundPropertyModel(handlerApp));

        versionCodeTextField = new LimitedTextField("versionCode", true, true, false, false, false, 12, getString("HandlerApp.versionCode"));
        versionCodeTextField.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        versionCodeTextField.setRequired(true);
        versionCodeTextField.setLabel(new ResourceModel("HandlerApp.versionCode"));

        if (handlerApp != null && handlerApp.getId() != null) {
            versionCodeTextField.setEnabled(false);
        }
        form.add(versionCodeTextField);

        isActiveSwitchBox = new SwitchBox("isActive",
                new ResourceModel("label.yes").getObject(),
                new ResourceModel("label.no").getObject(),
                new Model<>(handlerApp.getActive()));
        isActiveSwitchBox.setLabel(new ResourceModel("label.active"));
        form.add(isActiveSwitchBox);

        isDefaultSwitchBox = new SwitchBox("isDefault",
                new ResourceModel("label.yes").getObject(),
                new ResourceModel("label.no").getObject(),
                new Model<>(handlerApp.getDefaultForOrganization()));
        isActiveSwitchBox.setLabel(new ResourceModel("handlerApp.isDefault"));
        form.add(isDefaultSwitchBox);


        List<Organization> organizationList = OrgService.Instance.listAll();
        organizationDropDown =
                new MyDropDownChoicePanel("organization", organizationList, false, true, getString("HandlerApp.organization"), 1);
        organizationDropDown.setLabel(new ResourceModel("HandlerApp.organization"));
        organizationDropDown.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(organizationDropDown);

        List<OSEnvironment> osEnvironmentList = new ArrayList<>();
        Session session = HibernateUtil.getCurrentSession();
        osEnvironmentList = EnvironmentService.Instance.listAll(session);
        session.close();
        osEnvironmentDropDown =
                new MyDropDownChoicePanel("osEnvironment", osEnvironmentList, false, true, getString("HandlerApp.os.osEnvironment"), 1,true , new ChoiceRenderer()){
                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        OSEnvironment osEnvironment = (OSEnvironment) getSelectedItem();
                        String nullString = "";
                        selectedFileHandlerAppPathMdl.setObject(nullString);
                        selectedTestFileHandlerAppPathMdl.setObject(nullString);
                        selectedFileHandlerAppPathMdl32Bit.setObject(nullString);
                        selectedTestFileHandlerAppPathMdl32Bit.setObject(nullString);
                        selectedFileHandlerAppPathTitle.setDefaultModelObject(selectedFileHandlerAppPathMdl.getObject());
                        selectedTestFileHandlerAppPathTitle.setDefaultModelObject(selectedTestFileHandlerAppPathMdl.getObject());
                        selectedFileHandlerAppPathTitle32Bit.setDefaultModelObject(selectedFileHandlerAppPathMdl32Bit.getObject());
                        selectedTestFileHandlerAppPathTitle32Bit.setDefaultModelObject(selectedTestFileHandlerAppPathMdl32Bit.getObject());

                        if(osEnvironment!=null && !osEnvironment.equals(windowsEnv)){
                            target.appendJavaScript(
                                   "var fileHandler = $('#" +handlerFileMultiAjaxFileUploadPanel2.getMarkupId()+ "');"+
                                            "fileHandler.removeClass('col-md-6');" +
                                            "fileHandler.addClass('col-md-12');"+
                                            "var testFileHandler = $('#" +testHandlerFileMultiAjaxFileUploadPanel2.getMarkupId()+ "');"+
                                            "testFileHandler.removeClass('col-md-6');" +
                                            "testFileHandler.addClass('col-md-12');"
                            );
                            selectedFileHandlerAppPathContainer32Bit.setVisible(false);
                            selectedTestFileHandlerAppPathContainer32Bit.setVisible(false);
                            handlerFileMultiAjaxFileUploadPanel32Bit.setVisible(false);
                            testHandlerFileMultiAjaxFileUploadPanel32Bit.setVisible(false);
                            selectedFileHandlerAppPathTitle32Bit.setVisible(false);
                            selectedTestFileHandlerAppPathTitle32Bit.setVisible(false);
                            fileHandlerApp32BitLbl.setVisible(false);
                            testFileHandlerApp32BitLbl.setVisible(false);
                        }else {
                            target.appendJavaScript(
                                    "var fileHandler = $('#" + handlerFileMultiAjaxFileUploadPanel2.getMarkupId()+ "');"+
                                            "fileHandler.removeClass('col-md-12');" +
                                            "fileHandler.addClass('col-md-6');"+
                                            "var testFileHandler = $('#" + testHandlerFileMultiAjaxFileUploadPanel2.getMarkupId()+ "');"+
                                            "testFileHandler.removeClass('col-md-12');" +
                                            "testFileHandler.addClass('col-md-6');"
                            );
                            selectedFileHandlerAppPathContainer32Bit.setVisible(true);
                            selectedTestFileHandlerAppPathContainer32Bit.setVisible(true);
                            handlerFileMultiAjaxFileUploadPanel32Bit.setVisible(true);
                            testHandlerFileMultiAjaxFileUploadPanel32Bit.setVisible(true);
                            selectedFileHandlerAppPathTitle32Bit.setVisible(true);
                            selectedTestFileHandlerAppPathTitle32Bit.setVisible(true);
                            fileHandlerApp32BitLbl.setVisible(true);
                            testFileHandlerApp32BitLbl.setVisible(true);
                        }
                        target.add(selectedFileHandlerAppPathContainer32Bit);
                        target.add(selectedTestFileHandlerAppPathContainer32Bit);
                        target.add(handlerFileMultiAjaxFileUploadPanel32Bit);
                        target.add(testHandlerFileMultiAjaxFileUploadPanel32Bit);
                        target.add(handlerFileMultiAjaxFileUploadPanel2);
                        target.add(testHandlerFileMultiAjaxFileUploadPanel2);
                        target.add(testFileHandlerApp32BitLbl);
                        target.add(fileHandlerApp32BitLbl);

                        target.add(selectedFileHandlerAppPathTitle);
                        target.add(selectedTestFileHandlerAppPathTitle);
                        target.add(selectedFileHandlerAppPathTitle32Bit);
                        target.add(selectedTestFileHandlerAppPathTitle32Bit);
                    }
                };
        osEnvironmentDropDown.setLabel(new ResourceModel("HandlerApp.os.osEnvironment"));
        osEnvironmentDropDown.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(osEnvironmentDropDown);

//main Files


        if (handlerApp.getHandlerFile() != null && handlerApp.getHandlerFile().getFilePath() != null) {
            selectedFileHandlerAppPathMdl.setObject(ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH).replace("${key}", handlerApp.getHandlerFile().getFilePath()));
        }
        selectedFileHandlerAppPathContainer = new WebMarkupContainer("handlerAppDownloadFilePath");

        handlerFileMultiAjaxFileUploadPanel2 = new MultiAjaxFileUploadPanel2("handlerAppFile", 1, true, getString("HandlerApp.handlerAppDownloadFile")) {
            @Override
            protected void onUploadComplete(AjaxRequestTarget ajaxRequestTarget, UploadedFileInfo uploadedFileInfo) {
                super.onUploadComplete(ajaxRequestTarget, uploadedFileInfo);
                Collection<UploadedFileInfo> appHandlerFileList = (Collection<UploadedFileInfo>) handlerFileMultiAjaxFileUploadPanel2.getConvertedInput();
                Iterator<UploadedFileInfo> uploadedFileInfoIterator;
                if (appHandlerFileList != null && appHandlerFileList.size() > 0) {
                    uploadedFileInfoIterator = appHandlerFileList.iterator();
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
                    UploadedFileInfo handlerAppFile = uploadedFileInfoList.get(0);
//                    String iconDownloadPath =  FileServerService.FILE_DOWNLOAD_SERVER_PATH.replace("${key}", handlerAppFile.getFileId());
                    String iconDownloadPath = ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH).replace("${key}", handlerAppFile.getFileId());
                    selectedFileHandlerAppPathMdl.setObject(iconDownloadPath);
                }
                selectedFileHandlerAppPathTitle.setDefaultModelObject(selectedFileHandlerAppPathMdl.getObject());

                if (form.get("link") != null) {
                    ajaxRequestTarget.add(form.get("link"));
                } else {
                    ajaxRequestTarget.add(selectedFileHandlerAppPathTitle);
                }
                selectedFileHandlerAppPathContainer.add(new AttributeModifier("href", selectedFileHandlerAppPathMdl));
                ajaxRequestTarget.add(selectedFileHandlerAppPathContainer);
            }
        };
        handlerFileMultiAjaxFileUploadPanel2.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        handlerFileMultiAjaxFileUploadPanel2.setLabel(new ResourceModel("HandlerApp.handlerAppDownloadFile"));
        handlerFileMultiAjaxFileUploadPanel2.setModel(new Model<>(handlerApp.getHandlerFile()));
        form.add(handlerFileMultiAjaxFileUploadPanel2);

        selectedFileHandlerAppPathTitle = new Label("link", selectedFileHandlerAppPathMdl);
        selectedFileHandlerAppPathTitle.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        selectedFileHandlerAppPathContainer.add(selectedFileHandlerAppPathTitle);

        selectedFileHandlerAppPathContainer.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        selectedFileHandlerAppPathContainer.add(new AttributeModifier("href", selectedFileHandlerAppPathMdl));
        form.add(selectedFileHandlerAppPathContainer);


        if (handlerApp.getHandlerFile32bit() != null && handlerApp.getHandlerFile32bit().getFilePath() != null) {
            selectedFileHandlerAppPathMdl32Bit.setObject(ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH).replace("${key}", handlerApp.getHandlerFile32bit().getFilePath()));
        }
        selectedFileHandlerAppPathContainer32Bit = new WebMarkupContainer("handlerAppDownloadFilePath32Bit");

        handlerFileMultiAjaxFileUploadPanel32Bit = new MultiAjaxFileUploadPanel2("handlerAppFile32Bit", 1, true, getString("HandlerApp.handlerAppDownloadFile32Bit")) {
            @Override
            protected void onUploadComplete(AjaxRequestTarget ajaxRequestTarget, UploadedFileInfo uploadedFileInfo) {
                super.onUploadComplete(ajaxRequestTarget, uploadedFileInfo);
                Collection<UploadedFileInfo> appHandlerFile32BitList = (Collection<UploadedFileInfo>) handlerFileMultiAjaxFileUploadPanel32Bit.getConvertedInput();
                Iterator<UploadedFileInfo> uploadedFileInfoIterator;
                if (appHandlerFile32BitList != null && appHandlerFile32BitList.size() > 0) {
                    uploadedFileInfoIterator = appHandlerFile32BitList.iterator();
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
                    UploadedFileInfo handlerAppFile = uploadedFileInfoList.get(0);
//                    String iconDownloadPath =  FileServerService.FILE_DOWNLOAD_SERVER_PATH.replace("${key}", handlerAppFile.getFileId());
                    String iconDownloadPath = ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH).replace("${key}", handlerAppFile.getFileId());
                    selectedFileHandlerAppPathMdl32Bit.setObject(iconDownloadPath);
                }
                selectedFileHandlerAppPathTitle32Bit.setDefaultModelObject(selectedFileHandlerAppPathMdl32Bit.getObject());

                if (form.get("link32Bit") != null) {
                    ajaxRequestTarget.add(form.get("link32Bit"));
                } else {
                    ajaxRequestTarget.add(selectedFileHandlerAppPathTitle32Bit);
                }
                selectedFileHandlerAppPathContainer32Bit.add(new AttributeModifier("href", selectedFileHandlerAppPathMdl32Bit));
                ajaxRequestTarget.add(selectedFileHandlerAppPathContainer32Bit);
            }
        };
        handlerFileMultiAjaxFileUploadPanel32Bit.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        handlerFileMultiAjaxFileUploadPanel32Bit.setLabel(new ResourceModel("HandlerApp.handlerAppDownloadFile32Bit"));
        handlerFileMultiAjaxFileUploadPanel32Bit.setModel(new Model<>(handlerApp.getHandlerFile32bit()));
        form.add(handlerFileMultiAjaxFileUploadPanel32Bit);


        selectedFileHandlerAppPathTitle32Bit = new Label("link32Bit", selectedFileHandlerAppPathMdl32Bit);
        selectedFileHandlerAppPathTitle32Bit.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        selectedFileHandlerAppPathContainer32Bit.add(selectedFileHandlerAppPathTitle32Bit);



        selectedFileHandlerAppPathContainer32Bit.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        selectedFileHandlerAppPathContainer32Bit.add(new AttributeModifier("href", selectedFileHandlerAppPathMdl32Bit));
        form.add(selectedFileHandlerAppPathContainer32Bit);

        //test File

        if (handlerApp.getTestHandlerFile() != null && handlerApp.getTestHandlerFile().getFilePath() != null) {
//            selectedTestFileHandlerAppPathMdl.setObject(FileServerService.FILE_DOWNLOAD_SERVER_PATH.replace("${key}", handlerApp.getTestHandlerFile().getFilePath()));
            selectedTestFileHandlerAppPathMdl.setObject(ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH).replace("${key}", handlerApp.getTestHandlerFile().getFilePath()));
        }
        selectedTestFileHandlerAppPathContainer = new WebMarkupContainer("testHandlerAppDownloadFilePath");

        testHandlerFileMultiAjaxFileUploadPanel2 = new MultiAjaxFileUploadPanel2("testHandlerAppFile", 1, true, getString("HandlerApp.testHandlerAppDownloadFile")) {
            @Override
            protected void onUploadComplete(AjaxRequestTarget ajaxRequestTarget, UploadedFileInfo uploadedFileInfo) {
                super.onUploadComplete(ajaxRequestTarget, uploadedFileInfo);
                Collection<UploadedFileInfo> appHandlerFileList = (Collection<UploadedFileInfo>) testHandlerFileMultiAjaxFileUploadPanel2.getConvertedInput();
                Iterator<UploadedFileInfo> uploadedFileInfoIterator;
                if (appHandlerFileList != null && appHandlerFileList.size() > 0) {
                    uploadedFileInfoIterator = appHandlerFileList.iterator();
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
                    UploadedFileInfo handlerAppFile = uploadedFileInfoList.get(0);
//                    String iconDownloadPath =   FileServerService.FILE_DOWNLOAD_SERVER_PATH.replace("${key}", handlerAppFile.getFileId());
                    String iconDownloadPath = ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH).replace("${key}", handlerAppFile.getFileId());
                    selectedTestFileHandlerAppPathMdl.setObject(iconDownloadPath);
                }
                selectedTestFileHandlerAppPathTitle.setDefaultModelObject(selectedTestFileHandlerAppPathMdl.getObject());

                if (form.get("testLink") != null) {
                    ajaxRequestTarget.add(form.get("testLink"));
                } else {
                    ajaxRequestTarget.add(selectedTestFileHandlerAppPathTitle);
                }
                selectedTestFileHandlerAppPathContainer.add(new AttributeModifier("href", selectedTestFileHandlerAppPathMdl));
                ajaxRequestTarget.add(selectedTestFileHandlerAppPathContainer);
            }
        };
        testHandlerFileMultiAjaxFileUploadPanel2.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        testHandlerFileMultiAjaxFileUploadPanel2.setLabel(new ResourceModel("HandlerApp.testHandlerAppDownloadFile"));
        testHandlerFileMultiAjaxFileUploadPanel2.setModel(new Model<>(handlerApp.getTestHandlerFile()));
        form.add(testHandlerFileMultiAjaxFileUploadPanel2);

        selectedTestFileHandlerAppPathTitle = new Label("testLink", selectedTestFileHandlerAppPathMdl);
        selectedTestFileHandlerAppPathTitle.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        selectedTestFileHandlerAppPathContainer.add(selectedTestFileHandlerAppPathTitle);

        selectedTestFileHandlerAppPathContainer.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        selectedTestFileHandlerAppPathContainer.add(new AttributeModifier("href", selectedTestFileHandlerAppPathMdl));

        form.add(selectedTestFileHandlerAppPathContainer);


        if (handlerApp.getTestHandlerFile32bit() != null && handlerApp.getTestHandlerFile32bit().getFilePath() != null) {
            selectedTestFileHandlerAppPathMdl32Bit.setObject(ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH).replace("${key}", handlerApp.getTestHandlerFile32bit().getFilePath()));
        }
        selectedTestFileHandlerAppPathContainer32Bit = new WebMarkupContainer("testHandlerAppDownloadFilePath32Bit");

        testHandlerFileMultiAjaxFileUploadPanel32Bit = new MultiAjaxFileUploadPanel2("testHandlerAppFile32Bit", 1, true, getString("HandlerApp.testHandlerAppDownloadFile32Bit")) {
            @Override
            protected void onUploadComplete(AjaxRequestTarget ajaxRequestTarget, UploadedFileInfo uploadedFileInfo) {
                super.onUploadComplete(ajaxRequestTarget, uploadedFileInfo);
                Collection<UploadedFileInfo> appHandlerTestFile32BitList = (Collection<UploadedFileInfo>) testHandlerFileMultiAjaxFileUploadPanel32Bit.getConvertedInput();
                Iterator<UploadedFileInfo> uploadedFileInfoIterator;
                if (appHandlerTestFile32BitList != null && appHandlerTestFile32BitList.size() > 0) {
                    uploadedFileInfoIterator = appHandlerTestFile32BitList.iterator();
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
                    UploadedFileInfo handlerAppFile = uploadedFileInfoList.get(0);
                    String iconDownloadPath = ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH).replace("${key}", handlerAppFile.getFileId());
                    selectedTestFileHandlerAppPathMdl32Bit.setObject(iconDownloadPath);
                }
                selectedTestFileHandlerAppPathTitle32Bit.setDefaultModelObject(selectedTestFileHandlerAppPathMdl32Bit.getObject());

                if (form.get("testLink32Bit") != null) {
                    ajaxRequestTarget.add(form.get("testLink32Bit"));
                } else {
                    ajaxRequestTarget.add(selectedTestFileHandlerAppPathTitle32Bit);
                }
                selectedTestFileHandlerAppPathContainer32Bit.add(new AttributeModifier("href", selectedTestFileHandlerAppPathMdl32Bit));
                ajaxRequestTarget.add(selectedTestFileHandlerAppPathContainer32Bit);
            }
        };
        testHandlerFileMultiAjaxFileUploadPanel32Bit.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        testHandlerFileMultiAjaxFileUploadPanel32Bit.setLabel(new ResourceModel("HandlerApp.testHandlerAppDownloadFile32Bit"));
        testHandlerFileMultiAjaxFileUploadPanel32Bit.setModel(new Model<>(handlerApp.getTestHandlerFile32bit()));
        form.add(testHandlerFileMultiAjaxFileUploadPanel32Bit);

        selectedTestFileHandlerAppPathTitle32Bit = new Label("testLink32Bit", selectedTestFileHandlerAppPathMdl32Bit);
        selectedTestFileHandlerAppPathTitle32Bit.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        selectedTestFileHandlerAppPathContainer32Bit.add(selectedTestFileHandlerAppPathTitle32Bit);

        selectedTestFileHandlerAppPathContainer32Bit.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        selectedTestFileHandlerAppPathContainer32Bit.add(new AttributeModifier("href", selectedTestFileHandlerAppPathMdl32Bit));

        form.add(selectedTestFileHandlerAppPathContainer32Bit);


        fileHandlerApp32BitLbl = new Label("fileHandlerApp32BitLbl", fileHandlerAppLblMdl32Bit);
        fileHandlerApp32BitLbl.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        form.add(fileHandlerApp32BitLbl);


        testFileHandlerApp32BitLbl = new Label("testFileHandlerApp32BitLbl", testFileHandlerAppLblMdl32Bit);
        testFileHandlerApp32BitLbl.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(testFileHandlerApp32BitLbl);


        form.add(new AjaxFormButton("save", form, feedbackPanel) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {

                Long versionCode = null;

                String validationString = "";
                Session session = HibernateUtil.getCurrentSession();

                OS selectedOS = OSService.Instance.loadOSByOSId(osId, session);

                HandlerApp inputHandlerApp = (HandlerApp) form.getModelObject();
                if (hasError) {
                    inputHandlerApp.setId(null);
                }
                File handlerAppFile = null;
                File handlerAppFile32Bit = null;
                File testHandlerAppFile = null;
                File testHandlerAppFile32Bit = null;
                HandlerApp tmpHandHandlerApp = (inputHandlerApp.getId() != null ? (HandlerApp) session.load(HandlerApp.class, inputHandlerApp.getId()) : inputHandlerApp);
                boolean needToUpdateOs = false;
                if (tmpHandHandlerApp.getId() == null)
                    needToUpdateOs = true;

                try {
                    boolean hasTestHandlerApp = false;
                    boolean hasHandlerApp = false;

                    //test files
                    Collection<UploadedFileInfo> testHandlerAppFileList = (Collection<UploadedFileInfo>) testHandlerFileMultiAjaxFileUploadPanel2.getConvertedInput();
                    Iterator<UploadedFileInfo> testUploadedFileInfoIterator;
                    if (testHandlerAppFileList != null && testHandlerAppFileList.size() > 0) {
                        testUploadedFileInfoIterator = testHandlerAppFileList.iterator();
                    } else {
                        testUploadedFileInfoIterator = null;
                    }

                    List<UploadedFileInfo> testUploadedFileInfoList = new ArrayList();
                    if (testUploadedFileInfoIterator != null) {
                        while (testUploadedFileInfoIterator.hasNext()) {
                            UploadedFileInfo tmpUploadedFileInfo = testUploadedFileInfoIterator.next();
                            testUploadedFileInfoList.add(tmpUploadedFileInfo);
                        }
                    }
                    if (testUploadedFileInfoList != null && testUploadedFileInfoList.size() > 0) {
                        UploadedFileInfo testHandlerAppFileInfo = testUploadedFileInfoList.get(0);
                        testHandlerAppFile = new File();
                        testHandlerAppFile.setStereoType(StereoType.LAUNCHER_FILE);
                        testHandlerAppFile.setFileName(AppUtils.dateTagFileName(testHandlerAppFileInfo.getFileName()));
                        try {
                            FileServerService.Instance.persistFileToServer(testHandlerAppFileInfo.getFileId());
                        } catch (Exception ex) {
                            if (!ex.getMessage().contains("not found!"))
                                throw ex;
                        }

                        testHandlerAppFile.setFilePath(testHandlerAppFileInfo.getFileId());
                        tmpHandHandlerApp.setTestHandlerFile(testHandlerAppFile);
                        BaseEntityService.Instance.saveOrUpdate(testHandlerAppFile, session);
                        hasTestHandlerApp = true;
                    }

                    Collection<UploadedFileInfo> testHandlerAppFile32BitList = (Collection<UploadedFileInfo>) testHandlerFileMultiAjaxFileUploadPanel32Bit.getConvertedInput();
                    Iterator<UploadedFileInfo> test32BitUploadedFileInfoIterator;
                    if (testHandlerAppFile32BitList != null && testHandlerAppFile32BitList.size() > 0) {
                        test32BitUploadedFileInfoIterator = testHandlerAppFile32BitList.iterator();
                    } else {
                        test32BitUploadedFileInfoIterator = null;
                    }

                    List<UploadedFileInfo> test32BitUploadedFileInfoList = new ArrayList();
                    if (test32BitUploadedFileInfoIterator != null) {
                        while (test32BitUploadedFileInfoIterator.hasNext()) {
                            UploadedFileInfo tmpUploadedFileInfo = test32BitUploadedFileInfoIterator.next();
                            test32BitUploadedFileInfoList.add(tmpUploadedFileInfo);
                        }
                    }
                    if (test32BitUploadedFileInfoList != null && test32BitUploadedFileInfoList.size() > 0) {
                        UploadedFileInfo testHandlerAppFile32BitInfo = test32BitUploadedFileInfoList.get(0);
                        testHandlerAppFile32Bit = new File();
                        testHandlerAppFile32Bit.setStereoType(StereoType.LAUNCHER_FILE);
                        testHandlerAppFile32Bit.setFileName(AppUtils.dateTagFileName(testHandlerAppFile32BitInfo.getFileName()));
                        try {
                            FileServerService.Instance.persistFileToServer(testHandlerAppFile32BitInfo.getFileId());
                        } catch (Exception ex) {
                            if (!ex.getMessage().contains("not found!"))
                                throw ex;
                        }

                        testHandlerAppFile32Bit.setFilePath(testHandlerAppFile32BitInfo.getFileId());
                        tmpHandHandlerApp.setTestHandlerFile32bit(testHandlerAppFile32Bit);
                        BaseEntityService.Instance.saveOrUpdate(testHandlerAppFile32Bit, session);
                        hasTestHandlerApp = true;
                    }


                    //main Files
                    Collection<UploadedFileInfo> handlerAppFileList = (Collection<UploadedFileInfo>) handlerFileMultiAjaxFileUploadPanel2.getConvertedInput();
                    Iterator<UploadedFileInfo> uploadedFileInfoIterator;
                    if (handlerAppFileList != null && handlerAppFileList.size() > 0) {
                        uploadedFileInfoIterator = handlerAppFileList.iterator();
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

                    Collection<UploadedFileInfo> handlerAppFile32BitList = (Collection<UploadedFileInfo>) handlerFileMultiAjaxFileUploadPanel32Bit.getConvertedInput();
                    Iterator<UploadedFileInfo> uploadedFileInfoIterator32Bit;
                    if (handlerAppFile32BitList != null && handlerAppFile32BitList.size() > 0) {
                        uploadedFileInfoIterator32Bit = handlerAppFile32BitList.iterator();
                    } else {
                        uploadedFileInfoIterator32Bit = null;
                    }

                    List<UploadedFileInfo> uploadedFile32BitInfoList = new ArrayList();
                    if (uploadedFileInfoIterator32Bit != null) {
                        while (uploadedFileInfoIterator32Bit.hasNext()) {
                            UploadedFileInfo tmpUploadedFileInfo = uploadedFileInfoIterator32Bit.next();
                            uploadedFile32BitInfoList.add(tmpUploadedFileInfo);
                        }
                    }


                    if (uploadedFileInfoList != null && uploadedFileInfoList.size() > 0) {
                        UploadedFileInfo handlerAppFileInfo = uploadedFileInfoList.get(0);
                        handlerAppFile = new File();
                        handlerAppFile.setStereoType(StereoType.LAUNCHER_FILE);
                        handlerAppFile.setFileName(AppUtils.dateTagFileName(handlerAppFileInfo.getFileName()));
                        try {
                            FileServerService.Instance.persistFileToServer(handlerAppFileInfo.getFileId());
                        } catch (Exception ex) {
                            if (!ex.getMessage().contains("not found!"))
                                throw ex;
                        }

                        handlerAppFile.setFilePath(handlerAppFileInfo.getFileId());
                        tmpHandHandlerApp.setHandlerFile(handlerAppFile);
                        BaseEntityService.Instance.saveOrUpdate(handlerAppFile, session);
                        hasHandlerApp = true;
                    }

                    if (uploadedFile32BitInfoList != null && uploadedFile32BitInfoList.size() > 0) {
                        UploadedFileInfo handlerAppFile32BitInfo = uploadedFile32BitInfoList.get(0);
                        handlerAppFile32Bit = new File();
                        handlerAppFile32Bit.setStereoType(StereoType.LAUNCHER_FILE);
                        handlerAppFile32Bit.setFileName(AppUtils.dateTagFileName(handlerAppFile32BitInfo.getFileName()));
                        try {
                            FileServerService.Instance.persistFileToServer(handlerAppFile32BitInfo.getFileId());
                        } catch (Exception ex) {
                            if (!ex.getMessage().contains("not found!"))
                                throw ex;
                        }

                        handlerAppFile32Bit.setFilePath(handlerAppFile32BitInfo.getFileId());
                        tmpHandHandlerApp.setHandlerFile32bit(handlerAppFile32Bit);
                        BaseEntityService.Instance.saveOrUpdate(handlerAppFile32Bit, session);
                        hasHandlerApp = true;
                    }


                    if (!(hasHandlerApp || hasTestHandlerApp)) {
                        if (handlerApp.getId() == null) {
                            validationString += " - " +
                                    getString("RequiredOneOf").replace("${label}", getString("HandlerApp.handlerAppDownloadFile"))
                                            .replace("${label1}", getString("HandlerApp.testHandlerAppDownloadFile"))
                                    + "<br/>";

                        } else {
                            HandlerApp loadedHandlerApp = (HandlerApp) session.load(HandlerApp.class, handlerApp.getId());
                            if (loadedHandlerApp.getHandlerFile() == null &&loadedHandlerApp.getHandlerFile32bit() == null &&
                                    loadedHandlerApp.getTestHandlerFile() == null && loadedHandlerApp.getTestHandlerFile32bit() == null) {
                                validationString += " - " +
                                        getString("Required").replace("${label}", getString("HandlerApp.handlerAppDownloadFile")) + "<br/>";
                            }
                        }

                    }

                    if (versionCodeTextField.getConvertedInput() == null) {
                        if (!hasId) {
                            validationString += " - " +
                                    getString("Required").replace("${label}", versionCodeTextField.getLabel().getObject()) + "<br/>";
                        } else {
                            HandlerApp tmpLoadedHandlerApp = (HandlerApp) session.load(HandlerApp.class, handlerApp.getId());
                            versionCode = tmpHandHandlerApp.getVersionCode();
                        }
                    } else {
                        versionCode = Long.valueOf(versionCodeTextField.getConvertedInput().toString());
                    }

                } catch (Exception e) {
                    validationString += " - " +
                            getString("error.generalErr") + "<br/>";
                }


                if (versionCodeTextField != null && versionCodeTextField.getValidatorString() != null && !versionCodeTextField.getValidatorString().isEmpty()) {
                    for (String validationStringInList : versionCodeTextField.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }
                Organization organization = (Organization) organizationDropDown.getConvertedInput();
                OSEnvironment osEnvironment = (OSEnvironment) osEnvironmentDropDown.getConvertedInput();

                if (organization != null) {
                    tmpHandHandlerApp.setOrganization(organization);
                } else {
                    Organization defaultOrganization = OrgService.Instance.getDefaultOrganization(session);
                    organization = defaultOrganization;
                    if (defaultOrganization != null) {
                        tmpHandHandlerApp.setOrganization(defaultOrganization);
                    } else {
                        validationString += " - " + ResultStatus.ORGANIZATION_NOT_FOUND.toString() + "<br/>";
                    }
                }
                if (osEnvironment != null) {
                    tmpHandHandlerApp.setOsEnvironment(osEnvironment);
                } else {
                    OSEnvironment defaultOsEnvironment = EnvironmentService.Instance.getDefaultEnvironment();
                    if (defaultOsEnvironment != null) {
                        tmpHandHandlerApp.setOsEnvironment(defaultOsEnvironment);
                        osEnvironment = defaultOsEnvironment;
                    } else {
                        validationString += " - " + ResultStatus.ENVIRONMENT_NOT_FOUND.toString() + "<br/>";
                    }
                }

                HandlerAppService.HandlerAppCriteria existedHandlerAppCriteria = new HandlerAppService.HandlerAppCriteria();
                existedHandlerAppCriteria.setOrganization(organization);
                existedHandlerAppCriteria.setOsEnvironment(osEnvironment);
                existedHandlerAppCriteria.setVersionCode(versionCode);
                List<Long> osIdList = new ArrayList<>();
                osIdList.add(inputOsId);
                existedHandlerAppCriteria.setOsIds(osIdList);
                List<HandlerAppService.HandlerAppSearchResultModel> handlerAppList = HandlerAppService.Instance.list(existedHandlerAppCriteria, 0, -1, null, false, session);
                if (handlerAppList != null && !handlerAppList.isEmpty()) {
                    if (handlerApp.getId() == null) {
                        validationString += " - " + getString("HandlerApp.save.constraint.violation") + "<br/>";

                    } else {
                        for (HandlerAppService.HandlerAppSearchResultModel handlerAppSearchResultModel : handlerAppList) {
                            if (!handlerApp.getId().equals(handlerAppSearchResultModel.getId())) {
                                validationString += " - " + getString("HandlerApp.save.constraint.violation") + "<br/>";
                                target.appendJavaScript("showMessage('" + validationString + "');");
                                return;
                            }
                        }

                    }
                }


                Boolean isDefault = (Boolean) isDefaultSwitchBox.getConvertedInput();
                if (isDefault && handlerAppFile == null && handlerAppFile32Bit==null && !hasId) {
                    validationString += " - " + AppStorePropertyReader.getString("Required").replace("${label}", handlerFileMultiAjaxFileUploadPanel2.getLabel().getObject() + "<br/>");
                }

                if (!validationString.isEmpty()) {
                    target.appendJavaScript("showMessage('" + validationString + "');");
                    return;
                }

                try {
                    tmpHandHandlerApp.setVersionCode(inputHandlerApp.getVersionCode());
                    tmpHandHandlerApp.setActive((Boolean) isActiveSwitchBox.getConvertedInput());
                    tmpHandHandlerApp.setDefaultForOrganization(isDefault);
                    if (handlerAppFile != null || testHandlerAppFile != null || handlerAppFile32Bit != null || testHandlerAppFile32Bit != null) {
                        tmpHandHandlerApp.setParentOS(selectedOS);
                    }
                    if (handlerAppFile32Bit != null) {
                        tmpHandHandlerApp.setHandlerFile32bit(handlerAppFile32Bit);
                    }
                    if (handlerAppFile != null) {
                        tmpHandHandlerApp.setHandlerFile(handlerAppFile);
                        tmpHandHandlerApp.setUploadedFileDate(DateTime.now());
                    } else if (handlerAppFile32Bit != null) {
                        tmpHandHandlerApp.setUploadedFileDate(DateTime.now());
                    }

                    if (testHandlerAppFile32Bit != null) {
                        tmpHandHandlerApp.setTestHandlerFile32bit(testHandlerAppFile32Bit);
                    }
                    if (testHandlerAppFile != null) {
                        tmpHandHandlerApp.setTestHandlerFile(testHandlerAppFile);
                        tmpHandHandlerApp.setUploadedTestFileDate(DateTime.now());
                    } else if (testHandlerAppFile32Bit != null) {
                        tmpHandHandlerApp.setUploadedTestFileDate(DateTime.now());
                    }

                    Transaction tx = null;
                    try {
                        tx = session.beginTransaction();

                        if (isDefault) {
                            List<HandlerAppService.HandlerAppSearchResultModel> handlerAppSearchResultModelList = HandlerAppService.Instance.getDefaultHandlerApp(tmpHandHandlerApp.getOrganization() ,session);
                            for (HandlerAppService.HandlerAppSearchResultModel handlerAppSearchResultModel : handlerAppSearchResultModelList) {
                                if (handlerAppSearchResultModel != null && handlerAppSearchResultModel.getId() != null) {
                                    HandlerApp handlerAppInList = (HandlerApp) session.load(HandlerApp.class, handlerAppSearchResultModel.getId());
                                    if (tmpHandHandlerApp.getId() == null) {
                                        handlerAppInList.setDefaultForOrganization(false);
                                        HandlerAppService.Instance.saveOrUpdate(handlerAppInList, session);
                                    } else if (!handlerAppInList.getId().equals(tmpHandHandlerApp.getId())) {
                                        handlerAppInList.setDefaultForOrganization(false);
                                        HandlerAppService.Instance.saveOrUpdate(handlerAppInList, session);
                                    }
                                }
                            }
                        }
                        if(!tmpHandHandlerApp.getOsEnvironment().equals(windowsEnv)){
                            tmpHandHandlerApp.setHandlerFile32bit(null);
                            tmpHandHandlerApp.setTestHandlerFile32bit(null);
                        }
                        HandlerAppService.Instance.saveOrUpdate(tmpHandHandlerApp, session);
                        OS parentOs = null;
                        if (osId != null && needToUpdateOs) {
                            OS loadedOs = (OS) session.load(OS.class, osId);
                            List<HandlerApp> handlerApps;
                            handlerApps = (loadedOs.getHandlerApps() != null && !loadedOs.getHandlerApps().isEmpty()) ? loadedOs.getHandlerApps() : new ArrayList<HandlerApp>();
                            handlerApps.add(tmpHandHandlerApp);
                            loadedOs.setHandlerApps(handlerApps);
                            OSService.Instance.saveOrUpdate(loadedOs, session);
                        }
                        if (osId == null) {
                            osId = tmpHandHandlerApp.getParentOS().getId();
                        }

                        tx.commit();
                        hasError = false;
                        childFinished(target, new Model<>(), form.get("save"));

                    } catch (Exception ex) {
                        hasError = true;

                        if (ex instanceof ConstraintViolationException) {
                            target.appendJavaScript("showMessage('" + getString("HandlerApp.save.constraint.violation") + "');");
                        } else {
                            processException(target, ex);
                        }
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
