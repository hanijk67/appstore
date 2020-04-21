package com.fanap.midhco.ui.pages.anouncement;

import com.fanap.midhco.appstore.entities.Anouncement;
import com.fanap.midhco.appstore.entities.OSEnvironment;
import com.fanap.midhco.appstore.entities.OSType;
import com.fanap.midhco.appstore.entities.Organization;
import com.fanap.midhco.appstore.entities.helperClasses.AnouncementType;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.entities.helperClasses.DateType;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.anouncement.AnouncementService;
import com.fanap.midhco.appstore.service.anouncement.AppSearchAnouncementActionDescriptor;
import com.fanap.midhco.appstore.service.anouncement.IAnouncementActionDescriptor;
import com.fanap.midhco.appstore.service.environment.EnvironmentService;
import com.fanap.midhco.appstore.service.org.OrgService;
import com.fanap.midhco.appstore.service.osType.OSTypeService;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.access.WebAction;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.ajaxLinkPanel.AjaxLinkPanel;
import com.fanap.midhco.ui.component.dateTimePanel.DateTimeRangePanel;
import com.fanap.midhco.ui.component.limitedTextField.LimitedTextField;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.component.mydropdown.MyDropDownChoicePanel;
import com.fanap.midhco.ui.component.switchbox.SwitchBox;
import com.fanap.midhco.ui.component.table.MyAjaxDataTable;
import com.fanap.midhco.ui.component.table.column.IndexColumn;
import com.fanap.midhco.ui.component.table.column.PersianDateColumn;
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
import org.apache.wicket.markup.html.form.TextField;
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

import static com.fanap.midhco.appstore.wicketApp.EnumCaptionHelper.ANOUNCEMENTMAP;

/**
 * Created by A.Moshiri on 9/6/2017.
 */
@Authorize(view = Access.ANOUNCEMENT_LIST)

public class AnouncementList extends BasePanel implements IParentListner {
    AnouncementListSortableDataProvider dp = new AnouncementListSortableDataProvider();

    MyAjaxDataTable table;
    BootStrapModal modal = new BootStrapModal("modal");
    Form form;
    FeedbackPanel feedbackPanel;
    MyDropDownChoicePanel anouncementTypeDropDown;
    MyDropDownChoicePanel actionCategoryDropDown;
    MyDropDownChoicePanel organizationsMyDropDown;
    MyDropDownChoicePanel osEnvironmentMyDropDown;
    DateTimeRangePanel startDateTime;
    DateTimeRangePanel expireDateTime;
    MyDropDownChoicePanel osTypeDropDown;

    LimitedTextField text;

    public AnouncementList() {
        this(MAIN_PANEL_ID, new AnouncementService.AnouncmentCriteria() , SelectionMode.None);
    }


    protected AnouncementList(String id, final AnouncementService.AnouncmentCriteria criteria, final SelectionMode selectionMode) {
        super(id);
        add(modal);
        feedbackPanel = new FeedbackPanel("feedbackPanel");
        add(feedbackPanel);
        form = new Form("form", new CompoundPropertyModel(criteria));


        text  = new LimitedTextField("anouncementText",null , false, false,true,true,40 , getString("Anouncement.text"));
        text.setLabel( new ResourceModel("Anouncement.text"));
        text.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(text);


        List<AnouncementType> anouncementTypes = new ArrayList<>();
        anouncementTypes.add(AnouncementType.PRODUCTLISTTYPE);
        anouncementTypes.add(AnouncementType.VOID);

        anouncementTypeDropDown =
                new MyDropDownChoicePanel("anouncementType", anouncementTypes, true, false, getString("Anouncement.type"), 3, true, new ChoiceRenderer()) {
                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        List<AnouncementType> selectedItems = (List<AnouncementType>) getSelectedItem();
                        if (selectedItems == null || selectedItems.isEmpty())
                            actionCategoryDropDown.setChoices(new ArrayList(), target);
                        else {
                            List<Class<? extends IAnouncementActionDescriptor>> actionCategoryList = new ArrayList<>();
                            if (selectedItems.contains(AnouncementType.PRODUCTLISTTYPE)){
                                actionCategoryList.add(AppSearchAnouncementActionDescriptor.class);
                            }

                            actionCategoryDropDown.setChoices(actionCategoryList, target);

                        }
                    }
                };
        anouncementTypeDropDown.setLabel(new ResourceModel("Anouncement.type"));
        anouncementTypeDropDown.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(anouncementTypeDropDown);


        List<String> actionCategoryList = new ArrayList<>();
        actionCategoryDropDown =
                new MyDropDownChoicePanel("actionCategory", actionCategoryList, true, false, getString("Anouncement.actionCategory"), 3, false, new ChoiceRenderer(){
                    @Override
                    public Object getDisplayValue(Object o) {
                        if (o!= null) {
                            return ANOUNCEMENTMAP.get(o);
                        }
                        return null;
                    }

                    @Override
                    public String getIdValue(Object o, int i) {
                        return o.getClass().getCanonicalName();
                    }
                });
        actionCategoryDropDown.setLabel(new ResourceModel("Anouncement.category"));
        actionCategoryDropDown.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(actionCategoryDropDown);


        startDateTime= new DateTimeRangePanel("startDateTime", DateType.DateTime);
        startDateTime.setLabel(new ResourceModel("Anouncement.startDateTime"));
        startDateTime.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(startDateTime);

        expireDateTime = new DateTimeRangePanel("expireDateTime", DateType.DateTime);
        expireDateTime.setLabel(new ResourceModel("Anouncement.expireDateTime"));
        expireDateTime.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(expireDateTime);

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


        form.add(new AjaxFormButton("search", form) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {

                String validationString = "";
                if (text != null && text.getValidatorString() != null && !text.getValidatorString().isEmpty()) {
                    for (String validationStringInList : text.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }

                if (!validationString.isEmpty()) {
                    target.appendJavaScript("showMessage('" + validationString + "');");
                    return;
                }

                AnouncementService.AnouncmentCriteria criteria = (AnouncementService.AnouncmentCriteria) form.getModelObject();
                if (criteria != null) {
                    dp.setCriteria(criteria);
                    table.setVisible(true);
                    target.add(table);
                    if (selectionMode.isSelectable())
                        target.add(AnouncementList.this.get("select").setVisible(true));
                }
            }
        });

        form.add(new AjaxLink("reset") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                AnouncementService.AnouncmentCriteria criteria = new AnouncementService.AnouncmentCriteria();
                form.setModelObject(criteria);
                target.add(form);

                if (selectionMode.isSelectable())
                    target.add(AnouncementList.this.get("select").setVisible(true));
                else
                    AnouncementList.this.get("select").setVisible(false);

                target.add(AnouncementList.this.get("select"));
                table.setVisible(false);
                target.add(table);
            }
        });


        add(authorize(new AjaxLink("add") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                if (!selectionMode.equals(SelectionMode.WithoutAdd)) {

                    AnouncementForm anouncementForm = new AnouncementForm(modal.getContentId(),new Anouncement());
                    anouncementForm.setParentListner(AnouncementList.this);
                    modal.setContent(anouncementForm);
                    target.appendJavaScript("$('.modal-body').css('overflow-x', 'hidden')");
                    modal.show(target);
                }
            }
        }, WebAction.RENDER, Access.ANOUNCEMENT_ADD).setVisible(!selectionMode.equals(SelectionMode.WithoutAdd)));



        add(new AjaxLink("select") {
            public void onClick(AjaxRequestTarget target) {
                childFinished(target, null, null);
            }
        }.setOutputMarkupPlaceholderTag(true).setVisible(false));
        if (selectionMode.isSelectable() && table.getRowCount() > 0)
            get("select").setVisible(true);

        table = new MyAjaxDataTable("table", getColumns(), dp, 10);
        table.setSelectionMode(selectionMode);
        table.setVisible(true);
        table.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        boolean allFieldsNull = checkAllFieldsNull(criteria);
        if(allFieldsNull)
            table.setVisible(false);
        add(table);


        add(form);

    }


    private List<IColumn> getColumns() {
        List<IColumn> columnList = new ArrayList<IColumn>();
        columnList.add(new IndexColumn());

        columnList.add(new PropertyColumn(new ResourceModel("Anouncement.id"), "anouncement.id", "id"));
        columnList.add(new PropertyColumn(new ResourceModel("Anouncement.text"), "anouncement.anouncementText", "anouncementText"));
        columnList.add(new PropertyColumn(new ResourceModel("Anouncement.type"), "anouncement.type", "anouncementType"));
        columnList.add(new PropertyColumn(new ResourceModel("Anouncement.organizations"), "anouncement.organizations", "organizations"));
        columnList.add(new PropertyColumn(new ResourceModel("Anouncement.environment"), "anouncement.environment", "osEnvironments"));


        columnList.add(new PersianDateColumn(new ResourceModel("Anouncement.startDateTime"), "Anouncement.startDateTime", "startDateTime"));

        columnList.add(new PersianDateColumn(new ResourceModel("Anouncement.expireDateTime"), "Anouncement.expireDateTime", "expireDateTime"));

        columnList.add(new AbstractColumn(new ResourceModel("Anouncement.actionCategory", "actionCategory")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {

                Anouncement anouncement = (Anouncement) rowModel.getObject();
                String actionCategory= null;
                if (anouncement.getActionCategory()!=null) {
                    actionCategory= anouncement.getActionCategory();
                }
                if (actionCategory!=null && actionCategory.equals(AppSearchAnouncementActionDescriptor.class.getCanonicalName())) {
                    cellItem.add(new Label(componentId, new ResourceModel(getString("Application.search"), getString("Application.search"))));
                } else {
                    cellItem.add(new Label(componentId, ""));
                }
            }
        });


        columnList.add(new AbstractColumn(new ResourceModel("Anouncement.isExpired", "isExpired")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                Anouncement anouncement = (Anouncement) rowModel.getObject();
                if (anouncement.getExpireDateTime()!=null && anouncement.getExpireDateTime().compareTo(DateTime.now())<0) {
                    cellItem.add(new Label(componentId, getString("label.yes")));
                }else {
                    cellItem.add(new Label(componentId, getString("label.no")));
                }
            }
        });

        columnList.add(new AbstractColumn(new ResourceModel("label.activation.verb")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                Anouncement anouncement = (Anouncement) rowModel.getObject();
                if(PrincipalUtil.hasPermission(Access.ANOUNCEMENT_EDIT)) {
                    SwitchBox switchBox = new SwitchBox(componentId, new ResourceModel("label.yes").getObject(), new ResourceModel("label.no").getObject(), true) {
                        @Override
                        protected void onChange(AjaxRequestTarget target, Boolean currentState) {
                            Session session = HibernateUtil.getCurrentSession();
                            Transaction tx = null;

                            try {
                                tx = session.beginTransaction();
                                Anouncement intendedAnouncement = (Anouncement)session.load(Anouncement.class, anouncement.getId());
                                intendedAnouncement.setActive(currentState);

                                AnouncementService.Instance.saveOrUpdate(intendedAnouncement, session);

                                tx.commit();
                            } catch (Exception ex) {
                                processException(target, ex);
                                if(tx != null)
                                    tx.rollback();
                            } finally {
                                session.close();
                            }
                        }
                    };
                    switchBox.setModel(new Model<>(anouncement.getActive()));
                    cellItem.add(switchBox);
                } else {

                    cellItem.add(new Label(componentId, new Model<>(
                            (anouncement.getActive()!=null && anouncement.getActive())?getString("label.yes"):getString("label.no") )));
                }
            }
        });


        columnList.add(new AbstractColumn(new Model("")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                if (PrincipalUtil.hasPermission(Access.ANOUNCEMENT_EDIT)) {
                    Anouncement inputAnouncement = (Anouncement) rowModel.getObject();
                    cellItem.add(new AjaxLinkPanel(componentId, AjaxLinkPanel.Image.Edit) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            Long inputAnouncementId =Long.valueOf(inputAnouncement.getId());
                            Session session = HibernateUtil.getCurrentSession();
                            session.evict(inputAnouncement);
                            Anouncement loadedInputAnouncement = (Anouncement) session.load(Anouncement.class, inputAnouncementId);
                            AnouncementForm anouncementForm = new AnouncementForm(modal.getContentId(), loadedInputAnouncement);
                            anouncementForm.setParentListner(AnouncementList.this);
                            modal.setContent(anouncementForm);
                            modal.show(target);

                        }
                    });
                } else {
                    cellItem.add(new Label(componentId, new Model<>("")));
                }
            }
        });


        return columnList;
    }

    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {
        if (eventThrownCmp != null &&( eventThrownCmp.getId().equals("createAnouncement") || eventThrownCmp.getId().equals("saveAnouncement")))
            target.add(table);
        modal.close(target);
    }

    public static class AnouncementListSortableDataProvider extends SortableDataProvider {
        public AnouncementService.AnouncmentCriteria criteria;

        public AnouncementListSortableDataProvider() {
            setSort("ent.id", SortOrder.ASCENDING);
        }

        public void setCriteria(AnouncementService.AnouncmentCriteria criteria) {
            this.criteria = criteria;
        }

        @Override
        public Iterator iterator(long first, long count) {
            Session session = HibernateUtil.getCurrentSession();
            SortParam sortParam = getSort();
            String[] sortParams = new String[2];
            sortParams[1] = (String) sortParam.getProperty();
            try {
                List<Anouncement> announcementList =AnouncementService.Instance.list(criteria, (int) first, (int) count, sortParams, sortParam.isAscending(), session);
                for(Anouncement anouncement : announcementList){
                    if(anouncement.getOsEnvironments()!=null && anouncement.getOsEnvironments().isEmpty()){
                        anouncement.setOsEnvironments(null);
                    }

                    if(anouncement.getOrganizations()!=null && anouncement.getOrganizations().isEmpty()){
                        anouncement.setOrganizations(null);
                    }
                }
                return announcementList.iterator();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public long size() {
            Session session = HibernateUtil.getCurrentSession();
            try {
                return AnouncementService.Instance.count(criteria, session);
            } catch (Exception e) {
                return 0;
            }
        }

        @Override
        public IModel model(Object object) {
            return new Model((Serializable) object);
        }
    }

}

