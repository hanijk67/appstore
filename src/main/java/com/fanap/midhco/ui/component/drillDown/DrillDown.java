package com.fanap.midhco.ui.component.drillDown;

import com.fanap.midhco.appstore.applicationUtils.JsonUtil;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnEventHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.hibernate.event.internal.OnUpdateVisitor;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Created by A.Moshiri on 1/2/2018.
 */
public class DrillDown extends FormComponentPanel {
    private static ResourceReference fusionsChartChartJs = new JavaScriptResourceReference(
            DrillDown.class, "res/fusioncharts.charts.js");

    private static ResourceReference fusionsChartJs = new JavaScriptResourceReference(
            DrillDown.class, "res/fusioncharts.js");
    Form form;
    String dataSource;
    Integer index;

    public DrillDown(String id) {
        this(id, "",null);
    }
    public DrillDown(String id,String inputDataSource) {
        this(id, inputDataSource,null);
    }
    public DrillDown(String id,Integer index) {
        this(id, null,index);
    }


    public DrillDown(String id, String inputDataSource,Integer indexInput) {
        super(id);
        form = new Form("form");
        form.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        dataSource = inputDataSource;
        index = indexInput;
        add(form);

    }


    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(fusionsChartJs));
        response.render(JavaScriptHeaderItem.forReference(fusionsChartChartJs));
        if (dataSource == null || dataSource.trim().equals("")) {
            dataSource = null;
        }
        if(index==null){
            index =1;
        }

        String chartId = "chart-container"+index ;

        response.render(OnDomReadyHeaderItem.forScript("insertAction('" + getMarkupId() + "' , '" + chartId.toString() + "', '" +dataSource+ "', '" +index+ "');"));

    }


}
