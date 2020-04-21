package com.fanap.midhco.ui.pages.device;

import com.fanap.midhco.appstore.applicationUtils.AppUtils;
import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.service.BaseEntityService;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.device.DeviceService;
import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import com.fanap.midhco.appstore.service.os.OSService;
import com.fanap.midhco.appstore.service.osType.IUploadFilter;
import com.fanap.midhco.appstore.service.osType.OSTypeService;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.multiAjaxFileUpload2.MultiAjaxFileUploadPanel2;
import com.fanap.midhco.ui.component.multiAjaxFileUpload2.UploadedFileInfo;
import com.fanap.midhco.ui.component.mydropdown.MyDropDownChoicePanel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by A.Moshiri on 6/7/2017.
 */
@Authorize(views = {Access.DEVICE_ADD, Access.DEVICE_EDIT})
public class DeviceForm extends BasePanel {

    Form form;
    MyDropDownChoicePanel osDropDown;
    MyDropDownChoicePanel userDropDown;
    MyDropDownChoicePanel osTypeDropDown;
    MyDropDownChoicePanel stateDropDown;
    MultiAjaxFileUploadPanel2 deviceImageMultiAjaxFileUploadPanel2;
    FeedbackPanel feedbackPanel;
    MyDropDownChoicePanel activeDropDown;
    NonCachingImage tmpImageFile;


    public DeviceForm(String id, Device dev) {
        super(id);
        feedbackPanel = new FeedbackPanel("feedbackPanel");
        feedbackPanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(feedbackPanel);

        String fileKey ;
        if(dev!=null){
            fileKey = dev.getImageFile()!=null ? dev.getImageFile().getFilePath() : null;
        }else {
            fileKey = null;
        }

        form = new Form("form", new CompoundPropertyModel(dev));
        form.setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true);

        TextField deviceTitleTextField = new TextField("title");
        deviceTitleTextField.setLabel(new ResourceModel("Device.title"));
        deviceTitleTextField.setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true);
        deviceTitleTextField.setRequired(true);
        form.add(deviceTitleTextField);

        TextField deviceImeiTextField = new TextField("imei");
        deviceImeiTextField.setLabel(new ResourceModel("Device.imei"));
        deviceImeiTextField.setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true);
        deviceImeiTextField.setRequired(true);
        form.add(deviceImeiTextField);

        List<OSType> allOSTypes = OSTypeService.Instance.getEnabledOSTypes();
        osTypeDropDown =
                new MyDropDownChoicePanel("osType", allOSTypes, false, false, getString("OSType"), 1, true, new ChoiceRenderer<>()) {
                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        OSType osType = (OSType) getSelectedItem();
                        if (osType == null) {
                            osDropDown.setChoices(new ArrayList(), target);
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
                        }
                    }
                };
        osTypeDropDown.setLabel(new ResourceModel("OS.osType"));
        osTypeDropDown.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        osTypeDropDown.setModel(new Model<>(dev.getOsType()));
        osTypeDropDown.setRequired(true);
        osTypeDropDown.setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true);
        form.add(osTypeDropDown);

        List<OS> osList = new ArrayList<>();
        if (dev.getOsType() != null) {
            Session session = HibernateUtil.getCurrentSession();
            OSService.OSCriteria osCriteria = new OSService.OSCriteria();
            osCriteria.osType = new ArrayList<>();
            osCriteria.osType.add(dev.getOsType());
            osList.addAll(OSService.Instance.list(osCriteria, 0, -1, null, true, session));
        }

        osDropDown =
                new MyDropDownChoicePanel("os", osList, false, false, getString("OS"), 3, false, new ChoiceRenderer());
        osDropDown.setLabel(new ResourceModel("OS"));
        osDropDown.setModel(new Model<>(dev.getOs()));
        osDropDown.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        osDropDown.setRequired(true);

        form.add(osDropDown);

        List<User> userList = UserService.Instance.listAllUser();
        userDropDown =
                new MyDropDownChoicePanel("usedBy", userList, false, true, getString("Device.usedBy"), 3, false, new ChoiceRenderer());
        userDropDown.setLabel(new ResourceModel("Device.usedBy"));
        userDropDown.setModel(new Model<>(dev.getUsedBy()));
        userDropDown.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        userDropDown.setRequired(true);
        form.add(userDropDown);

        List<DeviceState> deviceStateList = new ArrayList<>();
        deviceStateList.add(DeviceState.FREE);
        deviceStateList.add(DeviceState.INUSED);
        stateDropDown =
                new MyDropDownChoicePanel("state", deviceStateList, false, true, getString("Device.state"), 3, false, new ChoiceRenderer());
        stateDropDown.setLabel(new ResourceModel("Device.state"));
        stateDropDown.setModel(new Model<>(dev.getDeviceState()));
        stateDropDown.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(stateDropDown);


        List<IUploadFilter> imageFilters = new ArrayList<>();
        imageFilters.add(IUploadFilter.getImageUploadFilter());


        deviceImageMultiAjaxFileUploadPanel2 = new MultiAjaxFileUploadPanel2("imageFile", imageFilters, 1, false, getString("APPPackage.iconFile"));
        deviceImageMultiAjaxFileUploadPanel2.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        deviceImageMultiAjaxFileUploadPanel2.setLabel(new ResourceModel("APPPackage.iconFile"));
        deviceImageMultiAjaxFileUploadPanel2.setModel(new Model<>(dev.getImageFile()));
        form.add(deviceImageMultiAjaxFileUploadPanel2);

        tmpImageFile = new NonCachingImage("tmpImageFile", new DynamicImageResource() {
            @Override
            protected byte[] getImageData(Attributes attributes) {
                if(fileKey!=null ){
                    return AppUtils.getImageAsBytes(FileServerService.FILE_DOWNLOAD_SERVER_PATH.replace("${key}", fileKey));
                }else {
                    return null;
                }
            }
        });

        tmpImageFile.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        if (fileKey==null) {
            tmpImageFile.setVisible(false);
        }

        form.add(tmpImageFile);

        List<Boolean> deviceStatus = new ArrayList<>();
        deviceStatus.add(true);
        deviceStatus.add(false);
        activeDropDown = new MyDropDownChoicePanel("active", deviceStatus, false, false, getString("Device.active"), 3, false, new ChoiceRenderer() {
            @Override
            public Object getDisplayValue(Object o) {
                if ((Boolean) o) {
                    return getString("label.active");
                }
                return getString("label.disabled");
            }

            @Override
            public String getIdValue(Object o, int i) {
                return o.toString();
            }
        });
        activeDropDown.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(activeDropDown);

        add(new AjaxFormButton("save", form) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {

                Device device = (Device) form.getModelObject();
                OSType selectedOsType = (OSType) osTypeDropDown.getConvertedInput();
                OS selectedOs = (OS) osDropDown.getConvertedInput();
                User selectedUser = (User) userDropDown.getConvertedInput();
                DeviceState selectedState = (DeviceState) stateDropDown.getConvertedInput();

                String validationString = "";
                String titleString = deviceTitleTextField.getConvertedInput() == null ? null : deviceTitleTextField.getConvertedInput().toString();
                String imeiString = deviceImeiTextField.getConvertedInput() == null ? null : deviceImeiTextField.getConvertedInput().toString();
                Collection<UploadedFileInfo> deviceImageFileList = (Collection<UploadedFileInfo>) deviceImageMultiAjaxFileUploadPanel2.getConvertedInput();
                Iterator<UploadedFileInfo> uploadedFileInfoIterator;
                if (deviceImageFileList != null && deviceImageFileList.size() > 0) {
                    uploadedFileInfoIterator = deviceImageFileList.iterator();
                } else {
                    uploadedFileInfoIterator = null;
                }

                if (titleString == null || titleString.trim().equals("")) {
                    validationString += " - " +
                            getString("Required").replace("${label}", deviceTitleTextField.getLabel().getObject()) + "<br/>";
                }
                if (imeiString == null || imeiString.trim().equals("")) {
                    validationString += " - " +
                            getString("Required").replace("${label}", deviceImeiTextField.getLabel().getObject()) + "<br/>";
                } else {
                    String imeiInput = device.getImei().toString();
                    for (int i = 0; i < imeiInput.length(); i++) {
                        try {
                            Integer.parseInt(imeiInput.substring(i, i + 1));
                        } catch (NumberFormatException e) {
                            validationString += " - " +
                                    getString("IConverter").replace("${label}", deviceImeiTextField.getLabel().getObject()) + "<br/>";
                            break;
                        }
                    }

                    if (imeiInput.length() != 15) {
                        validationString += " - " +
                                getString("Device.imei.length").replace("${label}", deviceImeiTextField.getLabel().getObject()) + "<br/>";
                    }
                }

                if (selectedOs == null) {
                    validationString += " - " +
                            getString("Required").replace("${label}", osDropDown.getLabel().getObject()) + "<br/>";
                }

                if (selectedState == null) {
                    validationString += " - " +
                            getString("Required").replace("${label}", stateDropDown.getLabel().getObject()) + "<br/>";
                }

                if (selectedOsType == null) {
                    validationString += " - " +
                            getString("Required").replace("${label}", osTypeDropDown.getLabel().getObject()) + "<br/>";
                }
                if (selectedUser == null) {
                    validationString += " - " +
                            getString("Required").replace("${label}", userDropDown.getLabel().getObject()) + "<br/>";
                }
                if (uploadedFileInfoIterator == null) {
                    if (device.getId()==null) {
                        validationString += " - " +
                                getString("Required").replace("${label}", getString("Device.image")) + "<br/>";
                    }
                }
                if (!validationString.trim().isEmpty()) {
                    target.appendJavaScript("showMessage('" + validationString + "');");
                    return;
                }
                Session session = HibernateUtil.getCurrentSession();
                device.setTitle(titleString);
                device.setImei(imeiString);
                device.setOs(selectedOs);
                device.setOsType(selectedOsType);
                device.setUsedBy(selectedUser);
                device.setDeviceState(selectedState);
                Boolean deviceActive = (Boolean) activeDropDown.getConvertedInput();
                device.setActive(deviceActive);


                List<UploadedFileInfo> uploadedFileInfoList = new ArrayList();
                if (uploadedFileInfoIterator != null) {
                while (uploadedFileInfoIterator.hasNext()) {
                    UploadedFileInfo tmpUploadedFileInfo = uploadedFileInfoIterator.next();
                    uploadedFileInfoList.add(tmpUploadedFileInfo);
                }
                }

                if (uploadedFileInfoList != null && uploadedFileInfoList.size() > 0) {
                    UploadedFileInfo deviceImageFile = uploadedFileInfoList.get(0);
                    File imageFile = new File();
                    imageFile.setStereoType(StereoType.THUMB_FILE);
                    imageFile.setFileName(AppUtils.dateTagFileName(deviceImageFile.getFileName()));
                    FileServerService.Instance.persistFileToServer(deviceImageFile.getFileId());
                    imageFile.setFilePath(deviceImageFile.getFileId());
                    device.setImageFile(imageFile);
                    BaseEntityService.Instance.saveOrUpdate(imageFile, session);
                }
                try {
                    Transaction tx = session.beginTransaction();
                    DeviceService.Instance.saveOrUpdate(device, session);
                    tx.commit();
                    childFinished(target, new Model<>(), this);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    session.close();
                }
            }
        });

        add(new AjaxLink("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                childFinished(target, null, this);
            }
        });

        add(form);
    }


}
