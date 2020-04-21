package com.fanap.midhco.appstore.service.test;

import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.service.BaseEntityService;
import com.fanap.midhco.appstore.service.HQLBuilder;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.app.AppPackageService;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by A.Moshiri on 6/21/2017.
 */
public class TestIssueServices {
    public static TestIssueServices Instance = new TestIssueServices();

    private TestIssueServices() {
    }

    public static class TestIssueCriteria implements Serializable {
        public String title;
        public Collection<ApprovalState> approvalState;
        public Collection<TestPriority> priority;

        public Long packageId;
        boolean fromSizeSearch = false;
        boolean showAllTest = false;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public boolean getShowAllTest() {
            return showAllTest;
        }

        public void setShowAllTest(boolean showAllTest) {
            this.showAllTest = showAllTest;
        }

        public Collection<ApprovalState> getApprovalState() {
            return approvalState;
        }

        public void setApprovalState(Collection<ApprovalState> approvalState) {
            this.approvalState = approvalState;
        }

        public Collection<TestPriority> getPriority() {
            return priority;
        }

        public void setPriority(Collection<TestPriority> priority) {
            this.priority = priority;
        }

        public Long getPackageId() {
            return packageId;
        }

        public void setPackageId(Long packageId) {
            this.packageId = packageId;
        }

        public boolean getFromSizeSearch() {
            return fromSizeSearch;
        }

        public void setFromSizeSearch(boolean fromSizeSearch) {
            this.fromSizeSearch = fromSizeSearch;
        }
    }

    public void applyCriteria(HQLBuilder builder, TestIssueCriteria testIssueCriteria) {

        if (testIssueCriteria.packageId != null) {
            if (!testIssueCriteria.getFromSizeSearch()) {
                builder.setFromClause("from App app left outer join app.appPackages pack join pack.testIssues as ent");
                builder.setSelectClause("select app, pack, ent");
            }else {
                builder.setFromClause("from AppPackage as pack join pack.testIssues as ent");
            }
        }else {
            if(testIssueCriteria.getShowAllTest() && !testIssueCriteria.getFromSizeSearch()){
                builder.setFromClause("from App app left outer join app.appPackages pack join pack.testIssues as ent");
                builder.setSelectClause("select app, pack, ent");
            }
        }
        if (testIssueCriteria.title != null && !testIssueCriteria.title.trim().isEmpty())
            builder.addClause("and ent.title like (:title_)", "title_", HQLBuilder.like(testIssueCriteria.title));

        if (testIssueCriteria.approvalState != null && testIssueCriteria.approvalState.size()>0)
            builder.addClause("and ent.approvalState in (:approvalState_)", "approvalState_", testIssueCriteria.approvalState);

        if (testIssueCriteria.priority != null && testIssueCriteria.priority.size()>0)
            builder.addClause("and ent.priority in (:priority_)", "priority_", testIssueCriteria.priority);

        if (testIssueCriteria.getPackageId() != null) {
            builder.addClause("and pack.id in (:id_)", "id_", testIssueCriteria.packageId);
        }

    }

    public Long count(TestIssueCriteria criteria, Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select count(ent.id) ", " from TestIssue ent ");
        if (criteria != null)
            applyCriteria(builder, criteria);

        Query query = builder.createQuery();
        return (Long) query.uniqueResult();
    }

    public List<TestIssueSearchModel> list(TestIssueCriteria criteria, int first, int count, String sortProp, boolean isAsc, Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select ent ", "from TestIssue ent ");
        if (criteria != null)
            applyCriteria(builder, criteria);

        Query query = builder.createQuery();

        if (sortProp != null)
            builder.addOrder(sortProp, isAsc);

        query.setFirstResult(first);
        if (count != -1)
            query.setMaxResults(count);
        List<Object[]> resultObjects = query.list();

        Stream<TestIssueSearchModel> searchResultModelStream = resultObjects.stream().map(objects -> {
            TestIssueSearchModel searchResultModel = new TestIssueSearchModel();
            App app = (App) objects[0];
            AppPackage appPackage = (AppPackage) objects[1];

            TestIssue testIssue = (TestIssue) objects[2];

            searchResultModel.id = testIssue.getId();
            searchResultModel.description = testIssue.getDescription();
            searchResultModel.title = testIssue.getTitle();
            searchResultModel.approvalState = testIssue.getApprovalState();
            searchResultModel.priority = testIssue.getPriority();
            searchResultModel.versionCode = appPackage.getVersionCode();
            searchResultModel.versionName = appPackage.getVersionName();
            searchResultModel.appTitle = app.getTitle();

            return searchResultModel;
        });


        List<TestIssueSearchModel> searchResultModelList =
                searchResultModelStream.collect(Collectors.<TestIssueSearchModel>toList());

        return searchResultModelList;

    }

    public List<TestIssueSearchModel> listAllToSearchModel(Session session) {

        HQLBuilder builder = new HQLBuilder(session, "select app, pack, ent", "from App app left outer join app.appPackages pack join pack.testIssues as ent");
        Query query = builder.createQuery();
        List<Object[]> resultObjects = query.list();

        Stream<TestIssueSearchModel> searchResultModelStream = resultObjects.stream().map(objects -> {
            TestIssueSearchModel searchResultModel = new TestIssueSearchModel();
            App app = (App) objects[0];
            TestIssue testIssue = (TestIssue) objects[1];
            AppPackage appPackage = (AppPackage) objects[2];

            searchResultModel.id = testIssue.getId();
            searchResultModel.description = testIssue.getDescription();
            searchResultModel.title = testIssue.getTitle();
            searchResultModel.approvalState = testIssue.getApprovalState();
            searchResultModel.priority = testIssue.getPriority();
            searchResultModel.versionCode = appPackage.getVersionCode();
            searchResultModel.versionName = appPackage.getVersionName();
            searchResultModel.appTitle = app.getTitle();

            return searchResultModel;
        });


        List<TestIssueSearchModel> searchResultModelList =
                searchResultModelStream.collect(Collectors.<TestIssueSearchModel>toList());

        return searchResultModelList;
    }

    public void saveOrUpdate(TestIssue testIssue, Session session) {
        if (testIssue.getId() == null) {
            testIssue.setCreationDate(DateTime.now());
            testIssue.setCreatorUser(PrincipalUtil.getCurrentUser());
        } else {
            testIssue.setLastModifyDate(DateTime.now());
            testIssue.setLastModifyUser(PrincipalUtil.getCurrentUser());
        }
        session.saveOrUpdate(testIssue);
    }

    public static class TestIssueSearchModel implements Serializable {
        Long id;
        public String title;
        public ApprovalState approvalState;
        public TestPriority priority;
        public String description;
        public String versionName;
        public String versionCode;
        public String appTitle;

        public String getAppTitle() {
            return appTitle;
        }

        public void setAppTitle(String appTitle) {
            this.appTitle = appTitle;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public ApprovalState getApprovalState() {
            return approvalState;
        }

        public void setApprovalState(ApprovalState approvalState) {
            this.approvalState = approvalState;
        }

        public TestPriority getPriority() {
            return priority;
        }

        public void setPriority(TestPriority priority) {
            this.priority = priority;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getVersionName() {
            return versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }

        public String getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(String versionCode) {
            this.versionCode = versionCode;
        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestIssueSearchModel that = (TestIssueSearchModel) o;
            return id.equals(that.id);
        }
    }

    public static void main(String[] args) {

        List<TestIssue> testIssues = new ArrayList<>();
        TestIssue testIssue = new TestIssue();
        testIssue.setTitle("test1");
        testIssue.setApprovalState(ApprovalState.DISAPPROVED);
        testIssue.setPriority(TestPriority.HIGH);
        testIssue.setDescription("first discription");
        testIssues.add(testIssue);

        TestIssue testIssue2 = new TestIssue();
        testIssue2.setTitle("test12");
        testIssue2.setApprovalState(ApprovalState.APPROVED);
        testIssue2.setPriority(TestPriority.MEDIUM);
        testIssue2.setDescription("second discription");
        testIssues.add(testIssue2);

        TestIssue testIssue3 = new TestIssue();
        testIssue3.setTitle("test3");
        testIssue3.setApprovalState(ApprovalState.APPROVED);
        testIssue3.setPriority(TestPriority.HIGH);
        testIssue3.setDescription("Third discription");
        testIssues.add(testIssue3);

        TestIssue testIssue4 = new TestIssue();
        testIssue4.setTitle("test1");
        testIssue4.setApprovalState(ApprovalState.DISAPPROVED);
        testIssue4.setPriority(TestPriority.LOW);
        testIssue4.setDescription("Forth discription");
        testIssues.add(testIssue4);

        TestIssue testIssue5 = new TestIssue();
        testIssue5.setTitle("test5");
        testIssue5.setApprovalState(ApprovalState.DISAPPROVED);
        testIssue5.setPriority(TestPriority.HIGH);
        testIssue5.setDescription("Fifth discription");
        testIssues.add(testIssue5);

        Session session = HibernateUtil.getNewSession();
        try {
            Transaction tx = session.beginTransaction();
            BaseEntityService.Instance.saveOrUpdate(testIssue, session);
            BaseEntityService.Instance.saveOrUpdate(testIssue2, session);
            BaseEntityService.Instance.saveOrUpdate(testIssue3, session);
            BaseEntityService.Instance.saveOrUpdate(testIssue4, session);
            BaseEntityService.Instance.saveOrUpdate(testIssue5, session);
            tx.commit();
        } finally {
            session.close();
        }
        Session newSession = HibernateUtil.getNewSession();
        List<AppPackage> appPackages = (List<AppPackage>) AppPackageService.Instance.listAll(newSession);
        AppPackage appPackage = appPackages.get(0);
        appPackage.setTestIssues(testIssues);
        try {
            Transaction newTx = newSession.beginTransaction();
            BaseEntityService.Instance.saveOrUpdate(appPackage, newSession);
            newTx.commit();
        } finally {
            newSession.close();
        }
        System.exit(0);
    }
}
