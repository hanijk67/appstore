package com.fanap.midhco.appstore.entities;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;

/**
 * Created by admin123 on 9/11/2017.
 */
@Entity
@Table(name = "PC1APPINSTALLREPORTQUEUE")
@GenericGenerator(name = "sequence_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_APP_INSTALL_QUEUE"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = "pooled"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1000"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
        }
)
public class AppInstallReportQueue extends BaseEntitiy<Long> {
    @Id
    @GeneratedValue(generator = "sequence_generator")
    @Column(name = "PAPPINSTALLQUEUEID")
    Long id;

    @Column(name = "PDEVICEID")
    String deviceId;

    @Column(name = "PISDELETED")
    boolean isDeleted;

    @Column(name = "PAPPINSTALLJSONSTRING")
    @Lob
    String appInstallJsonString;

    @ManyToOne
    @JoinColumn(name = "POSTYP")
    OSType osType;

    @Column(name="PSSOUSERNAME")
    String ssoUserName;

    @Column(name="PSSOUSERID")
    Long ssoUserId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String getAppInstallJsonString() {
        return appInstallJsonString;
    }

    public void setAppInstallJsonString(String appInstallJsonString) {
        this.appInstallJsonString = appInstallJsonString;
    }

    public OSType getOsType() {
        return osType;
    }

    public void setOsType(OSType osType) {
        this.osType = osType;
    }

    public String getSsoUserName() {
        return ssoUserName;
    }

    public void setSsoUserName(String ssoUserName) {
        this.ssoUserName = ssoUserName;
    }

    public Long getSsoUserId() {
        return ssoUserId;
    }

    public void setSsoUserId(Long ssoUserId) {
        this.ssoUserId = ssoUserId;
    }
}
