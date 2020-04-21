package com.fanap.midhco.ui.pages.note;

import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.textareapanel.TextAreaPanel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;

/**
 * Created by IntelliJ IDEA.
 * User: Hamid Reza Khanmirza
 * Date: 11/17/12
 * Time: 2:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class NotePanel extends BasePanel {
    protected Form form;
    AjaxFormButton confirmButton;
    AjaxFormButton cancelButton;
    FeedbackPanel feedback;
    TextAreaPanel noteTextArea;

    public NotePanel(String id) {
        this(id, "");
    }

    public NotePanel(String id, String generalNoteText) {
        super(id);

        feedback = new FeedbackPanel("feedback");
        add(feedback);

        setPageTitle(getMsg("label.note"));

        form = new Form("form", new Model());

        noteTextArea = new TextAreaPanel("generalNote", generalNoteText);
        noteTextArea.setModel(new Model());
        noteTextArea.setOutputMarkupId(true);
        form.add(noteTextArea);

        confirmButton = new AjaxFormButton("generalNote_confirm", form) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {
                String noteText = (String) noteTextArea.getConvertedInput();
                getParentListner().onChildFinished(target, new Model(noteText), confirmButton);
            }
        };
        confirmButton.setOutputMarkupId(true);
        form.add(confirmButton);

        cancelButton = new AjaxFormButton("cancel", form) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {
                getParentListner().onChildFinished(target, null, cancelButton);
            }
        };
        cancelButton.setOutputMarkupId(true);
        form.add(cancelButton);

        add(form);
    }
}
