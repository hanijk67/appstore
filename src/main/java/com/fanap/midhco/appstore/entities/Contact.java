package com.fanap.midhco.appstore.entities;

import com.fanap.midhco.appstore.entities.helperClasses.PhoneNumber;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by admin123 on 6/1/2016.
 */
@Embeddable
public class Contact implements Serializable {
    String firstName;
    String lastName;
    String email;
    String nationalCode;

    @Embedded
    PhoneNumber telNumber;

    @Embedded
    PhoneNumber mobileNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PCITYID")
    City city;

    String address;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNationalCode() {
        return nationalCode;
    }

    public void setNationalCode(String nationalCode) {
        this.nationalCode = nationalCode;
    }

    public PhoneNumber getTelNumber() {
        return telNumber;
    }

    public void setTelNumber(PhoneNumber telNumber) {
        this.telNumber = telNumber;
    }

    public PhoneNumber getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(PhoneNumber mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
