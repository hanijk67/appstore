package com.fanap.midhco.appstore.entities;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;
import java.util.List;

/**
 * Created by A.Moshiri on 6/20/2017.
 */
@Entity
@Table(name = "PC1TESTSUBISSUE")
@GenericGenerator(name = "sequence_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_TESTSUBISSUE"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = "pooled"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1000"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
        }
)
public class TestSubIssue extends BaseEntitiy<Long> {


    @Id
    @GeneratedValue(generator = "sequence_generator")
    @Column(name = "PTSTSUBISSUEID")
    Long id;

    @Column(name = "PDESCRIPTION")
    String description;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "state", column = @Column(name = "PAPPROVAL_STATE"))
    })
    ApprovalState approvalState = ApprovalState.DISAPPROVED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PTESTUSER")
    protected User testUser;

    @ManyToMany
    @JoinTable(
            name = "mm_subIssue2device",
            joinColumns = @JoinColumn(name = "PTSTSUBISSUEID"),
            inverseJoinColumns = @JoinColumn(name = "PDEVICEID")
    )
    List<Device> devices;

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

    public ApprovalState getApprovalState() {
        return approvalState;
    }

    public void setApprovalState(ApprovalState approvalState) {
        this.approvalState = approvalState;
    }

    public User getTestUser() {
        return testUser;
    }

    public void setTestUser(User testUser) {
        this.testUser = testUser;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }
}
