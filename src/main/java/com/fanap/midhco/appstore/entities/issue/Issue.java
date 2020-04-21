package com.fanap.midhco.appstore.entities.issue;

import com.fanap.midhco.appstore.entities.BaseEntitiy;
import com.fanap.midhco.appstore.entities.Note;
import com.fanap.midhco.appstore.entities.User;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;
import java.util.List;

/**
 * Created by admin123 on 12/14/2016.
 */
@Entity
@Table(name = "PC1ISSUE")
@GenericGenerator(name = "sequence_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_ISSUE"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = "pooled"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1000"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
        }
)
public abstract class Issue extends BaseEntitiy<Long> {
    @Id
    @GeneratedValue(generator = "sequence_generator")
    @Column(name = "PISSUEID")
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PASSIGNEE")
    User assinee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PASSIGNER")
    User assigner;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "state", column = @Column(name = "PISSUE_STATE"))
    })
    IssueState issueState;

    @Column(name = "PISFORWARD")
    Boolean isForward;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "MM_ISSUE2NOTE", joinColumns = {@JoinColumn(name = "PISSUEID")},
            inverseJoinColumns = {@JoinColumn(name = "PNOTEID")})
    private List<Note> notes;

    public void setId(Long id) {
        this.id = id;
    }

    public User getAssinee() {
        return assinee;
    }

    public void setAssinee(User assinee) {
        this.assinee = assinee;
    }

    public User getAssigner() {
        return assigner;
    }

    public void setAssigner(User assigner) {
        this.assigner = assigner;
    }

    public IssueState getIssueState() {
        return issueState;
    }

    public void setIssueState(IssueState issueState) {
        this.issueState = issueState;
    }

    public Boolean getForward() {
        return isForward;
    }

    public void setForward(Boolean forward) {
        isForward = forward;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    @Override
    public Long getId() {
        return id;
    }
}
