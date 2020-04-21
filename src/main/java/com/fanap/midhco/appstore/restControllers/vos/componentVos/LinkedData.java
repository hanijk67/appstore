package com.fanap.midhco.appstore.restControllers.vos.componentVos;

/**
 * Created by A.Moshiri on 1/2/2018.
 */
public class LinkedData {
    String id;
    LinkedChart linkedChart;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LinkedChart getLinkedChart() {
        return linkedChart;
    }

    public void setLinkedChart(LinkedChart linkedChart) {
        this.linkedChart = linkedChart;
    }
}
