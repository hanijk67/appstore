package com.fanap.midhco.ui.pages.packagesList;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.ui.component.ajaxDownload.AjaxDownload;
import org.apache.log4j.Logger;
import org.apache.wicket.core.util.resource.UrlResourceStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;

import java.io.File;
import java.net.URL;

/**
 * Created by admin123 on 2/6/2017.
 */
public class PackageList extends WebPage {
    final static Logger logger = Logger.getLogger(PackageList.class);

    String downloadFileKey;

    AjaxDownload ajaxDownload = new AjaxDownload() {
        @Override
        protected String getFileName() {
            return "Navin.apk";
        }

        @Override
        protected IResourceStream getResourceStream() {
            try {
                if (downloadFileKey != null && downloadFileKey.toUpperCase().startsWith("HTTP:")
                        || downloadFileKey.toUpperCase().startsWith("FILE:"))
                    return new UrlResourceStream(new URL(downloadFileKey));
                else
                    return new FileResourceStream(new File(new URL(downloadFileKey).getFile()));
            } catch (Exception ex) {
                logger.error("error downloading file : ", ex);
            }
            return null;
        }
    };


    public PackageList(final PageParameters pageParameters) {
        super(pageParameters);

//        AjaxLink navinDownloadLink = new AjaxLink("navinDownloadLink") {
//            @Override
//            public void onClick(AjaxRequestTarget target) {
//                downloadFileKey = ConfigUtil.getProperty(ConfigUtil.NAVIN_APP_DOWNLOAD_PATH);
//                ajaxDownload.initiate(target);
//            }
//        };
//        add(navinDownloadLink);

        add(ajaxDownload);
    }

}
