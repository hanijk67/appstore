package com.fanap.midhco.appstore.entities;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;
import java.util.List;

/**
 * Created by A.Moshiri on 7/17/2017.
 */
@Entity
@Table(name = "PC1PACKAGEHISTORY")
@GenericGenerator(name = "sequence_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_APP_PACK_HISTORY"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = "pooled"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1000"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
        }
)

public class AppPackageHistory extends BaseEntitiy {

    @Id
    @GeneratedValue(generator = "sequence_generator")
    @Column(name = "PAPPPACKHISTORYID")
    Long id;

    @Column(name = "PVERSIONCODE")
    String versionCode;

    @Column(name = "PVERSIONNAME")
    String versionName;

    @ManyToOne
    @JoinColumn(name = "PICONFILEID")
    File iconFile;

    @ManyToOne
    @JoinColumn(name = "PFILEID")
    File packFile;

    @Column(name = "PHASTESTISSUECHANGE")
    Boolean hasTestIssueChange;

    @Column(name = "PHASTESTCHANGE")
    Long changedTestIssue;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "MM_PACKHIST_GROUP",
            joinColumns = @JoinColumn(name = "PAPPPACKHISTORYID"),
            inverseJoinColumns = @JoinColumn(name = "PTESTGROUPID")
    )
    List<TestGroup> testGroups;

    @ManyToMany
    @JoinTable(
            name = "MM_PACKHIST_TESTISSUE" ,
            joinColumns = @JoinColumn(name = "PAPPPACKHISTORYID"),
            inverseJoinColumns = @JoinColumn(name ="PTESTISSUEID" )
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

    @ManyToMany
    @JoinTable(
            name = "appHistory2ThumbFiles",
            joinColumns = @JoinColumn(name = "PAPPPACKID"),
            inverseJoinColumns = @JoinColumn(name = "PFILEID")
    )
    List<File> thumbImages;


    @Column(name = "PICONFILEPATH")
    String iconFilePath;

    @Column(name = "PPACKFILEPATH")
    String packFilePath;

    @Override
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

    public Boolean getHasTestIssueChange() {
        return hasTestIssueChange;
    }

    public void setHasTestIssueChange(Boolean hasTestIssueChange) {
        this.hasTestIssueChange = hasTestIssueChange;
    }

    public Long getChangedTestIssue() {
        return changedTestIssue;
    }

    public void setChangedTestIssue(Long changedTestIssue) {
        this.changedTestIssue = changedTestIssue;
    }

    public List<File> getThumbImages() {
        return thumbImages;
    }

    public void setThumbImages(List<File> thumbImages) {
        this.thumbImages = thumbImages;
    }

    public String getIconFilePath() {
        return iconFilePath;
    }

    public void setIconFilePath(String iconFilePath) {
        this.iconFilePath = iconFilePath;
    }

    public String getPackFilePath() {
        return packFilePath;
    }

    public void setPackFilePath(String packFilePath) {
        this.packFilePath = packFilePath;
    }
}
