package com.fanap.midhco.appstore.entities;

import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;
import java.util.List;

/**
 * Created by admin123 on 6/1/2016.
 */
@Entity
@Table(name = "PC1PACKAGE")
@GenericGenerator(name = "sequence_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_APP_PACK"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = "pooled"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1000"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
        }
)
public class AppPackage extends BaseEntitiy<Long> {
    @Id
    @GeneratedValue(generator = "sequence_generator")
    @Column(name = "PAPPPACKID")
    Long id;

    @Column(name = "PVERSIONCODE")
    String versionCode;

    @Column(name = "PVERSIONNAME")
    String versionName;

    @ManyToMany
    @JoinTable(
            name = "app2ThumbFiles",
            joinColumns = @JoinColumn(name = "PAPPPACKID"),
            inverseJoinColumns = @JoinColumn(name = "PFILEID")
    )
    List<File> thumbImages;

    @ManyToOne
    @JoinColumn(name = "PICONFILEID")
    File iconFile;

    @ManyToOne
    @JoinColumn(name = "PFILEID")
    File packFile;

    @ManyToOne
    @JoinTable(
            name = "app2apppack",
            joinColumns = @JoinColumn(name = "PAPPPACKID"),
            inverseJoinColumns = @JoinColumn(name = "PAPPID")
    )
    App relatedApp;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "package2history",
            joinColumns = @JoinColumn(name = "PAPPPACKID"),
            inverseJoinColumns = @JoinColumn(name = "PAPPPACKHISTORYID")
    )
    List<AppPackageHistory> histories;


    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "package2group",
            joinColumns = @JoinColumn(name = "PAPPPACKID"),
            inverseJoinColumns = @JoinColumn(name = "PTESTGROUPID")
    )
    List<TestGroup> testGroups;

    @ManyToMany
    @JoinTable(
            name = "mm_pack2TestIssue",
            joinColumns = @JoinColumn(name = "PAPPPACKID"),
            inverseJoinColumns = @JoinColumn(name = "PTESTISSUEID")
    )
    List<TestIssue> testIssues;

    @Column(name = "PCHANGELOG")
    @Lob
    String changeLog;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "state", column = @Column(name = "PPUB_STATE"))
    })
    PublishState publishState = PublishState.UNPUBLISHED;

    @Column(name = "PMINSDK")
    String minSDK;

    @Column(name = "PTARGETSDK")
    String targetSDK;

    @Column(name = "PCERTINFO")
    @Lob
    String certificateInfo;

    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "PLASTPUBLISHDDT")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "PLASTPUBLISHDTM"))
    })
    DateTime lastPublishDate;

    @Column(name= "PPERMDETAIL")
    @Lob
    String permissionDetail;

    @Column(name = "PISDELETED")
    Boolean isDeleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public List<File> getThumbImages() {
        return thumbImages;
    }

    public void setThumbImages(List<File> thumbImages) {
        this.thumbImages = thumbImages;
    }

    public File getIconFile() {
        return iconFile;
    }

    public void setIconFile(File iconFile) {
        this.iconFile = iconFile;
    }

    public File getPackFile() {
        return packFile;
    }

    public void setPackFile(File packFile) {
        this.packFile = packFile;
    }

    public String getChangeLog() {
        return changeLog;
    }

    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }

    public PublishState getPublishState() {
        return publishState;
    }

    public void setPublishState(PublishState publishState) {
        this.publishState = publishState;
    }

    public String getMinSDK() {
        return minSDK;
    }

    public void setMinSDK(String minSDK) {
        this.minSDK = minSDK;
    }

    public String getTargetSDK() {
        return targetSDK;
    }

    public void setTargetSDK(String targetSDK) {
        this.targetSDK = targetSDK;
    }

    public String getCertificateInfo() {
        return certificateInfo;
    }

    public void setCertificateInfo(String certificateInfo) {
        this.certificateInfo = certificateInfo;
    }

    public List<AppPackageHistory> getHistories() {
        return histories;
    }

    public void setHistories(List<AppPackageHistory> histories) {
        this.histories = histories;
    }

    public List<TestGroup> getTestGroups() {
        return testGroups;
    }

    public void setTestGroups(List<TestGroup> testGroups) {
        this.testGroups = testGroups;
    }

    public List<TestIssue> getTestIssues() {
        return testIssues;
    }

    public void setTestIssues(List<TestIssue> testIssues) {
        this.testIssues = testIssues;
    }

    public DateTime getLastPublishDate() {
        return lastPublishDate;
    }

    public void setLastPublishDate(DateTime lastPublishDate) {
        this.lastPublishDate = lastPublishDate;
    }

    public String getPermissionDetail() {
        return permissionDetail;
    }

    public void setPermissionDetail(String permissionDetail) {
        this.permissionDetail = permissionDetail;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public App getRelatedApp() {
        return relatedApp;
    }

    public void setRelatedApp(App relatedApp) {
        this.relatedApp = relatedApp;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AppPackage))
            return false;

        return this.versionCode.equals(((AppPackage)o).getVersionCode());
    }

    @Override
    public int hashCode() {
        return 31 * versionCode.hashCode();
    }
}
