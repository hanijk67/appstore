package com.fanap.midhco.appstore.restControllers.vos;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by A.Moshiri on 11/14/2018.
 */
public class OrganizationParentChildVO {
    private Long Id;
    private String title;
    private Long parentId;
    private List<OrganizationParentChildVO> childrenItems;

    public OrganizationParentChildVO() {
        this.Id = null;
        this.title = "";
        this.parentId = null;
        this.childrenItems = new ArrayList<OrganizationParentChildVO>();
    }


    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public List<OrganizationParentChildVO> getChildrenItems() {
        return childrenItems;
    }

    public void setChildrenItems(List<OrganizationParentChildVO> childrenItems) {
        this.childrenItems = childrenItems;
    }

    public void addChildrenItem(OrganizationParentChildVO childrenItem){
        if(!this.childrenItems.contains(childrenItem))
            this.childrenItems.add(childrenItem);
    }

    @Override
    public String toString() {
        return "OrganizationParentChildVO [Id=" + Id + ", title=" + title + ", parentId="
                + parentId + ", childrenItems=" + childrenItems + "]";
    }

}
