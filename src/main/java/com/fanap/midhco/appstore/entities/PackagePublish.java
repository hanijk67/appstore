package com.fanap.midhco.appstore.entities;

import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;

/**
 * Created by A.Moshiri on 10/22/2017.
 */
@Entity
@Table(name = "PF1PACKAGEPUBLISH")
@GenericGenerator(name = "sequence_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_PACKAGEPUBLISH"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = "pooled"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1000"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
        }
)
public class PackagePublish extends BaseEntitiy<Long> {
    @Id
    @GeneratedValue(generator = "sequence_generator")
    @Column(name = "PPACKAGEPUBLISHID")
    Long id;

    @Column(name = "PAPPID")
    Long appId;

    @Column(name = "PPACKID")
    Long packId;

    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "PPUBLISHDT")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "PPUBLISHTM"))
    })
    DateTime publishDateTime;

    @Column(name = "PISAPPLIED")
    Boolean isApplied;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getPackId() {
        return packId;
    }

    public void setPackId(Long packId) {
        this.packId = packId;
    }

    public DateTime getPublishDateTime() {
        return publishDateTime;
    }

    public void setPublishDateTime(DateTime publishDateTime) {
        this.publishDateTime = publishDateTime;
    }

    public Boolean getApplied() {
        return isApplied;
    }

    public void setApplied(Boolean applied) {
        isApplied = applied;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PackagePublish that = (PackagePublish) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
