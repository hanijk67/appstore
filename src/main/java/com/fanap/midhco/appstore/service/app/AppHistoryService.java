package com.fanap.midhco.appstore.service.app;

import com.fanap.midhco.appstore.applicationUtils.DateUtil;
import com.fanap.midhco.appstore.applicationUtils.HistoryUtil;
import com.fanap.midhco.appstore.applicationUtils.MyCalendarUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.apache.wicket.model.ResourceModel;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxy;

import java.util.*;

/**
 * Created by A.Moshiri on 7/17/2017.
 */
public class AppHistoryService {
    public static AppHistoryService Instance = new AppHistoryService();

    private AppHistoryService() {
    }

    /*public void saveOrUpdate(AppHistory appHistory, Session session) {
        if (appHistory.getId() == null) {
            appHistory.setCreationDate(DateTime.now());
            appHistory.setCreatorUser(PrincipalUtil.getCurrentUser());
        } else {
            appHistory.setLastModifyDate(DateTime.now());
            appHistory.setLastModifyUser(PrincipalUtil.getCurrentUser());
        }
        session.saveOrUpdate(appHistory);
    }
*/

    public AppHistory setAppHistoryByApp(Session session, App app) {
        if (app != null) {
            AppHistory appHistory = new AppHistory();
            List<AppPackage> newAppPackageList = new ArrayList<AppPackage>();
            Session newSession = HibernateUtil.getNewSession();
            newSession.evict(app);
            App loadedApp = (App) newSession.load(App.class, app.getId());
            newSession.refresh(loadedApp);
            try {
                if (loadedApp != null) {
                    for (AppPackage appPackage : loadedApp.getAppPackages()) {
                        AppPackage loadedAppPackage = (AppPackage) newSession.load(AppPackage.class, appPackage.getId());
                        newAppPackageList.add(loadedAppPackage);
                    }
                    appHistory.setTitle(loadedApp.getTitle());
                    appHistory.setPackageName(loadedApp.getPackageName());
                    appHistory.setAppCategory(loadedApp.getAppCategory());
                    appHistory.setAppPackages(newAppPackageList);
                    appHistory.setDescription(loadedApp.getDescription());
                    appHistory.setDeveloper(loadedApp.getDeveloper());
                    appHistory.setMainPackage(loadedApp.getMainPackage());
                    appHistory.setOs(loadedApp.getOs());
                    appHistory.setOsType(loadedApp.getOsType());
                    appHistory.setCreationDate(DateTime.now());
                    appHistory.setCreatorUser(PrincipalUtil.getCurrentUser());
                    appHistory.getDeveloper();
                    appHistory.setCreationDate(DateTime.now());
                    appHistory.setCreatorUser(PrincipalUtil.getCurrentUser());
                    session.saveOrUpdate(appHistory);
//                    newSession.evict(appHistory);
//                    newSession.clear();
//                    newSession.flush();
//
//                    saveOrUpdate(appHistory, newSession);
//
//                    newSession.close();
//                    session.evict(appHistory);
//                    if (appHistory.getId() != null) {
//                        session.flush();
//                        AppHistory loadedAppHistory = (AppHistory) session.load(AppHistory.class, appHistory.getId());
//                        return loadedAppHistory;
//
//                    }

                    Hibernate.initialize(appHistory);
                    if (appHistory instanceof HibernateProxy) {
                        appHistory = (AppHistory) ((HibernateProxy) appHistory).getHibernateLazyInitializer()
                                .getImplementation();
                    }
                    return appHistory;

                }
            } finally {
                if (newSession != null && newSession.isOpen()) {
                    newSession.close();
                }
            }

        }
        return null;
    }


    public List<String> createHistoryMessage(App app) {
        List<String> validationString = new ArrayList<>();
        Session session = HibernateUtil.getNewSession();

        if (app != null && app.getId() != null) {

            App loadedApp = (App) session.load(App.class, app.getId());

            if (loadedApp != null) {
                List<AppHistory> appHistories = loadedApp.getHistories();

                if (appHistories != null && !appHistories.isEmpty()) {
                    Set<Long> checkedPackageHistory = new HashSet<>();

                    if (appHistories.size() > 1) {

                        Collections.sort(appHistories, (firstAppHist, secondAppHist) -> firstAppHist.getCreationDate().compareTo(secondAppHist.getCreationDate()));

                        for (int i = 1; i < appHistories.size(); i++) {

                            //to so convert Gregorian to Jalali

                            AppHistory firstAppHistory =
                                    (appHistories.get(i - 1) != null && appHistories.get(i - 1).getId() != null) ?
                                            ((AppHistory) session.load(AppHistory.class, appHistories.get(i - 1).getId())) : null;

                            if (firstAppHistory != null) {
                                firstAppHistory.setCreationDate(MyCalendarUtil.toPersian(firstAppHistory.getCreationDate()));
                            }

                            AppHistory secondAppHistory =
                                    (appHistories.get(i) != null && appHistories.get(i).getId() != null) ?
                                            ((AppHistory) session.load(AppHistory.class, appHistories.get(i).getId())) : null;

                            if (secondAppHistory != null) {
                                secondAppHistory.setCreationDate(MyCalendarUtil.toPersian(secondAppHistory.getCreationDate()));
                            }

                            if (firstAppHistory != null && secondAppHistory != null) {
                                if(secondAppHistory.getLastModifyUser()!=null){
                                    session.refresh(secondAppHistory);
                                    session.evict(secondAppHistory);
                                }
                                Long changerId = firstAppHistory.getLastModifyUser() !=null ?firstAppHistory.getLastModifyUser().getId(): firstAppHistory.getCreatorUser().getId();
                                User changer = (User) session.load(User.class, changerId);
                                validationString.addAll(checkAppHistoryVersions(firstAppHistory, secondAppHistory, loadedApp, session, checkedPackageHistory,changer.getFullName()));
                            }

                            session.clear();
                        }
                        if (validationString.size() == 0) {
                            validationString.add(AppStorePropertyReader.getString("label.history.noChange"));
                        }
                    }

                    Collections.sort(appHistories, (firstAppHist, secondAppHist) -> firstAppHist.getCreationDate().compareTo(secondAppHist.getCreationDate()));

                    AppHistory firstAppHistory =
                            (appHistories.get(appHistories.size() - 1) != null && appHistories.get(appHistories.size() - 1).getId() != null) ?
                                    ((AppHistory) session.load(AppHistory.class, appHistories.get(appHistories.size() - 1).getId())) : null;


                    if (firstAppHistory != null) {
                        if(loadedApp.getLastModifyUser()!=null){
                            session.evict(loadedApp.getLastModifyUser());
                            session.refresh(loadedApp.getLastModifyUser());
                        }
                        Long changerId = firstAppHistory.getLastModifyUser() !=null ?firstAppHistory.getLastModifyUser().getId(): firstAppHistory.getCreatorUser().getId();
                        User changer = (User) session.load(User.class, changerId);

                        firstAppHistory.setCreationDate(MyCalendarUtil.toPersian(firstAppHistory.getCreationDate()));
                        validationString.addAll(checkAppHistoryVersions(firstAppHistory, loadedApp, session, checkedPackageHistory,changer.getFullName()));

                    }

                    session.clear();

                    if (validationString.size() == 0) {
                        validationString.add(AppStorePropertyReader.getString("label.history.noChange"));
                    }

                } else {
                    validationString.add(AppStorePropertyReader.getString("label.history.noChange"));
                }
            }

        } else {
            validationString.add(AppStorePropertyReader.getString("no.app.found"));
        }

        if (session != null && session.isOpen()) {
            session.close();
        }
        Collections.sort(validationString);
        Collections.reverse(validationString);

        return validationString;
    }

    public List<String> checkAppHistoryVersions(AppHistory firstAppHistory, AppHistory secondAppHistory, App
            loadedApp, Session session, Set<Long> checkedPackageHistory ,String changer) {
        List<String> compareMessage = new ArrayList<>();
        if (!firstAppHistory.getTitle().trim().equals(secondAppHistory.getTitle().trim())) {
            compareMessage.add(HistoryUtil.createChangeDifferenceMessage(firstAppHistory.getTitle(), secondAppHistory.getTitle(), firstAppHistory.getCreationDate(),
                    AppStorePropertyReader.getString("App.title"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"), AppStorePropertyReader.getString("label.history.to"),
                    AppStorePropertyReader.getString("label.history.change"),changer) + "<br/>");
        }

        if (firstAppHistory.getDescription() != null || secondAppHistory.getDescription() != null) {
            if (firstAppHistory.getDescription() == null && secondAppHistory.getDescription() != null) {
                compareMessage.add(HistoryUtil.createAddDifferenceMessage(HistoryUtil.removeBackslash(secondAppHistory.getDescription()), firstAppHistory.getCreationDate(),
                        AppStorePropertyReader.getString("App.description"), AppStorePropertyReader.getString("label.value"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.to"),
                        AppStorePropertyReader.getString("label.history.change"),changer) + "<br/>");
            } else if (firstAppHistory.getDescription() != null && secondAppHistory.getDescription() == null) {
                compareMessage.add(
                        HistoryUtil.createChangeDifferenceMessage(HistoryUtil.removeBackslash(firstAppHistory.getDescription()), "", firstAppHistory.getCreationDate(),
                                AppStorePropertyReader.getString("App.description"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"), AppStorePropertyReader.getString("label.history.to"),
                                AppStorePropertyReader.getString("label.history.change"),changer) + "<br/>");
            } else if (!firstAppHistory.getDescription().trim().equals(secondAppHistory.getDescription().trim())) {
                compareMessage.add(
                        HistoryUtil.createChangeDifferenceMessage(HistoryUtil.removeBackslash(firstAppHistory.getDescription()),
                                HistoryUtil.removeBackslash(secondAppHistory.getDescription()), firstAppHistory.getCreationDate(), AppStorePropertyReader.getString("App.description"),
                                AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"), AppStorePropertyReader.getString("label.history.to"),
                                AppStorePropertyReader.getString("label.history.change"),changer) + "<br/>");
            }
        }

        if (!firstAppHistory.getPackageName().trim().equals(secondAppHistory.getPackageName().trim())) {
            compareMessage.add(
                    HistoryUtil.createChangeDifferenceMessage(firstAppHistory.getPackageName(), secondAppHistory.getPackageName(),
                            firstAppHistory.getCreationDate(), AppStorePropertyReader.getString("App.appPackageName"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"),
                            AppStorePropertyReader.getString("label.history.to"), AppStorePropertyReader.getString("label.history.change"),changer) + "<br/>");
        }


        if (!firstAppHistory.getAppCategory().equals(secondAppHistory.getAppCategory())) {
            compareMessage.add(
                    HistoryUtil.createChangeDifferenceMessage(firstAppHistory.getAppCategory().toString(), secondAppHistory.getAppCategory().toString(),
                            firstAppHistory.getCreationDate(), AppStorePropertyReader.getString("App.appCategory"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"),
                            AppStorePropertyReader.getString("label.history.to"), AppStorePropertyReader.getString("label.history.change"),changer) + "<br/>");
        }

        if (!firstAppHistory.getDeveloper().equals(secondAppHistory.getDeveloper())) {
            compareMessage.add(
                    HistoryUtil.createChangeDifferenceMessage(firstAppHistory.getDeveloper().toString(), secondAppHistory.getDeveloper().toString(),
                            firstAppHistory.getCreationDate(), AppStorePropertyReader.getString("App.developer"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"),
                            AppStorePropertyReader.getString("label.history.to"), AppStorePropertyReader.getString("label.history.change"),changer) + "<br/>");
        }

        if (firstAppHistory.getOs() != null || secondAppHistory.getOs() != null) {
            if (firstAppHistory.getOs() == null && secondAppHistory.getOs() != null) {
                compareMessage.add(
                        HistoryUtil.createAddDifferenceMessage(secondAppHistory.getOs().toString(), firstAppHistory.getCreationDate(), AppStorePropertyReader.getString("App.os"),
                                new ResourceModel("label.value").getObject(), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.to"),
                                AppStorePropertyReader.getString("label.history.change"),changer) + "<br/>");
            } else if (firstAppHistory.getOs() != null && secondAppHistory.getOs() == null) {

                compareMessage.add(
                        HistoryUtil.createChangeDifferenceMessage(firstAppHistory.getOs().toString(), " ",
                                firstAppHistory.getCreationDate(), AppStorePropertyReader.getString("App.os"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"),
                                AppStorePropertyReader.getString("label.history.to"), AppStorePropertyReader.getString("label.history.change"),changer) + "<br/>");
            } else if (!firstAppHistory.getOs().equals(secondAppHistory.getOs())) {
                compareMessage.add(
                        HistoryUtil.createChangeDifferenceMessage(firstAppHistory.getOs().toString(), secondAppHistory.getOs().toString(),
                                firstAppHistory.getCreationDate(), AppStorePropertyReader.getString("App.os"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"),
                                AppStorePropertyReader.getString("label.history.to"), AppStorePropertyReader.getString("label.history.change"),changer) + "<br/>");
            }
        }

        if (!firstAppHistory.getOsType().equals(secondAppHistory.getOsType())) {
            compareMessage.add(
                    HistoryUtil.createChangeDifferenceMessage(firstAppHistory.getOsType().toString(), secondAppHistory.getOsType().toString(),
                            firstAppHistory.getCreationDate(), AppStorePropertyReader.getString("App.osType"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"),
                            AppStorePropertyReader.getString("label.history.to"), AppStorePropertyReader.getString("label.history.change"),changer) + "<br/>");
        }

        if (!firstAppHistory.getMainPackage().equals(secondAppHistory.getMainPackage())) {
            AppPackage firstMainPackage = (AppPackage) session.load(AppPackage.class, firstAppHistory.getMainPackage().getId());
            AppPackage secondMainPackage = (AppPackage) session.load(AppPackage.class, secondAppHistory.getMainPackage().getId());
            compareMessage.add(
                    HistoryUtil.createChangeDifferenceMessage(firstMainPackage.getVersionCode(), secondMainPackage.getVersionCode(),
                            firstAppHistory.getCreationDate(), AppStorePropertyReader.getString("App.mainPackage"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"),
                            AppStorePropertyReader.getString("label.history.to"), AppStorePropertyReader.getString("label.history.change"),changer) + "<br/>");
        }

        List<AppPackage> firstAppHistoryAppPackages = firstAppHistory.getAppPackages();
        List<AppPackage> secondAppHistoryAppPackages = secondAppHistory.getAppPackages();
        if (firstAppHistory.getAppPackages() != null && secondAppHistory.getAppPackages() != null) {
            StringBuilder packageInfoString = new StringBuilder();
            packageInfoString.append(AppStorePropertyReader.getString("APPPackage")).append(" ").append(AppStorePropertyReader.getString("label.with")).append(" ").append(AppStorePropertyReader.getString("APPPackage.versionCode"));
            if (firstAppHistory.getAppPackages().size() != secondAppHistory.getAppPackages().size()) {
                for (AppPackage appPackage : secondAppHistoryAppPackages) {
                    appPackage.setCreationDate(MyCalendarUtil.toPersian(appPackage.getCreationDate()));

                    if (!firstAppHistoryAppPackages.contains(appPackage)) {
                        compareMessage.add(
                                HistoryUtil.createAddDifferenceMessage(appPackage.getVersionName(), appPackage.getCreationDate(), AppStorePropertyReader.getString("App.appPackages"),
                                        packageInfoString.toString(), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.to"),
                                        AppStorePropertyReader.getString("label.history.add"),changer) + "<br/>");
                    }
                }
            }
        }

        for (AppPackage appPackage : secondAppHistoryAppPackages) {
            if (firstAppHistoryAppPackages.contains(appPackage)) {
//                if (loadedApp.getMainPackage().equals(appPackage)) {
                if (!checkedPackageHistory.contains(appPackage.getId())) {
                    compareMessage.addAll(AppPackageHistoryService.Instance.createHistoryMessage(appPackage, true));
                    checkedPackageHistory.add(appPackage.getId());
                }
//                }
            }
        }
        return compareMessage;
    }

    public List<String> checkAppHistoryVersions(AppHistory firstAppHistory, App
            app, Session session, Set<Long> checkedPackageHistory ,String changer) {
        App loadedApp = (App) session.load(App.class, app.getId());
        loadedApp.setLastModifyDate(MyCalendarUtil.toPersian(loadedApp.getLastModifyDate()));

        List<String> compareMessage = new ArrayList<>();
        if (!firstAppHistory.getTitle().trim().equals(loadedApp.getTitle().trim())) {
            compareMessage.add(HistoryUtil.createChangeDifferenceMessage(firstAppHistory.getTitle(), loadedApp.getTitle(), firstAppHistory.getCreationDate(),
                    AppStorePropertyReader.getString("App.title"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"), AppStorePropertyReader.getString("label.history.to"),
                    AppStorePropertyReader.getString("label.history.change") ,changer) + "<br/>");
        }

        if (firstAppHistory.getDescription() != null || loadedApp.getDescription() != null) {
            if (firstAppHistory.getDescription() == null && loadedApp.getDescription() != null) {
                compareMessage.add(HistoryUtil.createAddDifferenceMessage(HistoryUtil.removeBackslash(loadedApp.getDescription()), firstAppHistory.getCreationDate(),
                        AppStorePropertyReader.getString("App.description"), AppStorePropertyReader.getString("label.value"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.to"),
                        AppStorePropertyReader.getString("label.history.change"),changer) + "<br/>");
            } else if (firstAppHistory.getDescription() != null && loadedApp.getDescription() == null) {
                compareMessage.add(
                        HistoryUtil.createChangeDifferenceMessage(HistoryUtil.removeBackslash(firstAppHistory.getDescription()), "", firstAppHistory.getCreationDate(),
                                AppStorePropertyReader.getString("App.description"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"), AppStorePropertyReader.getString("label.history.to"),
                                AppStorePropertyReader.getString("label.history.change"),changer) + "<br/>");
            } else if (!firstAppHistory.getDescription().trim().equals(loadedApp.getDescription().trim())) {
                compareMessage.add(
                        HistoryUtil.createChangeDifferenceMessage(HistoryUtil.removeBackslash(firstAppHistory.getDescription()),
                                HistoryUtil.removeBackslash(loadedApp.getDescription()), firstAppHistory.getCreationDate(), AppStorePropertyReader.getString("App.description"),
                                AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"), AppStorePropertyReader.getString("label.history.to"), AppStorePropertyReader.getString("label.history.change"),changer) + "<br/>");
            }
        }

        if (!firstAppHistory.getPackageName().trim().equals(loadedApp.getPackageName().trim())) {
            compareMessage.add(
                    HistoryUtil.createChangeDifferenceMessage(firstAppHistory.getPackageName(), loadedApp.getPackageName(),
                            firstAppHistory.getCreationDate(), AppStorePropertyReader.getString("App.appPackageName"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"),
                            AppStorePropertyReader.getString("label.history.to"), AppStorePropertyReader.getString("label.history.change"),changer) + "<br/>");
        }


        if (!firstAppHistory.getAppCategory().equals(loadedApp.getAppCategory())) {
            compareMessage.add(
                    HistoryUtil.createChangeDifferenceMessage(firstAppHistory.getAppCategory().toString(), loadedApp.getAppCategory().toString(),
                            firstAppHistory.getCreationDate(), AppStorePropertyReader.getString("App.appCategory"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"),
                            AppStorePropertyReader.getString("label.history.to"), AppStorePropertyReader.getString("label.history.change"),changer) + "<br/>");
        }

        if (!firstAppHistory.getDeveloper().equals(loadedApp.getDeveloper())) {
            compareMessage.add(
                    HistoryUtil.createChangeDifferenceMessage(firstAppHistory.getDeveloper().toString(), loadedApp.getDeveloper().toString(),
                            firstAppHistory.getCreationDate(), AppStorePropertyReader.getString("App.developer"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"),
                            AppStorePropertyReader.getString("label.history.to"), AppStorePropertyReader.getString("label.history.change"),changer) + "<br/>");
        }

        if (firstAppHistory.getOs() != null || loadedApp.getOs() != null) {
            if (firstAppHistory.getOs() == null && loadedApp.getOs() != null) {
                compareMessage.add(
                        HistoryUtil.createAddDifferenceMessage(loadedApp.getOs().toString(), firstAppHistory.getCreationDate(), AppStorePropertyReader.getString("App.os"),
                                new ResourceModel("label.value").getObject(), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.to"),
                                AppStorePropertyReader.getString("label.history.change"),changer) + "<br/>");
            } else if (firstAppHistory.getOs() != null && loadedApp.getOs() == null) {

                compareMessage.add(
                        HistoryUtil.createChangeDifferenceMessage(firstAppHistory.getOs().toString(), " ",
                                firstAppHistory.getCreationDate(), AppStorePropertyReader.getString("App.os"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"),
                                AppStorePropertyReader.getString("label.history.to"), AppStorePropertyReader.getString("label.history.change"),changer) + "<br/>");
            } else if (!firstAppHistory.getOs().equals(loadedApp.getOs())) {
                compareMessage.add(
                        HistoryUtil.createChangeDifferenceMessage(firstAppHistory.getOs().toString(), loadedApp.getOs().toString(),
                                firstAppHistory.getCreationDate(), AppStorePropertyReader.getString("App.os"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"),
                                AppStorePropertyReader.getString("label.history.to"), AppStorePropertyReader.getString("label.history.change"),changer) + "<br/>");
            }
        }

        if (!firstAppHistory.getOsType().equals(loadedApp.getOsType())) {
            compareMessage.add(
                    HistoryUtil.createChangeDifferenceMessage(firstAppHistory.getOsType().toString(), loadedApp.getOsType().toString(),
                            firstAppHistory.getCreationDate(), AppStorePropertyReader.getString("App.osType"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"),
                            AppStorePropertyReader.getString("label.history.to"), AppStorePropertyReader.getString("label.history.change"),changer) + "<br/>");
        }

        if (!firstAppHistory.getMainPackage().equals(loadedApp.getMainPackage())) {
            AppPackage firstMainPackage = (AppPackage) session.load(AppPackage.class, firstAppHistory.getMainPackage().getId());
            AppPackage secondMainPackage = (AppPackage) session.load(AppPackage.class, loadedApp.getMainPackage().getId());
            compareMessage.add(
                    HistoryUtil.createChangeDifferenceMessage(firstMainPackage.getVersionCode(), secondMainPackage.getVersionCode(),
                            firstAppHistory.getCreationDate(), AppStorePropertyReader.getString("App.mainPackage"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"),
                            AppStorePropertyReader.getString("label.history.to"), AppStorePropertyReader.getString("label.history.change"),changer) + "<br/>");
        }

        List<AppPackage> firstAppHistoryAppPackages = firstAppHistory.getAppPackages();
        List<AppPackage> secondAppHistoryAppPackages = loadedApp.getAppPackages();
        if (firstAppHistory.getAppPackages() != null && loadedApp.getAppPackages() != null) {
            StringBuilder packageInfoString = new StringBuilder();
            packageInfoString.append(AppStorePropertyReader.getString("APPPackage")).append(" ").append(AppStorePropertyReader.getString("label.with")).append(" ").append(AppStorePropertyReader.getString("APPPackage.versionCode"));
            if (firstAppHistory.getAppPackages().size() != loadedApp.getAppPackages().size()) {
                for (AppPackage appPackage : secondAppHistoryAppPackages) {
                    appPackage.setCreationDate(MyCalendarUtil.toPersian(appPackage.getCreationDate()));
                    if (!firstAppHistoryAppPackages.contains(appPackage)) {
                        compareMessage.add(
                                HistoryUtil.createAddDifferenceMessage(appPackage.getVersionName(), appPackage.getCreationDate(), AppStorePropertyReader.getString("App.appPackages"),
                                        packageInfoString.toString(), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.to"),
                                        AppStorePropertyReader.getString("label.history.add"),changer) + "<br/>");
                    }
                }
            }
        }

        for (AppPackage appPackage : secondAppHistoryAppPackages) {
            if (firstAppHistoryAppPackages.contains(appPackage)) {
//                if (loadedApp.getMainPackage().equals(appPackage)) {
                if (!checkedPackageHistory.contains(appPackage.getId())) {
                    compareMessage.addAll(AppPackageHistoryService.Instance.createHistoryMessage(appPackage, true));
                    checkedPackageHistory.add(appPackage.getId());
                }
//                }
            }
        }
        return compareMessage;
    }


    public App setAppHistoryToApp(Session session, App app) {
        AppHistory appHistory = setAppHistoryByApp(session, app);

        return app;
    }


}
