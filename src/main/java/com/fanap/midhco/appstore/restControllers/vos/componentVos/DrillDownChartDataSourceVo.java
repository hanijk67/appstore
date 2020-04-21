package com.fanap.midhco.appstore.restControllers.vos.componentVos;

import java.util.List;

/**
 * Created by A.Moshiri on 1/2/2018.
 */
public class DrillDownChartDataSourceVo {
    Chart chart;
    List<Data> data;
    List<LinkedData> linkedData ;

    public Chart getChart() {
        return chart;
    }

    public void setChart(Chart chart) {
        this.chart = chart;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public List<LinkedData> getLinkedData() {
        return linkedData;
    }

    public void setLinkedData(List<LinkedData> linkedData) {
        this.linkedData = linkedData;
    }
}
