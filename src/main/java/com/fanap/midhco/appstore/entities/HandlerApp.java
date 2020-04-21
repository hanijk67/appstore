package com.fanap.midhco.appstore.entities;


import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;

/**
 * Created by A.Moshiri on 8/14/2017.
 */
@Entity
@Table(name = "PF1HANDLERAPP", uniqueConstraints = @UniqueConstraint(columnNames = {"PVERSIONCODE", "POSID", "PORGID", "PENVID"}))
@GenericGenerator(name = "sequence_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_HANDLERAPP"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = "pooled"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1000"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
        }
)
public class HandlerApp extends BaseEntitiy<Long> {
    @Id
    @GeneratedValue(generator = "sequence_generator")
    @Column(name = "PHANDLERAPPID")
    Long id;

    @Column(name = "PVERSIONCODE")
    Long versionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POSID")
    OS parentOS;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PORGID")
    Organization organization;

    @OneToOne
    @JoinColumn(name = "PENVID")
    OSEnvironment osEnvironment;

    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "PUPLOADEDFILEDDT")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "PUPLOADEDFILEDTM"))
    })
    DateTime uploadedFileDate;


    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "PUPLOADEDTESTFILEDDT")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "PUPLOADEDTESTFILEDTM"))
    })
    DateTime uploadedTestFileDate;

    @Column(name = "PISACTIVE")
    Boolean isActive;

    @Column(name = "PISDEFAULTFORORG")
    Boolean isDefaultForOrganization;

    @OneToOne
    @JoinColumn(name = "PFILEID")
    File handlerFile;

    @OneToOne
    @JoinColumn(name = "PTESTFILEID")
    File testHandlerFile;

    @OneToOne
    @JoinColumn(name = "PFILEID32BIT")
    File handlerFile32bit;

    @OneToOne
    @JoinColumn(name = "PTESTFILEID32BIT")
    File testHandlerFile32bit;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(Long versionCode) {
        this.versionCode = versionCode;
    }

    public DateTime getUploadedFileDate() {
        return uploadedFileDate;
    }

    public void setUploadedFileDate(DateTime uploadedFileDate) {
        this.uploadedFileDate = uploadedFileDate;
    }

    public DateTime getUploadedTestFileDate() {
        return uploadedTestFileDate;
    }

    public void setUploadedTestFileDate(DateTime uploadedTestFileDate) {
        this.uploadedTestFileDate = uploadedTestFileDate;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public File getHandlerFile() {
        return handlerFile;
    }

    public void setHandlerFile(File handlerFile) {
        this.handlerFile = handlerFile;
    }

    public File getTestHandlerFile() {
        return testHandlerFile;
    }

    public void setTestHandlerFile(File testHandlerFile) {
        this.testHandlerFile = testHandlerFile;
    }

    public OS getParentOS() {
        return parentOS;
    }

    public void setParentOS(OS parentOS) {
        this.parentOS = parentOS;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public OSEnvironment getOsEnvironment() {
        return osEnvironment;
    }

    public void setOsEnvironment(OSEnvironment osEnvironment) {
        this.osEnvironment = osEnvironment;
    }

    public Boolean getDefaultForOrganization() {
        return isDefaultForOrganization;
    }

    public void setDefaultForOrganization(Boolean defaultForOrganization) {
        isDefaultForOrganization = defaultForOrganization;
    }

    public File getHandlerFile32bit() {
        return handlerFile32bit;
    }

    public void setHandlerFile32bit(File handlerFile32bit) {
        this.handlerFile32bit = handlerFile32bit;
    }

    public File getTestHandlerFile32bit() {
        return testHandlerFile32bit;
    }

    public void setTestHandlerFile32bit(File testHandlerFile32bit) {
        this.testHandlerFile32bit = testHandlerFile32bit;
    }
}
