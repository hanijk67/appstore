package com.fanap.midhco.appstore.restControllers.vos.componentVos;

import java.util.ArrayList;

/**
 * Created by A.Moshiri on 1/2/2018.
 */
public class LinkedChart {
    Chart chart;
    ArrayList<Data> data;

    public Chart getChart() {
        return chart;
    }

    public void setChart(Chart chart) {
        this.chart = chart;
    }

    public ArrayList<Data> getData() {
        return data;
    }

    public void setData(ArrayList<Data> data) {
        this.data = data;
    }
}
