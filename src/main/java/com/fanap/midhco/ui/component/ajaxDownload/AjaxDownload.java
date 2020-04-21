package com.fanap.midhco.ui.component.ajaxDownload;

import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.IResourceStream;

/**
 * @author Sven Meier
 * @author Ernesto Reinaldo Barreiro (reiern70@gmail.com)
 * @author Jordi Deu-Pons (jordi@jordeu.net)
 */
public abstract class AjaxDownload extends AbstractAjaxBehavior {
    private boolean addAntiCache;

    public AjaxDownload() {
        this(true);
    }

    public AjaxDownload(boolean addAntiCache) {
        super();
        this.addAntiCache = addAntiCache;
    }

    /**
     * Call this method to initiate the download.
     */
    public void initiate(AjaxRequestTarget target) {
        String url = getCallbackUrl().toString();

        if (addAntiCache) {
            url = url + (url.contains("?") ? "&" : "?");
            url = url + "antiCache=" + System.currentTimeMillis();
        }

        String label_not_found =
                AppStorePropertyReader.getString("label.download.file.error");

        // the timeout is needed to let Wicket release the channel
        target.appendJavaScript("setTimeout(ajax_download(\"" + url + "\"), 100);");
    }

    public void onRequest() {
        ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(getResourceStream(), getFileName());
        handler.setContentDisposition(ContentDisposition.ATTACHMENT);
        getComponent().getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
    }

    protected abstract String getFileName();

    /**
     * Hook method providing the actual resource stream.
     */
    protected abstract IResourceStream getResourceStream();

}