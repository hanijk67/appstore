package com.fanap.midhco.ui.pages.anouncement;

import com.fanap.midhco.appstore.entities.Anouncement;
import com.fanap.midhco.appstore.service.anouncement.AnouncementService;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.pages.os.OSList;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

/**
 * Created by A.Moshiri on 9/12/2017.
 */
public class AnouncementManagement  extends BasePanel implements IParentListner {

    public AnouncementManagement() {
        super(MAIN_PANEL_ID);

        WebMarkupContainer anouncementList_lbl = new WebMarkupContainer("anouncementList_lbl");
        add(anouncementList_lbl);
        WebMarkupContainer createAnouncement_lbl = new WebMarkupContainer("createAnouncement_lbl");
        add(createAnouncement_lbl);

        WebMarkupContainer anouncementListDiv = new WebMarkupContainer("anouncementListDiv");
        add(anouncementListDiv);

        WebMarkupContainer createAnouncementDiv = new WebMarkupContainer("createAnouncementDiv");
        add(createAnouncementDiv);


        if(!PrincipalUtil.hasPermission(Access.ANOUNCEMENT_LIST)) {
            anouncementList_lbl.setVisible(false);
            anouncementListDiv.setVisible(false);
        }else {
            AnouncementList anouncementList = new AnouncementList("anouncementListPanel", new AnouncementService.AnouncmentCriteria(), SelectionMode.WithoutAdd);
            anouncementListDiv.add(anouncementList);
        }

        if(!PrincipalUtil.hasPermission(Access.ANOUNCEMENT_ADD)) {
            createAnouncement_lbl.setVisible(false);
            createAnouncementDiv.setVisible(false);
        }else {
            AnouncementForm anouncementForm = new AnouncementForm("createAnouncementPanel",new Anouncement());
            createAnouncementDiv.add(anouncementForm);
        }
    }


    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {
    }


}

