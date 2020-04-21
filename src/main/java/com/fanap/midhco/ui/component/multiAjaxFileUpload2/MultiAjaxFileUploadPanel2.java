package com.fanap.midhco.ui.component.multiAjaxFileUpload2;


import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import com.fanap.midhco.appstore.service.myException.AppStoreRuntimeException;
import com.fanap.midhco.appstore.service.osType.IUploadFilter;
import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.StringValue;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class MultiAjaxFileUploadPanel2 extends FormComponentPanel {
    static Logger logger = Logger.getLogger(MultiAjaxFileUploadPanel2.class);
    WebMarkupContainer uploadDiv;
    Map<String, UploadedFileInfo> uploadedFilesInfoMap = new HashMap<String, UploadedFileInfo>();
    HiddenField uploadIndices;
    List<IUploadFilter> uploadFilters;
    Integer maxFileCount;
    Integer maxIconSize;
    AjaxEventBehavior fileUploadedEventBehavior;
    AjaxEventBehavior fileDeletedEventBehavior;
    boolean ajaxNotify;
    String title;
    static List<IUploadFilter> defaultUploadFilterList = new ArrayList<>();

    static {
        IUploadFilter uploadFilter = new IUploadFilter() {
            @Override
            public String getFilterTitle() {
                return "All files";
            }

            @Override
            public List<String> getFilterList() {
                return Arrays.asList("*");
            }
        };
        defaultUploadFilterList.add(uploadFilter);
    }

    public MultiAjaxFileUploadPanel2(String id) {
        this(id, defaultUploadFilterList, null, false, null , null);
    }

    public MultiAjaxFileUploadPanel2(String id, Integer maxFileCount, boolean ajaxNotify, String title) {
        this(id, defaultUploadFilterList, maxFileCount, ajaxNotify, title , null);
    }

    public MultiAjaxFileUploadPanel2(String id, Integer maxFileCount, boolean ajaxNotify, String title , Integer maxIconSize) {
        this(id, defaultUploadFilterList, maxFileCount, ajaxNotify, title , maxIconSize);
    }

    public MultiAjaxFileUploadPanel2(String id, List<IUploadFilter> uploadFilters, Integer maxFileCount, boolean ajaxNotify, String title) {
        this(id, uploadFilters, maxFileCount, ajaxNotify, title, null);
    }

    public MultiAjaxFileUploadPanel2(String id, List<IUploadFilter> uploadFilters, Integer maxFileCount, boolean ajaxNotify, String title,Integer maxIconSize) {
        super(id);

        this.uploadFilters = uploadFilters;
        this.maxFileCount = maxFileCount;
        this.maxIconSize = maxIconSize;
        this.ajaxNotify = ajaxNotify;
        this.title = title;

        uploadDiv = new WebMarkupContainer("uploadDiv", new Model());
        uploadDiv.setOutputMarkupId(true);

//        Form form = new Form("uploadForm", new Model());
//        form.setMultiPart(true);
//        add(form);

        uploadIndices = new HiddenField("uploadIndices");
        uploadIndices.setModel(new Model());
        add(uploadIndices);

        fileUploadedEventBehavior = new AjaxEventBehavior("fileUploaded") {
            @Override
            protected void onEvent(AjaxRequestTarget ajaxRequestTarget) {
                try {
                    StringValue indiceMapStringValue = getRequest().getRequestParameters().getParameterValue("indiceMap");

                    Set<UploadedFileInfo> retSet = parseIndiceMapString(indiceMapStringValue.toString());

                    setConvertedInput(Collections.unmodifiableSet(retSet));

                    onUploadComplete(ajaxRequestTarget, null);
                } catch (Exception ex) {
                    logger.error("error ocuured ", ex);
                }
            }
        };
        add(fileUploadedEventBehavior);

        fileDeletedEventBehavior = new AjaxEventBehavior("fileDeleted") {
            @Override
            protected void onEvent(AjaxRequestTarget ajaxRequestTarget) {
                try {
                    logger.debug("fileDeleted! ");
                    onDelete(ajaxRequestTarget, null);
                } catch (Exception ex) {
                        logger.error("error ocuured ", ex);
                }
            }
        };

        add(fileDeletedEventBehavior);

        add(uploadDiv);
    }

    protected void onUploadComplete(AjaxRequestTarget ajaxRequestTarget, UploadedFileInfo uploadedFileInfo) {
    }

    protected void onDelete(AjaxRequestTarget ajaxRequestTarget, UploadedFileInfo uploadedFileInfo) {
    }

    public void invokeLoadComplete(AjaxRequestTarget target) {
        target.appendJavaScript("$('#" + uploadDiv.getMarkupId() + "')[0].invoke();");
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        try {
            String guidPrefix = "uploadFile" + DateTime.now().getDateTimeLong();

            String uploadURL = FileServerService.FILE_UPLOAD_SERVER_PATH;

            response.render(OnDomReadyHeaderItem.forScript("insertAction('" + uploadDiv.getMarkupId() + "' , '" + guidPrefix + "'" +
                    (uploadFilters != null && !uploadFilters.isEmpty() ? "," + IUploadFilter.serializeAsJSONString(uploadFilters) : "")
                    + "," +
                    (maxFileCount != null ? maxFileCount : null)
                    + "," + (maxIconSize != null ? maxIconSize : null)
                    + ", " + ajaxNotify + ", '" + fileUploadedEventBehavior.getCallbackUrl() + "','" +
                    fileDeletedEventBehavior.getCallbackUrl()
                    + "', " +
                    (title != null ? "'" + title + "'" : null)
                    + ",'" + uploadURL + "');"));
        } catch (Exception ex) {
            throw new AppStoreRuntimeException(ex);
        }
        super.renderHead(response);
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

    private Set<UploadedFileInfo> parseIndiceMapString(String indiceMapAsString) throws JSONException {
        if(indiceMapAsString == null || indiceMapAsString.trim().equals(""))
            return new HashSet<>();
        JSONObject uploadedFilesMap = new JSONObject(indiceMapAsString);
        Set<UploadedFileInfo> retSet = new HashSet<UploadedFileInfo>();

        for (String key : (Set<String>) uploadedFilesMap.keySet()) {
            UploadedFileInfo uploadedFileInfo = new UploadedFileInfo();
            uploadedFileInfo.setPhysicalLocation(key);
            uploadedFileInfo.setFileId(key);

            JSONObject uploadedFileJson = (JSONObject)uploadedFilesMap.get(key);

            uploadedFileInfo.setFileName(uploadedFileJson.getString("fileName"));
            uploadedFileInfo.setFileSize(uploadedFileJson.getLong("fileSize"));

            retSet.add(uploadedFileInfo);
        }

        return retSet;
    }

    @Override
    public void convertInput() {
        try {
            String indicesAsJson = (String) uploadIndices.getConvertedInput();
            Set<UploadedFileInfo> retSet = parseIndiceMapString(indicesAsJson);

            setConvertedInput(Collections.unmodifiableSet(retSet));

        } catch (Exception ex) {
            setConvertedInput(null);
        }
    }

    public void setUploadFilters(List<IUploadFilter> uploadFilters, AjaxRequestTarget target) {
        this.uploadFilters = uploadFilters;
        target.appendJavaScript("$('#" + uploadDiv.getMarkupId() + "')[0]" + ".changeFilter(" +
                IUploadFilter.serializeAsJSONString(uploadFilters) + ");");
    }


}
