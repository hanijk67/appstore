package com.fanap.midhco.appstore.entities;

import com.fanap.midhco.appstore.entities.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;

/**
 * Created by A.Moshiri on 5/30/2017.
 */
@Entity
@Table(name = "PC1DEVICE")
@GenericGenerator(name = "sequence_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_DEVICE"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = "pooled"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1000"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
        }
)

public class Device extends BaseEntitiy<Long> {

    @Id
    @GeneratedValue(generator = "sequence_generator")
    @Column(name = "PDEVICEID")
    Long id;

    @Column(name = "PTITLE")
    String title;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PUSED_BYID")
    User usedBy;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "P_OSID")
    OS os;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "P_OSTYPEID")
    OSType osType;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "state", column = @Column(name = "PDEV_STATE"))
    })
    DeviceState deviceState = DeviceState.FREE;

    @Column(name = "PIMEI")
    String imei;

    @ManyToOne
    @JoinColumn(name = "PIMAGEFILEID")
    File imageFile;

    @Column(name = "PACTIVE")
    Boolean active;

    @Override
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

    public User getUsedBy() {
        return usedBy;
    }

    public void setUsedBy(User usedBy) {
        this.usedBy = usedBy;
    }

    public OS getOs() {
        return os;
    }

    public void setOs(OS os) {
        this.os = os;
    }

    public OSType getOsType() {
        return osType;
    }

    public void setOsType(OSType osType) {
        this.osType = osType;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public File getImageFile() {
        return imageFile;
    }

    public void setImageFile(File imageFile) {
        this.imageFile = imageFile;
    }

    public DeviceState getDeviceState() {
        return deviceState;
    }

    public void setDeviceState(DeviceState deviceState) {
        this.deviceState = deviceState;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
