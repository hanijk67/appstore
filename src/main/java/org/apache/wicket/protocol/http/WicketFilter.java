//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.apache.wicket.protocol.http;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.file.WebXmlFile;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WicketFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(WicketFilter.class);
    public static final String FILTER_MAPPING_PARAM = "filterMappingUrlPattern";
    public static final String APP_FACT_PARAM = "applicationFactoryClassName";
    public static final String IGNORE_PATHS_PARAM = "ignorePaths";
    private WebApplication application;
    private IWebApplicationFactory applicationFactory;
    private FilterConfig filterConfig;
    private String filterPath;
    private int filterPathLength = -1;
    private final Set<String> ignorePaths = new HashSet();
    private boolean isServlet = false;

    public WicketFilter() {
    }

    public WicketFilter(WebApplication application) {
        this.application = (WebApplication)Args.notNull(application, "application");
    }

    protected ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    boolean processRequest(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ThreadContext previousThreadContext = ThreadContext.detach();
        boolean res = true;
        ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader newClassLoader = this.getClassLoader();
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        HttpServletResponse httpServletResponse = (HttpServletResponse)response;

        boolean var11;
        try {
            if (previousClassLoader != newClassLoader) {
                Thread.currentThread().setContextClassLoader(newClassLoader);
            }

            String filterPath = this.getFilterPath(httpServletRequest);
            if (filterPath == null) {
                throw new IllegalStateException("filter path was not configured");
            }

            if (!this.shouldIgnorePath(httpServletRequest)) {
                if ("OPTIONS".equalsIgnoreCase(httpServletRequest.getMethod())) {
                    httpServletResponse.setStatus(200);
                    httpServletResponse.setHeader("Allow", "GET,POST,OPTIONS,PUT,HEAD,PATCH,DELETE,TRACE");
                    httpServletResponse.setHeader("Content-Length", "0");
                    var11 = true;
                    return var11;
                }

                String redirectURL = this.checkIfRedirectRequired(httpServletRequest);
                if (redirectURL == null) {
                    ThreadContext.setApplication(this.application);
                    WebRequest webRequest = this.application.createWebRequest(httpServletRequest, filterPath);
                    WebResponse webResponse = this.application.createWebResponse(webRequest, httpServletResponse);
                    RequestCycle requestCycle = this.application.createRequestCycle(webRequest, webResponse);
                    res = this.processRequestCycle(requestCycle, webResponse, httpServletRequest, httpServletResponse, chain);
                    return res;
                } else {
                    if (!Strings.isEmpty(httpServletRequest.getQueryString())) {
                        redirectURL = redirectURL + "?" + httpServletRequest.getQueryString();
                    }

                    try {
                        httpServletResponse.sendRedirect(httpServletResponse.encodeRedirectURL(redirectURL));
                        return res;
                    } catch (IOException var18) {
                        throw new RuntimeException(var18);
                    }
                }
            }

            log.debug("Ignoring request {}", httpServletRequest.getRequestURL());
            if (chain != null) {
                chain.doFilter(request, response);
            }

            var11 = false;
        } finally {
            ThreadContext.restore(previousThreadContext);
            if (newClassLoader != previousClassLoader) {
                Thread.currentThread().setContextClassLoader(previousClassLoader);
            }

            if (response.isCommitted() && !httpServletRequest.isAsyncStarted()) {
                response.flushBuffer();
            }

        }

        return var11;
    }

    protected boolean processRequestCycle(RequestCycle requestCycle, WebResponse webResponse, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain chain) throws IOException, ServletException {
        boolean res = true;
        if (requestCycle.processRequestAndDetach()) {
            webResponse.flush();
        } else {
            if (chain != null) {
                chain.doFilter(httpServletRequest, httpServletResponse);
            }

            res = false;
        }

        return res;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if(((HttpServletRequest) request).getRequestURI().contains("/restAPI/spring/service")) {
            chain.doFilter(request, response);
            return;
        }
        this.processRequest(request, response, chain);
    }

    protected IWebApplicationFactory getApplicationFactory() {
        String appFactoryClassName = this.filterConfig.getInitParameter("applicationFactoryClassName");
        if (appFactoryClassName == null) {
            return new ContextParamWebApplicationFactory();
        } else {
            try {
                Class<?> factoryClass = Class.forName(appFactoryClassName, false, Thread.currentThread().getContextClassLoader());
                return (IWebApplicationFactory)factoryClass.newInstance();
            } catch (ClassCastException var3) {
                throw new WicketRuntimeException("Application factory class " + appFactoryClassName + " must implement IWebApplicationFactory");
            } catch (ClassNotFoundException var4) {
                throw new WebApplicationFactoryCreationException(appFactoryClassName, var4);
            } catch (InstantiationException var5) {
                throw new WebApplicationFactoryCreationException(appFactoryClassName, var5);
            } catch (IllegalAccessException var6) {
                throw new WebApplicationFactoryCreationException(appFactoryClassName, var6);
            } catch (SecurityException var7) {
                throw new WebApplicationFactoryCreationException(appFactoryClassName, var7);
            }
        }
    }

    public final void init(FilterConfig filterConfig) throws ServletException {
        this.init(false, filterConfig);
    }

    public void init(boolean isServlet, FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        this.isServlet = isServlet;
        this.initIgnorePaths(filterConfig);
        ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader newClassLoader = this.getClassLoader();

        try {
            if (previousClassLoader != newClassLoader) {
                Thread.currentThread().setContextClassLoader(newClassLoader);
            }

            if (this.application == null) {
                this.applicationFactory = this.getApplicationFactory();
                this.application = this.applicationFactory.createApplication(this);
            }

            if (this.application.getName() == null) {
                this.application.setName(filterConfig.getFilterName());
            }

            this.application.setWicketFilter(this);
            String configureFilterPath = this.getFilterPath();
            if (configureFilterPath == null) {
                configureFilterPath = this.getFilterPathFromConfig(filterConfig);
                if (configureFilterPath == null) {
                    configureFilterPath = this.getFilterPathFromWebXml(isServlet, filterConfig);
                    if (configureFilterPath == null) {
                        configureFilterPath = this.getFilterPathFromAnnotation(isServlet);
                    }
                }

                if (configureFilterPath != null) {
                    this.setFilterPath(configureFilterPath);
                }
            }

            if (this.getFilterPath() == null) {
                log.warn("Unable to determine filter path from filter init-param, web.xml, or servlet 3.0 annotations. Assuming user will set filter path manually by calling setFilterPath(String)");
            }

            ThreadContext.setApplication(this.application);

            try {
                this.application.initApplication();
                this.application.logStarted();
            } finally {
                ThreadContext.detach();
            }
        } catch (Exception var18) {
            log.error(String.format("The initialization of an application with name '%s' has failed.", filterConfig.getFilterName()), var18);

            try {
                this.destroy();
            } catch (Exception var16) {
                log.error("Unable to destroy after initialization failure", var16);
            }

            throw new ServletException(var18);
        } finally {
            if (newClassLoader != previousClassLoader) {
                Thread.currentThread().setContextClassLoader(previousClassLoader);
            }

        }

    }

    protected String getFilterPathFromAnnotation(boolean isServlet) {
        String[] patterns = null;
        if (isServlet) {
            WebServlet servlet = (WebServlet)this.getClass().getAnnotation(WebServlet.class);
            if (servlet != null) {
                if (servlet.urlPatterns().length > 0) {
                    patterns = servlet.urlPatterns();
                } else {
                    patterns = servlet.value();
                }
            }
        } else {
            WebFilter filter = (WebFilter)this.getClass().getAnnotation(WebFilter.class);
            if (filter != null) {
                if (filter.urlPatterns().length > 0) {
                    patterns = filter.urlPatterns();
                } else {
                    patterns = filter.value();
                }
            }
        }

        if (patterns != null && patterns.length > 0) {
            String pattern = patterns[0];
            if (patterns.length > 1) {
                log.warn("Multiple url patterns defined for Wicket filter/servlet, using the first: {}", pattern);
            }

            if ("/*".equals(pattern)) {
                pattern = "";
            }

            if (pattern.endsWith("*")) {
                pattern = pattern.substring(0, pattern.length() - 1);
            }

            return pattern;
        } else {
            return null;
        }
    }

    protected String getFilterPathFromWebXml(boolean isServlet, FilterConfig filterConfig) {
        return (new WebXmlFile()).getUniqueFilterPath(isServlet, filterConfig);
    }

    public FilterConfig getFilterConfig() {
        return this.filterConfig;
    }

    protected String getFilterPath(HttpServletRequest request) {
        return this.filterPath;
    }

    public String getFilterPath() {
        return this.filterPath;
    }

    protected String getFilterPathFromConfig(FilterConfig filterConfig) {
        String result = filterConfig.getInitParameter("filterMappingUrlPattern");
        if (result != null) {
            if (result.equals("/*")) {
                result = "";
            } else {
                if (!result.startsWith("/") || !result.endsWith("/*")) {
                    throw new WicketRuntimeException("Your filterMappingUrlPattern must start with \"/\" and end with \"/*\". It is: " + result);
                }

                result = result.substring(1, result.length() - 1);
            }
        }

        return result;
    }

    public void destroy() {
        if (this.application != null) {
            try {
                ThreadContext.setApplication(this.application);
                this.application.internalDestroy();
            } finally {
                ThreadContext.detach();
                this.application = null;
            }
        }

        if (this.applicationFactory != null) {
            try {
                this.applicationFactory.destroy(this);
            } finally {
                this.applicationFactory = null;
            }
        }

    }

    private String checkIfRedirectRequired(HttpServletRequest request) {
        return this.checkIfRedirectRequired(request.getRequestURI(), request.getContextPath());
    }

    protected final String checkIfRedirectRequired(String requestURI, String contextPath) {
        int uriLength = requestURI.indexOf(59);
        if (uriLength == -1) {
            uriLength = requestURI.length();
        }

        int homePathLength = contextPath.length() + (this.filterPathLength > 0 ? 1 + this.filterPathLength : 0);
        if (uriLength != homePathLength) {
            return null;
        } else {
            String uri = Strings.stripJSessionId(requestURI);
            String homePageUri = contextPath + '/' + this.getFilterPath();
            if (homePageUri.endsWith("/")) {
                homePageUri = homePageUri.substring(0, homePageUri.length() - 1);
            }

            if (uri.equals(homePageUri)) {
                uri = uri + "/";
                return uri;
            } else {
                return null;
            }
        }
    }

    public final void setFilterPath(String filterPath) {
        if (this.filterPath != null) {
            throw new IllegalStateException("Filter path is write-once. You can not change it. Current value='" + filterPath + '\'');
        } else {
            if (filterPath != null) {
                filterPath = canonicaliseFilterPath(filterPath);
                if (filterPath.endsWith("/")) {
                    this.filterPathLength = filterPath.length() - 1;
                } else {
                    this.filterPathLength = filterPath.length();
                }
            }

            this.filterPath = filterPath;
        }
    }

    public String getRelativePath(HttpServletRequest request) {
        String path = Strings.stripJSessionId(request.getRequestURI());
        String contextPath = request.getContextPath();
        path = path.substring(contextPath.length());
        String filterPath;
        if (this.isServlet) {
            filterPath = request.getServletPath();
            path = path.substring(filterPath.length());
        }

        if (path.length() > 0) {
            path = path.substring(1);
        }

        filterPath = this.getFilterPath();
        if (!path.startsWith(filterPath) && filterPath.equals(path + "/")) {
            path = path + "/";
        }

        if (path.startsWith(filterPath)) {
            path = path.substring(filterPath.length());
        }

        return path;
    }

    protected WebApplication getApplication() {
        return this.application;
    }

    private boolean shouldIgnorePath(HttpServletRequest request) {
        boolean ignore = false;
        if (this.ignorePaths.size() > 0) {
            String relativePath = this.getRelativePath(request);
            if (!Strings.isEmpty(relativePath)) {
                Iterator i$ = this.ignorePaths.iterator();

                while(i$.hasNext()) {
                    String path = (String)i$.next();
                    if (relativePath.startsWith(path)) {
                        ignore = true;
                        break;
                    }
                }
            }
        }

        return ignore;
    }

    private void initIgnorePaths(FilterConfig filterConfig) {
        String paths = filterConfig.getInitParameter("ignorePaths");
        if (!Strings.isEmpty(paths)) {
            String[] parts = Strings.split(paths, ',');
            String[] arr$ = parts;
            int len$ = parts.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                String path = arr$[i$];
                path = path.trim();
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }

                this.ignorePaths.add(path);
            }
        }

    }

    static String canonicaliseFilterPath(String filterPath) {
        if (Strings.isEmpty(filterPath)) {
            return filterPath;
        } else {
            int beginIndex = 0;

            int endIndex;
            for(endIndex = filterPath.length(); beginIndex < endIndex; ++beginIndex) {
                char c = filterPath.charAt(beginIndex);
                if (c != '/') {
                    break;
                }
            }

            int o = beginIndex;
            int i = beginIndex;

            while(i < endIndex) {
                char c = filterPath.charAt(i);
                ++i;
                if (c != '/') {
                    o = i;
                }
            }

            if (o < endIndex) {
                ++o;
                filterPath = filterPath.substring(beginIndex, o);
            } else {
                filterPath = filterPath.substring(beginIndex) + '/';
            }

            if (filterPath.equals("/")) {
                return "";
            } else {
                return filterPath;
            }
        }
    }
}
