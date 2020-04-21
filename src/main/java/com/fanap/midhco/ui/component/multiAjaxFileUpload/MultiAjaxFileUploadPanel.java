package com.fanap.midhco.ui.component.multiAjaxFileUpload;


import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.service.myException.AppStoreRuntimeException;
import com.fanap.midhco.appstore.service.osType.IUploadFilter;
import org.json.*;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import java.util.*;

public class MultiAjaxFileUploadPanel extends FormComponentPanel<Set<UploadedFileInfo>> {
    WebMarkupContainer uploadDiv;
    Map<String, UploadedFileInfo> uploadedFilesInfoMap = new HashMap<String, UploadedFileInfo>();
    HiddenField uploadIndices;
    List<IUploadFilter> uploadFilters;
    Integer maxFileCount;

    ResourceReference plupload_js =
            new JavaScriptResourceReference(MultiAjaxFileUploadPanel.class, "pulp_upload_js/plupload.js");

    ResourceReference jquery_plupload_queue_js =
            new JavaScriptResourceReference(MultiAjaxFileUploadPanel.class, "pulp_upload_js/jquery.plupload.queue/jquery.plupload.queue.js");

    ResourceReference plupload_gears_js =
            new JavaScriptResourceReference(MultiAjaxFileUploadPanel.class, "pulp_upload_js/plupload.gears.js");
    ResourceReference plupload_browserplus_js =
            new JavaScriptResourceReference(MultiAjaxFileUploadPanel.class, "pulp_upload_js/plupload.browserplus.js");
    ResourceReference plupload_html4_js =
            new JavaScriptResourceReference(MultiAjaxFileUploadPanel.class, "pulp_upload_js/plupload.html4.js");
    ResourceReference plupload_html5_js =
            new JavaScriptResourceReference(MultiAjaxFileUploadPanel.class, "pulp_upload_js/plupload.html5.js");

    public MultiAjaxFileUploadPanel(String id) {
        this(id, null, null);
    }

    public MultiAjaxFileUploadPanel(String id, List<IUploadFilter> uploadFilters, Integer maxFileCount) {
        super(id);

        this.uploadFilters = uploadFilters;
        this.maxFileCount = maxFileCount;

        uploadDiv = new WebMarkupContainer("uploadDiv", new Model());
        uploadDiv.setOutputMarkupId(true);

        Form form = new Form("uploadForm", new Model());
        form.setMultiPart(true);
        add(form);

        uploadIndices = new HiddenField("uploadIndices");
        uploadIndices.setModel(new Model());
        form.add(uploadIndices);

        form.add(uploadDiv);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        try {
            response.render(JavaScriptHeaderItem.forReference(plupload_js));
            response.render(JavaScriptHeaderItem.forReference(plupload_gears_js));
            response.render(JavaScriptHeaderItem.forReference(plupload_browserplus_js));
            response.render(JavaScriptHeaderItem.forReference(plupload_html4_js));
            response.render(JavaScriptHeaderItem.forReference(plupload_html5_js));
            response.render(JavaScriptHeaderItem.forReference(jquery_plupload_queue_js));

            String guidPrefix = "uploadFile" + DateTime.now().getDateTimeLong();

            response.render(OnDomReadyHeaderItem.forScript("insertAction('" + uploadDiv.getMarkupId() + "' , '" + guidPrefix + "'" +
                    (uploadFilters != null && !uploadFilters.isEmpty() ? "," + IUploadFilter.serializeAsJSONString(uploadFilters) : "")
                    + ");"));
        } catch (Exception ex) {
            throw new AppStoreRuntimeException(ex);
        }
    }

    public List<UploadedFileInfo> getUploadedFiles() {
        List<UploadedFileInfo> uploadedList = new ArrayList<UploadedFileInfo>();

        for (String key : uploadedFilesInfoMap.keySet()) {
            uploadedList.add(uploadedFilesInfoMap.get(key));
        }

        return uploadedList;
    }

    public void empty() {
        uploadedFilesInfoMap = new HashMap<String, UploadedFileInfo>();
    }

    @Override
    public void convertInput() {
//        try {
//            String indicesAsJson = (String) uploadIndices.getConvertedInput();
//            JSONObject uploadedFilesMap = new JSONObject(indicesAsJson);
//            String tempUploadLocation = ConfigUtil.getProperty(ConfigUtil.APP_TEMP_FILES_LOCATION);
//
//            Set<UploadedFileInfo> retSet = new HashSet<UploadedFileInfo>();
//
//            for (String key : (Set<String>) uploadedFilesMap.keySet()) {
//                UploadedFileInfo uploadedFileInfo = new UploadedFileInfo();
//                uploadedFileInfo.setPhysicalLocation(tempUploadLocation + key);
//                uploadedFileInfo.setFileId(key);
//                uploadedFileInfo.setFileName((String) uploadedFilesMap.get(key));
//                retSet.add(uploadedFileInfo);
//            }
//
//            setConvertedInput(Collections.unmodifiableSet(retSet));
//
//        } catch (Exception ex) {
//            setConvertedInput(null);
//        }
    }
}
