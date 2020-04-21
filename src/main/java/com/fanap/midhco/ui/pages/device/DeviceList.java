package com.fanap.midhco.ui.pages.device;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.entities.helperClasses.DayDate;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.device.DeviceService;
import com.fanap.midhco.appstore.service.os.OSService;
import com.fanap.midhco.appstore.service.osType.OSTypeService;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.access.WebAction;
import com.fanap.midhco.ui.component.ISelectable;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.component.mydropdown.MyDropDownChoicePanel;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.hibernate.Session;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by A.Moshiri on 5/30/2017.
 */
@Authorize(view = Access.DEVICE_LIST)
public class DeviceList extends BasePanel implements IParentListner ,ISelectable {

    private static CssResourceReference CSS = new CssResourceReference(DeviceList.class, "res/DeviceList.css");

    private static ResourceReference JAVASCRIPT = new JavaScriptResourceReference(DeviceList.class, "res/DeviceList.js");
    DeviceListDataProvider dp = new DeviceListDataProvider();
    DeviceTable deviceTable;
    Form form;
    MyDropDownChoicePanel osDropDown;
    MyDropDownChoicePanel userDropDown;
    MyDropDownChoicePanel deviceStateDropDown;
    BootStrapModal modal;
    int numberItemInPerPage = 0;
    Set<DeviceService.DeviceSearchModel> selectedDeviceSearchModels ;
    MyDropDownChoicePanel activeDropDown;

    public DeviceList() {
        this(MAIN_PANEL_ID, new DeviceService.DeviceCriteria(), SelectionMode.None);
    }

    public DeviceList(String id, final DeviceService.DeviceCriteria criteria, final SelectionMode selectionMode) {
        super(id);

        selectedDeviceSearchModels = new HashSet<>();
        Session session = HibernateUtil.getCurrentSession();
        List<Device> deviceList = DeviceService.Instance.listAll(session);
        long deviceSize = deviceList!=null ? deviceList.size(): null;
        modal = new BootStrapModal("modal");
        add(modal);

        form = new Form("searchForm", new CompoundPropertyModel(criteria));
        form.setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true);

        TextField deviceTitleTextField = new TextField("title");
        deviceTitleTextField.setLabel(new ResourceModel(""));
        deviceTitleTextField.setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true);
        form.add(deviceTitleTextField);

        TextField deviceImeiTextField = new TextField("imei");
        deviceImeiTextField.setLabel(new ResourceModel(""));
        deviceImeiTextField.setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true);
        form.add(deviceImeiTextField);

        List<OSType> allOSTypes = OSTypeService.Instance.getEnabledOSTypes();
        final MyDropDownChoicePanel osTypeDropDown =
                new MyDropDownChoicePanel("osType", allOSTypes, true, false, getString("OS.osType"), 3, true, new ChoiceRenderer()) {
                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        List<OSType> selectedItems = (List<OSType>) getSelectedItem();
                        if (selectedItems == null || selectedItems.isEmpty())
                            osDropDown.setChoices(new ArrayList(), target);
                        else {
                            OSService.OSCriteria osCriteria = new OSService.OSCriteria();
                            osCriteria.osType = new ArrayList<OSType>();
                            osCriteria.osType.addAll(selectedItems);
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
        osTypeDropDown.setOutputMarkupId(true);
        osTypeDropDown.setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true);
        form.add(osTypeDropDown);

        List<OS> osList = new ArrayList<OS>();
        osDropDown =
                new MyDropDownChoicePanel("os", osList, true, false, getString("OS"), 3, false, new ChoiceRenderer());
        osDropDown.setLabel(new ResourceModel("OS"));
        osDropDown.setOutputMarkupId(true);
        form.add(osDropDown);

        List<User> userList = UserService.Instance.listAllUser();
        userDropDown =
                new MyDropDownChoicePanel("usedBy", userList, true,true, getString("Device.usedBy"), 3, false, new ChoiceRenderer());
        userDropDown.setLabel(new ResourceModel("Device.usedBy"));
        userDropDown.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(userDropDown);

        List<DeviceState> deviceStateList = new ArrayList<>();
        deviceStateList.add(DeviceState.FREE);
        deviceStateList.add(DeviceState.INUSED);
        deviceStateDropDown =
                new MyDropDownChoicePanel("state", deviceStateList,false,true, getString("Device.state"), 3, false, new ChoiceRenderer());
        deviceStateDropDown.setLabel(new ResourceModel("Device.state"));
            deviceStateDropDown.setOutputMarkupId(true);
        form.add(deviceStateDropDown);

        numberItemInPerPage = Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.DEVICE_PER_PAGE_IN_DEVICE_LIST));

        deviceTable = new DeviceTable("deviceTable", dp, criteria, numberItemInPerPage, deviceSize, selectionMode);
        deviceTable.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        deviceTable.setVisible(false);
        deviceTable.setModel(new Model<>());
        add(deviceTable);

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

        form.add(new AjaxFormButton("search", form) {
                     @Override
                     protected void onSubmit(Form form, AjaxRequestTarget target) {
                         DeviceService.DeviceCriteria criteria = (DeviceService.DeviceCriteria) form.getModelObject();
                         if (criteria != null) {
                             dp.setCriteria(criteria);
                         }
                         DeviceList.this.get("setSelected").setVisible(false);
                         deviceTable.setVisible(true);
                         target.add(deviceTable);
                         if (selectionMode.isSelectable())
                             target.add(DeviceList.this.get("setSelected").setVisible(true));
                     }
                 }
        );

        form.add(new AjaxLink("reset") {
                     @Override
                     public void onClick(AjaxRequestTarget target) {
                         form.setModelObject(new DeviceService.DeviceCriteria());
                         DeviceList.this.get("setSelected").setVisible(false);
                         target.add(DeviceList.this.get("setSelected"));
                         deviceTable.setVisible(false);
                         target.add(deviceTable);
                         target.add(form);
                     }
                 }
        );
        add(new AjaxLink("setSelected") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                selectedDeviceSearchModels.clear();
                selectedDeviceSearchModels =new HashSet<DeviceService.DeviceSearchModel>(deviceTable.selectedDeviceModels);
                childFinished(target,new Model((Serializable) new HashSet<DeviceService.DeviceSearchModel>(selectedDeviceSearchModels)),  this);
            }
        }.setOutputMarkupPlaceholderTag(true).setVisible(false));
        if (selectionMode.equals(SelectionMode.None))
            get("setSelected").setVisible(false);
        else
            get("setSelected").setVisible(true);

        add(authorize(new AjaxLink("add") {
                          @Override
                          public void onClick(AjaxRequestTarget target) {
                              DeviceForm devForm = new DeviceForm(modal.getContentId(), new Device());
                              devForm.setParentListner(DeviceList.this);
                              modal.setContent(devForm);
                              target.appendJavaScript("$('.modal-body').css('overflow-x', 'hidden')");
                              modal.show(target);
                          }
                      }
                , WebAction.RENDER, Access.DEVICE_ADD));

        if (get("add").isVisible()) {
            if (selectionMode.equals(SelectionMode.Multiple)) {
                get("add").setVisible(false);
            } else {
                get("add").setVisible(true);
            }
        }

        add(form);
    }

    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {
        if (eventThrownCmp != null && eventThrownCmp.getId().equals("save"))
            target.add(deviceTable);
        modal.close(target);
    }

    @Override
    public Collection getSelection() {
        return null;
    }

    public static class DeviceListDataProvider extends SortableDataProvider {
        public DeviceService.DeviceCriteria criteria;

        public DeviceListDataProvider() {
            setSort("ent.id", SortOrder.ASCENDING);
        }

        public void setCriteria(DeviceService.DeviceCriteria criteria) {
            this.criteria = criteria;
        }

        @Override
        public Iterator iterator(long first, long count) {
            Session session = HibernateUtil.getCurrentSession();
            SortParam sortParam = getSort();
            return DeviceService.Instance.list(criteria, (int) first, (int) count, (String) sortParam.getProperty(), sortParam.isAscending(), session).iterator();
        }

        @Override
        public long size() {
            Session session = HibernateUtil.getCurrentSession();
            return DeviceService.Instance.count(criteria, session);
        }

        @Override
        public IModel model(Object o) {
            return new Model<Serializable>((Serializable) o);
        }

    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(JAVASCRIPT));
        response.render(CssHeaderItem.forReference(CSS));
    }

    protected boolean checkAllFieldsNull(Object obj) {
        try {
            for (Field field : obj.getClass().getFields()) {
                Object value = field.get(obj);
                if (value == null)
                    continue;

                if (value instanceof DateTime[]) {
                    DateTime[] dts = (DateTime[]) value;
                    if (!DateTime.isNullOrUnknown(dts[0]) || !DateTime.isNullOrUnknown(dts[1]))
                        return false;
                } else if (value instanceof DayDate[]) {
                    DayDate[] dts = (DayDate[]) value;
                    if (!DayDate.isNullOrUnknown(dts[0]) || !DayDate.isNullOrUnknown(dts[1]))
                        return false;
                } else if (value instanceof Long[]) {
                    Long[] lng = (Long[]) value;
                    if (lng[0] != null || lng[1] != null)
                        return false;
                } else if (value instanceof Collection) {
                    Collection col = (Collection) value;
                    if (col.size() > 0)
                        return false;
                } else
                    return false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }


}
