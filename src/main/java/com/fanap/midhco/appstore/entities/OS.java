package com.fanap.midhco.appstore.entities;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;
import java.util.List;

/**
 * Created by admin123 on 6/5/2016.
 */
@Entity
@Table(name = "PF1OS" , uniqueConstraints = @UniqueConstraint(columnNames="POSCODE"))
@GenericGenerator(name = "sequence_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_OS"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = "pooled"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1000"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
        }
)
public class OS extends BaseEntitiy<Long> {
    @Id
    @GeneratedValue(generator = "sequence_generator")
    @Column(name = "POSID")
    Long id;

    @Column(name = "POSNAME")
    String osName;

    @Column(name = "POSCODE")
    String osCode;

    @Column(name = "POSVER")
    String osVersion;

    @ManyToOne
    @JoinColumn(name = "POSTYP")
    OSType osType;

    @Column(name = "PDISABLED")
    Boolean disabled;

    @Column(name = "PHANDLERAPPDOWNLOADPATH")
    String handlerAppDownloadPath;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentOS")
    List<HandlerApp> handlerApps;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public OSType getOsType() {
        return osType;
    }

    public void setOsType(OSType osType) {
        this.osType = osType;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public String getHandlerAppDownloadPath() {
        return handlerAppDownloadPath;
    }

    public String getOsCode() {
        return osCode;
    }

    public void setOsCode(String osCode) {
        this.osCode = osCode;
    }

    public void setHandlerAppDownloadPath(String handlerAppDownloadPath) {
        this.handlerAppDownloadPath = handlerAppDownloadPath;
    }

    public List<HandlerApp> getHandlerApps() {
        return handlerApps;
    }

    public void setHandlerApps(List<HandlerApp> handlerApps) {
        this.handlerApps = handlerApps;
    }

    @Override
    public String toString() {
        return osName;
    }
}
