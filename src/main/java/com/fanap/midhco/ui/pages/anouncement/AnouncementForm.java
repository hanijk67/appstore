package com.fanap.midhco.ui.pages.anouncement;

import com.fanap.midhco.appstore.applicationUtils.AppUtils;
import com.fanap.midhco.appstore.applicationUtils.ComponentKey;
import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.applicationUtils.MyCalendarUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.*;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.anouncement.AnouncementService;
import com.fanap.midhco.appstore.service.anouncement.AppSearchAnouncementActionDescriptor;
import com.fanap.midhco.appstore.service.anouncement.IAnouncementActionDescriptor;
import com.fanap.midhco.appstore.service.app.AppService;
import com.fanap.midhco.appstore.service.environment.EnvironmentService;
import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import com.fanap.midhco.appstore.service.org.OrgService;
import com.fanap.midhco.appstore.service.os.OSService;
import com.fanap.midhco.appstore.service.osType.IUploadFilter;
import com.fanap.midhco.appstore.service.osType.OSTypeService;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.dateTimePanel.DateTimePanel;
import com.fanap.midhco.ui.component.limitedTextField.LimitedTextField;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.component.multiAjaxFileUpload2.MultiAjaxFileUploadPanel2;
import com.fanap.midhco.ui.component.multiAjaxFileUpload2.UploadedFileInfo;
import com.fanap.midhco.ui.component.mydropdown.MyDropDownChoicePanel;
import com.fanap.midhco.ui.component.switchbox.SwitchBox;
import com.fanap.midhco.ui.pages.app.AppList;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.request.resource.IResource;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import static com.fanap.midhco.appstore.wicketApp.EnumCaptionHelper.ANOUNCEMENTMAP;

/**
 * Created by A.Moshiri on 9/6/2017.
 */
@Authorize(views = {Access.ANOUNCEMENT_ADD, Access.ANOUNCEMENT_EDIT})
public class AnouncementForm extends BasePanel implements IParentListner {

    Form form;
    BootStrapModal modal = new BootStrapModal("modal");

    FeedbackPanel feedbackPanel;
    MyDropDownChoicePanel anouncementTypeDropDownChoice;
    MyDropDownChoicePanel anouncementCategoryDropDownChoice;
    DateTimePanel expireDateTimePanel;
    DateTimePanel startDateTimePanel;
    AppList appList = null;
    AnouncementType selectedAnouncementType;
    String selectedActionCategoryString;
    String selectedActionDescriptor;
    MultiAjaxFileUploadPanel2 anouncementImagePanel;
    AjaxFormButton createAnouncement;
    MyDropDownChoicePanel osTypeDropDown;
    MyDropDownChoicePanel organizationsMyDropDown;
    MyDropDownChoicePanel osEnvironmentMyDropDown;
    NonCachingImage tmpImage;

    LimitedTextField anouncementText;
    SwitchBox isActiveSwitchBox;
    Boolean isLoaded;
    Map<AnouncementType, List<Class<? extends IAnouncementActionDescriptor>>> actionDescriptorMap = new HashMap<>();
    boolean showCheckTick = false;
    boolean hasAppInDescription = false;

    protected AnouncementForm(String id, Anouncement inputAnouncement) {
        super(id);
        isLoaded = false;
        selectedActionDescriptor = null;

        if (inputAnouncement.getActionDescriptor() != null && !inputAnouncement.getActionDescriptor().trim().isEmpty())
            showCheckTick = true;

        if (inputAnouncement != null && inputAnouncement.getId() != null) {
            selectedAnouncementType = inputAnouncement.getAnouncementType();
            selectedActionCategoryString = inputAnouncement.getActionCategory();
            selectedActionDescriptor = inputAnouncement.getActionDescriptor();
            isLoaded = true;
        }

        feedbackPanel = new FeedbackPanel("feedbackPanel");
        add(feedbackPanel);
        add(modal);
        form = new Form("form", new CompoundPropertyModel(inputAnouncement));
        List<AnouncementType> anouncementTypes = new ArrayList<>();
        anouncementTypes.add(AnouncementType.PRODUCTLISTTYPE);
        anouncementTypes.add(AnouncementType.VOID);
        String inputAnouncementCategoryStr = checkHasCategory(inputAnouncement);

        createAnouncement = new AjaxFormButton("createAnouncement", form) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {
                AppService.AppSearchCriteria appSearchCriteria = new AppService.AppSearchCriteria();
                if (inputAnouncement != null) {
                    appSearchCriteria = createAppSearchByAnouncement(inputAnouncement);
                }
                hasAppInDescription =true;
                appList = new AppList(modal.getContentId(), appSearchCriteria, Arrays.asList(new ComponentKey("publishStates")), SelectionMode.WithoutAdd);
                appList.setParentListner(AnouncementForm.this);
                appList.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
                modal.setContent(appList);
                target.appendJavaScript("$('.modal-body').css('overflow-x', 'hidden')");
                modal.show(target);
            }
        };
        createAnouncement.setVisible(false);
        createAnouncement.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        if (inputAnouncementCategoryStr != null && inputAnouncementCategoryStr.equals(AppSearchAnouncementActionDescriptor.class.getCanonicalName())) {
            createAnouncement.setVisible(true);
        }
        form.add(createAnouncement);

        List<Class<? extends IAnouncementActionDescriptor>> actionCategoryList = new ArrayList<>();

        anouncementTypeDropDownChoice = new MyDropDownChoicePanel("anouncementType", anouncementTypes, false, false, getString("Anouncement.type"), 3, true, new ChoiceRenderer<>()) {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.appendJavaScript("var myElement = $(\"td[jid='checkTickId']\");" +
                        " myElement.find('i').hide();");

                AnouncementType selectedItems = (AnouncementType) getSelectedItem();

                actionDescriptorMap.clear();
                actionCategoryList.clear();
                createAnouncement.setVisible(false);
                anouncementCategoryDropDownChoice.setEnabled(false);
                appList = null;

                if (selectedItems != null && selectedItems.equals(AnouncementType.PRODUCTLISTTYPE)) {
                    actionCategoryList.add(AppSearchAnouncementActionDescriptor.class);
                    actionDescriptorMap.put(selectedItems, actionCategoryList);
                    anouncementCategoryDropDownChoice.setEnabled(true);
                }
                selectedAnouncementType = selectedItems;
                target.add(anouncementCategoryDropDownChoice);
                target.add(createAnouncement);
            }
        };
        anouncementTypeDropDownChoice.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        anouncementTypeDropDownChoice.setLabel(new ResourceModel("Anouncement.type"));
        form.add(anouncementTypeDropDownChoice);

        if (inputAnouncement != null && inputAnouncement.getActionCategory() != null) {
            String categoryActionClassName = inputAnouncement.getActionCategory();
            try {
                Class anouncementActionDescriptorClass = Class.forName(categoryActionClassName);
                actionCategoryList.add(anouncementActionDescriptorClass);
            } catch (ClassNotFoundException e) {
            }
        }
        anouncementCategoryDropDownChoice =
                new MyDropDownChoicePanel("actionCategory", actionCategoryList, false, false, getString("Anouncement.actionCategory"), 3, true, new ChoiceRenderer() {
                    @Override
                    public Object getDisplayValue(Object o) {
                        if (o != null) {
                            return ANOUNCEMENTMAP.get(o);
                        }
                        return null;
                    }

                    @Override
                    public String getIdValue(Object o, int i) {
                        return o.getClass().getCanonicalName();
                    }
                }) {
                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        target.appendJavaScript("var myElement = $(\"td[jid='checkTickId']\");" +
                                " myElement.find('i').hide();");

                        Object actionCategoryObj = getSelectedItem();
                        String selectedActionSting = null;
                        if (actionCategoryObj != null) {
                            selectedActionSting = (((Class) actionCategoryObj).getCanonicalName());
                        }
                        createAnouncement.setVisible(false);
                        appList = null;
                        if (selectedActionSting != null && !selectedActionSting.trim().equals("") && selectedActionSting.equals(AppSearchAnouncementActionDescriptor.class.getCanonicalName())) {
                            createAnouncement.setVisible(true);
                            selectedActionCategoryString = selectedActionSting;
                        }
                        target.add(createAnouncement);
                    }
                };
        if (selectedAnouncementType != null && selectedAnouncementType.equals(AnouncementType.VOID)) {
            selectedActionCategoryString = null;
            anouncementCategoryDropDownChoice.setModel(null);
            showCheckTick = false;
        }
        anouncementCategoryDropDownChoice.setEnabled(false);
        anouncementCategoryDropDownChoice.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        anouncementCategoryDropDownChoice.setLabel(new ResourceModel("Anouncement.actionCategory"));
        if (inputAnouncement != null && inputAnouncement.getActionCategory() != null) {
            selectedActionCategoryString = inputAnouncement.getActionCategory();
            try {
                Class<? extends AppSearchAnouncementActionDescriptor> selectedClass = (Class<? extends AppSearchAnouncementActionDescriptor>) Class.forName(selectedActionCategoryString);
                anouncementCategoryDropDownChoice.setModel(new Model(selectedClass));

            } catch (ClassNotFoundException e) {
            }
        }

        if (inputAnouncement != null && inputAnouncement.getAnouncementType() != null && inputAnouncement.getAnouncementType().equals(AnouncementType.PRODUCTLISTTYPE)) {
            if (inputAnouncementCategoryStr != null && inputAnouncementCategoryStr.equals(AppSearchAnouncementActionDescriptor.class.getCanonicalName())) {
                anouncementCategoryDropDownChoice.setEnabled(true);
            }
        }
        form.add(anouncementCategoryDropDownChoice);

        anouncementText  = new LimitedTextField("anouncementText",null , false, false,true,true,40 , getString("Anouncement.text"));        anouncementText.setLabel(new ResourceModel("Anouncement.text"));
        anouncementText.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        form.add(anouncementText);

        startDateTimePanel = new DateTimePanel("startDateTime", DateType.DateTime, HourMeridianType._24HOUR);
        startDateTimePanel.setLabel(new ResourceModel("Anouncement.startDateTime"));
        startDateTimePanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        if (isLoaded && inputAnouncement.getStartDateTime() != null) {
            DateTime persianDateTime = MyCalendarUtil.toPersian(inputAnouncement.getStartDateTime());

            startDateTimePanel.setModel(new Model(persianDateTime));
        }
        form.add(startDateTimePanel);

        expireDateTimePanel = new DateTimePanel("expireDateTime", DateType.DateTime, HourMeridianType._24HOUR);
        expireDateTimePanel.setLabel(new ResourceModel("Anouncement.expireDateTime"));
        expireDateTimePanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        if (isLoaded && inputAnouncement.getExpireDateTime() != null) {
            DateTime persianDateTime = MyCalendarUtil.toPersian(inputAnouncement.getExpireDateTime());

            expireDateTimePanel.setModel(new Model(persianDateTime));
        }
        form.add(expireDateTimePanel);

        List<IUploadFilter> imageFilters = new ArrayList<>();
        imageFilters.add(IUploadFilter.getImageUploadFilter());

        anouncementImagePanel = new MultiAjaxFileUploadPanel2("anouncementImage", imageFilters, 1, false, getString("Anouncement.actionImage"));
        anouncementImagePanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        anouncementImagePanel.setLabel(new ResourceModel("Anouncement.actionImage"));
        anouncementImagePanel.setModel(new Model<>());
        form.add(anouncementImagePanel);

        Boolean hasDefaultState = null;
        if (inputAnouncement != null && inputAnouncement.getActive() != null) {
            hasDefaultState = inputAnouncement.getActive();
        }

        isActiveSwitchBox = new SwitchBox(hasDefaultState, "isActive", getString("label.yes"), getString("label.no"));
        isActiveSwitchBox.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        isActiveSwitchBox.setLabel(new ResourceModel("label.activation.verb"));
        isActiveSwitchBox.setModel(new Model<>());
        form.add(isActiveSwitchBox);
        String imageFileKey = null;
        if (inputAnouncement != null) {
            imageFileKey = inputAnouncement.getAnouncementImageFileKey();
        } else {
            imageFileKey = null;
        }

        String finalImageFileKey = imageFileKey;
        tmpImage = new NonCachingImage("tmpImage", new DynamicImageResource() {
            @Override
            protected byte[] getImageData(IResource.Attributes attributes) {
                if (finalImageFileKey != null) {
                    return AppUtils.getImageAsBytes(FileServerService.FILE_DOWNLOAD_SERVER_PATH.replace("${key}", finalImageFileKey));
                } else {
                    return null;
                }
            }
        });

        tmpImage.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        tmpImage.setVisible(true);
        if (tmpImage == null || inputAnouncement == null || inputAnouncement.getId() == null) {
            tmpImage.setVisible(false);
        }

        form.add(tmpImage);
        List<OSType> allOSTypes = OSTypeService.Instance.getEnabledOSTypes();
        osTypeDropDown =
                new MyDropDownChoicePanel("osTypes", allOSTypes, true, false, getString("OS.osType"), 3, false, new ChoiceRenderer());
        osTypeDropDown.setLabel(new ResourceModel("OS.osType"));

        osTypeDropDown.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(osTypeDropDown);


        List<Organization> allOrganization = OrgService.Instance.listAll();
        organizationsMyDropDown =
                new MyDropDownChoicePanel("organizations", allOrganization, true, false, getString("Anouncement.organizations"), 3);
        organizationsMyDropDown.setLabel(new ResourceModel("Anouncement.organizations"));

        organizationsMyDropDown.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(organizationsMyDropDown);

        List<OSEnvironment> allOsEnvironment = EnvironmentService.Instance.listAll();

        osEnvironmentMyDropDown =
                new MyDropDownChoicePanel("osEnvironments", allOsEnvironment, true, false, getString("Anouncement.environment"), 3);
        osEnvironmentMyDropDown.setLabel(new ResourceModel("Anouncement.environment"));

        osEnvironmentMyDropDown.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(osEnvironmentMyDropDown);

        form.add(new AjaxLink("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                childFinished(target, null, this);
            }
        });



        form.add(new AjaxFormButton("saveAnouncement", form) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {

                Collection<UploadedFileInfo> appCatIconFileList = (Collection<UploadedFileInfo>) anouncementImagePanel.getConvertedInput();
                Iterator<UploadedFileInfo> uploadedFileInfoIterator;
                List<OSType> selectedOsTypeList = null;
                List<Organization> selectedOrganizationList = null;
                List<OSEnvironment> selectedEnvironmentList = null;

                if (selectedAnouncementType == null) {
                    target.appendJavaScript("showMessage('" + getString("Required").replace("${label}", new ResourceModel("Anouncement.type").getObject()) + "');");
                    return;
                }


                if (appCatIconFileList != null && appCatIconFileList.size() > 0) {
                    uploadedFileInfoIterator = appCatIconFileList.iterator();
                } else {
                    uploadedFileInfoIterator = null;
                }

                try {
                Session session = HibernateUtil.getCurrentSession();

                    Anouncement anouncement = null;

                    if (inputAnouncement == null || inputAnouncement.getId() == null) {
                        anouncement = new Anouncement();
                    } else {
                        anouncement = (Anouncement) session.get(Anouncement.class, inputAnouncement.getId());
                    }
                    String validationString = "";
                    if (anouncementTypeDropDownChoice == null || anouncementTypeDropDownChoice.getConvertedInput() == null) {
                        validationString += " - " +
                                getString("Required").replace("${label}", anouncementTypeDropDownChoice.getLabel().getObject()) + "<br/>";
                    } else if (selectedAnouncementType != null && selectedAnouncementType.equals(AnouncementType.PRODUCTLISTTYPE)) {
                        if (anouncementCategoryDropDownChoice == null || anouncementCategoryDropDownChoice.getConvertedInput() == null) {
                            validationString += " - " +
                                    getString("Required").replace("${label}", anouncementCategoryDropDownChoice.getLabel().getObject()) + "<br/>";
                        } else {
                            Object selectedActionObj = anouncementCategoryDropDownChoice.getConvertedInput();
                            String selectedActionSting = ((Class) selectedActionObj).getCanonicalName();
                            if (selectedActionSting != null && !selectedActionSting.trim().equals("") && selectedActionSting.equals(AppSearchAnouncementActionDescriptor.class.getCanonicalName())) {
                                if (appList == null && (selectedActionDescriptor == null)) {
                                    validationString += " - " +
                                            getString("Required").replace("${label}", getString("Anouncement.description")) + "<br/>";
                                } else {
                                    String actionDescriptor = appList != null ? appList.reportCriteria() : selectedActionDescriptor;
                                    if (actionDescriptor == null || actionDescriptor.trim().equals("")) {
                                        validationString += " - " +
                                                getString("Required").replace("${label}", getString("Anouncement.description")) + "<br/>";
                                    }
                                    anouncement.setActionDescriptor(actionDescriptor);
                                }
                            }
                        }
                    }
                    if (anouncementText == null || anouncementText.getConvertedInput() == null || anouncementText.getConvertedInput().toString().trim().equals("")) {
                        validationString += " - " +
                                getString("Required").replace("${label}", anouncementText.getLabel().getObject()) + "<br/>";
                    }
                    if (startDateTimePanel == null || startDateTimePanel.getConvertedInput() == null) {
                        validationString += " - " +
                                getString("Required").replace("${label}", startDateTimePanel.getLabel().getObject()) + "<br/>";
                    }
                    if (expireDateTimePanel == null || expireDateTimePanel.getConvertedInput() == null) {
                        validationString += " - " +
                                getString("Required").replace("${label}", expireDateTimePanel.getLabel().getObject()) + "<br/>";
                    }

                    if (osTypeDropDown == null || osTypeDropDown.getConvertedInput() == null) {
                        validationString += " - " +
                                getString("Required").replace("${label}", osTypeDropDown.getLabel().getObject()) + "<br/>";
                    } else {
                        selectedOsTypeList = (List<OSType>) osTypeDropDown.getConvertedInput();
                        if (selectedOsTypeList == null || selectedOsTypeList.isEmpty()) {
                            validationString += " - " +
                                    getString("Required").replace("${label}", osTypeDropDown.getLabel().getObject()) + "<br/>";
                        }
                    }


                    if (organizationsMyDropDown == null || organizationsMyDropDown.getConvertedInput() == null) {
                        validationString += " - " +
                                getString("Required").replace("${label}", organizationsMyDropDown.getLabel().getObject()) + "<br/>";
                    } else {
                        selectedOrganizationList = (List<Organization>) organizationsMyDropDown.getConvertedInput();
                        if (selectedOrganizationList == null || selectedOrganizationList.isEmpty()) {
                            validationString += " - " +
                                    getString("Required").replace("${label}", organizationsMyDropDown.getLabel().getObject()) + "<br/>";
                        }
                    }


                    if (osEnvironmentMyDropDown == null || osEnvironmentMyDropDown.getConvertedInput() == null) {
                        validationString += " - " +
                                getString("Required").replace("${label}", osEnvironmentMyDropDown.getLabel().getObject()) + "<br/>";
                    } else {
                        selectedEnvironmentList = (List<OSEnvironment>) osEnvironmentMyDropDown.getConvertedInput();
                        if (selectedEnvironmentList == null || selectedEnvironmentList.isEmpty()) {
                            validationString += " - " +
                                    getString("Required").replace("${label}", osEnvironmentMyDropDown.getLabel().getObject()) + "<br/>";
                        }
                    }

                    if (startDateTimePanel != null && startDateTimePanel.getConvertedInput() != null && expireDateTimePanel != null && expireDateTimePanel.getConvertedInput() != null) {

                        DateTime startDateTime = (DateTime) startDateTimePanel.getConvertedInput();
                        DateTime expireDateTime = (DateTime) expireDateTimePanel.getConvertedInput();
                        DateTime minDateTime = new DateTime(DayDate.MIN_DAY_DATE, DayTime.MIN_DAY_TIME);
                        if (startDateTime.compareTo(minDateTime) == 0) {
                            validationString += " - " +
                                    getString("Required").replace("${label}", startDateTimePanel.getLabel().getObject()) + "<br/>";
                        }
                        if (expireDateTime.compareTo(minDateTime) == 0) {
                            validationString += " - " +
                                    getString("Required").replace("${label}", expireDateTimePanel.getLabel().getObject()) + "<br/>";
                        } else if (expireDateTime.getDayDate().compareTo(DateTime.now().getDayDate()) < 0) {
                            validationString += " - " +
                                    getString("error.dayAndTime.validation.less").replace("${first}", expireDateTimePanel.getLabel().getObject()).replace("${second}", getString("label.current.date")) + "<br/>";
                        } else if (expireDateTime.compareTo(startDateTime) < 0 && startDateTime.compareTo(minDateTime) != 0) {
                            validationString += " - " +
                                    getString("error.dayAndTime.validation.less").replace("${first}", expireDateTimePanel.getLabel().getObject()).replace("${second}", startDateTimePanel.getLabel().getObject()) + "<br/>";
                        }
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
                        if (!FileServerService.Instance.doesFileExistOnFileServer(appCatIconFile.getFileId()))
                            FileServerService.Instance.persistFileToServer(appCatIconFile.getFileId());
                        anouncement.setAnouncementImageFileKey(appCatIconFile.getFileId());
                    } else if (!isLoaded) {
                        validationString += " - " +
                                getString("Required").replace("${label}", getString("Anouncement.actionImage")) + "<br/>";
                    }

                    if (anouncementText != null && anouncementText.getValidatorString() != null && !anouncementText.getValidatorString().isEmpty()) {
                        for (String validationStringInList : anouncementText.getValidatorString()) {
                            validationString += " - " +
                                    validationStringInList + "<br/>";
                        }
                    }

                    if (!validationString.isEmpty()) {
                        target.appendJavaScript("showMessage('" + validationString + "');");
                        return;
                    }

                    Object selectedActionObj = anouncementCategoryDropDownChoice.getConvertedInput();
                    String selectedActionSting = null;
                    if (selectedActionObj != null) {
                        selectedActionSting = (((Class) selectedActionObj).getCanonicalName());
                    }

                    DateTime startDateTime = (DateTime) startDateTimePanel.getConvertedInput();
                    DateTime expireDateTime = (DateTime) expireDateTimePanel.getConvertedInput();

                    if (selectedAnouncementType != null && !selectedAnouncementType.equals(AnouncementType.VOID)) {
                        if (selectedAnouncementType.equals(AnouncementType.PRODUCTLISTTYPE) && selectedActionSting != null) {
                            anouncement.setActionCategory(selectedActionSting);
                        }
                    } else {
                        anouncement.setActionCategory(null);
                        showCheckTick = false;
                    }
                    boolean isActive = isActiveSwitchBox == null ? false : (boolean) isActiveSwitchBox.getConvertedInput();
                    anouncement.setActive(isActive);
                    anouncement.setAnouncementText((String) anouncementText.getConvertedInput());
                    anouncement.setAnouncementType(selectedAnouncementType);
                    anouncement.setOsTypes(selectedOsTypeList);
                    anouncement.setOrganizations(selectedOrganizationList);
                    anouncement.setOsEnvironments(selectedEnvironmentList);
                    anouncement.setStartDateTime(startDateTime);
                    anouncement.setExpireDateTime(expireDateTime);
                    Transaction tx = null;
                    try {
                        tx = session.beginTransaction();

                        AnouncementService.Instance.saveOrUpdate(anouncement, session);
                        tx.commit();

                        target.appendJavaScript("showMessage('" + (getString("label.saveSuccessfully")) + "');");

                        form.setModelObject(new Anouncement());
                        target.add(form);
                        if (getParentListner() != null) {
                        childFinished(target, new Model<>(), this);
                        }
                    } catch (Exception ex) {
                        if (tx != null)
                            tx.rollback();

                        processException(target, ex);
                    } finally {
                        if (session.isOpen())
                            session.close();
                        return;

                    }

                } catch (Exception e) {
                    processException(target, e);
                }
            }
        });

        add(form);
    }

    private AppService.AppSearchCriteria createAppSearchByAnouncement(Anouncement anouncement) {
        AppService.AppSearchCriteria appSearchCriteria = new AppService.AppSearchCriteria();
        Session session = HibernateUtil.getCurrentSession();

        if (anouncement != null && anouncement.getId() != null) {
            Anouncement loadedAnouncement = (Anouncement) session.get(Anouncement.class, anouncement.getId());
            String actionDescriptor = loadedAnouncement.getActionDescriptor();
            if (actionDescriptor != null && !actionDescriptor.trim().equals("")) {
                JSONObject jsonObject = new JSONObject(actionDescriptor);
                if (jsonObject.has("keyWords") && jsonObject.get("keyWords") != null && jsonObject.get("keyWords") instanceof JSONArray) {
                    JSONArray keyWord = jsonObject.getJSONArray("keyWords");
                    List<String> keyWordString = new ArrayList<>();
                    for (Object object : keyWord) {
                        keyWordString.add(object.toString().trim());
                    }
                    appSearchCriteria.keyword = keyWordString;
                }
                if (jsonObject.has("appCategoryIDList") && jsonObject.get("appCategoryIDList") != null
                        && jsonObject.get("appCategoryIDList") instanceof JSONArray) {
                    JSONArray appCategoryIDList = jsonObject.getJSONArray("appCategoryIDList");
                    List<Long> appCategoryIds = new ArrayList<>();
                    Collection<AppCategory> appCategoryArrayList = new ArrayList<>();
                    for (Object object : appCategoryIDList) {
                        AppCategory appCategory = (AppCategory) session.get(AppCategory.class, Long.valueOf(object.toString()));
                        appCategoryIds.add(appCategory.getId());
                        appCategoryArrayList.add(appCategory);
                    }
                    appSearchCriteria.appCategoryId = appCategoryIds;
                    appSearchCriteria.appCategory = appCategoryArrayList;
                }
                if (jsonObject.has("developerIDList") && jsonObject.get("developerIDList") != null && jsonObject.get("developerIDList") instanceof JSONArray) {
                    JSONArray developerList = jsonObject.getJSONArray("developerIDList");
                    Collection<User> developerUserCollection = new ArrayList<>();
                    List<String> developerUserName = new ArrayList<>();
                    for (Object object : developerList) {
                        User user = (User) session.get(User.class, Long.valueOf(object.toString()));
                        developerUserCollection.add(user);
                        developerUserName.add(user.getUserName());
                    }
                    appSearchCriteria.developerName = developerUserName;
                    appSearchCriteria.developers = developerUserCollection;
                }
                if (jsonObject.has("creatorIDList") && jsonObject.get("creatorIDList") != null && jsonObject.get("creatorIDList") instanceof JSONArray) {
                    JSONArray creatorList = jsonObject.getJSONArray("creatorIDList");
                    Collection<User> creatorUserCollection = new ArrayList<>();
                    List<String> creatorUserName = new ArrayList<>();
                    for (Object object : creatorList) {
                        User user = (User) session.get(User.class, Long.valueOf(object.toString()));
                        creatorUserCollection.add(user);
                        creatorUserName.add(user.getUserName());
                    }
                    appSearchCriteria.creatorUserName = creatorUserName;
                    appSearchCriteria.creatorUsers = creatorUserCollection;
                }
                if (jsonObject.has("osTypeIDList") && jsonObject.get("osTypeIDList") != null && jsonObject.get("osTypeIDList") instanceof JSONArray) {
                    JSONArray osTypeIdList = jsonObject.getJSONArray("osTypeIDList");
                    Collection<OSType> osTypeCollection = new ArrayList<>();
                    for (Object object : osTypeIdList) {
                        OSType osType = (OSType) session.get(OSType.class, Long.valueOf(object.toString()));
                        osTypeCollection.add(osType);
                    }
                    appSearchCriteria.osType = osTypeCollection;
                }
                if (jsonObject.has("osIDList") && jsonObject.get("osIDList") != null && jsonObject.get("osIDList") instanceof JSONArray) {
                    JSONArray osIDList = jsonObject.getJSONArray("osIDList");
                    Collection<OS> osCollection = new ArrayList<>();
                    List<String> osNames = new ArrayList<>();
                    for (Object object : osIDList) {
                        OS os = (OS) session.get(OS.class, Long.valueOf(object.toString()));
                        osCollection.add(os);
                        osNames.add(os.getOsName());
                    }
                    appSearchCriteria.os = osCollection;
                    appSearchCriteria.osName = osNames;
                }
//                if (jsonObject.has("bytePublishStates") && jsonObject.get("bytePublishStates") != null && jsonObject.get("bytePublishStates") instanceof JSONArray) {
//                    JSONArray publishStateList = jsonObject.getJSONArray("bytePublishStates");
//                    Collection<PublishState> publishStateCollection = new ArrayList<>();
//                    for (Object object : publishStateList) {
//                        PublishState publishState = new PublishState(Byte.valueOf(object.toString()));
//                        publishStateCollection.add(publishState);
//                    }
//                    appSearchCriteria.publishStates = publishStateCollection;
//                }
                appSearchCriteria.publishStates = new ArrayList<>();
                appSearchCriteria.publishStates.add(PublishState.PUBLISHED);

                if (jsonObject.has("creationDateTime") && jsonObject.get("creationDateTime") != null && jsonObject.get("creationDateTime") instanceof JSONArray) {
                    JSONArray creationDateTime = jsonObject.getJSONArray("creationDateTime");
                    DateTime[] creationDateTimeArray = new DateTime[0];
                    DateTime minDateTime =null;
                    DateTime maxDateTime = null;

                    if (creationDateTime.length() != 0) {

                        if (creationDateTime.get(0)!=null&& !creationDateTime.get(0).equals(null) ) {
                        Date minDate = new Date(Long.valueOf(creationDateTime.get(0).toString()));
                            minDateTime = new DateTime(minDate);
                            creationDateTimeArray = new DateTime[]{minDateTime, maxDateTime};
                        }

                        if (creationDateTime.get(1)!=null&& !creationDateTime.get(1).equals(null) ) {
                        Date maxDate = new Date(Long.valueOf(creationDateTime.get(1).toString()));
                            maxDateTime = new DateTime(maxDate);
                        creationDateTimeArray = new DateTime[]{minDateTime, maxDateTime};
                    }
                    appSearchCriteria.creationDateTime = creationDateTimeArray;


                    }

                }
                if (jsonObject.has("packageName") && jsonObject.get("packageName") != null && jsonObject.get("packageName") instanceof String) {
                    String packageName = jsonObject.getString("packageName");
                    appSearchCriteria.packageName = packageName;
                }
                if (jsonObject.has("title") && jsonObject.get("title") != null && jsonObject.get("title") instanceof String) {
                    String title = jsonObject.getString("title");
                    appSearchCriteria.title = title;
                }
                if (jsonObject.has("versionName") && jsonObject.get("versionName") != null && jsonObject.get("versionName") instanceof String) {
                    String versionName = jsonObject.getString("versionName");
                    appSearchCriteria.versionName = versionName;
                }
                if (jsonObject.has("versionCode") && jsonObject.get("versionCode") != null && jsonObject.get("versionCode") instanceof String) {
                    String versionCode = jsonObject.getString("versionCode");
                    appSearchCriteria.versionCode = versionCode;
                }
            }
        }
        return appSearchCriteria;
    }

    private String checkHasCategory(Anouncement anouncement) {
        if (anouncement != null && anouncement.getActionCategory() != null) {
            return anouncement.getActionCategory();
        }
        return null;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        if (showCheckTick) {
            response.render(OnDomReadyHeaderItem.forScript(
                    "var myElement = $(\"td[jid='checkTickId']\");" +
                            "    myElement.find('i').show();"));
        }else {
            response.render(OnDomReadyHeaderItem.forScript(
                    "var myElement = $(\"td[jid='checkTickId']\");" +
                            "    myElement.find('i').hide();"));
        }
    }

    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {
        if (eventThrownCmp != null && eventThrownCmp.getId().equals("createAnouncement")){

            if (getParentListner() != null || hasAppInDescription) {

            target.appendJavaScript("var myElement = $(\"td[jid='checkTickId']\");" +
                    "    myElement.find('i').show();"
            );
        }
        }
    }
}
