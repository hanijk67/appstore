package com.fanap.midhco.appstore.entities;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;
import java.util.List;

/**
 * Created by A.Moshiri on 7/23/2017.
 */
@Entity
@Table(name = "PC1TESTISSUEHISTORY")
@GenericGenerator(name = "sequence_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_TESTISSUEHISTORY"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = "pooled"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1000"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
        }
)
public class TestIssueHistory extends BaseEntitiy<Long> {

    @Id
    @GeneratedValue(generator = "sequence_generator")
    @Column(name = "PTESTISSUEHISTORYID")
    Long id;

    @Column(name = "PDESCRIPTION")
    @Lob
    String description;

    @Column(name = "PTITLE")
    String title;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "state", column = @Column(name = "PPRIORITY_STATE"))
    })
    TestPriority priority = TestPriority.LOW;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "state", column = @Column(name = "PAPPROVAL_STATE"))
    })
    ApprovalState approvalState = ApprovalState.DISAPPROVED;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "mm_testIssueHist_subIssue",
            joinColumns = @JoinColumn(name = "PTESTISSUEHISTORYID"),
            inverseJoinColumns = @JoinColumn(name = "PTSTSUBISSUEID")
    )
    List<TestSubIssue> subIssues;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TestPriority getPriority() {
        return priority;
    }

    public void setPriority(TestPriority priority) {
        this.priority = priority;
    }

    public ApprovalState getApprovalState() {
        return approvalState;
    }

    public void setApprovalState(ApprovalState approvalState) {
        this.approvalState = approvalState;
    }

    public List<TestSubIssue> getSubIssues() {
        return subIssues;
    }

    public void setSubIssues(List<TestSubIssue> subIssues) {
        this.subIssues = subIssues;
    }
}
