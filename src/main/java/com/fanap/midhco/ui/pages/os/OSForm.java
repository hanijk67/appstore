package com.fanap.midhco.ui.pages.os;

import com.fanap.midhco.appstore.entities.HandlerApp;
import com.fanap.midhco.appstore.entities.OS;
import com.fanap.midhco.appstore.entities.OSType;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.handlerApp.HandlerAppService;
import com.fanap.midhco.appstore.service.os.OSService;
import com.fanap.midhco.appstore.service.osType.OSTypeService;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.ajaxLinkPanel.AjaxLinkPanel;
import com.fanap.midhco.ui.component.limitedTextField.LimitedTextField;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.component.mydropdown.MyDropDownChoicePanel;
import com.fanap.midhco.ui.component.switchbox.SwitchBox;
import com.fanap.midhco.ui.component.table.MyAjaxDataTable;
import com.fanap.midhco.ui.component.table.column.IndexColumn;
import com.fanap.midhco.ui.pages.handlerApp.HandlerAppForm;
import com.fanap.midhco.ui.pages.handlerApp.HandlerAppList;
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
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by admin123 on 6/29/2016.
 */
@Authorize(views = {Access.OS_ADD, Access.OS_EDIT})
public class OSForm extends BasePanel implements IParentListner {
    HandlerAppSortableDataProvider dp = new HandlerAppSortableDataProvider();
    Form form;
    FeedbackPanel feedbackPanel;
    Label selectedFileHandlerAppPathTitle;
    BootStrapModal modalWindow;
    MyAjaxDataTable table;
    Long osId;
    Label handlerAppLabel;
    SwitchBox disabledSwitchBox;
    LimitedTextField nameTextField;
    LimitedTextField osCodeTextField;
    LimitedTextField osVersionTextField;
    MyDropDownChoicePanel osTypeDropDown;
    final HandlerAppService.HandlerAppCriteria handlerAppCriteria;
    AjaxLink addAjaxLink;
    AjaxLink listLauncherAjaxLink;

    public OSForm(String id, final OS os) {
        super(id);
        osId = (os != null && os.getId() != null) ? os.getId() : null;
        feedbackPanel = new FeedbackPanel("feedbackPabel");
        feedbackPanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(feedbackPanel);

        modalWindow = new BootStrapModal("modal");
        add(modalWindow);
        handlerAppCriteria = new HandlerAppService.HandlerAppCriteria();
        form = new Form("form", new CompoundPropertyModel(os));
        form.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        Boolean hasDefaultState = null;
        if (os != null && os.getDisabled() != null) {
            hasDefaultState = os.getDisabled();
        }
        nameTextField = new LimitedTextField("osName", true, false, false, false, false, 40, getString("OS.osName"));
        nameTextField.setRequired(true);
        nameTextField.setLabel(new ResourceModel("OS.osName"));
        form.add(nameTextField);

        osCodeTextField = new LimitedTextField("osCode", true, null, false, false, false, 40, getString("OS.osCode"));

        osCodeTextField.setRequired(true);
        osCodeTextField.setLabel(new ResourceModel("OS.osCode"));
        form.add(osCodeTextField);

        osVersionTextField = new LimitedTextField("osVersion", true, null, true, false, false, 40, getString("OS.osVersion"));

        osVersionTextField.setRequired(true);
        osVersionTextField.setLabel(new ResourceModel("OS.osVersion"));
        form.add(osVersionTextField);


        handlerAppLabel = new Label("handlerAppLable", new Model<>(AppStorePropertyReader.getString("os.handler")));
        handlerAppLabel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(handlerAppLabel);

        disabledSwitchBox = new SwitchBox(hasDefaultState, "disable", getString("label.yes"), getString("label.no"), null, true) {
            @Override
            protected void onChange(AjaxRequestTarget target, Boolean currentState) {
                boolean deActiveExistedOs = currentState;

                listLauncherAjaxLink.setVisible(!deActiveExistedOs);
                listLauncherAjaxLink.setEnabled(!deActiveExistedOs);
                if (PrincipalUtil.hasPermission(Access.HANDLERAPP_LIST)) {
                    listLauncherAjaxLink.setEnabled(!deActiveExistedOs);
                    listLauncherAjaxLink.setVisible(!deActiveExistedOs);
                }

                table.setVisible(!deActiveExistedOs);
                table.setEnabled(!deActiveExistedOs);
                if (PrincipalUtil.hasPermission(Access.HANDLERAPP_LIST)) {
                    table.setEnabled(!deActiveExistedOs);
                    table.setVisible(!deActiveExistedOs);
                }
                addAjaxLink.setVisible(!deActiveExistedOs);
                addAjaxLink.setEnabled(!deActiveExistedOs);
                if (PrincipalUtil.hasPermission(Access.HANDLERAPP_ADD)) {
                    addAjaxLink.setEnabled(!deActiveExistedOs);
                    addAjaxLink.setVisible(!deActiveExistedOs);
                }
                addAjaxLink.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
                listLauncherAjaxLink.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
                table.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
                target.add(addAjaxLink);
                target.add(listLauncherAjaxLink);
                target.add(table);
            }
        };

        disabledSwitchBox.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        disabledSwitchBox.setLabel(new ResourceModel("label.disable.verb"));
        disabledSwitchBox.setModel(new Model<>());
        form.add(disabledSwitchBox);


        HandlerAppService.HandlerAppCriteria criteria = new HandlerAppService.HandlerAppCriteria();
        List<Long> osIdList = new ArrayList<>();
        osIdList.add(osId);
        if (osId == null) {
            osIdList.add(-1L);
        } else {
            osIdList.add(osId);
        }
        criteria.setOsIds(osIdList);
        dp.setCriteria(criteria);

        table = new MyAjaxDataTable("table", getColumns(), dp, 10);
        table.setSelectionMode(SelectionMode.None);
        table.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        if (osId == null || !PrincipalUtil.hasPermission(Access.HANDLERAPP_LIST)) {
            table.setVisible(false);
            table.setEnabled(false);
            handlerAppLabel.setVisible(false);
            handlerAppLabel.setEnabled(false);
        }
        form.add(table);
        List<OSType> allOSTypes = OSTypeService.Instance.getEnabledOSTypes();
        osTypeDropDown =
                new MyDropDownChoicePanel("osType", allOSTypes, false, false, getString("OS.osType"), 3, true, new ChoiceRenderer()) {
                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {

                        OSType osType = (OSType) getSelectedItem();
                        if (osType != null && !osType.getDisabled()) {
                            OSService.OSCriteria osCriteria = new OSService.OSCriteria();
                            List<OSType> osTypeList = new ArrayList<>();
                            osTypeList.add(osType);
                            osCriteria.setOsType(osTypeList);
                            osCriteria.setDisabled(false);
                            Session session = HibernateUtil.getNewSession();
                            List<OS> osList = OSService.Instance.list(osCriteria, 0, -1, null, false, session);
                            session.close();
                            List<Long> osIds = new ArrayList<>();

                            if (osList != null && !osList.isEmpty()) {
                                for (OS osInList : osList) {
                                    osIds.add(osInList.getId());
                                }
                            }

                            handlerAppCriteria.setOsIds(osIds);
                            listLauncherAjaxLink.setEnabled(true);
                        } else {
                            listLauncherAjaxLink.setEnabled(false);
                        }
                        target.add(listLauncherAjaxLink);

                    }
                };
        osTypeDropDown.setLabel(new ResourceModel("OS.osType"));
        osTypeDropDown.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        osTypeDropDown.setRequired(true);
        form.add(osTypeDropDown);

        form.add(new AbstractFormValidator() {
            @Override
            public FormComponent<?>[] getDependentFormComponents() {
                return new FormComponent<?>[0];
            }

            @Override
            public void validate(Form<?> form) {
                if (os.getId() == null) {
                    OSType osType = (OSType) osTypeDropDown.getConvertedInput();
                    if (osType != null && osType.getDisabled() != null && osType.getDisabled())
                        error(osTypeDropDown, "OSType.isDisabled");
                }
            }
        });

        boolean activeExistedOs = false;

        if (os != null && os.getId() != null) {
            Session session = HibernateUtil.getCurrentSession();
            OS loadedOs = (OS) session.load(OS.class, os.getId());
            if (!loadedOs.getDisabled()) {
                OSType osType = loadedOs.getOsType();
                if (!osType.getDisabled()) {
                    activeExistedOs = true;
                }
            }
        }


        addAjaxLink = new AjaxLink("add") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                HandlerApp handlerApp = new HandlerApp();
                HandlerAppForm handlerAppFormForm = new HandlerAppForm(modalWindow.getContentId(), handlerApp, osId);
                handlerAppFormForm.setParentListner(OSForm.this);
                modalWindow.setContent(handlerAppFormForm);
                modalWindow.setTitle(getString("HandlerApp.createForm"));
                modalWindow.show(target);
            }
        };

        if (PrincipalUtil.hasPermission(Access.HANDLERAPP_ADD)) {
            addAjaxLink.setEnabled(activeExistedOs);
            addAjaxLink.setVisible(activeExistedOs);
        } else {
            addAjaxLink.setEnabled(false);
            addAjaxLink.setVisible(false);
        }
        form.add(addAjaxLink);

        listLauncherAjaxLink = new AjaxLink("listLauncher") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                HandlerAppList handlerAppList = new HandlerAppList(modalWindow.getContentId(), handlerAppCriteria, SelectionMode.None, osId);
                handlerAppList.setParentListner(OSForm.this);
                modalWindow.setContent(handlerAppList);

                modalWindow.setTitle(getString("HandlerApp"));
                modalWindow.show(target);
            }
        };

        listLauncherAjaxLink.setEnabled(activeExistedOs);
        if (os != null) {
            OSType osType = os.getOsType();
            if (osType != null && !osType.getDisabled()) {
                listLauncherAjaxLink.setEnabled(true);
            }
        }

        if (PrincipalUtil.hasPermission(Access.HANDLERAPP_LIST)) {
            listLauncherAjaxLink.setVisible(true);
        } else {
            listLauncherAjaxLink.setVisible(false);
        }
        form.add(listLauncherAjaxLink);

        form.add(new AjaxFormButton("save", form, feedbackPanel) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {

                String validationString = "";
                if (nameTextField != null && nameTextField.getValidatorString() != null && !nameTextField.getValidatorString().isEmpty()) {
                    for (String validationStringInList : nameTextField.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }

                if (osVersionTextField != null && osVersionTextField.getValidatorString() != null && !osVersionTextField.getValidatorString().isEmpty()) {
                    for (String validationStringInList : osVersionTextField.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }

                if (osCodeTextField != null && osCodeTextField.getValidatorString() != null && !osCodeTextField.getValidatorString().isEmpty()) {
                    for (String validationStringInList : osCodeTextField.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }

                if (!validationString.isEmpty()) {
                    target.appendJavaScript("showMessage('" + validationString + "');");
                    return;
                }

                Session session = HibernateUtil.getCurrentSession();
                boolean disabled = disabledSwitchBox == null ? false : (boolean) disabledSwitchBox.getConvertedInput();
                OS os = (OS) form.getModelObject();
                OS loadedOs = (os != null && os.getId() != null) ? (OS) session.load(OS.class, os.getId()) : new OS();
                loadedOs.setHandlerApps(os.getHandlerApps());
                loadedOs.setOsName(os.getOsName());
                loadedOs.setOsCode(os.getOsCode());
                loadedOs.setOsType(os.getOsType());
                loadedOs.setOsVersion(os.getOsVersion());
                loadedOs.setDisabled(disabled);

                if (loadedOs.getOsCode() != null && !loadedOs.getOsCode().trim().equals("")) {
                    OSService.OSCriteria osCriteria = new OSService.OSCriteria();
                    osCriteria.setOsCode(loadedOs.getOsCode());
                    List<OS> osList = OSService.Instance.list(osCriteria, 0, -1, null, false, session);
                    if (osList != null && !osList.isEmpty()) {
                        if (loadedOs.getId() == null) {
                            validationString += " - " +
                                    getString("error.os.uniqued.osCode") + "<br/>";
                            target.appendJavaScript("showMessage('" + validationString + "');");
                            return;
                        } else {
                            for (OS osInList : osList) {
                                if (!osInList.getId().equals(loadedOs.getId())) {
                                    validationString += " - " +
                                            getString("error.os.uniqued.osCode") + "<br/>";
                                    target.appendJavaScript("showMessage('" + validationString + "');");
                                    return;
                                }
                            }
                        }
                    }
                }


                Transaction tx = null;

                try {
                    tx = session.beginTransaction();
                    OSService.Instance.saveOrUpdate(loadedOs, session);
                    tx.commit();
                } catch (Exception ex) {
                    if (tx != null && tx.isActive())
                        tx.rollback();
                    processException(target, ex);
                } finally {
                    if (session != null && session.isOpen()) {
                        session.close();
                    }
                }

                childFinished(target, null, this);
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

    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {
        if (eventThrownCmp != null && eventThrownCmp.getId().equals("save"))
            target.add(table);
        modalWindow.close(target);
    }

    public List<IColumn> getColumns() {
        List<IColumn> columnList = new ArrayList<IColumn>();
        columnList.add(new IndexColumn());

        columnList.add(new PropertyColumn(new ResourceModel("HandlerApp.id"), "handlerApp.id", "id"));
        columnList.add(new PropertyColumn(new ResourceModel("HandlerApp.versionCode"), "handlerApp.versionCode", "versionCode"));
        columnList.add(new PropertyColumn(new ResourceModel("HandlerApp.organization"), "handlerApp.organization", "nickName"));
        columnList.add(new PropertyColumn(new ResourceModel("HandlerApp.os.osEnvironment"), "handlerApp.osEnvironment", "osEnvironment"));

        columnList.add(new PropertyColumn(new ResourceModel("HandlerApp.uploadedTime"), "handlerApp.uploadedFileDate", "uploadedFileDate"));

        columnList.add(new AbstractColumn(new Model("")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                if (PrincipalUtil.hasPermission(Access.HANDLERAPP_EDIT)) {
                    HandlerAppService.HandlerAppSearchResultModel searchResultModel = (HandlerAppService.HandlerAppSearchResultModel) rowModel.getObject();
                    cellItem.add(new AjaxLinkPanel(componentId, AjaxLinkPanel.Image.Edit) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            Long handlerAppId = searchResultModel.getId();
                            Session session = HibernateUtil.getCurrentSession();

                            HandlerApp loadedHandlerApp = (HandlerApp) session.load(HandlerApp.class, handlerAppId);
                            HandlerAppForm handlerAppForm = new HandlerAppForm(modalWindow.getContentId(), loadedHandlerApp, osId);
                            handlerAppForm.setParentListner(OSForm.this);
                            modalWindow.setContent(handlerAppForm);
                            modalWindow.setTitle(getString("HandlerApp.editForm"));
                            modalWindow.show(target);

                        }
                    });
                } else {
                    cellItem.add(new Label(componentId, new Model<>("")));
                }
            }
        });

        columnList.add(new AbstractColumn(new ResourceModel("label.activation.verb")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                final HandlerAppService.HandlerAppSearchResultModel handlerAppSearchResultModel = (HandlerAppService.HandlerAppSearchResultModel) rowModel.getObject();
                if (PrincipalUtil.hasPermission(Access.HANDLERAPP_EDIT)) {
                    SwitchBox switchBox = new SwitchBox(componentId, new ResourceModel("label.yes").getObject(), new ResourceModel("label.no").getObject(), true) {
                        @Override
                        protected void onChange(AjaxRequestTarget target, Boolean currentState) {
                            Session session = HibernateUtil.getCurrentSession();
                            Transaction tx = null;

                            try {
                                tx = session.beginTransaction();
                                HandlerApp intendedHandlerApp = (HandlerApp) session.load(HandlerApp.class, handlerAppSearchResultModel.getId());
                                intendedHandlerApp.setActive(currentState);

                                HandlerAppService.Instance.saveOrUpdate(intendedHandlerApp, session);

                                tx.commit();
                            } catch (Exception ex) {
                                processException(target, ex);
                                if (tx != null)
                                    tx.rollback();
                            } finally {
                                session.close();
                            }
                        }
                    };
                    switchBox.setModel(new Model<>(handlerAppSearchResultModel.getActive()));
                    cellItem.add(switchBox);
                } else {
                    cellItem.add(new Label(componentId, new Model<>("")));
                }
            }
        });


        return columnList;
    }


    public static class HandlerAppSortableDataProvider extends SortableDataProvider {
        HandlerAppService.HandlerAppCriteria criteria;

        public HandlerAppSortableDataProvider() {
            setSort("ent.id", SortOrder.ASCENDING);
        }

        public void setCriteria(HandlerAppService.HandlerAppCriteria criteria) {
            this.criteria = criteria;
        }

        @Override
        public Iterator iterator(long first, long count) {
            Session session = HibernateUtil.getCurrentSession();
            SortParam sortParam = getSort();

            //todo correct this
            return HandlerAppService.Instance.list(criteria, (int) first, (int) count, null, sortParam.isAscending(), session).iterator();
        }

        @Override
        public long size() {
            Session session = HibernateUtil.getCurrentSession();
            return HandlerAppService.Instance.count(criteria, session);
        }

        @Override
        public IModel model(Object o) {
            return new Model((Serializable) o);
        }
    }


}

