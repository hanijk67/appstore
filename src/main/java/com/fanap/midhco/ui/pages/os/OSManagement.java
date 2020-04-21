package com.fanap.midhco.ui.pages.os;

import com.fanap.midhco.appstore.service.os.OSService;
import com.fanap.midhco.appstore.service.osType.OSTypeService;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.component.SelectionMode;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

/**
 * Created by admin123 on 6/28/2016.
 */
@Authorize(views = {Access.OS_LIST, Access.OSTYPE_LIST})

public class OSManagement extends BasePanel implements IParentListner {

    public OSManagement() {
        super(MAIN_PANEL_ID);

        WebMarkupContainer osList_lbl = new WebMarkupContainer("osList_lbl");
        add(osList_lbl);
        WebMarkupContainer osTypeList_lbl = new WebMarkupContainer("osTypeList_lbl");
        add(osTypeList_lbl);

        WebMarkupContainer osListDiv = new WebMarkupContainer("osListDiv");
        add(osListDiv);

        WebMarkupContainer osTypeListDiv = new WebMarkupContainer("osTypeListDiv");
        add(osTypeListDiv);

        if(!PrincipalUtil.hasPermission(Access.OS_LIST)) {
            osList_lbl.setVisible(false);
            osListDiv.setVisible(false);
        }else {
            OSList osList = new OSList("osListPanel", new OSService.OSCriteria(), SelectionMode.None);
            osListDiv.add(osList);
        }

        if(!PrincipalUtil.hasPermission(Access.OSTYPE_LIST)) {
            osTypeList_lbl.setVisible(false);
            osTypeListDiv.setVisible(false);
        }else {
            OSTypeList osTypeList = new OSTypeList("osTypeListPanel", new OSTypeService.OSTypeCriteria(), SelectionMode.None);
            osTypeListDiv.add(osTypeList);
        }
    }


    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {
    }
}
