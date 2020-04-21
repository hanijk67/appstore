package com.fanap.midhco.appstore.entities;

import com.fanap.midhco.appstore.entities.helperClasses.AnouncementType;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;
import java.util.List;

/**
 * Created by admin123 on 8/28/2017.
 */
@Entity
@Table(name = "PC1ANOUNCEMENT")
@GenericGenerator(name = "sequence_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_ANOUNCEMENT"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = "pooled"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1000"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
        }
)
public class Anouncement extends BaseEntitiy<Long> {
    @Id
    @GeneratedValue(generator = "sequence_generator")
    @Column(name = "PANOUNCEMENTID")
    Long id;

    @Column(name = "PANOUNCMENTFILEKEY")
    String anouncementImageFileKey;

    @Column(name = "PACTIONCATEGORY")
    String actionCategory;

    @Column(name="PACTIONDESCRIPTOR")
    @Lob
    String actionDescriptor;

    @Column(name="PANOUNCEMENTTEXT")
    String anouncementText;

    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "PEXPIREDT")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "PEXPIRETM"))
    })
    DateTime expireDateTime;


    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "PSTARTDT")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "PSTARTTM"))
    })
    DateTime startDateTime;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "type", column = @Column(name = "PANOUNCE_TYPE"))
    })
    AnouncementType anouncementType;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "mm_anouncement_osType",
            joinColumns = @JoinColumn(name = "PANOUNCEMENTID"),
            inverseJoinColumns = @JoinColumn(name = "POSTYPID")
    )
    List<OSType> osTypes;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "mm_anouncement_organinzation",
            joinColumns = @JoinColumn(name = "PANOUNCEMENTID"),
            inverseJoinColumns = @JoinColumn(name = "PPORGID")
    )
    List<Organization> organizations;



    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "mm_anouncement_environment",
            joinColumns = @JoinColumn(name = "PANOUNCEMENTID"),
            inverseJoinColumns = @JoinColumn(name = "PENVID")
    )
    List<OSEnvironment> osEnvironments;


    @Column(name = "PISACTIVE")
    Boolean isActive;

    @Transient
    Boolean isExpired;



    public void setId(Long id) {
        this.id = id;
    }

    public String getAnouncementImageFileKey() {
        return anouncementImageFileKey;
    }

    public void setAnouncementImageFileKey(String anouncementImageFileKey) {
        this.anouncementImageFileKey = anouncementImageFileKey;
    }

    public String getActionCategory() {
        return actionCategory;
    }

    public void setActionCategory(String actionCategory) {
        this.actionCategory = actionCategory;
    }

    public String getActionDescriptor() {
        return actionDescriptor;
    }

    public void setActionDescriptor(String actionDescriptor) {
        this.actionDescriptor = actionDescriptor;
    }

    public String getAnouncementText() {
        return anouncementText;
    }

    public void setAnouncementText(String anouncementText) {
        this.anouncementText = anouncementText;
    }

    public DateTime getExpireDateTime() {
        return expireDateTime;
    }

    public void setExpireDateTime(DateTime expireDateTime) {
        this.expireDateTime = expireDateTime;
    }

    public DateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(DateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Boolean getExpired() {
        return isExpired;
    }

    public void setExpired(Boolean expired) {
        isExpired = expired;
    }

    public AnouncementType getAnouncementType() {
        return anouncementType;
    }

    public void setAnouncementType(AnouncementType anouncementType) {
        this.anouncementType = anouncementType;
    }

    public List<OSType> getOsTypes() {
        return osTypes;
    }

    public void setOsTypes(List<OSType> osTypes) {
        this.osTypes = osTypes;
    }

    public List<Organization> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<Organization> organizations) {
        this.organizations = organizations;
    }

    public List<OSEnvironment> getOsEnvironments() {
        return osEnvironments;
    }

    public void setOsEnvironments(List<OSEnvironment> osEnvironments) {
        this.osEnvironments = osEnvironments;
    }

    @Override
    public Long getId() {
        return id;
    }
}
