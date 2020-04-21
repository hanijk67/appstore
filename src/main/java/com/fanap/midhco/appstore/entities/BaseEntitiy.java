package com.fanap.midhco.appstore.entities;

import com.fanap.midhco.appstore.entities.helperClasses.DateTime;

import javax.persistence.*;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin123 on 6/1/2016.
 */

@MappedSuperclass
public abstract class BaseEntitiy<L> implements Serializable,IEntity<L> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREATOR")
    User creatorUser;

    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "PCREATIONDT")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "PCREATIONTM"))
    })
    DateTime creationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LASTMDFUSER")
    User lastModifyUser;

    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "PLASTMDFDT")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "PLASTMDFTM"))
    })
    protected DateTime lastModifyDate;

    ///////////////////////////////////////////////////////////////////////////////////

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PHBCREATOR")
    protected User hb_creatorUser;

    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "PHBCREATIONDT")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "PHBCREATIONTM"))
    })
    DateTime hb_creationDate;

    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "PHBLASTMDFDT")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "PHBLASTMDFTM"))
    })
    protected DateTime hb_lastModifyDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PHBLASTMDFUSR")
    protected User hb_lastModifyUser;

    @Version
    @Column(name = "PHBVER")
    private Long version;

    public User getCreatorUser() {
        return creatorUser;
    }

    public void setCreatorUser(User creatorUser) {
        this.creatorUser = creatorUser;
    }

    public DateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(DateTime creationDate) {
        this.creationDate = creationDate;
    }

    public User getLastModifyUser() {
        return lastModifyUser;
    }

    public void setLastModifyUser(User lastModifyUser) {
        this.lastModifyUser = lastModifyUser;
    }

    public DateTime getLastModifyDate() {
        return lastModifyDate;
    }

    public void setLastModifyDate(DateTime lastModifyDate) {
        this.lastModifyDate = lastModifyDate;
    }

    public User getHb_creatorUser() {
        return hb_creatorUser;
    }

    public void setHb_creatorUser(User hb_creatorUser) {
        this.hb_creatorUser = hb_creatorUser;
    }

    public DateTime getHb_creationDate() {
        return hb_creationDate;
    }

    public void setHb_creationDate(DateTime hb_creationDate) {
        this.hb_creationDate = hb_creationDate;
    }

    public DateTime getHb_lastModifyDate() {
        return hb_lastModifyDate;
    }

    public void setHb_lastModifyDate(DateTime hb_lastModifyDate) {
        this.hb_lastModifyDate = hb_lastModifyDate;
    }

    public User getHb_lastModifyUser() {
        return hb_lastModifyUser;
    }

    public void setHb_lastModifyUser(User hb_lastModifyUser) {
        this.hb_lastModifyUser = hb_lastModifyUser;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public List<String> getDeclaredField(){
        List<String> declaredFields = new ArrayList<>();
        for(Field fld : this.getClass().getDeclaredFields()){
            declaredFields.add(fld.getName());
        }
        return declaredFields;
    }
}
