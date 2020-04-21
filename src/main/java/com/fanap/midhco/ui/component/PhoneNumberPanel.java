package com.fanap.midhco.ui.component;

import com.fanap.midhco.appstore.entities.helperClasses.PhoneNumber;
import com.fanap.midhco.ui.util.MyUtil;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

import java.util.Arrays;
import java.util.List;

public class PhoneNumberPanel extends FormComponentPanel {
    private TextField areaCode = new TextField("areaCode", new Model());
    private TextField number = new TextField("number", new Model());

    private List fields = Arrays.asList(areaCode, number);

    public PhoneNumberPanel(String id) {
        this(id, null);
    }

    public PhoneNumberPanel(String id, long codeFieldLength) {
        this(id, null, codeFieldLength);
    }

    public PhoneNumberPanel(String id, long codeFieldLength, long numberFieldLength) {
        this(id, null, codeFieldLength, numberFieldLength);
    }

    public PhoneNumberPanel(String id, IModel model) {
        this(id, model, -1);
    }

    public PhoneNumberPanel(String id, IModel model, long codeFieldLength) {
        this(id, model, codeFieldLength, -1);
    }

    public PhoneNumberPanel(String id, IModel model, long codeFieldLength, long numberFieldLength) {
        super(id, model);
        setType(PhoneNumber.class);

        areaCode.setOutputMarkupId(true);
        number.setOutputMarkupId(true);

        String onkeyup = String.format("phoneNumberPanel_handleClipBoard(this, '%s','%s');",
                areaCode.getMarkupId(), number.getMarkupId());

        String onChange = String.format("checkPhoneNumberSectionLength(this, %d);",
                numberFieldLength);

        onkeyup += onChange;

        areaCode.add(new AttributeAppender("onkeyup",
                new Model("changeAreaCodeFocusToNumber(this," + codeFieldLength + ");" + onkeyup), " "));

        number.add(new AttributeAppender("onkeyup", new Model(onkeyup), " "));
        number.add(new AttributeAppender("onchange", new Model(onChange), " "));

        number.setLabel(new ResourceModel("PhoneNumber.number"));
        add(areaCode);
        add(number);
    }

    public void disable() {
        MyUtil.disable(fields);
    }

    public void readOnly() {
        MyUtil.readOnly(fields);
    }

    public void enable() {
        MyUtil.enable(fields);
    }

    public boolean isEnable() {
        for (Object field : fields) {
            if (MyUtil.haveDisable((FormComponent) field) != -2)
                return false;
        }
        return true;
    }

    @Override
    protected void onBeforeRender() {
        PhoneNumber phoneNumber = (PhoneNumber) getModelObject();
        if (phoneNumber != null) {
            areaCode.setModel(new Model(phoneNumber.getAreaCode()));
            number.setModel(new Model(phoneNumber.getNumber()));
        }
        super.onBeforeRender();
    }

    @Override
    public void convertInput() {
        String ac = (String) areaCode.getConvertedInput();
        String n = (String) number.getConvertedInput();
        PhoneNumber phoneNumber = null;
        if (n != null)
            phoneNumber = new PhoneNumber(ac, n.toString());
        setConvertedInput(phoneNumber);
    }
}
