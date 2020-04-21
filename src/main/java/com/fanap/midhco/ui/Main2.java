package com.fanap.midhco.ui;

import com.fanap.midhco.ui.access.Anonymous;
import com.fanap.midhco.ui.pages.BasePage2;
import org.apache.log4j.Logger;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.HashMap;
import java.util.Map;

@Anonymous
public class Main2 extends BasePage2 {
    private static final Logger logger = Logger.getLogger(BasePage2.class);
    private final static Map<Integer, Class> EXCEPTIONS_PAGE = new HashMap<Integer, Class>();

    public Main2(BasePanel panel) {
        add(panel);
    }

    public Main2(PageParameters parameters) {
        BaseMain.processMainRequest(parameters, this);
    }
}
