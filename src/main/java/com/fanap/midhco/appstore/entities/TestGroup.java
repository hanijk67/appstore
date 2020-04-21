package com.fanap.midhco.appstore.entities;

import com.fanap.midhco.appstore.entities.BaseEntitiy;
import com.fanap.midhco.appstore.entities.User;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;
import java.util.List;

/**
 * Created by A.Moshiri on 6/20/2017.
 */
@Entity
@Table(name = "PC1TESTGROUP")
@GenericGenerator(name = "sequence_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_TESTGROUP"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = "pooled"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1000"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
        }
)
public class TestGroup extends BaseEntitiy<Long> {

    @Id
    @GeneratedValue(generator = "sequence_generator")
    @Column(name = "PTESTGROUPID")
    Long id;

    @Column(name = "PTITLE")
    String title;

    @ManyToMany
    @JoinTable(
            name = "mm_testGroup2user",
            joinColumns = @JoinColumn(name = "PTSTSUBISSUEID"),
            inverseJoinColumns = @JoinColumn(name = "PDEVICEID")
    )
    List<User> groupUsers;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<User> getGroupUsers() {
        return groupUsers;
    }

    public void setGroupUsers(List<User> groupUsers) {
        this.groupUsers = groupUsers;
    }
}
