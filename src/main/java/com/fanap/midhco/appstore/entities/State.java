package com.fanap.midhco.appstore.entities;

import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;


@Entity
@Table(name = "PF1STATE")
public class State extends BaseEntitiy<Long> {
    @Id
    @Column(name = "PSTATEID")
    Long id;

    @Column(name = "PNAME")
    String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country")
    @ForeignKey(name = "state_country_fk")
    Country country;

    public State() {
    }

    public State(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
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
        if (o == null || !(o instanceof State))
            return false;
        State that = (State) o;
        return id.equals(that.id) && name.equals(that.name);
    }
}
