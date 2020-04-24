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
@Table(name = "PC1APP")
@GenericGenerator(name = "sequence_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_APP"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = "pooled"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1000"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
        }
)
public class App extends BaseEntitiy<Long> {
    @Id
    @GeneratedValue(generator = "sequence_generator")
    @Column(name = "PAPPID")
    Long id;

    @Column(name = "PPACKNAME")
    String packageName;

    @Column(name = "PTITLE")
    String title;

    @Column(name = "PSHORTDESCRIOTION")
    String shortDescription;

    @Lob
    @Column(name = "PAPPDESC")
    String description;

    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "PMAINPACKMODDT")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "PMAINPACKMODTM"))
    })
    DateTime mainPackageModificationDate;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "app2apppack",
            joinColumns = @JoinColumn(name = "PAPPID"),
            inverseJoinColumns = @JoinColumn(name = "PAPPPACKID")
    )
    List<AppPackage> appPackages;

    @OneToOne
    @JoinColumn(name = "PMAINPACKID")
    AppPackage mainPackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PDEVELOPER")
    protected User developer;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "app2comment",
            joinColumns = @JoinColumn(name = "PAPPID"),
            inverseJoinColumns = @JoinColumn(name = "PCOMMENTID")
    )
    List<Comment> comments;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "app2history",
            joinColumns = @JoinColumn(name = "PAPPID"),
            inverseJoinColumns = @JoinColumn(name = "PAPPHISTORYID")
    )
    List<AppHistory> histories;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "app2rate",
            joinColumns = @JoinColumn(name = "PAPPID"),
            inverseJoinColumns = @JoinColumn(name = "PRATEID")
    )
    List<Rate> rates;

    @ManyToOne
    @JoinColumn(name = "POSTYP")
    OSType osType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "P_OS")
    OS os;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PAPPCATID")
    AppCategory appCategory;

    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "PRELATEDCALCDT")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "PRELATEDCALCTM"))
    })
    DateTime relatedAppCalculationDate;

    @Column(name = "PHASSCHEDULER")
    Boolean hasScheduler;

    @Column(name = "PISDELETED")
    Boolean isDeleted;

    public void setId(Long id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<AppPackage> getAppPackages() {
        return appPackages;
    }

    public void setAppPackages(List<AppPackage> appPackages) {
        this.appPackages = appPackages;
    }

    public AppPackage getMainPackage() {
        return mainPackage;
    }

    public void setMainPackage(AppPackage mainPackage) {
        this.mainPackage = mainPackage;
    }

    public User getDeveloper() {
        return developer;
    }

    public void setDeveloper(User developer) {
        this.developer = developer;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<Rate> getRates() {
        return rates;
    }

    public void setRates(List<Rate> rates) {
        this.rates = rates;
    }

    public Long getId() {
        return id;
    }

    public OS getOs() {
        return os;
    }

    public void setOs(OS os) {
        this.os = os;
    }

    public DateTime getMainPackageModificationDate() {
        return mainPackageModificationDate;
    }

    public void setMainPackageModificationDate(DateTime mainPackageModificationDate) {
        this.mainPackageModificationDate = mainPackageModificationDate;
    }

    public OSType getOsType() {
        return osType;
    }

    public void setOsType(OSType osType) {
        this.osType = osType;
    }

    public AppCategory getAppCategory() {
        return appCategory;
    }

    public void setAppCategory(AppCategory appCategory) {
        this.appCategory = appCategory;
    }

    public List<AppHistory> getHistories() {
        return histories;
    }

    public void setHistories(List<AppHistory> histories) {
        this.histories = histories;
    }

    public DateTime getRelatedAppCalculationDate() {
        return relatedAppCalculationDate;
    }

    public void setRelatedAppCalculationDate(DateTime relatedAppCalculationDate) {
        this.relatedAppCalculationDate = relatedAppCalculationDate;
    }


    public Boolean getHasScheduler() {
        return hasScheduler;
    }

    public void setHasScheduler(Boolean hasScheduler) {
        this.hasScheduler = hasScheduler;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}
