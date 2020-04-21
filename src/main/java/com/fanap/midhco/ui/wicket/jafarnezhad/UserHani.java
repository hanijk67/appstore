package com.fanap.midhco.ui.wicket.jafarnezhad;

/**
 * Created by h.jafarnezhad on 4/20/2020.
 */
public class UserHani {

    private String name;
    private String family;
    private String gender;

    ////////////////////////////////////////////////////////////////////////////////////////////


    public UserHani() {
    }

    public UserHani(String name, String family, String gender) {
        this.name = name;
        this.family = family;
        this.gender = gender;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserHani user = (UserHani) o;

        if (getName() != null ? !getName().equals(user.getName()) : user.getName() != null) return false;
        if (getFamily() != null ? !getFamily().equals(user.getFamily()) : user.getFamily() != null) return false;
        return !(getGender() != null ? !getGender().equals(user.getGender()) : user.getGender() != null);

    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getFamily() != null ? getFamily().hashCode() : 0);
        result = 31 * result + (getGender() != null ? getGender().hashCode() : 0);
        return result;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "UserHani{" +
                "name='" + name + '\'' +
                ", family='" + family + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }

}
