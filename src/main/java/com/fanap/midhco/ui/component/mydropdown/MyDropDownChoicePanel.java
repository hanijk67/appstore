package com.fanap.midhco.ui.component.mydropdown;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

/**
 * Created by admin123 on 2/5/15.
 */
public class MyDropDownChoicePanel extends FormComponentPanel {
    DropDownChoice dropDownChoice;
    ListMultipleChoice listMultipleChoice;
    boolean isMultiple;
    OnChangeAjaxBehavior ajaxEventBehavior;
    boolean isAjaxy = false;

    public MyDropDownChoicePanel(String id, List choices, boolean isMultiple,
                                 boolean hasDataLiveSearh, String title, int selectedCountShow) {
        this(id, choices, isMultiple, hasDataLiveSearh, title, selectedCountShow, false, null);
    }

    public MyDropDownChoicePanel(String id, List choices, boolean isMultiple,
                                 boolean hasDataLiveSearh, String title, int selectedCountShow,
                                 boolean isAjaxy, IChoiceRenderer choiceRenderer) {
        super(id);
        this.isMultiple = isMultiple;
        this.isAjaxy = isAjaxy;

        if (!isMultiple) {
            if(choiceRenderer == null)
                dropDownChoice = new DropDownChoice("select", choices);
            else
                dropDownChoice = new DropDownChoice("select", choices, choiceRenderer);
            dropDownChoice.add(new AttributeModifier("data-selected-text-format", new Model("count>" + selectedCountShow)));
            if (hasDataLiveSearh)
                dropDownChoice.add(new AttributeModifier("data-live-search", new Model("true")));
            dropDownChoice.add(new AttributeModifier("class", new Model("selectpicker")));
            dropDownChoice.add(new AttributeModifier("title", new Model(title)));
            dropDownChoice.setNullValid(true);
            dropDownChoice.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

            add(dropDownChoice);
        } else {
            listMultipleChoice = new ListMultipleChoice("select", choices);
            if(choiceRenderer != null)
                listMultipleChoice.setChoiceRenderer(choiceRenderer);

            listMultipleChoice.add(new AttributeModifier("data-selected-text-format", new Model("count>" + selectedCountShow)));
            if (hasDataLiveSearh)
                listMultipleChoice.add(new AttributeModifier("data-live-search", new Model("true")));
            listMultipleChoice.add(new AttributeModifier("class", new Model("selectpicker")));
            listMultipleChoice.add(new AttributeModifier("title", new Model(title)));
            listMultipleChoice.add(new AttributeModifier("data-actions-box", new Model("true")));
            listMultipleChoice.add(new AttributeModifier("data-select-all-text", new Model("انتخاب همه")));
            listMultipleChoice.add(new AttributeModifier("data-deselect-all-text", new Model("پاک کردن همه")));
            listMultipleChoice.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

            add(listMultipleChoice);
        }
        setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        if (isAjaxy) {
            ajaxEventBehavior = new OnChangeAjaxBehavior() {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    MyDropDownChoicePanel.this.onUpdate(target);
                }
            };
            if (isMultiple)
                listMultipleChoice.add(ajaxEventBehavior);
            else
                dropDownChoice.add(ajaxEventBehavior);
        }
    }

    @Override
    public void convertInput() {
        if (!isMultiple)
            setConvertedInput(dropDownChoice.getConvertedInput());
        else
            setConvertedInput(listMultipleChoice.getConvertedInput());
    }

    public Object getSelectedItem() {
        if (isMultiple)
            return listMultipleChoice.getConvertedInput();
        else
            return dropDownChoice.getConvertedInput();
    }

    public void setChoices(List choices, AjaxRequestTarget target) {
        if (isMultiple) {
            listMultipleChoice.setChoices(choices);
            target.add(listMultipleChoice);
        } else {
            dropDownChoice.setChoices(choices);
            target.add(dropDownChoice);
        }
        if (isMultiple) {
            target.appendJavaScript("$('#" + getMarkupId() + "').find('div.btn-group').remove();");
            target.appendJavaScript("$('#" + listMultipleChoice.getMarkupId() + "').selectpicker({});");
        } else {
            target.appendJavaScript("$('#" + getMarkupId() + "').find('div.btn-group').remove();");
            target.appendJavaScript("$('#" + dropDownChoice.getMarkupId() + "').selectpicker();");
        }
    }

    protected void onUpdate(AjaxRequestTarget target) {
    }

    private String getReassignScript() {
        String selectParentVar = "selectParent" + Math.abs(new Random().nextInt());
        return "var " + selectParentVar + "= $('#" + getMarkupId() + "');\n" +
                "function tranferSelectedItems() {\n" +
                "    var selected_lis = " + selectParentVar + ".find('ul.dropdown-menu').find('li.selected');\n" +
                "    var selectArray = [];\n" +
                "    for(var i=0; i < selected_lis.length; i++) {\n" +
                "        var selectedIndex = $(selected_lis[i]).attr('data-original-index');\n" +
                "        selectArray.push(selectedIndex);\n" +
                "    }\n" +
                selectParentVar + ".find(\".selectpicker\").val(selectArray);\n" +
                "\n" +
                "}\n" +
                "\n" +
                selectParentVar + ".find('ul.dropdown-menu').find('li').click(function() {\n" +
                "    tranferSelectedItems();\n" +
                (isAjaxy ? "Wicket.Ajax.get({'u':'" + ajaxEventBehavior.getCallbackUrl() + "'});" : "") +
                "});";
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        if (isMultiple) {
            response.render(OnDomReadyHeaderItem.forScript("$('#" + listMultipleChoice.getMarkupId() + "').selectpicker();"));
        } else {
            response.render(OnDomReadyHeaderItem.forScript("$('#" + dropDownChoice.getMarkupId() + "').selectpicker();"));
        }
        response.render(OnDomReadyHeaderItem.forScript(getReassignScript()));
    }

    @Override
    protected void onBeforeRender() {
        Object o = getModelObject();
        if(isMultiple)
            listMultipleChoice.setModel(new Model((Serializable)o));
        else
            dropDownChoice.setModel(new Model((Serializable)o));
        super.onBeforeRender();
    }
}
