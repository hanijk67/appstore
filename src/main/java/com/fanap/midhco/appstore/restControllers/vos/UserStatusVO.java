package com.fanap.midhco.appstore.restControllers.vos;

/**
 * Created by A.Moshiri on 3/26/2018.
 */
public class UserStatusVO {
    int status;
    String statusSpec;

    public UserStatusVO() {
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStatusSpec() {
        return statusSpec;
    }

    public void setStatusSpec(String statusSpec) {
        this.statusSpec = statusSpec;
    }
}
