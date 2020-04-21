package com.fanap.midhco.ui.pages.org;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.Organization;
import com.fanap.midhco.appstore.service.BaseEntityService;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.org.OrgService;
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
 * Created by A.Moshiri on 2/26/2018.
 */
@Authorize(view = Access.ORGANIZATION_LIST)
public class OrgList extends BasePanel implements IParentListner, ISelectable {
    Form searchForm;
    OrgSortableDataProvider dp = new OrgSortableDataProvider();
    MyAjaxDataTable table;
    BootStrapModal modalWindow = new BootStrapModal("modal");
    LimitedTextField nickName;
    LimitedTextField fullName;
    LimitedTextField englishFullName;

    public OrgList() {
        this(MAIN_PANEL_ID, new OrgService.OrgCriteria(), SelectionMode.None);
    }

    protected OrgList(String id, OrgService.OrgCriteria criteria, SelectionMode selectionMode) {
        super(id);

        setPageTitle(getString("organization"));

        add(modalWindow);

        boolean allFieldsNull = checkAllFieldsNull(criteria);

        searchForm = new Form("searchForm", new CompoundPropertyModel(criteria));
        searchForm.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(searchForm);

        nickName = new LimitedTextField("nickName",null , null,false,true,false,40 , getString("organization.nickName"));
        nickName.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        searchForm.add(nickName);

        fullName = new LimitedTextField("fullName",false , null,false,true,false,40 , getString("organization.fullName"));
        fullName.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        searchForm.add(fullName);

        englishFullName = new LimitedTextField("englishFullName",true , null,false,true,false,40 , getString("organization.englishFullName"));

        englishFullName.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        searchForm.add(englishFullName);


        int rowsPerPage =
                Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.APP_LIST_ROWS_PER_PAGE));
        table = new MyAjaxDataTable("table", getColumns(), dp, rowsPerPage);
        table.setSelectionMode(selectionMode);
        table.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(table);

        MyDropDownChoicePanel isDefaultBox = new MyDropDownChoicePanel("isDefault",
                Arrays.asList(true, false), false, false, getString("organization.default"), 1, false,
                new ChoiceRenderer() {
                    @Override
                    public Object getDisplayValue(Object o) {
                        if ((Boolean)o) {
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
        searchForm.add(isDefaultBox);


        if (allFieldsNull)
            table.setVisible(false);


        add(new AjaxLink("select") {
            public void onClick(AjaxRequestTarget target) {
                childFinished(target, null, null);
            }
        }.setOutputMarkupPlaceholderTag(true).setVisible(false));
        if (selectionMode.isSelectable() && table.getRowCount() > 0)
            get("select").setVisible(true);


        add(new AjaxLink("createOrganization") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                Organization organization = new Organization();
                OrgForm orgForm = new OrgForm(modalWindow.getContentId(), organization);
                orgForm.setParentListner(OrgList.this);
                modalWindow.setContent(orgForm);
                modalWindow.setTitle(getString("organization.createForm"));
                modalWindow.show(target);
            }
        });

        searchForm.add(new AjaxFormButton("search", searchForm) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {

                String validationString = "";
                if (nickName != null && nickName.getValidatorString() != null && !nickName.getValidatorString().isEmpty()) {
                    for (String validationStringInList : nickName.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }

                if (fullName != null && fullName.getValidatorString() != null && !fullName.getValidatorString().isEmpty()) {
                    for (String validationStringInList : fullName.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }

                if (englishFullName != null && englishFullName.getValidatorString() != null && !englishFullName.getValidatorString().isEmpty()) {
                    for (String validationStringInList : englishFullName.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }

                if (!validationString.isEmpty()) {
                    target.appendJavaScript("showMessage('" + validationString + "');");
                    return;
                }


                OrgService.OrgCriteria criteria = (OrgService.OrgCriteria) form.getModelObject();
                if (criteria != null) {
                    dp.setCriteria(criteria);
                    table.setVisible(true);
                    target.add(table);
                    if (selectionMode.isSelectable())
                        target.add(OrgList.this.get("select").setVisible(true));
                }
            }
        });

        searchForm.add(new AjaxLink("reset") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                searchForm.setModel(new CompoundPropertyModel(new OrgService.OrgCriteria()));
                target.add(searchForm);
                table.setVisible(false);
                target.add(table);
                OrgList.this.get("select").setVisible(false);
                target.add(OrgList.this.get("select"));
                target.add(OrgList.this.get("select"));
            }
        });
    }

    private List<IColumn> getColumns() {
        List<IColumn> columnList = new ArrayList<IColumn>();
        columnList.add(new IndexColumn());

        columnList.add(new PropertyColumn(new ResourceModel("organization.id"), "ent.id", "id"));
        columnList.add(new PropertyColumn(new ResourceModel("organization.fullName"), "ent.fullName", "fullName"));
        columnList.add(new PropertyColumn(new ResourceModel("organization.englishFullName"), "ent.englishFullName", "englishFullName"));
        columnList.add(new PropertyColumn(new ResourceModel("organization.nickName"), "ent.nickName", "nickName"));

        columnList.add(new AbstractColumn(new ResourceModel("organization.default")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                final Organization organization = (Organization) rowModel.getObject();
                if(PrincipalUtil.hasPermission(Access.ORGANIZATION_EDIT)) {
                    SwitchBox switchBox = new SwitchBox(componentId, new ResourceModel("label.yes").getObject(), new ResourceModel("label.no").getObject(), true) {
                        @Override
                        protected void onChange(AjaxRequestTarget target, Boolean currentState) {
                            Session session = HibernateUtil.getCurrentSession();
                            Transaction tx = null;

                            organization.setDefault(currentState);
                            try {
                                tx = session.beginTransaction();

                                boolean isDefault = organization.getDefault();

                                if(isDefault){
                                    OrgService.OrgCriteria orgCriteria = new OrgService.OrgCriteria();
                                    orgCriteria.setIsDefault(isDefault);
                                    List<Organization> organizationList = OrgService.Instance.list(orgCriteria , 0 , -1 ,null ,false , session);
                                    for(Organization orgInList : organizationList){
                                            orgInList.setDefault(false);
                                            BaseEntityService.Instance.saveOrUpdate(orgInList, session);
                                        }
                                    }
                                BaseEntityService.Instance.saveOrUpdate(organization, session);

                                tx.commit();
                            } catch (Exception ex) {
                                logger.error("Error commiting transaction : ", ex);
                                if (tx != null && tx.isActive()) {
                                    logger.debug("Rolling back transaction!");
                                    tx.rollback();
                                }
                                processException(target, ex);
                            } finally {
                                if (session!=null && session.isOpen()) {
                                    session.close();
                                }
                            }

                            target.add(table);
                        }
                    };
                    switchBox.setModel(new Model<>(organization.getDefault()));
                    cellItem.add(switchBox);
                } else {

                    cellItem.add(new Label(componentId, new Model<>(
                            (organization.getDefault()!=null && organization.getDefault())?getString("label.yes"):getString("label.no") )));
                }
            }
        });

        if (PrincipalUtil.hasPermission(Access.ORGANIZATION_EDIT))
            columnList.add(new AbstractColumn(new Model<String>("")) {
                @Override
                public void populateItem(Item cellItem, String componentId, final IModel rowModel) {
                    Organization organization = (Organization) rowModel.getObject();
                    Session session = HibernateUtil.getNewSession();
                    Organization loadedOrganization = (Organization) session.get(Organization.class,organization.getId());
                    session.close();
                    cellItem.add(new AjaxLinkPanel(componentId, AjaxLinkPanel.Image.Edit) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            OrgForm orgForm = new OrgForm(modalWindow.getContentId(), loadedOrganization);
                            orgForm.setParentListner(OrgList.this);
                            modalWindow.setContent(orgForm);
                            modalWindow.setTitle(getString("organization.editForm"));
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


    public static class OrgSortableDataProvider extends SortableDataProvider {
        OrgService.OrgCriteria criteria;

        public OrgSortableDataProvider() {
            setSort("ent.id", SortOrder.ASCENDING);
        }

        public void setCriteria(OrgService.OrgCriteria criteria) {
            this.criteria = criteria;
        }

        @Override
        public Iterator iterator(long first, long count) {
            Session session = HibernateUtil.getCurrentSession();
            SortParam sortParam = getSort();
            return OrgService.Instance.list(criteria, (int) first, (int) count, (String) sortParam.getProperty(), sortParam.isAscending(), session).iterator();
        }

        @Override
        public long size() {
            Session session = HibernateUtil.getCurrentSession();
            return OrgService.Instance.count(criteria, session);
        }

        @Override
        public IModel model(Object o) {
            return new Model((Serializable) o);
        }
    }

}
