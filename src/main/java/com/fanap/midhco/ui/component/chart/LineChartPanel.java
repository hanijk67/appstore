package com.fanap.midhco.ui.component.chart;

import com.fanap.midhco.ui.BasePanel;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by admin123 on 9/19/2017.
 */
public class LineChartPanel extends BasePanel {
    List<String> labels;
    Map<String, List<String>> dataMap;
    WebMarkupContainer canvasElem;


    private static ResourceReference CHART_JAVASCRIPT = new JavaScriptResourceReference(
            LineChartPanel.class, "res/drawChart.js");

    public LineChartPanel(String id, String chartTitle, List<String> labels, Map<String, List<String>> dataMap) {
        super(id);

        this.labels = labels;
        this.dataMap = dataMap;

        Label chartTitleLabel = new Label("chartTitle", new Model<>(chartTitle));
        add(chartTitleLabel);

        canvasElem = new WebMarkupContainer("lineChart");
        canvasElem.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(canvasElem);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(JavaScriptHeaderItem.forReference(CHART_JAVASCRIPT));

        StringBuffer labelStringBuffer = new StringBuffer();
        labelStringBuffer.append("[");
        for(int i= 0; i < labels.size(); i++) {
            labelStringBuffer.append("'");
            labelStringBuffer.append(labels.get(i));
            labelStringBuffer.append("'");
            if(i < labels.size() - 1)
                labelStringBuffer.append(",");
        }
        labelStringBuffer.append("]");


        JSONArray dataMapJsonArray = new JSONArray();

        for(String key : dataMap.keySet()) {
            JSONObject tempJsonObject = new JSONObject();
            tempJsonObject.put("label", key);
            tempJsonObject.put("data", new JSONArray(dataMap.get(key)));
            dataMapJsonArray.put(tempJsonObject);
        }

        String labels = labelStringBuffer.toString();
        String data = dataMapJsonArray.toString();

        response.render(OnLoadHeaderItem.forScript("drawChart('" + canvasElem.getMarkupId() + "', " + labels +", " + data +")"));

        super.renderHead(response);
    }
}
