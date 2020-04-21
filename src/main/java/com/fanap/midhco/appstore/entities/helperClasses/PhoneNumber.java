package com.fanap.midhco.appstore.entities.helperClasses;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class PhoneNumber implements Serializable {

    private String areaCode;
    private String number;

    public PhoneNumber() {
    }

    public PhoneNumber(String areaCode, String number) {
        this.areaCode = areaCode;
        this.number = number;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return (areaCode != null ? areaCode : "") + (number != null ? "-" + number : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhoneNumber that = (PhoneNumber) o;

        return !(areaCode != null ? !areaCode.equals(that.areaCode) : that.areaCode != null) && !(number != null ? !number.equals(that.number) : that.number != null);
    }

    @Override
    public int hashCode() {
        int result;
        result = (areaCode != null ? areaCode.hashCode() : 0);
        result = 31 * result + (number != null ? number.hashCode() : 0);
        return result;
    }
}
