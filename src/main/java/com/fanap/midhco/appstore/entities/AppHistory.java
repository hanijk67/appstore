package com.fanap.midhco.appstore.entities;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;
import java.util.List;

/**
 * Created by A.Moshiri on 7/12/2017.
 */
@Entity
@Table(name = "PC1APPHISTORY")
@GenericGenerator(name = "sequence_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_APPHISTORY"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = "pooled"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1000"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
        }
)
public class AppHistory extends BaseEntitiy<Long> {

    @Id
    @GeneratedValue(generator = "sequence_generator")
    @Column(name = "PAPPHISTORYID")
    Long id;

    @Column(name = "PPACKNAME")
    String packageName;

    @Column(name = "PTITLE")
    String title;

    @Lob
    @Column(name = "PAPPDESC")
    String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "mm_appHistory_apppack",
            joinColumns = @JoinColumn(name = "PAPPHISTORYID"),
            inverseJoinColumns = @JoinColumn(name = "PAPPPACKID")
    )
    List<AppPackage> appPackages;

    @ManyToOne
    @JoinColumn(name = "PMAINPACKID")
    AppPackage mainPackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PDEVELOPER")
    protected User developer;

    @ManyToOne
    @JoinColumn(name = "POSTYP")
    OSType osType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "P_OS")
    OS os;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PAPPCATID")
    AppCategory appCategory;



    @Override
    public Long getId() {
        return id;
    }

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

    public OSType getOsType() {
        return osType;
    }

    public void setOsType(OSType osType) {
        this.osType = osType;
    }

    public OS getOs() {
        return os;
    }

    public void setOs(OS os) {
        this.os = os;
    }

    public AppCategory getAppCategory() {
        return appCategory;
    }

    public void setAppCategory(AppCategory appCategory) {
        this.appCategory = appCategory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppHistory that = (AppHistory) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
