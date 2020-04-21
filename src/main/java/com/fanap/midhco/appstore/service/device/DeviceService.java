package com.fanap.midhco.appstore.service.device;

import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.service.BaseEntityService;
import com.fanap.midhco.appstore.service.HQLBuilder;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.os.OSService;
import com.fanap.midhco.appstore.service.osType.OSTypeService;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.component.multiAjaxFileUpload2.UploadedFileInfo;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by A.Moshiri on 6/3/2017.
 */
public class DeviceService {
    public static DeviceService Instance = new DeviceService();

    private DeviceService() {
    }

    public static class DeviceCriteria implements Serializable {
        public String title;
        public Collection<OSType> osType;
        public Collection<OS> os;
        public Collection<User> usedBy;
        public String imei;
        public DeviceState state;
        public Boolean active;
//        public Collection<User> active;
        public Long id;

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Collection<OSType> getOsType() {
            return osType;
        }

        public void setOsType(Collection<OSType> osType) {
            this.osType = osType;
        }

        public Collection<OS> getOs() {
            return os;
        }

        public void setOs(Collection<OS> os) {
            this.os = os;
        }

        public Collection<User> getUsedBy() {
            return usedBy;
        }

        public void setUsedBy(Collection<User> usedBy) {
            this.usedBy = usedBy;
        }

        public String getImei() {
            return imei;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        public DeviceState getDeviceStates() {
            return state;
        }

        public void setDeviceStates(DeviceState deviceStates) {
            this.state = deviceStates;
        }
    }

    public void applyCriteria(HQLBuilder builder, DeviceCriteria deviceCriteria) {

        if (deviceCriteria.title != null && !deviceCriteria.title.trim().isEmpty())
            builder.addClause("and ent.title like (:title_)", "title_", HQLBuilder.like(deviceCriteria.title));

        if (deviceCriteria.imei != null && !deviceCriteria.imei.isEmpty())
            builder.addClause("and  ent.imei like (:imei_)", "imei_", HQLBuilder.like(deviceCriteria.imei));

        if (deviceCriteria.os != null && deviceCriteria.os.size()>0)
            builder.addClause("and ent.os in (:os_)", "os_", deviceCriteria.os);

        if (deviceCriteria.osType != null && deviceCriteria.osType.size()>0)
            builder.addClause("and ent.osType in (:osType_)", "osType_", deviceCriteria.osType);

        if (deviceCriteria.state != null)
            builder.addClause("and ent.deviceState in (:deviceState_)", "deviceState_", deviceCriteria.state);

        if (deviceCriteria.usedBy != null && deviceCriteria.usedBy.size()>0)
            builder.addClause("and ent.usedBy in (:usedBy_)", "usedBy_", deviceCriteria.usedBy);

        if (deviceCriteria.getActive()!=null) {
        builder.addClause("and ent.active in (:active_)", "active_", deviceCriteria.active);
        }

    }

    public Long count(DeviceCriteria criteria, Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select count(ent.id) ", " from Device ent ");
        if (criteria != null)
            applyCriteria(builder, criteria);

        Query query = builder.createQuery();
        return (Long) query.uniqueResult();
    }

    public List<DeviceSearchModel> list(DeviceCriteria criteria, int first, int count, String sortProp, boolean isAsc, Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select ent ", "from Device ent ");
        if (criteria != null)
            applyCriteria(builder, criteria);

        Query query = builder.createQuery();

        if (sortProp != null)
            builder.addOrder(sortProp, isAsc);

        query.setFirstResult(first);
        if (count != -1)
            query.setMaxResults(count);

        List<Object> resultObjects = query.list();
        List<DeviceSearchModel> deviceSearchModelList =new ArrayList<>();

        for(Object obj : resultObjects){
            DeviceSearchModel searchResultModel = new DeviceSearchModel();
            Device device = (Device) obj;
            searchResultModel.id = device.getId();
            if (device.getUsedBy()!=null) {
                User user = (User) session.load(User.class , device.getUsedBy().getId());
                user.getContact();
                searchResultModel.usedBy = user;
            }
            if (device.getOsType() != null) {
                searchResultModel.osType = device.getOsType().getName();
            }
            searchResultModel.title = device.getTitle();
            if (device.getImageFile() != null) {
                searchResultModel.imagePath = device.getImageFile().getFilePath();
            }
            searchResultModel.imei = device.getImei();
            if (device.getOs() != null) {
                searchResultModel.os = device.getOs().getOsName();
            }
            if(device.getDeviceState()!=null){
                searchResultModel.state = device.getDeviceState();
            }
            if (device.getActive()!=null) {
            searchResultModel.setActive(device.getActive());
            }

            deviceSearchModelList.add(searchResultModel);
        }

        return deviceSearchModelList;
    }

    public List<Device> listAll(Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select ent ", "from Device ent ");
        Query query = builder.createQuery();
        return query.list();
    }

    public void saveOrUpdate(Device device, Session session) {
        if (device.getId() == null) {
            device.setCreationDate(DateTime.now());
            device.setCreatorUser(PrincipalUtil.getCurrentUser());
        } else {
            device.setLastModifyDate(DateTime.now());
            device.setLastModifyUser(PrincipalUtil.getCurrentUser());
        }
        session.saveOrUpdate(device);
    }

    public static class DeviceSearchModel implements Serializable {
        Long id;
        String title;
        String os;
        String osType;
        String imei;
        User usedBy;
        String imagePath;
        public UploadedFileInfo imageFileInfo;
        public DeviceState state;
        boolean selected;

        public boolean getActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        boolean active
                ;

        public boolean getSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getOs() {
            return os;
        }

        public void setOs(String os) {
            this.os = os;
        }

        public String getOsType() {
            return osType;
        }

        public void setOsType(String osType) {
            this.osType = osType;
        }

        public String getImei() {
            return imei;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        public User getUsedBy() {
            return usedBy;
        }

        public void setUsedBy(User usedBy) {
            this.usedBy = usedBy;
        }

        public String getImagePath() {
            return imagePath;
        }

        public void setImagePath(String imagePath) {
            this.imagePath = imagePath;
        }

        public UploadedFileInfo getImageFileInfo() {
            return imageFileInfo;
        }

        public void setImageFileInfo(UploadedFileInfo imageFileInfo) {
            this.imageFileInfo = imageFileInfo;
        }

        public DeviceState getState() {
            return state;
        }

        public void setState(DeviceState state) {
            this.state = state;
        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DeviceSearchModel that = (DeviceSearchModel) o;
            return id.equals(that.id);
        }
    }

    public static void main(String[] args) {

        Device dev = new Device();
        Device secondDev = new Device();
        Device thirdDev = new Device();
        Device forthDev = new Device();

        dev.setUsedBy(PrincipalUtil.getCurrentUser());
        secondDev.setUsedBy(PrincipalUtil.getCurrentUser());
        thirdDev.setUsedBy(PrincipalUtil.getCurrentUser());
        forthDev.setUsedBy(PrincipalUtil.getCurrentUser());

        Session session = HibernateUtil.getCurrentSession();

        dev.setImei("123456789012345");
        dev.setTitle("Motorolla");
        secondDev.setTitle("samsung");
        thirdDev.setTitle("Sony");
        forthDev.setTitle("Lg");
        dev.setActive(true);
        secondDev.setActive(true);
        thirdDev.setActive(true);
        forthDev.setActive(true);
        secondDev.setImei("123456789012345");
        thirdDev.setImei("123456789012345");
        forthDev.setImei("123456789012345");
        List<OS> osList = OSService.Instance.listAll(session);

        if (osList != null && osList.size() > 0) {
            dev.setOs(osList.get(0));
            secondDev.setOs(osList.get(0));
            thirdDev.setOs(osList.get(0));
            forthDev.setOs(osList.get(0));
        }
        List<OSType> osTypeList = OSTypeService.Instance.listAll();
        if (osTypeList != null && osTypeList.size() > 0) {
            dev.setOsType(osTypeList.get(0));
            secondDev.setOsType(osTypeList.get(0));
            thirdDev.setOsType(osTypeList.get(0));
            forthDev.setOsType(osTypeList.get(0));
        }
        dev.setDeviceState(DeviceState.FREE);
        secondDev.setDeviceState(DeviceState.FREE);
        thirdDev.setDeviceState(DeviceState.FREE);
        forthDev.setDeviceState(DeviceState.FREE);
        Transaction tx = session.beginTransaction();
        File imageFile;
        File secondImageFile;
        try {
            imageFile = new File();
            imageFile.setFileName("name");
            imageFile.setFilePath("CAF3D97E6F157630BCED33158CD363C07.126443869020732E15");
            imageFile.setStereoType(StereoType.THUMB_FILE);

            secondImageFile = new File();
            secondImageFile.setFileName("second name");
            secondImageFile.setFilePath("CAF3D97E6F157630BCED33158CD363C07.126443869020732E15");
            secondImageFile.setStereoType(StereoType.THUMB_FILE);
            BaseEntityService.Instance.saveOrUpdate(imageFile, session);
            BaseEntityService.Instance.saveOrUpdate(secondImageFile, session);
            tx.commit();
        } finally {
            session.close();
        }

        Session newSession = HibernateUtil.getNewSession();
        try {
            tx = newSession.beginTransaction();
            dev.setImageFile(imageFile);
            newSession.saveOrUpdate(dev);
            secondDev.setImageFile(secondImageFile);
            newSession.saveOrUpdate(secondDev);
            thirdDev.setImageFile(imageFile);
            newSession.saveOrUpdate(thirdDev);
            forthDev.setImageFile(secondImageFile);
            newSession.saveOrUpdate(forthDev);
            tx.commit();
        } finally {
            newSession.close();
        }
        System.exit(0);
    }
}