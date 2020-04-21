package com.fanap.midhco.ui.pages.uploadPanel;

import com.fanap.midhco.appstore.service.osType.IUploadFilter;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.component.multiAjaxFileUpload2.MultiAjaxFileUploadPanel2;
import com.fanap.midhco.ui.component.multiAjaxFileUpload2.UploadedFileInfo;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.util.*;

/**
 * Created by Hamid on 7/7/2016.
 */
public class UploadPanel extends BasePanel {
    Form form;

    public UploadPanel(String id, List<IUploadFilter> uploadFilters, Integer maxFileCount, String title) {
        super(id);

        form = new Form("form", new Model<>());
        final Map<String, UploadedFileInfo> mp =  new HashMap<String, UploadedFileInfo>();

        MultiAjaxFileUploadPanel2 uploadPanel =
                new MultiAjaxFileUploadPanel2("up", uploadFilters, maxFileCount, true, title){
                    @Override
                    protected void onUploadComplete(AjaxRequestTarget ajaxRequestTarget, UploadedFileInfo uploadedFileInfo) {
                        Collection<UploadedFileInfo> fileInfoList = (Collection<UploadedFileInfo>)this.getConvertedInput();
                        mp.clear();
                        for(UploadedFileInfo upInf : fileInfoList)
                            mp.put(upInf.getFileId(), upInf);
                    }

                    @Override
                    protected void onDelete(AjaxRequestTarget ajaxRequestTarget, UploadedFileInfo uploadedFileInfo) {
                        Collection<UploadedFileInfo> fileInfoList = (Collection<UploadedFileInfo>)this.getConvertedInput();
                        mp.clear();
                        for(UploadedFileInfo upInf : fileInfoList)
                            mp.put(upInf.getFileId(), upInf);
                    }
                };
        uploadPanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        uploadPanel.setModel(new Model<>());
        form.add(uploadPanel);

        AjaxLink okButton = new AjaxLink("ok") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                Collection<UploadedFileInfo> uploadedFileInfoSet = mp.values();
                childFinished(target, new Model((Serializable) new ArrayList<>(uploadedFileInfoSet)), this);
            }
        };
        add(okButton);

        AjaxLink cancelButton = new AjaxLink("cancel") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                childFinished(ajaxRequestTarget, null, this);
            }
        };
        add(cancelButton);

        add(form);
    }
}
