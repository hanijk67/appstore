package com.fanap.midhco.ui.pages.device;

import com.fanap.midhco.appstore.service.device.DeviceService;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

import java.util.List;
import java.util.Set;

/**
 * Created by A.Moshiri on 5/30/2017.
 */
@Authorize(view = Access.DEVICE_LIST)
public class DevList extends BasePanel implements IParentListner{
    DeviceList myDeviceList;
    Form form;
    List<DeviceService.DeviceSearchModel> selectedDeviceSearchModelList ;
    BootStrapModal modal;
    public DevList() {
        this(MAIN_PANEL_ID);
    }

    public DevList(String id) {
        super(id);
        form = new Form("form");

        setPageTitle(new ResourceModel("Device.management").getObject());

        modal = new BootStrapModal("modal");
        add(modal);
        myDeviceList = new DeviceList("devLst" , new DeviceService.DeviceCriteria(), SelectionMode.None);
        myDeviceList.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        myDeviceList.setParentListner(this);
        form.add(myDeviceList);

        add(form);
    }

    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {
        if (eventThrownCmp != null && eventThrownCmp.getId().equals("selectedItem")) {
            Set<DeviceService.DeviceSearchModel> deviceSearchModels = (Set<DeviceService.DeviceSearchModel>) childModel.getObject();
            if (myDeviceList!=null && myDeviceList.getSelection()!=null) {
                List<DeviceService.DeviceSearchModel> deviceSearchModelList = (List<DeviceService.DeviceSearchModel>) myDeviceList.getSelection();
                for(DeviceService.DeviceSearchModel deviceSearchModel : deviceSearchModelList){
                        // in this loop we have selected Devices
                }
            }

        }
        modal.close(target);
    }
}
