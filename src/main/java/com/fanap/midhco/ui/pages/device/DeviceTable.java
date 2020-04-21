package com.fanap.midhco.ui.pages.device;

import com.fanap.midhco.appstore.applicationUtils.AppUtils;
import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.Device;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.device.DeviceService;
import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.ajaxLinkPanel.AjaxLinkPanel;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.component.table.MyPagingNavigator;
import org.apache.commons.collections.IteratorUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.hibernate.Query;
import org.hibernate.Session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by A.Moshiri on 6/6/2017.
 */
public class DeviceTable extends FormComponentPanel implements IPageable, IParentListner {
    long currentPageNumber = 0;
    int pageCount = 0;
    Form form;
    BootStrapModal modal;
    MyPagingNavigator myPagingNavigator;
    DeviceList.DeviceListDataProvider globalDeviceDataProvider;
    List<DeviceService.DeviceSearchModel> deviceList = new ArrayList<>();
    Model deviceListModel = new Model();
    List<DeviceService.DeviceSearchModel> selectedDeviceModels = new ArrayList<>();
    long deviceSize = 0;
    PageableListView listView;
    private static ResourceReference JAVASCRIPT = new JavaScriptResourceReference(
            DeviceList.class, "res/DeviceList.js");
    private static CssResourceReference CSS = new CssResourceReference(DeviceList.class, "" +
            "res/DeviceList.css");

    protected DeviceTable(String id, DeviceList.DeviceListDataProvider deviceDataProvider, DeviceService.DeviceCriteria deviceCriteria, int itemPerPage, Long devSize, SelectionMode selectionMode) {
        super(id);
        pageCount = itemPerPage;
        deviceSize = devSize == null ? 0 : devSize;
        form = new Form("devForm");
        add(form);

        WebMarkupContainer span = new WebMarkupContainer("span");
        form.add(span);
        span.add(new AttributeModifier("colspan", new Model(String.valueOf(deviceList.size()))));
        deviceDataProvider.setCriteria(deviceCriteria);
        this.globalDeviceDataProvider = deviceDataProvider;
        listView = new PageableListView<DeviceService.DeviceSearchModel>("rows", deviceListModel, itemPerPage) {
            @Override
            protected void populateItem(ListItem<DeviceService.DeviceSearchModel> item) {
                Label tmpPathLabel = new Label("imgPath", new PropertyModel(item.getModel(), "imagePath"));
                Label activeLabel = new Label("active", new PropertyModel(item.getModel(), "active"));
                DeviceService.DeviceSearchModel deviceSearchModelInList = item.getModelObject();
                String activeStr = activeLabel.getDefaultModelObjectAsString();
                String active;
                if (activeStr.equals(new ResourceModel("label.true").getObject())) {
                    active = new ResourceModel("label.enabled").getObject();
                } else if (activeStr.equals(new ResourceModel("label.false").getObject())) {
                    active = new ResourceModel("label.disabled").getObject();
                } else {
                    active = new ResourceModel("label.unknown").getObject();
                }
                item.add(new NonCachingImage("image", new DynamicImageResource() {
                    @Override
                    protected byte[] getImageData(Attributes attributes) {
                        return AppUtils.getImageAsBytes(FileServerService.FILE_DOWNLOAD_SERVER_PATH.replace("${key}", tmpPathLabel.getDefaultModelObjectAsString()));
                    }
                }));
                item.add(new AjaxCheckBox("selectedItem", new PropertyModel(item.getModel(), "selected")) {
                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        boolean isSelected = getConvertedInput();
                        DeviceService.DeviceSearchModel deviceSearchModelInList = item.getModelObject();
                        if (isSelected) {
                            selectedDeviceModels.add(deviceSearchModelInList);
                        } else {
                            if (selectedDeviceModels.contains(deviceSearchModelInList)) {
                                selectedDeviceModels.remove(deviceSearchModelInList);
                            }
                        }
                    }
                });

                if(selectionMode.equals(SelectionMode.Multiple)){
                    item.add(new Label("editDevice", new Model<>("")));
                }
                else if (PrincipalUtil.hasPermission(Access.DEVICE_EDIT)) {
                    final DeviceService.DeviceSearchModel searchResultModel = (DeviceService.DeviceSearchModel) deviceSearchModelInList;
                    item.add(new AjaxLinkPanel("editDevice", AjaxLinkPanel.Image.Edit) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            Long devId = searchResultModel.getId();
                            Session session = HibernateUtil.getCurrentSession();
                            Device device = (Device) session.load(Device.class, devId);
                            DeviceForm deviceForm = new DeviceForm(modal.getContentId(), device);
                            deviceForm.setParentListner(DeviceTable.this);
                            modal.setContent(deviceForm);
                            modal.show(target);
                        }
                    });
                } else {
                    item.add(new Label("editDevice", new Model<>("")));
                }

                item.add(new Label("title", new PropertyModel(item.getModel(), "title")));
                item.add(new Label("state", new PropertyModel(item.getModel(), "state")));
                item.add(new Label("usedBy", new PropertyModel(item.getModel(), "usedBy")));
                item.add(new Label("os", new PropertyModel(item.getModel(), "os")));
                item.add(new Label("osType", new PropertyModel(item.getModel(), "osType")));
                item.add(new Label("active", active));
                item.add(new Label("imei", new PropertyModel(item.getModel(), "imei")));
            }
        };
        listView.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        myPagingNavigator= new MyPagingNavigator("navigator",  listView);
        myPagingNavigator.setVisible(false);
        span.add(myPagingNavigator);

        modal = new BootStrapModal("modal");
        add(modal);
        form.add(listView);
    }

    @Override
    protected void onBeforeRender() {
        long fromItemNo = 0, toItemNo = 0;
        fromItemNo = (currentPageNumber * pageCount);
        toItemNo =deviceSize;
        Iterator<DeviceService.DeviceSearchModel> deviceIterator = globalDeviceDataProvider.iterator(fromItemNo, toItemNo);
        deviceList.clear();
        deviceList = IteratorUtils.toList(deviceIterator);
        deviceListModel.setObject((Serializable) deviceList);
        if(!deviceList.isEmpty()) {
            myPagingNavigator.setVisible(true);
        }else {
            myPagingNavigator.setVisible(false);
        }
        super.onBeforeRender();
    }


    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(JAVASCRIPT));
        response.render(CssHeaderItem.forReference(CSS));
    }

    @Override
    public long getCurrentPage() {
        return currentPageNumber;
    }

    @Override
    public void setCurrentPage(long page) {
        Long pageLong = page;
        this.currentPageNumber = pageLong == null ? 0 : pageLong;
    }

    @Override
    public long getPageCount() {
        return pageCount;
    }

    @Override
    public void convertInput() {
        List<Device> selectedDevices;
        List<Long> selectedDeviceIds = new ArrayList<>();

        for (DeviceService.DeviceSearchModel deviceSearchModel : selectedDeviceModels) {
            Long deviceId = deviceSearchModel.getId();
            selectedDeviceIds.add(deviceId);
        }

        if (!selectedDeviceIds.isEmpty()) {
            String queryString = "select device from Device device where device.id in (:deviceIds_)";
            Query query = HibernateUtil.getCurrentSession().createQuery(queryString);
            query.setParameterList("deviceIds_", selectedDeviceIds);
            selectedDevices = query.list();
        } else
            selectedDevices = new ArrayList<>();

        setConvertedInput(selectedDevices);
    }

    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {
        if (eventThrownCmp != null && eventThrownCmp.getId().equals("save"))
            target.add(this);
        modal.close(target);
    }
}
