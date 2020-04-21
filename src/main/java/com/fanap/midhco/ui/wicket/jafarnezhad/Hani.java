package com.fanap.midhco.ui.wicket.jafarnezhad;

import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by h.jafarnezhad on 4/19/2020.
 */
@Authorize(view = Access.HANI)
public class Hani extends BasePanel implements IParentListner {

//    private ModalWindow modalWindow;
    private List<String> genderChoices= new ArrayList<>();

    public Hani(){this(MAIN_PANEL_ID);}
    public Hani(String id) {
        super(id);

        genderChoices.add("male");
        genderChoices.add("female");
        final UserHani user= new UserHani();

        setPageTitle(new ResourceModel("hani").getObject());
        Form form = new Form("form");
        final TextField<String> textField=new TextField<String>("text",
                new PropertyModel<String>(user, "name"));
//        textField.setOutputMarkupId(true);
//        textField.setOutputMarkupPlaceholderTag(true);

        final TextField<String> textField2=new TextField<String>("text2",
                new PropertyModel<String>(user, "family"));
//        textField2.setOutputMarkupId(true);
//        textField2.setOutputMarkupPlaceholderTag(true);

        final DropDownChoice<String> gender = new DropDownChoice<String>("gender",
                new PropertyModel<String>(user, "gender"), genderChoices);
        gender.setOutputMarkupId(true);

        Button button= new Button("submit") {
            @Override
            public void onSubmit() {
                super.onSubmit();


        System.out.println(textField.getValue());
        System.out.println(textField2.getValue());
            }
        };

        add(form);
        form.add(textField);
        form.add(textField2);
        form.add(gender);
        form.add(button);
    }



    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {

    }

}


