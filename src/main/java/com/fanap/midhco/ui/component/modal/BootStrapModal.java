package com.fanap.midhco.ui.component.modal;

import org.apache.wicket.*;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.EnumeratedType;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;

public class BootStrapModal extends Panel {
    private static final long serialVersionUID = 1L;
    private boolean withAutoFitOnBestSize = true;
    private boolean stopRendering;
    private String customScriptAfterClose;

    private static ResourceReference JAVASCRIPT = new JavaScriptResourceReference(
            BootStrapModal.class, "res/myModal.js");

    private static CssResourceReference CSS = new CssResourceReference(BootStrapModal.class, "res/myModal.css");

    public BootStrapModal(String id, boolean withAutoFitOnBestSize) {
        this(id);
        this.withAutoFitOnBestSize = withAutoFitOnBestSize;
    }

    public BootStrapModal(String id, String customScriptAfterClose) {
        this(id);
        this.customScriptAfterClose = customScriptAfterClose;
    }

    public BootStrapModal(String id) {
        super(id);
        setVersioned(false);
        add(empty = new WebMarkupContainer(getContentId()));

        add(new CloseButtonBehavior());
        add(new WindowClosedBehavior());
    }

    public boolean isShown() {
        return shown;
    }

    public void setCloseButtonCallback(ModalWindow.CloseButtonCallback callback) {
        closeButtonCallback = callback;
    }

    public void setWindowClosedCallback(ModalWindow.WindowClosedCallback callback) {
        windowClosedCallback = callback;
    }

    public void show(AjaxRequestTarget target) {
        if (!shown) {
            target.add(this);
            target.appendJavaScript(getWindowOpenJavascript());
            shown = true;
        }
    }

    public static void closeCurrent(AjaxRequestTarget target) {
        target.appendJavaScript(getCloseJavacript());
    }

    public void close(AjaxRequestTarget target) {
        target.appendJavaScript(getCloseJavacript());
        if (customScriptAfterClose != null && !customScriptAfterClose.equals(""))
            target.appendJavaScript(customScriptAfterClose);
        shown = false;
        getContent().replaceWith(new WebMarkupContainer(getContentId()).setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true));
        target.add(getContent());
    }

    private static String getCloseJavacript() {
        return "var win;\n" //
                + "try {\n" + "	win = window.parent.Wicket.Window;\n"
                + "} catch (ignore) {\n"
                + "}\n"
                + "if (typeof(win) == \"undefined\" || typeof(win.current) == \"undefined\") {\n"
                + "  try {\n"
                + "     win = window.Wicket.Window;\n"
                + "  } catch (ignore) {\n"
                + "  }\n"
                + "}\n"
                + "if (typeof(win) != \"undefined\" && typeof(win.current) != \"undefined\") {\n"
                + " var close = function(w) { w.setTimeout(function() {\n"
                + "		win.current.close();\n"
                + "	}, 0);  } \n"
                + "	try { close(window.parent); } catch (ignore) { close(window); };\n" + "}";
    }

    public String getContentId() {
        return "content";
    }

    public void setTitle(String title) {
        this.title = new Model(title);
    }

    public void setTitle(IModel title) {
        this.title = title;
    }

    public IModel getTitle() {
        return title;
    }

    public static final class MaskType extends EnumeratedType {

        private static final long serialVersionUID = 1L;

        public static final MaskType TRANSPARENT = new MaskType("TRANSPARENT");

        public static final MaskType SEMI_TRANSPARENT = new MaskType("SEMI_TRANSPARENT");


        public MaskType(String name) {
            super(name);
        }
    }

    public void setMaskType(MaskType mask) {
        maskType = mask;
    }

    public MaskType getMaskType() {
        return maskType;
    }

    protected void onBeforeRender() {
        super.onBeforeRender();

        getContent().setOutputMarkupId(true);
        getContent().setVisible(shown);
    }

    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        tag.put("style", "display:none");
    }

    public Component getContent() {
        return get(getContentId());
    }

    private boolean isCustomComponent() {
        return getContent() != empty;
    }

    public void removeContent(Component component) {
        super.remove(component);
        if (component.getId().equals(getContentId())) {
            add(empty = new WebMarkupContainer(getContentId()));
        }
    }

    public void setContent(Component component) {
        if (component.getId().equals(getContentId()) == false) {
            throw new WicketRuntimeException("Modal window content id is wrong.");
        }
        component.setOutputMarkupPlaceholderTag(true);
        replace(component);
        shown = false;
    }

    private class WindowClosedBehavior extends AbstractDefaultAjaxBehavior {
        private static final long serialVersionUID = 1L;

        protected void respond(AjaxRequestTarget target) {
            shown = false;

            if (windowClosedCallback != null) {
                windowClosedCallback.onClose(target);
            }
        }

        public CharSequence getCallbackScript() {
            return super.getCallbackScript();
        }
    }

    private class CloseButtonBehavior extends AbstractDefaultAjaxBehavior {
        private static final long serialVersionUID = 1L;

        protected void respond(AjaxRequestTarget target) {
            if (closeButtonCallback == null ||
                    closeButtonCallback.onCloseButtonClicked(target) == true) {
                close(target);
            }
        }

        @Override
        protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
            super.updateAjaxAttributes(attributes);
            attributes.getAjaxCallListeners().add(new AjaxCallListener() {
                @Override
                public CharSequence getSuccessHandler(Component component) {
                    return "";
                }

            });
        }


        public CharSequence getCallbackScript() {
            return super.getCallbackScript();
        }
    }

    private String getContentMarkupId() {
        return getContent().getMarkupId();
    }

    private String escapeQuotes(String string) {
        if (string.indexOf('"') != -1) {
            string = Strings.replaceAll(string, "\"", "\\\"").toString();
        }
        return string;
    }

    boolean useInitialWidth = true;

    public void setUseInitialWidth(boolean useInitialWidth) {
        this.useInitialWidth = useInitialWidth;
    }

    private String getWindowOpenJavascript() {
        AppendingStringBuffer buffer = new AppendingStringBuffer();

        if (isCustomComponent()) {
            buffer.append("var element = document.getElementById(\"" + getContentMarkupId() + "\");\n");
        }

        buffer.append("var settings = new Object();\n");
        buffer.append("settings.element = element;\n");

        Object title = getTitle() != null ? getTitle().getObject() : null;
        if (title != null) {
            buffer.append("settings.title=\"" + escapeQuotes(title.toString()) + "\";\n");
        }

        if (getMaskType() == MaskType.TRANSPARENT) {
            buffer.append("settings.mask=\"transparent\";\n");
        } else if (getMaskType() == MaskType.SEMI_TRANSPARENT) {
            buffer.append("settings.mask=\"semi-transparent\";\n");
        }

        postProcessSettings(buffer);

        buffer.append("var s = Wicket.Window.create(settings);" +
                "if(Wicket.Window.prevWin)" +
                "console.log(Wicket.Window.prevWin.content.firstChild, $(s.settings.element).attr('id'));" +
                "if(Wicket.Window.prevWin && Wicket.Window.prevWin.content.firstChild.id==$(s.settings.element).attr('id')) {" +
                "$(Wicket.Window.prevWin.window).modal('hide');Wicket.Window.prevWin = null;" +
                "} ;s.show();Wicket.Window.prevWin = Wicket.Window.current;\n");

        return buffer.toString();
    }

    protected AppendingStringBuffer postProcessSettings(AppendingStringBuffer settings) {
        return settings;
    }

    private boolean shown = false;
    private WebMarkupContainer empty;
    private IModel title = null;
    private MaskType maskType = MaskType.SEMI_TRANSPARENT;
    private ModalWindow.CloseButtonCallback closeButtonCallback = null;
    private ModalWindow.WindowClosedCallback windowClosedCallback = null;

    protected void onDetach() {
        super.onDetach();
        if (title != null) {
            title.detach();
        }
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(JAVASCRIPT));
        response.render(CssHeaderItem.forReference(CSS));
    }

}
