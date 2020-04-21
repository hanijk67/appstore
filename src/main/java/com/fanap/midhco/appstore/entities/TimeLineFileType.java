package com.fanap.midhco.appstore.entities;

import com.fanap.midhco.appstore.wicketApp.EnumCaptionHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by A.Moshiri on 9/16/2018.
 */
public class TimeLineFileType implements Serializable{
    public static TimeLineFileType HTML = new TimeLineFileType(Long.valueOf(0));
    public static TimeLineFileType IMAGE = new TimeLineFileType(Long.valueOf(1));
    public static TimeLineFileType VIDEO = new TimeLineFileType(Long.valueOf(2));
    public static TimeLineFileType IMAGE_AND_VIDEO = new TimeLineFileType(Long.valueOf(3));
    public static TimeLineFileType TEXT = new TimeLineFileType(Long.valueOf(4));
    public static TimeLineFileType ANY_FILE = new TimeLineFileType(Long.valueOf(5));

    Long state;

    public TimeLineFileType(Long state) {
        this.state = state;
    }

    public Long getState() {
        return state;
    }

    public void setState(Long state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeLineFileType that = (TimeLineFileType) o;

        return state != null ? state.equals(that.state) : that.state == null;
    }

    @Override
    public int hashCode() {
        return state != null ? state.hashCode() : 0;
    }

    @Override
    public String toString() {
        return EnumCaptionHelper.TIME_LINE_FILE_TYPE.get(this);
    }

    public static List<TimeLineFileType> listAll() {
        List<TimeLineFileType> timeLineFileTypeList = new ArrayList<>();
        timeLineFileTypeList.add(HTML);
        timeLineFileTypeList.add(VIDEO);
        timeLineFileTypeList.add(IMAGE);
        timeLineFileTypeList.add(IMAGE_AND_VIDEO);
        timeLineFileTypeList.add(TEXT);
        timeLineFileTypeList.add(ANY_FILE);
        return  timeLineFileTypeList;
    }
}
