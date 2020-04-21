package com.fanap.midhco.ui.pages.os;

import com.fanap.midhco.appstore.entities.OS;
import com.fanap.midhco.appstore.entities.OSType;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.os.OSService;
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
import com.fanap.midhco.ui.component.limitedTextField.LimitedTextField;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.component.mydropdown.MyDropDownChoicePanel;
import com.fanap.midhco.ui.component.switchbox.SwitchBox;
import com.fanap.midhco.ui.component.table.MyAjaxDataTable;
import com.fanap.midhco.ui.component.table.column.IndexColumn;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
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
 * Created by admin123 on 6/28/2016.
 */
@Authorize(view = Access.OS_LIST)
public class OSList extends BasePanel implements IParentListner {
    Form form;
    OSSortableDataProvider dp = new OSSortableDataProvider();
    MyAjaxDataTable table;
    BootStrapModal modalWindow = new BootStrapModal("modal");
    LimitedTextField osIdTextField;
    LimitedTextField osNameTextField;
    LimitedTextField osVersionTextField;
    LimitedTextField osCodeTextField;

    public OSList() {
        this(MAIN_PANEL_ID, new OSService.OSCriteria(), SelectionMode.None);
    }

    public OSList(String id, OSService.OSCriteria osCriteria, final SelectionMode selectionMode) {
        super(id);

        add(modalWindow);

        form = new Form("searchForm", new CompoundPropertyModel(osCriteria));
        form.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        osIdTextField  = new LimitedTextField("osId",true ,true ,false,false,false,12 ,getString("OS.id"));
        osIdTextField.setLabel(new ResourceModel("OS.id"));
        form.add(osIdTextField);

        osNameTextField  = new LimitedTextField("osName",true ,false ,false,false,false,40 ,getString("OS.osName"));

        osIdTextField.setLabel(new ResourceModel("OS.osName"));
        form.add(osNameTextField);

        osVersionTextField =new LimitedTextField("osVersion",true ,null ,true,false,false,40 ,getString("OS.osVersion"));
        osVersionTextField.setLabel(new ResourceModel("OS.osVersion"));
        form.add(osVersionTextField);

        osCodeTextField =new LimitedTextField("osCode",true ,null ,true,false,false,40 ,getString("OS.osCode"));

        osCodeTextField.setLabel(new ResourceModel("OS.osCode"));
        form.add(osCodeTextField);

        List<OSType> allOSTypes = OSTypeService.Instance.getEnabledOSTypes();
        final MyDropDownChoicePanel osTypeDropDown =
                new MyDropDownChoicePanel("osType", allOSTypes, true, false, getString("OS.osType"), 3);
        osTypeDropDown.setLabel(new ResourceModel("OS.osType"));
        osTypeDropDown.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(osTypeDropDown);

        form.add(new WebMarkupContainer("reloadOSTypes").add(new AjaxEventBehavior("onclick") {
            @Override
            protected void onEvent(AjaxRequestTarget target) {
                List<OSType> allOSTypes = OSTypeService.Instance.getEnabledOSTypes();
                osTypeDropDown.setChoices(allOSTypes, target);
            }
        }));

        form.add(new AjaxFormButton("search", form) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {
                try {

                    String validationString = "";
                    if (osNameTextField != null && osNameTextField.getValidatorString() != null && !osNameTextField.getValidatorString().isEmpty()) {
                        for (String validationStringInList : osNameTextField.getValidatorString()) {
                            validationString += " - " +
                                    validationStringInList + "<br/>";
                        }
                    }

                    if (osIdTextField != null && osIdTextField.getValidatorString() != null && !osIdTextField.getValidatorString().isEmpty()) {
                        for (String validationStringInList : osIdTextField.getValidatorString()) {
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


                    OSService.OSCriteria cri =
                            (OSService.OSCriteria) form.getModelObject();
                    dp.setCriteria(cri);
                    table.setVisible(true);
                    target.add(table);
                    if (selectionMode.isSelectable()) {
                        target.add(OSList.this.get("select").setVisible(true));
                    }
                } catch (Exception e) {
                    logger.error("Error:", e);
                }
            }
        });

        form.add(new AjaxLink("reset") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                form.setModel(new CompoundPropertyModel(new OSService.OSCriteria()));
                target.add(form);
                table.setVisible(false);
                target.add(table);
                OSList.this.get("select").setVisible(false);
                target.add(OSList.this.get("select"));
            }
        });

        add(authorize(new AjaxLink("add") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                OSForm osForm = new OSForm(modalWindow.getContentId(), new OS());
                osForm.setParentListner(OSList.this);
                modalWindow.setContent(osForm);
                modalWindow.setTitle(getString("os.createForm"));
                modalWindow.show(target);
            }
        }, WebAction.RENDER, Access.OS_ADD));

        table = new MyAjaxDataTable("table", getColumns(), dp, 10);
        table.setSelectionMode(selectionMode);
        table.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(table);

        boolean allFieldsNull = checkAllFieldsNull(osCriteria);

        if (allFieldsNull)
            table.setVisible(false);

        add(new AjaxLink("select") {
            public void onClick(AjaxRequestTarget target) {
                childFinished(target, null, null);
            }
        }.setOutputMarkupPlaceholderTag(true).setVisible(false));
        if (selectionMode.isSelectable() && table.getRowCount() > 0)
            get("select").setVisible(true);

        add(form);
    }

    private List<IColumn> getColumns() {
        List<IColumn> columnList = new ArrayList<IColumn>();
        columnList.add(new IndexColumn());

        columnList.add(new PropertyColumn(new ResourceModel("OS.id"), "ent.id", "id"));
        columnList.add(new PropertyColumn(new ResourceModel("OS.osName"), "ent.osName", "osName"));
        columnList.add(new PropertyColumn(new ResourceModel("OS.osCode"), "ent.osCode", "osCode"));
        columnList.add(new PropertyColumn(new ResourceModel("OS.osVersion"), "ent.osVersion", "osVersion"));
        columnList.add(new PropertyColumn(new ResourceModel("OS.osType"), "ent.osType", "osType.name"));

        columnList.add(new AbstractColumn(new ResourceModel("label.disabled.verb")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                final OS os = (OS) rowModel.getObject();
                if(PrincipalUtil.hasPermission(Access.OS_EDIT)) {
                    SwitchBox switchBox = new SwitchBox(componentId, new ResourceModel("label.yes").getObject(), new ResourceModel("label.no").getObject(), true) {
                        @Override
                        protected void onChange(AjaxRequestTarget target, Boolean currentState) {
                            Session session = HibernateUtil.getCurrentSession();
                            Transaction tx = null;

                            os.setDisabled(currentState);
                            try {
                                tx = session.beginTransaction();
                                OSService.Instance.saveOrUpdate(os, session);
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
                    switchBox.setModel(new Model<>(os.getDisabled()));
                    cellItem.add(switchBox);
                } else {

                    cellItem.add(new Label(componentId, new Model<>(
                            (os.getDisabled()!=null && os.getDisabled())?getString("label.yes"):getString("label.no") )));
                }
            }
        });

        if (PrincipalUtil.hasPermission(Access.OS_EDIT)) {
            columnList.add(new AbstractColumn(new Model<String>("")) {
                @Override
                public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                    final OS os = (OS) rowModel.getObject();
                    Session session = HibernateUtil.getNewSession();
                    OS loadedOs = (OS) session.get(OS.class , os.getId());
                    session.close();

                    cellItem.add(new AjaxLinkPanel(componentId, AjaxLinkPanel.Image.Edit) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            OSForm osForm = new OSForm(modalWindow.getContentId(), loadedOs);
                            osForm.setParentListner(OSList.this);
                            modalWindow.setContent(osForm);
                            modalWindow.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
                            modalWindow.setTitle(getString("os.editForm"));
                            modalWindow.show(target);
                        }
                    });
                }
            });
        }

        return columnList;
    }

    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {
        if (eventThrownCmp.getId().equals("save")) {
            if(table.isVisible())
                target.add(table);
        }
        modalWindow.close(target);
    }

    public static class OSSortableDataProvider extends SortableDataProvider {
        OSService.OSCriteria criteria;

        public OSSortableDataProvider() {
            setSort("id", SortOrder.ASCENDING);
        }

        public void setCriteria(OSService.OSCriteria criteria) {
            this.criteria = criteria;
        }

        @Override
        public Iterator iterator(long first, long count) {
            Session session = HibernateUtil.getCurrentSession();
            SortParam sortParam = getSort();
            return OSService.Instance.list(criteria, (int) first, (int) count, (String) sortParam.getProperty(), sortParam.isAscending(), session).iterator();
        }

        @Override
        public long size() {
            Session session = HibernateUtil.getCurrentSession();
            return OSService.Instance.count(criteria, session);
        }

        @Override
        public IModel model(Object o) {
            return new Model((Serializable) o);
        }
    }
}
