package com.fanap.midhco.appstore.entities;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;
import java.util.List;

/**
 * Created by A.Moshiri on 6/20/2017.
 */

@Entity
@Table(name = "PC1TESTISSUE")
@GenericGenerator(name = "sequence_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_TESTISSUE"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = "pooled"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1000"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
        }
)
public class TestIssue extends BaseEntitiy<Long> {

    @Id
    @GeneratedValue(generator = "sequence_generator")
    @Column(name = "PTESTISSUEID")
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


    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "issue2subIssue",
            joinColumns = @JoinColumn(name = "PTESTISSUEID"),
            inverseJoinColumns = @JoinColumn(name = "PTSTSUBISSUEID")
    )
    List<TestSubIssue> subIssues;


    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "testIssue2history",
            joinColumns = @JoinColumn(name = "PTESTISSUEID"),
            inverseJoinColumns = @JoinColumn(name = "PTESTISSUEHISTORYID")
    )
    List<TestIssueHistory> histories;

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

    public List<TestIssueHistory> getHistories() {
        return histories;
    }

    public void setHistories(List<TestIssueHistory> histories) {
        this.histories = histories;
    }
}