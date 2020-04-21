package com.fanap.midhco.ui.pages.handlerApp;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.environment.EnvironmentService;
import com.fanap.midhco.appstore.service.handlerApp.HandlerAppService;
import com.fanap.midhco.appstore.service.org.OrgService;
import com.fanap.midhco.appstore.service.os.OSService;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.component.ISelectable;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.ajaxLinkPanel.AjaxLinkPanel;
import com.fanap.midhco.ui.component.limitedTextField.LimitedTextField;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.component.mydropdown.MyDropDownChoicePanel;
import com.fanap.midhco.ui.component.switchbox.SwitchBox;
import com.fanap.midhco.ui.component.table.MyAjaxDataTable;
import com.fanap.midhco.ui.component.table.column.IndexColumn;
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
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.*;

/**
 * Created by A.Moshiri on 5/5/2018.
 */
@Authorize(view = Access.HANDLERAPP_LIST)
public class HandlerAppList extends BasePanel implements IParentListner, ISelectable {


    Form searchForm;
    HandlerAppSortableDataProvider dp = new HandlerAppSortableDataProvider();
    MyAjaxDataTable table;
    BootStrapModal modalWindow = new BootStrapModal("modal");

    MyDropDownChoicePanel organizationDropDown;
    MyDropDownChoicePanel osEnvironmentDropDown;
    MyDropDownChoicePanel isActiveDropDown;
    MyDropDownChoicePanel isDefaultDropDown;
    LimitedTextField versionCodeTextField;
    Long osId = null;
    OS inputOs;
    HandlerAppService.HandlerAppCriteria inputCriteria;

    public HandlerAppList(String id, HandlerAppService.HandlerAppCriteria criteria, SelectionMode selectionMode, Long inputOsId) {
        super(id);

        osId = inputOsId;

        setPageTitle(getString("HandlerApp"));

        add(modalWindow);
        inputCriteria = criteria;
        boolean allFieldsNull = checkAllFieldsNull(criteria);

        searchForm = new Form("searchForm", new CompoundPropertyModel(criteria));
        searchForm.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(searchForm);

        versionCodeTextField = new LimitedTextField("versionCode", true, true, false, false,false, 12, getString("HandlerApp.versionCode"));
        versionCodeTextField.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        searchForm.add(versionCodeTextField);

        List<Organization> organizationList = OrgService.Instance.listAll();
        organizationDropDown =
                new MyDropDownChoicePanel("organization", organizationList, false, true, getString("HandlerApp.organization"), 1);
        organizationDropDown.setLabel(new ResourceModel("HandlerApp.organization"));
        searchForm.add(organizationDropDown);

        List<OSEnvironment> osEnvironmentList = new ArrayList<>();

        Session session = HibernateUtil.getCurrentSession();
        osEnvironmentList = EnvironmentService.Instance.listAll(session);
        if (osId != null) {
            inputOs = OSService.Instance.loadOSByOSId(osId, session);
        } else {
            inputOs = new OS();
        }

//        session.close();
        osEnvironmentDropDown =
                new MyDropDownChoicePanel("osEnvironment", osEnvironmentList, false, true, getString("HandlerApp.os.osEnvironment"), 1);
        osEnvironmentDropDown.setLabel(new ResourceModel("HandlerApp.os.osEnvironment"));
        searchForm.add(osEnvironmentDropDown);


        int rowsPerPage =
                Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.APP_LIST_ROWS_PER_PAGE));
        table = new MyAjaxDataTable("table", getColumns(), dp, rowsPerPage);
        table.setSelectionMode(selectionMode);
        table.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(table);

        isActiveDropDown = new MyDropDownChoicePanel("isActive",
                Arrays.asList(true, false), false, false, getString("HandlerApp.isActive"), 1, false,
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
        searchForm.add(isActiveDropDown);

        isDefaultDropDown = new MyDropDownChoicePanel("isDefault",
                Arrays.asList(true, false), false, false, getString("HandlerApp.isDefault"), 1, false,
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
        searchForm.add(isDefaultDropDown);


        if (allFieldsNull)
            table.setVisible(false);


        add(new AjaxLink("select") {
            public void onClick(AjaxRequestTarget target) {
                childFinished(target, null, null);
            }
        }.setOutputMarkupPlaceholderTag(true).setVisible(false));
        if (selectionMode.isSelectable() && table.getRowCount() > 0)
            get("select").setVisible(true);

        add(new AjaxLink("close") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                childFinished(target, null, this);
            }
        });


        boolean activeExistedOs = false;

        if (inputOsId != null) {
            OS loadedOs = (OS) session.load(OS.class, inputOsId);
            if (!loadedOs.getDisabled()) {
                OSType osType = loadedOs.getOsType();
                if (!osType.getDisabled()) {
                    activeExistedOs = true;
                }
            }
        }

        AjaxLink createAjaxLink = new AjaxLink("create") {
            @Override
            public void onClick(AjaxRequestTarget target) {

                HandlerApp handlerApp = new HandlerApp();
                HandlerAppForm handlerAppForm = new HandlerAppForm(modalWindow.getContentId(), handlerApp, osId);
                handlerAppForm.setParentListner(HandlerAppList.this);
                modalWindow.setContent(handlerAppForm);
                modalWindow.setTitle(getString("HandlerApp.createForm"));
                modalWindow.show(target);
            }
        };
        if (PrincipalUtil.hasPermission(Access.HANDLERAPP_ADD)) {
            createAjaxLink.setEnabled(activeExistedOs);
            createAjaxLink.setVisible(activeExistedOs);
        } else {
            createAjaxLink.setEnabled(false);
            createAjaxLink.setVisible(false);
        }

        add(createAjaxLink);

        searchForm.add(new AjaxFormButton("search", searchForm) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {
                String validationString = "";

                if (versionCodeTextField != null && versionCodeTextField.getValidatorString() != null && !versionCodeTextField.getValidatorString().isEmpty()) {
                    for (String validationStringInList : versionCodeTextField.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }

                if (!validationString.isEmpty()) {
                    target.appendJavaScript("showMessage('" + validationString + "');");
                    return;
                }


                HandlerAppService.HandlerAppCriteria criteria = (HandlerAppService.HandlerAppCriteria) form.getModelObject();
                List<Long> osIdList =null;
                if (inputCriteria!=null && inputCriteria.getOsIds()!=null && !inputCriteria.getOsIds().isEmpty()) {
                    osIdList =inputCriteria.getOsIds();
                }else {
                    osIdList = new ArrayList<>();
                }
                if (osId!=null && !osIdList.contains(osId)) {
                    osIdList.add(osId);
                }
                criteria.setOsIds(osIdList);
                if (criteria != null) {
                    dp.setCriteria(criteria);
                    table.setVisible(true);
                    target.add(table);
                    if (selectionMode.isSelectable())
                        target.add(HandlerAppList.this.get("select").setVisible(true));
                }
            }
        });

        searchForm.add(new AjaxLink("reset") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                searchForm.setModel(new CompoundPropertyModel(new HandlerAppService.HandlerAppCriteria()));
                target.add(searchForm);
                table.setVisible(false);
                target.add(table);
                HandlerAppList.this.get("select").setVisible(false);
                target.add(HandlerAppList.this.get("select"));
                target.add(HandlerAppList.this.get("select"));
            }
        });
    }

    private List<IColumn> getColumns() {
        List<IColumn> columnList = new ArrayList<IColumn>();
        columnList.add(new IndexColumn());

        columnList.add(new PropertyColumn(new ResourceModel("HandlerApp.id"), "handlerApp.id", "id"));
        columnList.add(new PropertyColumn(new ResourceModel("HandlerApp.versionCode"), "handlerApp.versionCode", "versionCode"));
        columnList.add(new PropertyColumn(new ResourceModel("HandlerApp.organization"), "handlerApp.organization", "nickName"));
        columnList.add(new PropertyColumn(new ResourceModel("HandlerApp.os.osEnvironment"), "handlerApp.osEnvironment", "osEnvironment.envName"));

        columnList.add(new AbstractColumn(new ResourceModel("HandlerApp.isDefault")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                final HandlerAppService.HandlerAppSearchResultModel handlerAppSearchResultModel = (HandlerAppService.HandlerAppSearchResultModel) rowModel.getObject();
                if (PrincipalUtil.hasPermission(Access.HANDLERAPP_EDIT)) {

                    SwitchBox switchBox = new SwitchBox(componentId, new ResourceModel("label.yes").getObject(), new ResourceModel("label.no").getObject(), true) {
                        @Override
                        protected void onChange(AjaxRequestTarget target, Boolean currentState) {
                            Session session = HibernateUtil.getCurrentSession();
                            Transaction tx = null;
                            HandlerApp handlerApp = (HandlerApp) session.load(HandlerApp.class, handlerAppSearchResultModel.getId());
                            handlerApp.setDefaultForOrganization(currentState);
                            try {
                                tx = session.beginTransaction();

                                boolean isDefaultForOrganization = handlerApp.getDefaultForOrganization();

                                if (isDefaultForOrganization) {
                                    HandlerAppService.HandlerAppCriteria handlerAppCriteria = new HandlerAppService.HandlerAppCriteria();
                                    handlerAppCriteria.setOrganization(handlerApp.getOrganization());
                                    handlerAppCriteria.setDefault(isDefaultForOrganization);
                                    List<HandlerAppService.HandlerAppSearchResultModel> handlerAppSearchResultModelList = HandlerAppService.Instance.list(handlerAppCriteria, 0, -1, null, false, session);
                                    for (HandlerAppService.HandlerAppSearchResultModel handlerAppSearchResultModel : handlerAppSearchResultModelList) {
                                        HandlerApp handlerAppInList = (HandlerApp) session.load(HandlerApp.class, handlerAppSearchResultModel.getId());
                                        if (!handlerAppInList.getId().equals(handlerApp.getId())) {
                                            handlerAppInList.setDefaultForOrganization(false);
                                            HandlerAppService.Instance.saveOrUpdate(handlerAppInList, session);
                                        }
                                    }
                                }

                                HandlerAppService.Instance.saveOrUpdate(handlerApp, session);

                                tx.commit();
                            } catch (Exception ex) {
                                logger.error("Error commiting transaction : ", ex);
                                if (tx != null && tx.isActive()) {
                                    logger.debug("Rolling back transaction!");
                                    tx.rollback();
                                }
                                processException(target, ex);
                            } finally {
                                session.close();
                            }

                            target.add(table);
                        }
                    };
                    switchBox.setModel(new Model<>(handlerAppSearchResultModel.getDefault()));
                    cellItem.add(switchBox);
                } else {

                    cellItem.add(new Label(componentId, new Model<>(
                            (handlerAppSearchResultModel.getDefault() != null && handlerAppSearchResultModel.getDefault()) ? getString("label.yes") : getString("label.no"))));
                }
            }
        });

        columnList.add(new AbstractColumn(new ResourceModel("HandlerApp.isActive")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                final HandlerAppService.HandlerAppSearchResultModel handlerAppSearchResultModel = (HandlerAppService.HandlerAppSearchResultModel) rowModel.getObject();
                if (PrincipalUtil.hasPermission(Access.HANDLERAPP_EDIT)) {
                    SwitchBox switchBox = new SwitchBox(componentId, new ResourceModel("label.yes").getObject(), new ResourceModel("label.no").getObject(), true) {
                        @Override
                        protected void onChange(AjaxRequestTarget target, Boolean currentState) {
                            Session session = HibernateUtil.getCurrentSession();
                            Transaction tx = null;
                            HandlerApp handlerApp = (HandlerApp) session.load(HandlerApp.class, handlerAppSearchResultModel.getId());
                            handlerApp.setActive(currentState);
                            try {
                                tx = session.beginTransaction();

                                HandlerAppService.Instance.saveOrUpdate(handlerApp, session);

                                tx.commit();
                            } catch (Exception ex) {
                                logger.error("Error commiting transaction : ", ex);
                                if (tx != null && tx.isActive()) {
                                    logger.debug("Rolling back transaction!");
                                    tx.rollback();
                                }
                                processException(target, ex);
                            } finally {
                                session.close();
                            }

                            target.add(table);
                        }
                    };
                    switchBox.setModel(new Model<>(handlerAppSearchResultModel.getActive()));
                    cellItem.add(switchBox);
                } else {

                    cellItem.add(new Label(componentId, new Model<>(
                            (handlerAppSearchResultModel.getActive() != null && handlerAppSearchResultModel.getActive()) ? getString("label.yes") : getString("label.no"))));
                }
            }
        });

        if (PrincipalUtil.hasPermission(Access.HANDLERAPP_EDIT))
            columnList.add(new AbstractColumn(new Model<String>("")) {
                @Override
                public void populateItem(Item cellItem, String componentId, final IModel rowModel) {
                    final HandlerAppService.HandlerAppSearchResultModel handlerAppSearchResultModel = (HandlerAppService.HandlerAppSearchResultModel) rowModel.getObject();
                    Session session = HibernateUtil.getCurrentSession();
                    HandlerApp handlerApp = (HandlerApp) session.load(HandlerApp.class, handlerAppSearchResultModel.getId());
                    cellItem.add(new AjaxLinkPanel(componentId, AjaxLinkPanel.Image.Edit) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            HandlerAppForm handlerAppForm = new HandlerAppForm(modalWindow.getContentId(), handlerApp, osId);
                            handlerAppForm.setParentListner(HandlerAppList.this);
                            modalWindow.setContent(handlerAppForm);
                            modalWindow.setTitle(getString("HandlerApp.editForm"));
                            modalWindow.show(target);
                        }
                    });
                }
            });

        return columnList;
    }


    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {
        if (eventThrownCmp != null && eventThrownCmp.getId().equals("save")) {
            target.add(table);
        }
        modalWindow.close(target);
    }


    @Override
    public Collection<Object> getSelection() {
        return table.getSelectedObjetcs();
    }

    public static class HandlerAppSortableDataProvider extends SortableDataProvider {
        HandlerAppService.HandlerAppCriteria criteria;

        public HandlerAppSortableDataProvider() {
            setSort("handlerApp.id", SortOrder.ASCENDING);
        }

        public void setCriteria(HandlerAppService.HandlerAppCriteria criteria) {
            this.criteria = criteria;
        }

        @Override
        public Iterator iterator(long first, long count) {
            Session session = HibernateUtil.getCurrentSession();
            SortParam sortParam = getSort();
            return HandlerAppService.Instance.list(criteria, (int) first, (int) count, (String) sortParam.getProperty(), sortParam.isAscending(), session).iterator();
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
