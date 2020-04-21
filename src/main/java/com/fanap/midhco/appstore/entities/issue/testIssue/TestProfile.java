package com.fanap.midhco.appstore.entities.issue.testIssue;

import com.fanap.midhco.appstore.entities.BaseEntitiy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;

/**
 * Created by admin123 on 12/14/2016.
 */
@Entity
@Table(name = "PC1TESTPROFILE")
@GenericGenerator(name = "sequence_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_TEST_PROFILE"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = "pooled"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1000"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
        }
)
public class TestProfile extends BaseEntitiy<Long> {
    @Id
    @GeneratedValue(generator = "sequence_generator")
    @Column(name = "PTESTPROFILEID")
    Long id;

    @Column(name="PPROFILENAME")
    String profileName;

    @Column(name = "PACTIVE")
    Boolean active;



    public void setId(Long id) {
        this.id = id;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public Long getId() {
        return null;
    }
}
