package com.fanap.midhco.ui.component.circleBadge;

import com.fanap.midhco.ui.BasePanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

/**
 * Created by admin123 on 9/17/2017.
 */
public class CircleBadge extends BasePanel {

    public CircleBadge(String id, String chartTitle, String circleValue) {
        super(id);

        Label chartTitleLabel = new Label("chartTitle", new Model<>(chartTitle));
        add(chartTitleLabel);

        Label circleValueLbl = new Label("circleValueLbl", new Model<>(circleValue));
        add(circleValueLbl);
    }
}
