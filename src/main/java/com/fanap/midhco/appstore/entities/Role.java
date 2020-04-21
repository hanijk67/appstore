package com.fanap.midhco.appstore.entities;

import com.fanap.midhco.appstore.service.HibernateUtil;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;

/**
 * Created by admin123 on 6/1/2016.
 */
@Entity
@Table(name = "PF1ROLE")
@GenericGenerator(name = "sequence_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_ROLE"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = "pooled"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1000"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
        }
)
public class Role extends BaseEntitiy<Long> {
        @Id
        @GeneratedValue(generator = "sequence_generator")
        @Column(name = "PROLEID")
        Long id;

        @Column(name = "PROLENAME")
        String name;

        @Column(name = "PACCESSCODS")
        String accessCodes;

        @Column(name = "Peditable")
        Boolean isEditable = true;

        public Long getId() {
                return id;
        }

        public void setId(Long id) {
                this.id = id;
        }

        public String getName() {
                return name;
        }

        public void setName(String name) {
                this.name = name;
        }

        public String getAccessCodes() {
                return accessCodes;
        }

        public void setAccessCodes(String accessCodes) {
                this.accessCodes = accessCodes;
        }

        public Boolean getEditable() {
                return isEditable;
        }

        public void setEditable(Boolean editable) {
                isEditable = editable;
        }

        @Override
        public String toString() {
                return name;
        }

        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != HibernateUtil.findClass(o)) return false;

                Role role = (Role) o;

                if (id != null ? !id.equals(role.id) : role.id != null) return false;
                return name != null ? name.equals(role.name) : role.name == null;

        }

        @Override
        public int hashCode() {
                int result = id != null ? id.hashCode() : 0;
                result = 31 * result + (name != null ? name.hashCode() : 0);
                return result;
        }
}
