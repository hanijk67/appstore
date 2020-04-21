package com.fanap.midhco.ui;

import com.fanap.midhco.ui.access.Anonymous;
import com.fanap.midhco.ui.pages.BasePage;
import org.apache.log4j.Logger;
import org.apache.wicket.request.mapper.parameter.PageParameters;

@Anonymous
public class Main extends BasePage {
    private static final Logger logger = Logger.getLogger(BasePage.class);

    public Main(BasePanel panel) {
        add(panel);
    }

    public Main(PageParameters parameters) {
        BaseMain.processMainRequest(parameters, this);
    }
}
