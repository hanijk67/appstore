package com.fanap.midhco.appstore.entities;

import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.entities.helperClasses.Gender;
import com.fanap.midhco.appstore.entities.helperClasses.UserStatus;
import com.fanap.midhco.appstore.service.HibernateUtil;
import org.hibernate.annotations.*;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Set;

/**
 * Created by admin123 on 6/1/2016.
 */
@Entity
@Table(name = "PC1USER")
@GenericGenerator(name = "sequence_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_USER"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = "pooled"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1000"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
        }
)
public class User extends BaseEntitiy<Long> {
    @Id
    @GeneratedValue(generator = "sequence_generator")
    @Column(name = "PUSERID")
    Long id;

    @Column(name = "PUSERNAME", unique = true)
    String userName;

    @Column(name = "PUSEUSERID",unique=true)
    Long userId;

    @Column(name = "PPASSWD")
    String password;

    @Column(name = "PPASSSALT")
    String passwordSalt;

    @AttributeOverrides({
            @AttributeOverride(name = "firstName", column = @Column(name = "PFIRSTNAME")),
            @AttributeOverride(name = "lastName", column = @Column(name = "PLASTNAME")),
            @AttributeOverride(name = "email", column = @Column(name = "PEMAIL")),
            @AttributeOverride(name = "telNumber.areaCode", column = @Column(name = "PTELAREACOD")),
            @AttributeOverride(name = "telNumber.number", column = @Column(name = "PTELNUM")),
            @AttributeOverride(name = "mobileNumber.areaCode", column = @Column(name = "PMOBAREACOD")),
            @AttributeOverride(name = "mobileNumber.number", column = @Column(name = "PMOBNUM")),
            @AttributeOverride(name = "address", column = @Column(name = "PADRS"))
    })
    @AssociationOverrides({
            @AssociationOverride(name = "city", joinColumns = {@JoinColumn(name = "PCITYID")})
    })
    Contact contact;

    @Column(name = "PLOGGED")
    boolean logged;

    @Column(name = "PNUMOFWRONGTRIES")
    Byte numOfWrongTries;

    @Embedded
    UserStatus userStatus = UserStatus.ENABLED;

    @Column(name = "PLASTIP")
    String lastIp;

    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "PLAST_LOGIN_DATE")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "PLAST_LOGIN_TIME"))})
    DateTime lastLoginDate;

    @ManyToMany(fetch = FetchType.EAGER, targetEntity = Role.class)
    @JoinTable(name = "mm_user_roles")
    Set<Role> roles;

    @AttributeOverrides({
        @AttributeOverride(name = "type", column = @Column(name = "PGENDER"))
    })
    Gender gender;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "publicKey", column = @Column(name = "PPUBKEY")),
            @AttributeOverride(name = "privateKey", column = @Column(name = "PPRIVKEY"))})
    PublicPrivatePair publicPrivatePair;

    @Transient
    private byte[] allAllowedPermissions;

    @Transient
    private byte[] allDeniedPermissions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public boolean isLogged() {
        return logged;
    }

    public String getLastIp() {
        return lastIp;
    }

    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    public Byte getNumOfWrongTries() {
        return numOfWrongTries;
    }

    public void setNumOfWrongTries(Byte numOfWrongTries) {
        this.numOfWrongTries = numOfWrongTries;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

    public DateTime getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(DateTime lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public PublicPrivatePair getPublicPrivatePair() {
        return publicPrivatePair;
    }

    public void setPublicPrivatePair(PublicPrivatePair publicPrivatePair) {
        this.publicPrivatePair = publicPrivatePair;
    }

    public byte[] getAllAllowedPermissions() {
        return allAllowedPermissions;
    }

    public void setAllAllowedPermissions(byte[] allAllowedPermissions) {
        this.allAllowedPermissions = allAllowedPermissions;
    }

    public byte[] getAllDeniedPermissions() {
        return allDeniedPermissions;
    }

    public void setAllDeniedPermissions(byte[] allDeniedPermissions) {
        this.allDeniedPermissions = allDeniedPermissions;
    }

    public String getFullName() {
        String fullName = "";
        if(contact != null)
            fullName = contact.firstName + " " + contact.lastName;
        return fullName + "@" + userName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != HibernateUtil.findClass(o)) return false;

       User user = (User) o;

        if (id != null ? !id.equals(user.getId()) : user.getId() != null) return false;
        return userName != null ? userName.equals(user.getUserName()) : user.getUserName() == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return
                (contact != null ? contact.getFirstName() + " " + contact.getLastName() : "")
                + "@" + userName;
    }
}
