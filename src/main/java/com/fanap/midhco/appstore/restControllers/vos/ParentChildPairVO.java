package com.fanap.midhco.appstore.restControllers.vos;

/**
 * Created by A.Moshiri on 11/17/2018.
 */
public class ParentChildPairVO {
    private Long childId ;
    private Long parentId;
    private String title;


    public ParentChildPairVO(Long childId, Long parentId, String title) {
        this.childId = childId;
        this.parentId = parentId;
        this.title = title;
    }

    public Long getChildId() {
        return childId;
    }

    public void setChildId(Long childId) {
        this.childId = childId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
