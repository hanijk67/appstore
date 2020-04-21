package com.fanap.midhco.ui.pages.app;

import com.fanap.midhco.appstore.entities.App;
import com.fanap.midhco.appstore.entities.AppPackage;
import com.fanap.midhco.appstore.entities.OSType;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.app.AppPackageService;
import com.fanap.midhco.appstore.service.app.AppService;
import com.fanap.midhco.appstore.service.app.IAPPPackageService;
import com.fanap.midhco.appstore.service.myException.AppStoreRuntimeException;
import com.fanap.midhco.appstore.service.myException.PageValidationException;
import com.fanap.midhco.appstore.service.myException.appBundle.BaseAppBundleException;
import com.fanap.midhco.appstore.service.osType.IUploadFilter;
import com.fanap.midhco.appstore.service.osType.OSTypeService;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.component.multiAjaxFileUpload2.MultiAjaxFileUploadPanel2;
import com.fanap.midhco.ui.component.multiAjaxFileUpload2.UploadedFileInfo;
import com.fanap.midhco.ui.component.switchbox.SwitchBox;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.hibernate.Session;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Created by admin123 on 7/1/2016.
 */
@Authorize(view = Access.APP_ADD_PACKAGE)
public class AddPackagePanel extends BasePanel {
    Form form;
    FeedbackPanel feedbackPanel;
    OSType osType;
    TextField versionCodeTextField;
    TextField versionNameTextField;

    TextField minSDKTextField;
    TextField targetSDKTextField;

    MultiAjaxFileUploadPanel2 packAjaxFileUpload;
    MultiAjaxFileUploadPanel2 iconAjaxUpload;
    MultiAjaxFileUploadPanel2 thumbImages;

    AppPackageService.NewPackageInfo packageInfo;
    boolean usePrevious = false;
    SwitchBox usePreviousFile;
    Label usePreviousFileLbl;
    App app;

    public AddPackagePanel(String id, App app) {
        super(id);

        this.app = app;

        if (app != null)
            osType = app.getOsType();

        feedbackPanel = new FeedbackPanel("feedbackPabel1");
        feedbackPanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(feedbackPanel);

        form = new Form("form1", new Model<>());
        form.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        versionCodeTextField = new TextField("versionCode");
        versionCodeTextField.setLabel(new ResourceModel("APPPackage.versionCode"));
        versionCodeTextField.setEnabled(false);
        versionCodeTextField.isEnabled();
        versionCodeTextField.setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true);
        versionCodeTextField.setModel(new Model<>());
        form.add(versionCodeTextField);

        versionNameTextField = new TextField("versionName");
        versionNameTextField.setLabel(new ResourceModel("APPPackage.versionName"));
        versionNameTextField.setEnabled(false);
        versionNameTextField.isEnabled();
        versionNameTextField.setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true);
        versionNameTextField.setModel(new Model<>());
        form.add(versionNameTextField);

        minSDKTextField = new TextField("minSDK");
        minSDKTextField.setLabel(new ResourceModel("APPPackage.minSDK"));
        minSDKTextField.setEnabled(false);
        minSDKTextField.isEnabled();
        minSDKTextField.setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true);
        minSDKTextField.setModel(new Model<>());
        form.add(minSDKTextField);

        targetSDKTextField = new TextField("targetSDK");
        targetSDKTextField.setLabel(new ResourceModel("APPPackage.targetSDK"));
        targetSDKTextField.setEnabled(false);
        targetSDKTextField.isEnabled();
        targetSDKTextField.setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true);
        targetSDKTextField.setModel(new Model<>());
        form.add(targetSDKTextField);

        packAjaxFileUpload = new MultiAjaxFileUploadPanel2("packFile", 1, true, getString("APPPackage.packFile")) {
            @Override
            protected void onUploadComplete(AjaxRequestTarget target, UploadedFileInfo uploadedFileInfo) {
                Set<UploadedFileInfo> uploadedFileInfoList =
                        (Set<UploadedFileInfo>) this.getConvertedInput();
                packAjaxFileUpload.setLabel(new ResourceModel("APPPackage.packFile"));
                if (osType == null) {

                    return;
                }

                if (uploadedFileInfoList == null || uploadedFileInfoList.isEmpty()) {
                    return;
                }

                UploadedFileInfo tempUploadedFile = uploadedFileInfoList.iterator().next();
                try {
                    Session session = HibernateUtil.getCurrentSession();
                    IAPPPackageService iappPackageService = AppPackageService.Instance.processPackageFile(tempUploadedFile.getPhysicalLocation(), osType);
                    if (app.getId() == null) {

                        Boolean packageExist = AppService.Instance.doesPackageExists(iappPackageService.getPackage(), osType, session);

                        if (packageExist != null && packageExist) {
                            try {
                                if (PrincipalUtil.isCurrentUserDeveloper() != null && PrincipalUtil.isCurrentUserDeveloper()) {
                                    target.appendJavaScript("showMessage('" + getString("App.upload.error") + "');");
                                } else {
                                    target.appendJavaScript("showMessage('" + getString("App.appPackageName.exist") + "');");
                                }
                            } catch (Exception e) {
                                target.appendJavaScript("showMessage('" + getString("error.generalErr") + "');");
                            }
                            return;
                        }
                    } else {
                        Comparator versionComparator = OSTypeService.Instance.getVersionComparatorForOSType(osType);

                        App loadedApp = (App) session.load(App.class, app.getId());
                        List<AppPackage> appPackageList = loadedApp.getAppPackages();
                        String versionCode = iappPackageService.getVersionCode();
                        for (AppPackage appPackageInList : appPackageList) {
                            int compareResult = versionComparator.compare(appPackageInList.getVersionCode(),versionCode);
                            if (compareResult == -1 || compareResult == 0) {
                                throw new Exception(AppStorePropertyReader.getString("error.appPackage.add.sameVersion"));
                            }
                        }
                    }

                    versionCodeTextField.setModel(new Model<>(iappPackageService.getVersionCode()));
                    versionNameTextField.setModel(new Model<>(iappPackageService.getVersionName()));

                    minSDKTextField.setModel(new Model<>(iappPackageService.getMinSDK()));
                    targetSDKTextField.setModel(new Model<>(iappPackageService.getTargetSDK()));

                    target.add(versionCodeTextField);
                    target.add(versionNameTextField);
                    target.add(minSDKTextField);
                    target.add(targetSDKTextField);

                    AppPackageService.NewPackageInfo packageInfo =
                            AppPackageService.Instance.validateNewAppPackage(app, iappPackageService, false);

                    AddPackagePanel.this.packageInfo = packageInfo;

                    if (app.getId() == null) {
                        childFinished(target, new Model<>(iappPackageService), this);
                    }

                } catch (AppPackageService.APPPackageException ex) {
                    logger.error("exception in processing app package file ", ex);
                    target.appendJavaScript("showMessage('" + ex.getMessage() + "');");
                    packageInfo = null;
                } catch (Exception ex) {
                    logger.error("exception in processing app package file ", ex);
                    target.appendJavaScript("showMessage('" + ex.getMessage() + "');");
                    packageInfo = null;                }
            }

            @Override
            protected void onDelete(AjaxRequestTarget target, UploadedFileInfo uploadedFileInfo) {
                reset(target);
            }
        };

        packAjaxFileUpload.setLabel(new ResourceModel("APPPackage.packFile"));
        packAjaxFileUpload.setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true);

        packAjaxFileUpload.setModel(new Model<>());
        form.add(packAjaxFileUpload);

        List<IUploadFilter> imageFilters = new ArrayList<>();
        imageFilters.add(IUploadFilter.getImageUploadFilter());

//        AjaxLink copyIconFromPrevPackage = new AjaxLink("copyIconFromPrevPackage") {
//            @Override
//            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
//                Session session = HibernateUtil.getCurrentSession();
//                App appReloaded = (App)session.load(App.class, app.getId());
//                String downloadURL = ConfigUtil.getProperty(ConfigUtil.FILE_DOWNLOAD_SERVER_PATH);
//
//                File iconFile = appReloaded.getMainPackage().getIconFile();
//            }
//        };
//        form.add(copyIconFromPrevPackage);


        iconAjaxUpload = new MultiAjaxFileUploadPanel2("iconFile", imageFilters, 1, false, getString("APPPackage.iconFile"));
        iconAjaxUpload.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        iconAjaxUpload.setLabel(new ResourceModel("APPPackage.iconFile"));
        iconAjaxUpload.setModel(new Model<>());
        form.add(iconAjaxUpload);


//        AjaxLink copyThumbsFromPrevPackage = new AjaxLink("copyThumbsFromPrevPackage") {
//            @Override
//            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
//
//            }
//        };
//        form.add(copyThumbsFromPrevPackage);

        thumbImages = new MultiAjaxFileUploadPanel2("thumbImages", imageFilters, null, false, getString("APPPackage.thumbImages"));
        thumbImages.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        thumbImages.setLabel(new ResourceModel("APPPackage.thumbImages"));
        thumbImages.setModel(new Model<>());
        form.add(thumbImages);

        usePreviousFile = new SwitchBox("usePreviousFile", getString("label.yes"), getString("label.no"));
        usePreviousFile.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        usePreviousFile.setModel(new Model());
        usePreviousFile.setModelObject(false);

        Model str = new Model(AppStorePropertyReader.getString("AppPackage.usePreviousFile"));
        usePreviousFileLbl = new Label("usePreviousFileLbl");
        usePreviousFileLbl.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        usePreviousFileLbl.setDefaultModel(str);

        if (app == null || app.getId() == null) {
            usePreviousFile.setEnabled(false);
            usePreviousFile.setVisible(false);
            usePreviousFileLbl.setEnabled(false);
            usePreviousFileLbl.setVisible(false);
        } else {
            usePreviousFile.setEnabled(true);
            usePreviousFile.setVisible(true);
            usePreviousFileLbl.setEnabled(true);
            usePreviousFileLbl.setVisible(true);
        }
        form.add(usePreviousFileLbl);
        form.add(usePreviousFile);
        add(form);
    }

    public OSType getOsType() {
        return osType;
    }

    public void setOsType(AjaxRequestTarget target, OSType osType) {
        this.osType = osType;
        try {
            IUploadFilter uploadFilter = AppPackageService.Instance.getUploadFilterForOSType(osType);
            List<IUploadFilter> packFilters = new ArrayList<>();
            packFilters.add(uploadFilter);
            packAjaxFileUpload.setUploadFilters(packFilters, target);
        } catch (Exception ex) {
            throw new AppStoreRuntimeException(ex);
        }
    }

    public void process(AjaxRequestTarget target) {
        ((MultiAjaxFileUploadPanel2) form.get("packFile")).invokeLoadComplete(target);
    }

    public void reset(AjaxRequestTarget target) {
        IAPPPackageService iappPackageService = new IAPPPackageService() {
            @Override
            public String getVersionCode() {
                return null;
            }

            @Override
            public String getVersionName() {
                return null;
            }

            @Override
            public String getPackage() {
                return null;
            }

            @Override
            public String getMinSDK() {
                return null;
            }

            @Override
            public String getTargetSDK() {
                return null;
            }

            @Override
            public Certificate verifyPackage(Certificate previousCertficate) throws BaseAppBundleException {
                return null;
            }

            @Override
            public List<String> getPermissions() {
                return null;
            }
        };
        childFinished(target, new Model<>(iappPackageService), this);
        versionCodeTextField.setModel(new Model<>(iappPackageService.getVersionCode()));
        versionNameTextField.setModel(new Model<>(iappPackageService.getVersionName()));
        minSDKTextField.setModel(new Model<>(iappPackageService.getMinSDK()));
        targetSDKTextField.setModel(new Model<>(iappPackageService.getTargetSDK()));

        target.add(versionCodeTextField);
        target.add(versionNameTextField);
        target.add(minSDKTextField);
        target.add(targetSDKTextField);
        packageInfo = null;
    }

    public AppPackageService.AppPackageModel getAppPackage() throws PageValidationException {
        String versionCode = (String) versionCodeTextField.getConvertedInput();
        String versionName = (String) versionNameTextField.getConvertedInput();

        Set<UploadedFileInfo> packUploadedFileInfoSet = (Set<UploadedFileInfo>) packAjaxFileUpload.getConvertedInput();
        Set<UploadedFileInfo> iconUploadedFileInfoSet = (Set<UploadedFileInfo>) iconAjaxUpload.getConvertedInput();
        Set<UploadedFileInfo> thumbsUploadedFileInfoSet = (Set<UploadedFileInfo>) thumbImages.getConvertedInput();

        AppPackageService.AppPackageModel appPackModel = new AppPackageService.AppPackageModel();

        if (app.getId() == null && (packUploadedFileInfoSet == null || packUploadedFileInfoSet.isEmpty())) {
            String requiredMessage = getString("Required");
            requiredMessage = requiredMessage.replace("${label}", getString("APPPackage.packFile"));
            throw new PageValidationException(requiredMessage);
        }

        if (packUploadedFileInfoSet != null && !packUploadedFileInfoSet.isEmpty())
            appPackModel.setPackFileInfo(packUploadedFileInfoSet.iterator().next());

        if (iconUploadedFileInfoSet != null && !iconUploadedFileInfoSet.isEmpty())
            appPackModel.setIconFileInfo(iconUploadedFileInfoSet.iterator().next());
        if (thumbsUploadedFileInfoSet != null && !thumbsUploadedFileInfoSet.isEmpty())
            appPackModel.setThumbFilesInfo(thumbsUploadedFileInfoSet);
        usePrevious = usePreviousFile.getConvertedInput() != null ? (boolean) usePreviousFile.getConvertedInput() : false;
        appPackModel.setUsePreviousFile(usePrevious);

        if (packageInfo != null)
            appPackModel.setCertificate(packageInfo.getCertificate());

        return appPackModel;
    }
}

