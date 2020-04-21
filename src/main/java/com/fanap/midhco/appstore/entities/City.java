package com.fanap.midhco.appstore.entities;

import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;


@Entity
@Table(name = "TBL_CITY")
public class City extends BaseEntitiy<Long> {

    @Id
    Long id;

    String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state")
    @ForeignKey(name = "city_state_fk")
    State state;

    public City() {
    }

    public City(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("%s - %s", this.name, this.id);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof City))
            return false;
        City that = (City) o;
        return id.equals(that.id) && name.equals(that.name) /*&& abbreviation.equals(that.abbreviation)*/;
    }
}
