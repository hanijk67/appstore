package com.fanap.midhco.appstore.service.app;

import com.fanap.midhco.appstore.applicationUtils.HistoryUtil;
import com.fanap.midhco.appstore.applicationUtils.MyCalendarUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.service.BaseEntityService;
import com.fanap.midhco.appstore.service.HQLBuilder;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by A.Moshiri on 7/18/2017.
 */
public class AppPackageHistoryService {
    public static AppPackageHistoryService Instance = new AppPackageHistoryService();

    private AppPackageHistoryService() {
    }

    public void saveOrUpdate(AppPackageHistory appPackageHistory, Session session) {
        if (appPackageHistory.getId() == null) {
            appPackageHistory.setCreationDate(DateTime.now());
            if (appPackageHistory.getCreatorUser()==null) {
            appPackageHistory.setCreatorUser(PrincipalUtil.getCurrentUser());
            }
        } else {
            appPackageHistory.setLastModifyDate(DateTime.now());
            if (PrincipalUtil.getCurrentUser()!=null) {
            appPackageHistory.setLastModifyUser(PrincipalUtil.getCurrentUser());
        }
        }
        session.saveOrUpdate(appPackageHistory);
    }

    public static AppPackageHistory setAppPackageHistoryByAppPackage(AppPackage appPackage, Session session) {
        if (appPackage != null && appPackage.getId() != null) {

//            Session newSession = HibernateUtil.getNewSession();
            AppPackage loadedAppPackageForSetHistory = (AppPackage) session.get(AppPackage.class, appPackage.getId());

            if (loadedAppPackageForSetHistory != null) {
                try {
                    AppPackageHistory appPackageHistory = new AppPackageHistory();
                    appPackageHistory.setChangeLog(loadedAppPackageForSetHistory.getChangeLog());
                    appPackageHistory.setIconFile(loadedAppPackageForSetHistory.getIconFile());
                    appPackageHistory.setMinSDK(loadedAppPackageForSetHistory.getMinSDK());
                    appPackageHistory.setPackFile(loadedAppPackageForSetHistory.getPackFile());
                    appPackageHistory.setCertificateInfo(loadedAppPackageForSetHistory.getCertificateInfo());
                    appPackageHistory.setPublishState(loadedAppPackageForSetHistory.getPublishState());
                    appPackageHistory.setTargetSDK(loadedAppPackageForSetHistory.getTargetSDK());
                    appPackageHistory.setVersionCode(loadedAppPackageForSetHistory.getVersionCode());
                    appPackageHistory.setVersionName(loadedAppPackageForSetHistory.getVersionName());
                    if (loadedAppPackageForSetHistory.getIconFile() != null) {
                        appPackageHistory.setIconFilePath(loadedAppPackageForSetHistory.getIconFile().getFilePath());
                    }
                    if (loadedAppPackageForSetHistory.getPackFile() != null) {
                        appPackageHistory.setPackFilePath(loadedAppPackageForSetHistory.getPackFile().getFilePath());
                    }
                    if (appPackage.getCreatorUser() != null) {
                        appPackageHistory.setCreatorUser(loadedAppPackageForSetHistory.getCreatorUser());
                    } else if (loadedAppPackageForSetHistory.getCreatorUser() != null) {
                        appPackageHistory.setCreatorUser(loadedAppPackageForSetHistory.getCreatorUser());
                    }
                    if (loadedAppPackageForSetHistory.getLastModifyUser() != null) {
                        appPackageHistory.setLastModifyUser(loadedAppPackageForSetHistory.getLastModifyUser());
                    } else if (appPackage.getLastModifyUser() != null) {
                        appPackageHistory.setLastModifyUser(loadedAppPackageForSetHistory.getLastModifyUser());
                    }
                    appPackageHistory.setThumbImages(loadedAppPackageForSetHistory.getThumbImages());
                    if (loadedAppPackageForSetHistory.getTestGroups() != null && loadedAppPackageForSetHistory.getTestGroups().size() > 0) {
                        List<TestGroup> testGroupList = new ArrayList<>();
                        for (TestGroup testGroup : loadedAppPackageForSetHistory.getTestGroups()) {
                            testGroupList.add(testGroup);
                        }
                        appPackageHistory.setTestGroups(testGroupList);
                    }

                    if (loadedAppPackageForSetHistory.getTestIssues() != null && loadedAppPackageForSetHistory.getTestIssues().size() > 0) {
                        List<TestIssue> testIssueList = new ArrayList<>();
                        for (TestIssue testIssue : loadedAppPackageForSetHistory.getTestIssues()) {
                            testIssueList.add(testIssue);
                        }
                        appPackageHistory.setTestIssues(testIssueList);
                    }

                    return appPackageHistory;
                } finally {
//                    if (newSession != null && newSession.isOpen())
//                        newSession.close();

                }
            }
        }
        return null;
    }

    public AppPackage setAppPackageHistoryForAppPackage(Session session, AppPackage appPackage) {
        AppPackage loadedAppPackage = (AppPackage) session.load(AppPackage.class, appPackage.getId());

        Session newSession = HibernateUtil.getNewSession();
        AppPackageHistory appPackageHistory = setAppPackageHistoryByAppPackage(appPackage, newSession);

        if (appPackageHistory != null) {
            AppPackageHistoryService.Instance.saveOrUpdate(appPackageHistory, session);
            List<AppPackageHistory> appPackageHistories = null;

            appPackageHistories = loadedAppPackage.getHistories();

            if (appPackageHistories == null)
                appPackageHistories = new ArrayList<AppPackageHistory>();

            appPackageHistories.add(appPackageHistory);

            appPackage.setHistories(appPackageHistories);
        }

        newSession.close();

        return appPackage;












/*
//        AppPackageHistory appPackageHistory = setAppPackageHistoryByAppPackage(loadedAppPackage);
        Session newSession = HibernateUtil.getNewSession();
        AppPackageHistory appPackageHistory = setAppPackageHistoryByAppPackage(appPackage, newSession);
        newSession.close();
        AppPackage loadedAppPackage = (AppPackage) session.load(AppPackage.class, appPackage.getId());
        if (appPackageHistory != null) {
            AppPackageHistoryService.Instance.saveOrUpdate(appPackageHistory, session);

            List<AppPackageHistory> appPackageHistories = null;

            appPackageHistories = loadedAppPackage.getHistories();

            if (appPackageHistories == null)
                appPackageHistories = new ArrayList<AppPackageHistory>();

            appPackageHistories.add(appPackageHistory);

            appPackage.setHistories(appPackageHistories);
        }
        return appPackage;*/
    }

    public List<String> createHistoryMessage(AppPackage appPackage, boolean fromApp) {
        List<String> validationString = new ArrayList<>();
        Session session = HibernateUtil.getNewSession();
        if (appPackage != null && appPackage.getId() != null) {
            AppPackage loadedAppPackage = (AppPackage) session.load(AppPackage.class, appPackage.getId());


            if (loadedAppPackage != null) {
                List<AppPackageHistory> appPackageHistories = loadedAppPackage.getHistories();
                List<AppPackageHistory> appPackageHistoryList =removeNullItemFromAppPackageHistory(appPackageHistories,session);
                if (appPackageHistoryList != null && !appPackageHistoryList.isEmpty()) {
                    if (appPackageHistoryList.size() > 1) {
                        Collections.sort(appPackageHistoryList,
                                (firstAppPackageHist, secondAppPackageHist) -> firstAppPackageHist.getCreationDate().compareTo(secondAppPackageHist.getCreationDate()));
                        for (int i = 1; i < appPackageHistoryList.size(); i++) {
                            AppPackageHistory firstAppPackageHistory =
                                    (appPackageHistoryList.get(i - 1) != null && appPackageHistoryList.get(i - 1).getId() != null) ?
                                            ((AppPackageHistory) session.load(AppPackageHistory.class, appPackageHistoryList.get(i - 1).getId())) : null;
                            if (firstAppPackageHistory != null) {
                                firstAppPackageHistory.setCreationDate(MyCalendarUtil.toPersian(firstAppPackageHistory.getCreationDate()));
                            }

                            AppPackageHistory secondAppPackageHistory =
                                    (appPackageHistoryList.get(i) != null && appPackageHistoryList.get(i).getId() != null) ?
                                            ((AppPackageHistory) session.load(AppPackageHistory.class, appPackageHistoryList.get(i).getId())) : null;

                            if (secondAppPackageHistory != null) {
                                secondAppPackageHistory.setCreationDate(MyCalendarUtil.toPersian(secondAppPackageHistory.getCreationDate()));
                            }
                            if (firstAppPackageHistory != null && secondAppPackageHistory != null) {
                                if (secondAppPackageHistory.getLastModifyUser() != null) {
                                    session.evict(secondAppPackageHistory.getLastModifyUser());
                                    session.refresh(secondAppPackageHistory.getLastModifyUser());
                                }
                                Long changerId = firstAppPackageHistory.getLastModifyUser() != null ? firstAppPackageHistory.getLastModifyUser().getId() : firstAppPackageHistory.getCreatorUser().getId();
                                User changer = (User) session.load(User.class, changerId);
                                validationString.addAll(checkAppHistoryVersions(firstAppPackageHistory, secondAppPackageHistory, session, fromApp, changer.getFullName()));
                            }
                            session.clear();
                        }
                        if (validationString.size() == 0 && !fromApp) {
                            validationString.add(AppStorePropertyReader.getString("label.history.noChange"));
                        }
                    }
                    Collections.sort(appPackageHistoryList,
                            (firstAppPackageHist, secondAppPackageHist) -> firstAppPackageHist.getCreationDate().compareTo(secondAppPackageHist.getCreationDate()));
                    AppPackageHistory firstAppPackageHistory =
                            (appPackageHistoryList.get(appPackageHistoryList.size() - 1) != null && appPackageHistoryList.get(appPackageHistories.size() - 1).getId() != null) ?
                                    ((AppPackageHistory) session.load(AppPackageHistory.class, appPackageHistoryList.get(appPackageHistories.size() - 1).getId())) : null;
                    if (firstAppPackageHistory != null) {
                        if (loadedAppPackage.getLastModifyUser() != null) {
                            session.evict(loadedAppPackage.getLastModifyUser());
                            session.refresh(loadedAppPackage.getLastModifyUser());
                        }
                        Long changerId = firstAppPackageHistory.getLastModifyUser() != null ? firstAppPackageHistory.getLastModifyUser().getId() : firstAppPackageHistory.getCreatorUser().getId();
                        User changer = (User) session.load(User.class, changerId);
                        firstAppPackageHistory.setCreationDate(MyCalendarUtil.toPersian(firstAppPackageHistory.getCreationDate()));
                        validationString.addAll(checkAppHistoryVersions(loadedAppPackage, firstAppPackageHistory, session, fromApp, changer.getFullName()));

                    }


                    session.clear();

                    if (validationString.size() == 0 && !fromApp) {
                        validationString.add(AppStorePropertyReader.getString("label.history.noChange"));
                    }

                } else if (!fromApp) {
                    validationString.add(AppStorePropertyReader.getString("label.history.noChange"));
                }
            }

        } else if (!fromApp) {
            validationString.add(AppStorePropertyReader.getString("no.appPackage.found"));
        }
        Collections.reverse(validationString);
        if(validationString!=null && validationString.size()>1 && validationString.contains(AppStorePropertyReader.getString("label.history.noChange"))){
            validationString.remove(AppStorePropertyReader.getString("label.history.noChange"));
        }
        if (session != null && session.isOpen()) {
            session.close();
        }
        return validationString;
    }

    private List<AppPackageHistory> removeNullItemFromAppPackageHistory(List<AppPackageHistory> appPackageHistories, Session session) {
        List<AppPackageHistory>appPackageHistoryList = new ArrayList<>();
        if (appPackageHistories!=null) {
            for(AppPackageHistory appPackageHistory : appPackageHistories){
                if(appPackageHistory!=null && appPackageHistory.getId()!=null){
                    AppPackageHistory loadedAppPackageHistory = (AppPackageHistory) session.get(AppPackageHistory.class, appPackageHistory.getId());
                    if (loadedAppPackageHistory!=null) {
                        appPackageHistoryList.add(loadedAppPackageHistory);
                    }
                }
            }
            return appPackageHistoryList;
        }else {
            return null;
        }
    }

    public List<String> checkAppHistoryVersions(AppPackageHistory firstAppPackageHistory, AppPackageHistory secondAppPackageHistory, Session session, boolean fromApp, String changer) {
        List<String> compareMessage = new ArrayList<>();

        if (!firstAppPackageHistory.getPublishState().equals(secondAppPackageHistory.getPublishState())) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(AppStorePropertyReader.getString("APPPackage.publishState"));
            if (fromApp) {
                stringBuilder.append(" ").append(AppStorePropertyReader.getString("label.for")).append(" ").
                        append(AppStorePropertyReader.getString("app.newAppPackage")).append(" ").append(AppStorePropertyReader.getString("label.with"))
                        .append(AppStorePropertyReader.getString("APPPackage.versionCode")).append(" ").append(firstAppPackageHistory.getVersionCode());
            }
            String titleString = stringBuilder.toString();
            compareMessage.add(
                    HistoryUtil.createChangeDifferenceMessage(firstAppPackageHistory.getPublishState().toString(), secondAppPackageHistory.getPublishState().toString(),
                            firstAppPackageHistory.getCreationDate(), titleString,
                            AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"),
                            AppStorePropertyReader.getString("label.history.to"), AppStorePropertyReader.getString("label.history.change"), changer) + "<br/>");
        }

        if (!fromApp) {
            if (!firstAppPackageHistory.getVersionCode().trim().equals(secondAppPackageHistory.getVersionCode().trim())) {
                compareMessage.add(
                        HistoryUtil.createChangeDifferenceMessage(firstAppPackageHistory.getVersionCode(),
                                HistoryUtil.removeBackslash(secondAppPackageHistory.getVersionCode()), firstAppPackageHistory.getCreationDate(),
                                AppStorePropertyReader.getString("APPPackage.versionCode"), AppStorePropertyReader.getString("label.history.date"),
                                AppStorePropertyReader.getString("label.history.from"), AppStorePropertyReader.getString("label.history.to"),
                                AppStorePropertyReader.getString("label.history.change"), changer) + "<br/>");
            }


            if (!firstAppPackageHistory.getVersionName().trim().equals(secondAppPackageHistory.getVersionName().trim())) {
                compareMessage.add(
                        HistoryUtil.createChangeDifferenceMessage(firstAppPackageHistory.getVersionName(),
                                HistoryUtil.removeBackslash(secondAppPackageHistory.getVersionName()), firstAppPackageHistory.getCreationDate(),
                                AppStorePropertyReader.getString("APPPackage.versionName"), AppStorePropertyReader.getString("label.history.date"),
                                AppStorePropertyReader.getString("label.history.from"), AppStorePropertyReader.getString("label.history.to"),
                                AppStorePropertyReader.getString("label.history.change"), changer) + "<br/>");
            }
            if (firstAppPackageHistory.getChangeLog() != null || secondAppPackageHistory.getChangeLog() != null) {

                if (firstAppPackageHistory.getChangeLog() == null && secondAppPackageHistory.getChangeLog() != null
                        && !secondAppPackageHistory.getChangeLog().trim().equals("") && !secondAppPackageHistory.getChangeLog().trim().equals("<br>")) {
                    compareMessage.add(
                            HistoryUtil.createAddDifferenceMessage(secondAppPackageHistory.getChangeLog(), firstAppPackageHistory.getCreationDate(),
                                    AppStorePropertyReader.getString("APPPackage.changeLog"), AppStorePropertyReader.getString("label.history.add"),
                                    AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.to"),
                                    AppStorePropertyReader.getString("label.history.add"), changer) + "<br/>");
                } else if (firstAppPackageHistory.getChangeLog() != null && !firstAppPackageHistory.getChangeLog().trim().equals("")
                        && !firstAppPackageHistory.getChangeLog().trim().equals("<br>") && secondAppPackageHistory.getChangeLog() == null) {
                    compareMessage.add(
                            HistoryUtil.createChangeDifferenceMessage(HistoryUtil.removeBackslash(firstAppPackageHistory.getChangeLog()), "", firstAppPackageHistory.getCreationDate(),
                                    AppStorePropertyReader.getString("APPPackage.changeLog"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"),
                                    AppStorePropertyReader.getString("label.history.to"), AppStorePropertyReader.getString("label.history.change"), changer) + "<br/>");
                } else if (firstAppPackageHistory.getChangeLog()!=null && secondAppPackageHistory.getChangeLog()!=null && !firstAppPackageHistory.getChangeLog().trim().equals(secondAppPackageHistory.getChangeLog().trim())) {
                    compareMessage.add(
                            HistoryUtil.createChangeDifferenceMessage(HistoryUtil.removeBackslash(firstAppPackageHistory.getChangeLog()),
                                    HistoryUtil.removeBackslash(secondAppPackageHistory.getChangeLog()), firstAppPackageHistory.getCreationDate(),
                                    AppStorePropertyReader.getString("APPPackage.changeLog"), AppStorePropertyReader.getString("label.history.date"),
                                    AppStorePropertyReader.getString("label.history.from"), AppStorePropertyReader.getString("label.history.to"),
                                    AppStorePropertyReader.getString("label.history.change"), changer) + "<br/>");
                }

            }

            if (!firstAppPackageHistory.getTargetSDK().trim().equals(secondAppPackageHistory.getTargetSDK().trim())) {
                compareMessage.add(
                        HistoryUtil.createChangeDifferenceMessage(firstAppPackageHistory.getTargetSDK(),
                                HistoryUtil.removeBackslash(secondAppPackageHistory.getTargetSDK()), firstAppPackageHistory.getCreationDate(), AppStorePropertyReader.getString("APPPackage.targetSDK"),
                                AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"),
                                AppStorePropertyReader.getString("label.history.to"), AppStorePropertyReader.getString("label.history.change"), changer) + "<br/>");
            }


            if (!firstAppPackageHistory.getMinSDK().trim().equals(secondAppPackageHistory.getMinSDK().trim())) {
                compareMessage.add(
                        HistoryUtil.createChangeDifferenceMessage(firstAppPackageHistory.getMinSDK(),
                                HistoryUtil.removeBackslash(secondAppPackageHistory.getMinSDK()), firstAppPackageHistory.getCreationDate(),
                                AppStorePropertyReader.getString("APPPackage.minSDK"), AppStorePropertyReader.getString("label.history.date"),
                                AppStorePropertyReader.getString("label.history.from"), AppStorePropertyReader.getString("label.history.to"),
                                AppStorePropertyReader.getString("label.history.change"), changer) + "<br/>");
            }

            if (firstAppPackageHistory.getTestGroups().size() != secondAppPackageHistory.getTestGroups().size()) {
                List<TestGroup> firstAppPackageHistoryTestGroups = firstAppPackageHistory.getTestGroups();
                List<TestGroup> secondAppPackageHistoryTestGroups = secondAppPackageHistory.getTestGroups();
                for (TestGroup testGroup : secondAppPackageHistoryTestGroups) {
                    if (!firstAppPackageHistoryTestGroups.contains(testGroup)) {
                        compareMessage.add(
                                HistoryUtil.createAddDifferenceMessage(testGroup.getTitle(), testGroup.getCreationDate(),
                                        AppStorePropertyReader.getString("APPPackage.testGroup"), AppStorePropertyReader.getString("label.history.new"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.to"),
                                        AppStorePropertyReader.getString("label.history.add"), changer) + "<br/>");
                    }
                }
            }
            if (firstAppPackageHistory.getTestIssues().size() != secondAppPackageHistory.getTestIssues().size()) {
                List<TestIssue> firstAppPackageHistoryTestIssues = firstAppPackageHistory.getTestIssues();
                List<TestIssue> secondAppPackageHistoryTestIssues = secondAppPackageHistory.getTestIssues();
                for (TestIssue testIssue : secondAppPackageHistoryTestIssues) {
                    if (!firstAppPackageHistoryTestIssues.contains(testIssue)) {
                        compareMessage.add(
                                HistoryUtil.createAddDifferenceMessage(testIssue.getTitle(), testIssue.getCreationDate(),
                                        AppStorePropertyReader.getString("APPPackage.testIssue"), AppStorePropertyReader.getString("label.history.new"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.to"),
                                        AppStorePropertyReader.getString("label.history.add"), changer) + "<br/>");
                    }
                }
            }

            if (!firstAppPackageHistory.getCertificateInfo().equals(secondAppPackageHistory.getCertificateInfo())) {
                compareMessage.add(
                        HistoryUtil.createChangeDifferenceMessage(firstAppPackageHistory.getCertificateInfo(),
                                HistoryUtil.removeBackslash(secondAppPackageHistory.getCertificateInfo()), firstAppPackageHistory.getCreationDate(),
                                AppStorePropertyReader.getString("AppPackage.certificateInfo"), AppStorePropertyReader.getString("label.history.date"),
                                AppStorePropertyReader.getString("label.history.from"), AppStorePropertyReader.getString("label.history.to"),
                                AppStorePropertyReader.getString("label.history.change"), changer) + "<br/>");
            }

            if (!firstAppPackageHistory.getPackFilePath().equals(secondAppPackageHistory.getPackFilePath())) {
                compareMessage.add(
                        HistoryUtil.createChangeDifferenceMessage(firstAppPackageHistory.getPackFilePath(),
                                HistoryUtil.removeBackslash(secondAppPackageHistory.getPackFile().getFilePath()), firstAppPackageHistory.getCreationDate(),
                                AppStorePropertyReader.getString("APPPackage.packFile"), AppStorePropertyReader.getString("label.history.date"),
                                AppStorePropertyReader.getString("label.history.from"), AppStorePropertyReader.getString("label.history.to"),
                                AppStorePropertyReader.getString("label.history.change"), changer) + "<br/>");
            }

            if (!firstAppPackageHistory.getIconFilePath().equals(secondAppPackageHistory.getIconFilePath())) {
                compareMessage.add(
                        HistoryUtil.createChangeDifferenceMessage(firstAppPackageHistory.getIconFilePath(),
                                HistoryUtil.removeBackslash(secondAppPackageHistory.getIconFile().getFilePath()), firstAppPackageHistory.getCreationDate(),
                                AppStorePropertyReader.getString("APPPackage.iconFile"), AppStorePropertyReader.getString("label.history.date"),
                                AppStorePropertyReader.getString("label.history.from"), AppStorePropertyReader.getString("label.history.to"),
                                AppStorePropertyReader.getString("label.history.change"), changer) + "<br/>");
            }

//todo check this lines if we want to show history of thumbFile
//            List<File> firstHistoryPackThumbFile = firstAppPackageHistory.getThumbImages();
//            List<File> secondHistoryPackThumbFile = secondAppPackageHistory.getThumbImages();
//            for (File secondHistoryThumbFile : secondHistoryPackThumbFile) {
//                if (!firstHistoryPackThumbFile.contains(secondHistoryThumbFile)) {
//                    compareMessage.add(
//                            HistoryUtil.createAddDifferenceMessage(secondHistoryThumbFile.getFileName(), secondHistoryThumbFile.getCreationDate(),
//                                    AppStorePropertyReader.getString("APPPackage.testIssue"), AppStorePropertyReader.getString("label.history.new"),
//                                    AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.to"),
//                                    AppStorePropertyReader.getString("label.history.add")) + "<br/>");
//                }
//            }
//
//            for (File firstHistoryThumbFile : firstHistoryPackThumbFile) {
//                if (!secondHistoryPackThumbFile.contains(firstHistoryThumbFile)) {
//                    compareMessage.add(
//                            HistoryUtil.createAddDifferenceMessage(firstHistoryThumbFile.getFileName(), firstHistoryThumbFile.getLastModifyDate(),
//                                    AppStorePropertyReader.getString("APPPackage.testIssue"), AppStorePropertyReader.getString("label.history.new"),
//                                    AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.to"),
//                                    AppStorePropertyReader.getString("label.history.deleted")) + "<br/>");
//                }
//            }


        }

        return compareMessage;
    }

    public List<String> checkAppHistoryVersions(AppPackage appPackage, AppPackageHistory firstAppPackageHistory, Session session, boolean fromApp, String changer) {
        List<String> compareMessage = new ArrayList<>();
        AppPackage loadedAppPackage = (AppPackage) session.load(AppPackage.class, appPackage.getId());
        loadedAppPackage.setLastModifyDate(MyCalendarUtil.toPersian(loadedAppPackage.getCreationDate()));
        loadedAppPackage.getTestGroups();
        loadedAppPackage.getTestIssues();
        if (!loadedAppPackage.getPublishState().equals(firstAppPackageHistory.getPublishState())) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(AppStorePropertyReader.getString("APPPackage.publishState"));
            if (fromApp) {
                stringBuilder.append(" ").append(AppStorePropertyReader.getString("label.for")).append(" ").
                        append(AppStorePropertyReader.getString("app.newAppPackage")).append(" ").append(AppStorePropertyReader.getString("label.with"))
                        .append(AppStorePropertyReader.getString("APPPackage.versionCode")).append(" ").append(firstAppPackageHistory.getVersionCode());
            }
            String titleString = stringBuilder.toString();
            compareMessage.add(
                    HistoryUtil.createChangeDifferenceMessage(firstAppPackageHistory.getPublishState().toString(), loadedAppPackage.getPublishState().toString(),
                            firstAppPackageHistory.getCreationDate(), titleString,
                            AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"),
                            AppStorePropertyReader.getString("label.history.to"), AppStorePropertyReader.getString("label.history.change"), changer) + "<br/>");
        }

        if (!fromApp) {
            if (!loadedAppPackage.getVersionCode().trim().equals(firstAppPackageHistory.getVersionCode().trim())) {
                compareMessage.add(
                        HistoryUtil.createChangeDifferenceMessage(HistoryUtil.removeBackslash(firstAppPackageHistory.getVersionCode()),
                                HistoryUtil.removeBackslash(loadedAppPackage.getVersionCode()), firstAppPackageHistory.getCreationDate(),
                                AppStorePropertyReader.getString("APPPackage.versionCode"), AppStorePropertyReader.getString("label.history.date"),
                                AppStorePropertyReader.getString("label.history.from"), AppStorePropertyReader.getString("label.history.to"),
                                AppStorePropertyReader.getString("label.history.change"), changer) + "<br/>");
            }


            if (!loadedAppPackage.getVersionName().trim().equals(firstAppPackageHistory.getVersionName().trim())) {
                compareMessage.add(
                        HistoryUtil.createChangeDifferenceMessage(HistoryUtil.removeBackslash(firstAppPackageHistory.getVersionName()),
                                HistoryUtil.removeBackslash(loadedAppPackage.getVersionName()), loadedAppPackage.getCreationDate(),
                                AppStorePropertyReader.getString("APPPackage.versionName"), AppStorePropertyReader.getString("label.history.date"),
                                AppStorePropertyReader.getString("label.history.from"), AppStorePropertyReader.getString("label.history.to"),
                                AppStorePropertyReader.getString("label.history.change"), changer) + "<br/>");
            }
            if ((loadedAppPackage.getChangeLog() != null && !loadedAppPackage.getChangeLog().trim().equals("") && !loadedAppPackage.getChangeLog().trim().equals("<br>")) ||
                    (firstAppPackageHistory.getChangeLog() != null && !firstAppPackageHistory.getChangeLog().trim().equals("") && !firstAppPackageHistory.getChangeLog().trim().equals("<br>"))) {

                if (loadedAppPackage.getChangeLog() == null && firstAppPackageHistory.getChangeLog() != null) {
                    compareMessage.add(
                            HistoryUtil.createChangeDifferenceMessage(HistoryUtil.removeBackslash(loadedAppPackage.getChangeLog()), "", firstAppPackageHistory.getCreationDate(),
                                    AppStorePropertyReader.getString("APPPackage.changeLog"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"),
                                    AppStorePropertyReader.getString("label.history.to"), AppStorePropertyReader.getString("label.history.change"), changer) + "<br/>");
                } else if (loadedAppPackage.getChangeLog() != null && firstAppPackageHistory.getChangeLog() == null) {
                    compareMessage.add(
                            HistoryUtil.createAddDifferenceMessage(firstAppPackageHistory.getChangeLog(), firstAppPackageHistory.getCreationDate(),
                                    AppStorePropertyReader.getString("APPPackage.changeLog"), AppStorePropertyReader.getString("label.history.add"),
                                    AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.to"),
                                    AppStorePropertyReader.getString("label.history.add"), changer) + "<br/>");
                } else if (!loadedAppPackage.getChangeLog().trim().equals(firstAppPackageHistory.getChangeLog().trim())) {
                    compareMessage.add(
                            HistoryUtil.createChangeDifferenceMessage(HistoryUtil.removeBackslash(firstAppPackageHistory.getChangeLog()),
                                    HistoryUtil.removeBackslash(loadedAppPackage.getChangeLog()), firstAppPackageHistory.getCreationDate(),
                                    AppStorePropertyReader.getString("APPPackage.changeLog"), AppStorePropertyReader.getString("label.history.date"),
                                    AppStorePropertyReader.getString("label.history.from"), AppStorePropertyReader.getString("label.history.to"),
                                    AppStorePropertyReader.getString("label.history.change"), changer) + "<br/>");
                }

            }

            if (!loadedAppPackage.getTargetSDK().trim().equals(firstAppPackageHistory.getTargetSDK().trim())) {
                compareMessage.add(
                        HistoryUtil.createChangeDifferenceMessage(HistoryUtil.removeBackslash(firstAppPackageHistory.getTargetSDK()),
                                HistoryUtil.removeBackslash(loadedAppPackage.getTargetSDK()), firstAppPackageHistory.getCreationDate(), AppStorePropertyReader.getString("APPPackage.targetSDK"),
                                AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.from"),
                                AppStorePropertyReader.getString("label.history.to"), AppStorePropertyReader.getString("label.history.change"), changer) + "<br/>");
            }


            if (!loadedAppPackage.getMinSDK().trim().equals(firstAppPackageHistory.getMinSDK().trim())) {
                compareMessage.add(
                        HistoryUtil.createChangeDifferenceMessage(HistoryUtil.removeBackslash(firstAppPackageHistory.getMinSDK()),
                                HistoryUtil.removeBackslash(loadedAppPackage.getMinSDK()), firstAppPackageHistory.getCreationDate(),
                                AppStorePropertyReader.getString("APPPackage.minSDK"), AppStorePropertyReader.getString("label.history.date"),
                                AppStorePropertyReader.getString("label.history.from"), AppStorePropertyReader.getString("label.history.to"),
                                AppStorePropertyReader.getString("label.history.change"), changer) + "<br/>");
            }

            if (loadedAppPackage.getTestGroups() != null && firstAppPackageHistory.getTestGroups() != null &&
                    loadedAppPackage.getTestGroups().size() != firstAppPackageHistory.getTestGroups().size()) {
                List<TestGroup> firstAppPackageHistoryTestGroups = firstAppPackageHistory.getTestGroups();
                List<TestGroup> secondAppPackageHistoryTestGroups = loadedAppPackage.getTestGroups();
                for (TestGroup testGroup : secondAppPackageHistoryTestGroups) {
                    if (!firstAppPackageHistoryTestGroups.contains(testGroup)) {
                        compareMessage.add(
                                HistoryUtil.createAddDifferenceMessage(testGroup.getTitle(), testGroup.getCreationDate(),
                                        AppStorePropertyReader.getString("APPPackage.testGroup"), AppStorePropertyReader.getString("label.history.new"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.to"),
                                        AppStorePropertyReader.getString("label.history.add"), changer) + "<br/>");
                    }
                }
            }
            if (loadedAppPackage.getTestIssues() != null && firstAppPackageHistory.getTestGroups() != null &&
                    loadedAppPackage.getTestIssues().size() != firstAppPackageHistory.getTestIssues().size()) {
                List<TestIssue> firstAppPackageHistoryTestIssues = firstAppPackageHistory.getTestIssues();
                List<TestIssue> secondAppPackageHistoryTestIssues = loadedAppPackage.getTestIssues();
                for (TestIssue testIssue : secondAppPackageHistoryTestIssues) {
                    if (!firstAppPackageHistoryTestIssues.contains(testIssue)) {
                        compareMessage.add(
                                HistoryUtil.createAddDifferenceMessage(testIssue.getTitle(), testIssue.getCreationDate(),
                                        AppStorePropertyReader.getString("APPPackage.testIssue"), AppStorePropertyReader.getString("label.history.new"), AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.to"),
                                        AppStorePropertyReader.getString("label.history.add"), changer) + "<br/>");
                    }
                }
            }

            if (!firstAppPackageHistory.getCertificateInfo().equals(loadedAppPackage.getCertificateInfo())) {
                compareMessage.add(
                        HistoryUtil.createChangeDifferenceMessage(firstAppPackageHistory.getCertificateInfo(),
                                HistoryUtil.removeBackslash(loadedAppPackage.getCertificateInfo()), firstAppPackageHistory.getCreationDate(),
                                AppStorePropertyReader.getString("AppPackage.certificateInfo"), AppStorePropertyReader.getString("label.history.date"),
                                AppStorePropertyReader.getString("label.history.from"), AppStorePropertyReader.getString("label.history.to"),
                                AppStorePropertyReader.getString("label.history.change"), changer) + "<br/>");
            }

            if (!firstAppPackageHistory.getPackFilePath().equals(loadedAppPackage.getPackFile().getFilePath())) {
                compareMessage.add(
                        HistoryUtil.createChangeDifferenceMessage(firstAppPackageHistory.getPackFilePath(),
                                HistoryUtil.removeBackslash(loadedAppPackage.getPackFile().getFilePath()), firstAppPackageHistory.getCreationDate(),
                                AppStorePropertyReader.getString("APPPackage.packFile"), AppStorePropertyReader.getString("label.history.date"),
                                AppStorePropertyReader.getString("label.history.from"), AppStorePropertyReader.getString("label.history.to"),
                                AppStorePropertyReader.getString("label.history.change"), changer) + "<br/>");
            }

            if (firstAppPackageHistory != null &&
                    firstAppPackageHistory.getIconFilePath() != null &&
                    !firstAppPackageHistory.getIconFilePath().equals(loadedAppPackage.getIconFile().getFilePath())) {
                compareMessage.add(

                        HistoryUtil.createChangeDifferenceMessage(firstAppPackageHistory.getPackFilePath(),
                                HistoryUtil.removeBackslash(loadedAppPackage.getIconFile().getFilePath()), firstAppPackageHistory.getCreationDate(),
                                AppStorePropertyReader.getString("APPPackage.iconFile"), AppStorePropertyReader.getString("label.history.date"),
                                AppStorePropertyReader.getString("label.history.from"), AppStorePropertyReader.getString("label.history.to"),
                                AppStorePropertyReader.getString("label.history.change"), changer) + "<br/>");
            }

//todo check this lines if we want to show history of thumbFile
//            List<File> firstHistoryPackThumbFile = firstAppPackageHistory.getThumbImages();
//            List<File> secondHistoryPackThumbFile = loadedAppPackage.getThumbImages();
//            for (File secondHistoryThumbFile : secondHistoryPackThumbFile) {
//                if (!firstHistoryPackThumbFile.contains(secondHistoryThumbFile)) {
//                    compareMessage.add(
//                            HistoryUtil.createAddDifferenceMessage(secondHistoryThumbFile.getFileName(), secondHistoryThumbFile.getCreationDate(),
//                                    AppStorePropertyReader.getString("APPPackage.thumbImages"), AppStorePropertyReader.getString("label.history.new"),
//                                    AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.to"),
//                                    AppStorePropertyReader.getString("label.history.add"), changer) + "<br/>");
//                }
//            }
//
//            for (File firstHistoryThumbFile : firstHistoryPackThumbFile) {
//                if (!secondHistoryPackThumbFile.contains(firstHistoryThumbFile)) {
//                    compareMessage.add(
//                            HistoryUtil.createAddDifferenceMessage(firstHistoryThumbFile.getFileName(), firstHistoryThumbFile.getLastModifyDate(),
//                                    AppStorePropertyReader.getString("APPPackage.thumbImages"), AppStorePropertyReader.getString("label.history.new"),
//                                    AppStorePropertyReader.getString("label.history.date"), AppStorePropertyReader.getString("label.history.to"),
//                                    AppStorePropertyReader.getString("label.history.deleted"),changer) + "<br/>");
//                }
//            }


        }

        return compareMessage;
    }


    public void setAppPackageHistoryForNewAppPackage(Session session, AppPackage appPackage) {
        AppPackageHistory appPackageHistory = new AppPackageHistory();
        appPackageHistory.setChangeLog(appPackage.getChangeLog());
        appPackageHistory.setIconFile(appPackage.getIconFile());
        appPackageHistory.setMinSDK(appPackage.getMinSDK());
        appPackageHistory.setPackFile(appPackage.getPackFile());
        appPackageHistory.setCertificateInfo(appPackage.getCertificateInfo());
        appPackageHistory.setPublishState(appPackage.getPublishState());
        appPackageHistory.setTargetSDK(appPackage.getTargetSDK());
        appPackageHistory.setVersionCode(appPackage.getVersionCode());
        appPackageHistory.setVersionName(appPackage.getVersionName());
        if (appPackage.getIconFile() != null) {
            appPackageHistory.setIconFilePath(appPackage.getIconFile().getFilePath());
        }

        if (appPackage.getCreatorUser() != null) {
            appPackageHistory.setCreatorUser(appPackage.getCreatorUser());
        }
        if (appPackage.getLastModifyUser() != null) {
            appPackageHistory.setLastModifyUser(appPackage.getLastModifyUser());
        }
        if (appPackage.getPackFile() != null) {
            appPackageHistory.setPackFilePath(appPackage.getPackFile().getFilePath());
        }
        appPackageHistory.setThumbImages(appPackage.getThumbImages());
        if (appPackage.getTestGroups() != null && appPackage.getTestGroups().size() > 0) {
            List<TestGroup> testGroupList = new ArrayList<>();
            for (TestGroup testGroup : appPackage.getTestGroups()) {
                testGroupList.add(testGroup);
            }
            appPackageHistory.setTestGroups(testGroupList);
        }

        if (appPackage.getTestIssues() != null && appPackage.getTestIssues().size() > 0) {
            List<TestIssue> testIssueList = new ArrayList<>();
            for (TestIssue testIssue : appPackage.getTestIssues()) {
                testIssueList.add(testIssue);
            }
            appPackageHistory.setTestIssues(testIssueList);
        }
        appPackageHistory.setCreationDate(DateTime.now());
        session.saveOrUpdate(appPackageHistory);
        List<AppPackageHistory> appPackageHistories = new ArrayList<>();
        appPackageHistories.add(appPackageHistory);
        appPackage.setHistories(appPackageHistories);
    }

    private List<AppPackageHistory> listAll(Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select appPackageHistory ", "from AppPackageHistory appPackageHistory ");

        Query query = builder.createQuery();
        return query.list();
    }
}
