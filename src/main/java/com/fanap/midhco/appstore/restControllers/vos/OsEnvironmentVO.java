package com.fanap.midhco.appstore.restControllers.vos;

import com.fanap.midhco.appstore.entities.OSEnvironment;
import org.json.JSONObject;

/**
 * Created by A.Moshiri on 4/23/2018.
 */
public class OsEnvironmentVO {

    public OsEnvironmentVO() {
    }

    public OsEnvironmentVO(String request) {
        JSONObject jsonObject = new JSONObject(request);
        if (jsonObject.has("id")) {
            this.id = jsonObject.getLong("id");
        }
        if (jsonObject.has("name")) {
            this.envName = jsonObject.getString("name");
        }

    }

    Long id;
    String envName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }


    public static OsEnvironmentVO buildEnvironmentVOByEnvironment(OSEnvironment environment){
        OsEnvironmentVO environmentVO = new OsEnvironmentVO();

        environmentVO.setId(environment.getId());
        environmentVO.setEnvName(environment.getEnvName());
        return environmentVO;
    }
}
