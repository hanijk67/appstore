package com.fanap.midhco.appstore.entities;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Created by admin123 on 7/4/2016.
 */
@Embeddable
public class StereoType implements Serializable {
    public static final StereoType MAIN_APP_PACK_FILE = new StereoType(0, "pack");
    public static final StereoType THUMB_FILE = new StereoType(1, "images");
    public static final StereoType ICON_FILE = new StereoType(2, "icon");
    public static final StereoType LAUNCHER_FILE = new StereoType(3, "launcher");
    public static final StereoType TIME_LINE_FILE = new StereoType(4, "timeLine");

    int type;
    String folder;

    public StereoType() {}

    public StereoType(int type, String folder) {
        this.type = type;
        this.folder = folder;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof StereoType))
            return false;

        return type == ((StereoType)o).type;
    }

    @Override
    public int hashCode() {
        return type;
    }
}
