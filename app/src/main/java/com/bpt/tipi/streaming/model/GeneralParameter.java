package com.bpt.tipi.streaming.model;

/**
 * Created by jpujolji on 7/03/18.
 */

public class GeneralParameter {

    public String wowzaServerUrl, wowzaAppName, wowzaUser, wowzaPwd;
    public int wowzaPort;
    public int id = 1;

    public GeneralParameter(String wowzaServerUrl, int wowzaPort, String wowzaAppName, String wowzaUser, String wowzaPwd) {
        this.wowzaServerUrl = wowzaServerUrl;
        this.wowzaPort = wowzaPort;
        this.wowzaAppName = wowzaAppName;
        this.wowzaUser = wowzaUser;
        this.wowzaPwd = wowzaPwd;
    }

    public GeneralParameter(String wowzaServerUrl, String wowzaAppName, String wowzaUser, String wowzaPwd, int wowzaPort) {
        this.wowzaServerUrl = wowzaServerUrl;
        this.wowzaAppName = wowzaAppName;
        this.wowzaUser = wowzaUser;
        this.wowzaPwd = wowzaPwd;
        this.wowzaPort = wowzaPort;
    }

    public String getWowzaServerUrl() {
        return wowzaServerUrl;
    }

    public void setWowzaServerUrl(String wowzaServerUrl) {
        this.wowzaServerUrl = wowzaServerUrl;
    }

    public String getWowzaAppName() {
        return wowzaAppName;
    }

    public void setWowzaAppName(String wowzaAppName) {
        this.wowzaAppName = wowzaAppName;
    }

    public String getWowzaUser() {
        return wowzaUser;
    }

    public void setWowzaUser(String wowzaUser) {
        this.wowzaUser = wowzaUser;
    }

    public String getWowzaPwd() {
        return wowzaPwd;
    }

    public void setWowzaPwd(String wowzaPwd) {
        this.wowzaPwd = wowzaPwd;
    }

    public int getWowzaPort() {
        return wowzaPort;
    }

    public void setWowzaPort(int wowzaPort) {
        this.wowzaPort = wowzaPort;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
