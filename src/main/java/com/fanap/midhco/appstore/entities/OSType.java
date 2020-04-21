package com.fanap.midhco.appstore.entities;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;

/**
 * Created by admin123 on 6/5/2016.
 */
@Entity
@Table(name = "PF1OSTYP")
@GenericGenerator(name = "sequence_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_OSTYP"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = "pooled"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1000"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
        }
)
public class OSType extends BaseEntitiy<Long> {
    @Id
    @GeneratedValue(generator = "sequence_generator")
    @Column(name = "POSTYPID")
    Long id;

    @Column(name = "PNAME")
    String name;

    @Column(name = "PDISABLED")
    Boolean disabled;

    @Column(name = "POSCOMPSCRIPT")
    @Lob
    String osCompareScript;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public String getOsCompareScript() {
        return osCompareScript;
    }

    public void setOsCompareScript(String osCompareScript) {
        this.osCompareScript = osCompareScript;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof OSType))
            return false;
        OSType other = (OSType)o;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return 31 * (id != null ? id.intValue() : 0) * name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
