package com.fanap.midhco.appstore.restControllers.controllers;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.OSType;
import com.fanap.midhco.appstore.entities.User;
import com.fanap.midhco.appstore.restControllers.vos.ResponseVO;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.login.JWTService;
import com.fanap.midhco.appstore.service.osType.OSTypeService;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLDecoder;

/**
 * Created by admin123 on 7/16/2016.
 */
public class AppStoreWebServiceFilter implements javax.servlet.Filter {
    static Logger logger = Logger.getLogger(AppStoreWebServiceFilter.class);

    FilterConfig filterConfig;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;

        String osTypeHeaderKey = ConfigUtil.getProperty(ConfigUtil.REQUEST_SERVLET_OSTYPE_HEADER);
        String osTypeHeaderValue = httpRequest.getHeader(osTypeHeaderKey);
        PrincipalUtil.setWebServiceRequestOSType(null);
        if (osTypeHeaderValue != null)
            osTypeHeaderValue = URLDecoder.decode(osTypeHeaderValue);

        if (osTypeHeaderValue != null && !osTypeHeaderValue.trim().isEmpty()) {
            Session session = HibernateUtil.getCurrentSession();
            try {
                OSType osType = OSTypeService.Instance.getOSTypeByName(osTypeHeaderValue, session);
                PrincipalUtil.setWebServiceRequestOSType(osType);
            } finally {
                session.close();
            }

        }

        String fromIndexHeaderKey = ConfigUtil.getProperty(ConfigUtil.REQUEST_SERVLET_FROM_INDEX_HEADER);
        String fromIndexHeaderValue = httpRequest.getHeader(fromIndexHeaderKey);
        PrincipalUtil.setWebServiceRequestFromIndex(0);
        if (fromIndexHeaderValue != null && !fromIndexHeaderValue.trim().equals("")) {
            try {
                PrincipalUtil.setWebServiceRequestFromIndex(Integer.valueOf(fromIndexHeaderValue));
            } catch (Exception e) {
                PrincipalUtil.setWebServiceRequestFromIndex(0);
            }
        }


        String sortByHeaderKey = ConfigUtil.getProperty(ConfigUtil.REQUEST_SERVLET_SORT_FIELD_HEADER);
        String sortByHeaderValue = httpRequest.getHeader(sortByHeaderKey);
        PrincipalUtil.setWebServiceRequestSortBy(null);
        if (sortByHeaderValue != null && !sortByHeaderValue.trim().equals("")) {
            try {
                PrincipalUtil.setWebServiceRequestSortBy(sortByHeaderValue);
            } catch (Exception e) {
                PrincipalUtil.setWebServiceRequestSortBy(null);
            }
        }

        String countIndexHeaderKey = ConfigUtil.getProperty(ConfigUtil.REQUEST_SERVLET_COUNT_INDEX_HEADER);
        String countIndexHeaderValue = httpRequest.getHeader(countIndexHeaderKey);
        PrincipalUtil.setWebServiceRequestCountIndex(-1);
        if (countIndexHeaderValue != null && !countIndexHeaderValue.trim().equals("")) {
            try {
                PrincipalUtil.setWebServiceRequestCountIndex(Integer.valueOf(countIndexHeaderValue));
            } catch (Exception e) {
                PrincipalUtil.setWebServiceRequestCountIndex(-1);
            }
        }


        String resultCountHeaderKey = ConfigUtil.getProperty(ConfigUtil.REQUEST_SERVLET_GET_RESULT_COUNT_HEADER);
        String resultCountHeaderValue = httpRequest.getHeader(resultCountHeaderKey);
        PrincipalUtil.setWebServiceRequestResultCount(false);
        if (resultCountHeaderValue != null && !resultCountHeaderValue.trim().equals("")) {
            try {
                PrincipalUtil.setWebServiceRequestResultCount(resultCountHeaderValue.equals(AppStorePropertyReader.getString("label.true")));
            } catch (Exception e) {
                PrincipalUtil.setWebServiceRequestResultCount(false);
            }
        }

        String ascResultHeaderKey = ConfigUtil.getProperty(ConfigUtil.REQUEST_SERVLET_SORT_TYPE_ASC_HEADER);
        String ascResultHeaderValue = httpRequest.getHeader(ascResultHeaderKey);
        PrincipalUtil.setWebServiceRequestAsc(false);
        if (ascResultHeaderValue != null && !ascResultHeaderValue.trim().equals("")) {
            try {
                PrincipalUtil.setWebServiceRequestAsc(ascResultHeaderValue.equals(AppStorePropertyReader.getString("label.true")));
            } catch (Exception e) {
                PrincipalUtil.setWebServiceRequestAsc(false);
            }
        }


        String jwtTokenHeaderKey = ConfigUtil.getProperty(ConfigUtil.REQUEST_SERVLET_JWT_TOKEN_HEADER);


        String jwtTokenHeaderValue = httpRequest.getHeader(jwtTokenHeaderKey);
        PrincipalUtil.setWebServiceRequestUser(null);
        PrincipalUtil.setWebServiceRequestJwtToken(null);

        if (jwtTokenHeaderValue != null)
            jwtTokenHeaderValue = URLDecoder.decode(jwtTokenHeaderValue);

        if (jwtTokenHeaderValue != null && !jwtTokenHeaderValue.trim().isEmpty() && !jwtTokenHeaderKey.trim().equals("")) {
            logger.debug("jwtTokenHeaderValue is " + jwtTokenHeaderValue);
            Session session = HibernateUtil.getCurrentSession();
            try {
                if (jwtTokenHeaderValue != null) {
                    JWTService.JWTUserClass jwtUser = null;
                    ResponseVO responseVO = new ResponseVO();
                    jwtUser = UserService.Instance.getJwtUserByGivenToken(jwtTokenHeaderValue, responseVO);

                    if (jwtUser != null && jwtUser.getUserId() != null) {
                        User userByUserId = UserService.Instance.findUserWithUserId(Long.valueOf(jwtUser.getUserId()), session);
                        if (userByUserId != null) {
                            PrincipalUtil.setWebServiceRequestUser(userByUserId);
                            PrincipalUtil.setWebServiceRequestJwtToken(jwtTokenHeaderValue);
                        }
                    }
                }
            } catch (Exception e) {
            } finally {
                session.close();
            }

        }


        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
}
