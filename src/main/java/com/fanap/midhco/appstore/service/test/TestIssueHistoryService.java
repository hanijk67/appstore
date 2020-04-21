package com.fanap.midhco.appstore.service.test;

import com.fanap.midhco.appstore.applicationUtils.HistoryUtil;
import com.fanap.midhco.appstore.entities.TestIssue;
import com.fanap.midhco.appstore.entities.TestIssueHistory;
import com.fanap.midhco.appstore.entities.TestSubIssue;
import com.fanap.midhco.appstore.entities.User;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.apache.wicket.model.ResourceModel;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by A.Moshiri on 7/23/2017.
 */
public class TestIssueHistoryService {
    public static TestIssueHistoryService Instance = new TestIssueHistoryService();

    private TestIssueHistoryService() {
    }

    public void saveOrUpdate(TestIssueHistory testIssueHistory, Session session) {
        if (testIssueHistory.getId() == null) {
            testIssueHistory.setCreationDate(DateTime.now());
            testIssueHistory.setCreatorUser(PrincipalUtil.getCurrentUser());
        } else {
            testIssueHistory.setLastModifyDate(DateTime.now());
            testIssueHistory.setLastModifyUser(PrincipalUtil.getCurrentUser());
        }
        session.saveOrUpdate(testIssueHistory);
    }


    public TestIssueHistory setTestIssueHistoryByTestIssue(TestIssue formTestIssue) {
        TestIssueHistory testHistory = new TestIssueHistory();
        testHistory.setApprovalState(formTestIssue.getApprovalState());
        testHistory.setPriority(formTestIssue.getPriority());
        testHistory.setTitle(formTestIssue.getTitle());
        testHistory.setDescription(formTestIssue.getDescription());
        if (formTestIssue.getSubIssues() != null && formTestIssue.getSubIssues().size() > 0) {
            List<TestSubIssue> testSubIssueList = new ArrayList<>();
            for (TestSubIssue testSubIssue : formTestIssue.getSubIssues()) {
                testSubIssueList.add(testSubIssue);
            }
            testHistory.setSubIssues(testSubIssueList);
        }
        return testHistory;
    }


    public List<String> createHistoryMessage(TestIssue testIssue, boolean requestFromAppPackage) {
        List<String> validationString = new ArrayList<>();
        Session session = HibernateUtil.getCurrentSession();

        if (testIssue != null && testIssue.getId() != null) {

            TestIssue loadedTeTestIssue = (TestIssue) session.load(TestIssue.class, testIssue.getId());
            List<TestIssueHistory> testIssueHistoryList = loadedTeTestIssue.getHistories();
            int testIssueHistorySize = 0;
            if (testIssueHistoryList != null) {
                testIssueHistorySize = testIssueHistoryList.size();
            }

            if (testIssueHistorySize > 1) {
                Collections.sort(testIssueHistoryList, (firstTestIssueHist, secondTestIssueHist) -> firstTestIssueHist.getCreationDate().compareTo(secondTestIssueHist.getCreationDate()));

                for (int i = 1; i < testIssueHistorySize; i++) {
                    TestIssueHistory firstTestIssueHistory =
                            (testIssueHistoryList.get(i - 1) != null && testIssueHistoryList.get(i - 1).getId() != null) ?
                                    ((TestIssueHistory) session.load(TestIssueHistory.class, testIssueHistoryList.get(i - 1).getId())) : null;

                    TestIssueHistory secondTestIssueHistory =
                            (testIssueHistoryList.get(i) != null && testIssueHistoryList.get(i).getId() != null) ?
                                    ((TestIssueHistory) session.load(TestIssueHistory.class, testIssueHistoryList.get(i).getId())) : null;


                    if (firstTestIssueHistory != null && secondTestIssueHistory != null) {
                        Long changerId = firstTestIssueHistory.getLastModifyUser() !=null ?firstTestIssueHistory.getLastModifyUser().getId(): firstTestIssueHistory.getCreatorUser().getId();
                        User changer = (User) session.load(User.class, changerId);
                        validationString.addAll(checkAppHistoryVersions(firstTestIssueHistory, secondTestIssueHistory, session, requestFromAppPackage,changer.getFullName()));
                    }
                }
                if (validationString.size() == 0) {
                    validationString.add(AppStorePropertyReader.getString("label.history.noChange"));
                }
            } else {
                if (!requestFromAppPackage) {
                    validationString.add(AppStorePropertyReader.getString(("label.history.noChange")));
                }
            }

        } else {
            if (!requestFromAppPackage) {
                validationString.add(AppStorePropertyReader.getString(("no.testIssue.found")));
            }
        }
        Collections.reverse(validationString);
        return validationString;
    }

    public List<String> checkAppHistoryVersions(TestIssueHistory firstTestIssueHistory, TestIssueHistory secondTestIssueHistory, Session session, boolean requestFromAppPackage ,String changer) {

        List<String> compareMessage = new ArrayList<>();
        if (!firstTestIssueHistory.getTitle().trim().equals(secondTestIssueHistory.getTitle().trim())) {
            compareMessage.add(
                    HistoryUtil.createChangeDifferenceMessage(firstTestIssueHistory.getTitle(), secondTestIssueHistory.getTitle(), secondTestIssueHistory.getCreationDate(),
                            new ResourceModel("TestIssue.title").toString(), new ResourceModel("label.history.date").toString(), new ResourceModel("label.history.from").toString(),
                            new ResourceModel("label.history.to").toString(), new ResourceModel("label.history.change").toString(),changer) + "<br/>");
        }

        // if request was from app Package for each testIssue we have to show it's title
        if (requestFromAppPackage) {
            if (firstTestIssueHistory.getDescription() != null || secondTestIssueHistory.getDescription() != null) {
                if (firstTestIssueHistory.getDescription() != null && secondTestIssueHistory.getDescription() == null) {
                    compareMessage.add(
                            HistoryUtil.createAddDifferenceTestIssueMessage(secondTestIssueHistory.getDescription(),secondTestIssueHistory.getTitle(), secondTestIssueHistory.getCreationDate(),
                                    String.valueOf(new ResourceModel("APPPackage.changeLog")), new ResourceModel("label.value").getObject(), new ResourceModel("label.history.date").getObject(),
                                    new ResourceModel("label.history.to").getObject(), new ResourceModel("label.history.add").getObject(),changer) + "<br/>");
                } else if (firstTestIssueHistory.getDescription() == null && secondTestIssueHistory.getDescription() != null) {

                    compareMessage.add(
                            HistoryUtil.createChangeDifferenceTestIssueMessage("", firstTestIssueHistory.getDescription(), HistoryUtil.removeBackslash(secondTestIssueHistory.getDescription()),
                                    secondTestIssueHistory.getCreationDate(), new ResourceModel("TestIssue.description").getObject(), new ResourceModel("label.history.date").getObject(),
                                    new ResourceModel("label.history.from").getObject(), new ResourceModel("label.history.to").getObject(),
                                    new ResourceModel("label.history.change").getObject(),changer) + "<br/>");
                } else if (!firstTestIssueHistory.getDescription().trim().equals(secondTestIssueHistory.getDescription().trim())) {
                    compareMessage.add(
                            HistoryUtil.createChangeDifferenceTestIssueMessage(HistoryUtil.removeBackslash(firstTestIssueHistory.getDescription()), firstTestIssueHistory.getTitle(),
                                    HistoryUtil.removeBackslash(secondTestIssueHistory.getDescription()), secondTestIssueHistory.getCreationDate(),
                                    new ResourceModel("TestIssue.description").getObject(), new ResourceModel("label.history.date").getObject(), new ResourceModel("label.history.from").getObject(),
                                    new ResourceModel("label.history.to").getObject(), new ResourceModel("label.history.change").getObject(),changer) + "<br/>");
                }
            }

            if (!firstTestIssueHistory.getApprovalState().equals(secondTestIssueHistory.getApprovalState())) {
                compareMessage.add(
                        HistoryUtil.createChangeDifferenceTestIssueMessage(firstTestIssueHistory.getApprovalState().toString(), firstTestIssueHistory.getTitle(),
                                secondTestIssueHistory.getApprovalState().toString(), secondTestIssueHistory.getCreationDate(), new ResourceModel("TestIssue.approvalState").getObject(),
                                new ResourceModel("label.history.date").getObject(), new ResourceModel("label.history.from").getObject(), new ResourceModel("label.history.to").getObject(),
                                new ResourceModel("label.history.change").getObject(),changer) + "<br/>");
            }

            if (!firstTestIssueHistory.getPriority().equals(secondTestIssueHistory.getPriority())) {
                compareMessage.add(
                        HistoryUtil.createChangeDifferenceTestIssueMessage(firstTestIssueHistory.getPriority().toString(), firstTestIssueHistory.getTitle(),
                                secondTestIssueHistory.getPriority().toString(), secondTestIssueHistory.getCreationDate(), new ResourceModel("TestIssue.priority").getObject(),
                                new ResourceModel("label.history.date").getObject(), new ResourceModel("label.history.from").getObject(), new ResourceModel("label.history.to").getObject(),
                                new ResourceModel("label.history.change").getObject(),changer) + "<br/>");
            }


            if (firstTestIssueHistory.getSubIssues().size() != secondTestIssueHistory.getSubIssues().size()) {
                List<TestSubIssue> firstAppPackageHistoryTestSubIssues = firstTestIssueHistory.getSubIssues();
                List<TestSubIssue> secondAppPackageHistoryTestSubIssues = secondTestIssueHistory.getSubIssues();
                for (TestSubIssue testSubIssue : secondAppPackageHistoryTestSubIssues) {
                    if (!firstAppPackageHistoryTestSubIssues.contains(testSubIssue)) {
                        compareMessage.add(
                                HistoryUtil.createAddDifferenceTestIssueMessage(testSubIssue.getDescription(), firstTestIssueHistory.getTitle(), testSubIssue.getCreationDate(),
                                        new ResourceModel("TestSubIssue").getObject(), new ResourceModel("label.history.new").getObject(),
                                        new ResourceModel("label.history.date").getObject(), new ResourceModel("label.history.to").getObject(),
                                        new ResourceModel("label.history.add").getObject(),changer) + "<br/>");
                    }
                }
            }

        } else {
            if (firstTestIssueHistory.getDescription() != null || secondTestIssueHistory.getDescription() != null) {
                if (firstTestIssueHistory.getDescription() != null && secondTestIssueHistory.getDescription() == null) {
                    compareMessage.add(
                            HistoryUtil.createAddDifferenceMessage(secondTestIssueHistory.getDescription(), secondTestIssueHistory.getCreationDate(),
                                    new ResourceModel("APPPackage.changeLog").getObject(), new ResourceModel("label.value").getObject(), new ResourceModel("label.history.date").getObject(),
                                    new ResourceModel("label.history.to").getObject(), new ResourceModel("label.history.add").getObject(),changer) + "<br/>");
                } else if (firstTestIssueHistory.getDescription() == null && secondTestIssueHistory.getDescription() != null) {

                    compareMessage.add(
                            HistoryUtil.createChangeDifferenceMessage("", HistoryUtil.removeBackslash(secondTestIssueHistory.getDescription()), secondTestIssueHistory.getCreationDate(),
                                    new ResourceModel("TestIssue.description").getObject(), new ResourceModel("label.history.date").getObject(), new ResourceModel("label.history.from").getObject(),
                                    new ResourceModel("label.history.to").getObject(), new ResourceModel("label.history.change").getObject(),changer) + "<br/>");
                } else if (!firstTestIssueHistory.getDescription().trim().equals(secondTestIssueHistory.getDescription().trim())) {
                    compareMessage.add(
                            HistoryUtil.createChangeDifferenceMessage(HistoryUtil.removeBackslash(firstTestIssueHistory.getDescription()),
                                    HistoryUtil.removeBackslash(secondTestIssueHistory.getDescription()), secondTestIssueHistory.getCreationDate(),
                                    new ResourceModel("TestIssue.description").getObject(), new ResourceModel("label.history.date").getObject(), new ResourceModel("label.history.from").getObject(),
                                    new ResourceModel("label.history.to").getObject(), new ResourceModel("label.history.change").getObject(),changer) + "<br/>");
                }
            }


            if (!firstTestIssueHistory.getApprovalState().equals(secondTestIssueHistory.getApprovalState())) {
                compareMessage.add(
                        HistoryUtil.createChangeDifferenceMessage(firstTestIssueHistory.getApprovalState().toString(),
                                secondTestIssueHistory.getApprovalState().toString(), secondTestIssueHistory.getCreationDate(), new ResourceModel("TestIssue.approvalState").getObject(),
                                new ResourceModel("label.history.date").getObject(), new ResourceModel("label.history.from").getObject(), new ResourceModel("label.history.to").getObject(),
                                new ResourceModel("label.history.change").getObject(),changer) + "<br/>");
            }

            if (!firstTestIssueHistory.getPriority().equals(secondTestIssueHistory.getPriority())) {
                compareMessage.add(
                        HistoryUtil.createChangeDifferenceMessage(firstTestIssueHistory.getPriority().toString(),
                                secondTestIssueHistory.getPriority().toString(), secondTestIssueHistory.getCreationDate(), new ResourceModel("TestIssue.priority").getObject(),
                                new ResourceModel("label.history.date").getObject(), new ResourceModel("label.history.from").getObject(), new ResourceModel("label.history.to").getObject(),
                                new ResourceModel("label.history.change").getObject(),changer) + "<br/>");
            }

            if (firstTestIssueHistory.getSubIssues().size() != secondTestIssueHistory.getSubIssues().size()) {
                List<TestSubIssue> firstAppPackageHistoryTestSubIssues = firstTestIssueHistory.getSubIssues();
                List<TestSubIssue> secondAppPackageHistoryTestSubIssues = secondTestIssueHistory.getSubIssues();
                for (TestSubIssue testSubIssue : secondAppPackageHistoryTestSubIssues) {
                    if (!firstAppPackageHistoryTestSubIssues.contains(testSubIssue)) {
                        compareMessage.add(
                                HistoryUtil.createAddDifferenceMessage(testSubIssue.getDescription(), testSubIssue.getCreationDate(),
                                        new ResourceModel("TestSubIssue").getObject(), new ResourceModel("label.history.new").getObject(),
                                        new ResourceModel("label.history.date").getObject(), new ResourceModel("label.history.to").getObject(),
                                        new ResourceModel("label.history.add").getObject(),changer) + "<br/>");
                    }
                }
            }
        }


        return compareMessage;

    }


}
