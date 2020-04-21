package com.fanap.midhco.appstore.entities;

import com.fanap.midhco.appstore.service.HibernateUtil;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;

/**
 * Created by A.Moshiri on 2/26/2018.
 */
@Entity
@Table(name = "PF1ORG")
@GenericGenerator(name = "sequence_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_ORG"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = "pooled"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1000"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
        }
)
public class Organization extends BaseEntitiy<Long> {

    @Id
    @GeneratedValue(generator = "sequence_generator")
    @Column(name = "PORGID")
    Long id;

    @Column(name = "PNICKNAME")
    String nickName;

    @Column(name = "PFULLNAME")
    String fullName;



    @Column(name = "PENGLISHFULLNAME")
    String englishFullName;

    @ManyToOne
    @JoinColumn(name = "PICONFILEID")
    File iconFile;

    @Column(name = "PISDEFAULT")
    Boolean isDefault;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEnglishFullName() {
        return englishFullName;
    }

    public void setEnglishFullName(String englishFullName) {
        this.englishFullName = englishFullName;
    }

    public File getIconFile() {
        return iconFile;
    }

    public void setIconFile(File iconFile) {
        this.iconFile = iconFile;
    }

    public Boolean getDefault() {
        return isDefault;
    }

    public void setDefault(Boolean aDefault) {
        isDefault = aDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != HibernateUtil.findClass(o)) return false;

        Organization that = (Organization) o;

        if(id == null) return false;

        return id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return  nickName ;
    }
}
