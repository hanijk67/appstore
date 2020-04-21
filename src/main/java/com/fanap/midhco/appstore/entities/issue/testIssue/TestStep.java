package com.fanap.midhco.appstore.entities.issue.testIssue;

import com.fanap.midhco.appstore.entities.BaseEntitiy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;
import java.util.List;

/**
 * Created by admin123 on 12/14/2016.
 */
@Entity
@Table(name = "PC1TESTSTEP")
@GenericGenerator(name = "sequence_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_TEST_STEP"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = "pooled"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1000"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
        }
)
public class TestStep extends BaseEntitiy<Long> {
    @Id
    @GeneratedValue(generator = "sequence_generator")
    @Column(name = "PTESTSTEP")
    Long id;

    @Column(name = "PSTEPDESC")
    String stepDescription;

    @Column(name= "PSTEPORDER")
    int stepOrder;

    @Column(name = "PSTEPTITLE")
    String stepTitle;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name="MM_STEP_STEPTYPS",
            joinColumns={@JoinColumn(name="PTESTSTEPID")},
            inverseJoinColumns={@JoinColumn(name="PSTESTSTEPTYPID")})
    List<TestStepType> stepTypes;

    public void setId(Long id) {
        this.id = id;
    }

    public String getStepDescription() {
        return stepDescription;
    }

    public void setStepDescription(String stepDescription) {
        this.stepDescription = stepDescription;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(int stepOrder) {
        this.stepOrder = stepOrder;
    }

    public String getStepTitle() {
        return stepTitle;
    }

    public void setStepTitle(String stepTitle) {
        this.stepTitle = stepTitle;
    }

    public List<TestStepType> getStepTypes() {
        return stepTypes;
    }

    public void setStepTypes(List<TestStepType> stepTypes) {
        this.stepTypes = stepTypes;
    }

    @Override
    public Long getId() {
        return id;
    }
}
