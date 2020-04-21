package com.fanap.midhco.appstore.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PF1COUNTRY")
public class Country extends BaseEntitiy<Long> {

    @Id
    @Column(name = "PCOUNTRYID")
    Long id;

    @Column(name = "PNAME")
    String name;

    public Country() {
    }

    public Country(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        if (o == null || !(o instanceof Country))
            return false;
        Country that = (Country) o;
        return id.equals(that.id) && name.equals(that.name);
    }
}
