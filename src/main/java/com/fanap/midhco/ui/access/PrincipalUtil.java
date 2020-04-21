package com.fanap.midhco.ui.access;

import com.fanap.midhco.appstore.entities.OSType;
import com.fanap.midhco.appstore.entities.User;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.appstore.wicketApp.AppStoreApplication;
import com.fanap.midhco.ui.AppStoreSession;

/**
 * Created by admin123 on 6/15/2016.
 */
public class PrincipalUtil {
    private static ThreadLocal<User> currentUser = new ThreadLocal<User>();
    private static ThreadLocal<AppStoreSession> currentWebSession = new ThreadLocal<AppStoreSession>();
    private static ThreadLocal<OSType> webServiceRequestOSType = new ThreadLocal<>();
    private static ThreadLocal<User> webServiceRequestUser = new ThreadLocal<>();
    private static ThreadLocal<String> webServiceRequestSortBy = new ThreadLocal<>();
    private static ThreadLocal<String> webServiceRequestJwtToken = new ThreadLocal<>();
    private static ThreadLocal<Integer> webServiceRequestFromIndex = new ThreadLocal<>();
    private static ThreadLocal<Integer> webServiceRequestCountIndex = new ThreadLocal<>();
    private static ThreadLocal<Boolean> webServiceRequestResultCount = new ThreadLocal<>();
    private static ThreadLocal<Boolean> webServiceRequestAsc = new ThreadLocal<>();
    private static ThreadLocal<Boolean> isCurrentUserDeveloper = new ThreadLocal<>();
    private static ThreadLocal<Boolean> isCurrentUserRoot = new ThreadLocal<>();


    public PrincipalUtil() {
        isCurrentUserDeveloper.set(false);
    }

    public static User getCurrentUser() {
        return currentUser != null ? currentUser.get() : null;
    }

    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }

    public static void cleanCurrentPerson() {
        currentUser.remove();
    }

    public static boolean hasPermission(Access access) {
        if (AppStoreApplication.isInTestMode())
            return true;
        User currentUser = getCurrentUser();
        return UserService.Instance.hasPermission(currentUser, access);
    }

    public static AppStoreSession getCurrentWebSession() {
        return currentWebSession != null ? currentWebSession.get() : null;
    }

    public static void setCurrentWebSession(AppStoreSession session) {
        currentWebSession.set(session);
    }

    public static boolean hasPermissionWithChildren(Access access) {
        if (AppStoreApplication.isInTestMode())
            return true;

        User currentUser = getCurrentUser();
        if (UserService.Instance.hasPermission(currentUser, access))
            return true;
        else {
            for (Access child : access.getChildren())
                if (hasPermissionWithChildren(child))
                    return true;
        }
        return false;
    }

    public static boolean isRootUser() {
        return UserService.Instance.isRoot(getCurrentUser());
    }

    public static void setWebServiceRequestOSType(OSType osType) {
        webServiceRequestOSType.set(osType);
    }

    public static OSType getCurrentOSTYPE() {
        return webServiceRequestOSType.get();
    }


    public static Boolean getResultCount() {
        return webServiceRequestResultCount.get();
    }

    public static void setWebServiceRequestResultCount(Boolean resultCount) {
        webServiceRequestResultCount.set(resultCount);
    }


   public static Boolean isAscending() {
        return webServiceRequestAsc.get();
    }

    public static void setWebServiceRequestAsc(Boolean asc) {
        webServiceRequestAsc.set(asc);
    }

    public static void setWebServiceRequestUser(User user) {
        webServiceRequestUser.set(user);
    }

    public static User getJwtTokenUser() {
        return webServiceRequestUser.get();
    }

    public static void setWebServiceRequestFromIndex(Integer from) {
        webServiceRequestFromIndex.set(from);
    }

    public static Integer getFromIndex() {
        return webServiceRequestFromIndex.get();
    }


    public static void setWebServiceRequestSortBy(String sortBy) {
        webServiceRequestSortBy.set(sortBy);
    }

    public static String getSortBy() {
        return webServiceRequestSortBy.get();
    }


    public static void setWebServiceRequestJwtToken(String jwtToken) {
        webServiceRequestJwtToken.set(jwtToken);
    }

    public static String getJwtToken() {
        return webServiceRequestJwtToken.get();
    }

    public static void setWebServiceRequestCountIndex(Integer count) {
        webServiceRequestCountIndex.set(count);
    }

    public static Integer getCountIndex() {
        return webServiceRequestCountIndex.get();
    }


    public static Boolean isCurrentUserDeveloper() {
        return isCurrentUserDeveloper.get() == null ? false : isCurrentUserDeveloper.get();
    }

    public static void setIsUserDeveloper(boolean isDeveloper) {
        isCurrentUserDeveloper.set(isDeveloper);
    }

    public static void setIsUserRoot(boolean isUserRoot) {
        isCurrentUserRoot.set(isUserRoot);
    }

    public static Boolean isCurrentUserRoot() {
        return isCurrentUserRoot.get() == null ? false : isCurrentUserRoot.get();
    }

    public static void shutdown() {
        currentUser.remove();
        currentWebSession.remove();
        webServiceRequestOSType.remove();
        webServiceRequestUser.remove();
        isCurrentUserDeveloper.remove();
        isCurrentUserRoot.remove();
    }
}
