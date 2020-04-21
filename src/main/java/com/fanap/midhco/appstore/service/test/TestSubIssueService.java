package com.fanap.midhco.appstore.service.test;

import com.fanap.midhco.appstore.entities.ApprovalState;
import com.fanap.midhco.appstore.entities.TestIssue;
import com.fanap.midhco.appstore.entities.TestSubIssue;
import com.fanap.midhco.appstore.entities.User;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.service.HQLBuilder;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.hibernate.Query;
import org.hibernate.Session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by A.Moshiri on 6/21/2017.
 */
public class TestSubIssueService {
    public static TestSubIssueService Instance = new TestSubIssueService();

    private TestSubIssueService() {
    }

    public static class TestSubIssueCriteria implements Serializable {
        public Long testIssueId;

        public Long getTestIssueId() {
            return testIssueId;
        }

        public void setTestIssueId(Long testIssueId) {
            this.testIssueId = testIssueId;
        }

    }

    public void applyCriteria(HQLBuilder builder, TestSubIssueCriteria testSubIssueCriteria) {
        if (testSubIssueCriteria.getTestIssueId() != null) {
            builder.addClause("and ent.id in (:id_)", "id_", testSubIssueCriteria.testIssueId);
        }

    }

    public Long count(TestSubIssueCriteria criteria, Session session) {

        int subIssues =0;
        if(criteria!=null && criteria.getTestIssueId()!=null){
            TestIssue testIssue = (TestIssue) session.load(TestIssue.class , criteria.getTestIssueId());
            subIssues = testIssue.getSubIssues().size();
        }
        return Long.valueOf(subIssues);
    }

    public List<TestSubIssueSearchModel> list(TestSubIssueCriteria criteria, int first, int count, String sortProp, boolean isAsc, Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select ent.subIssues ", " from TestIssue ent ");
        if (criteria != null)
            applyCriteria(builder, criteria);

        Query query = builder.createQuery();

        if (sortProp != null)
            builder.addOrder(sortProp, isAsc);

        query.setFirstResult(first);
        if (count != -1)
            query.setMaxResults(count);
        List<Object> resultObjects = query.list();

// use lambda expression
//        Stream<TestSubIssueSearchModel> searchResultModelStream = resultObjects.stream().map(object -> {
//            TestSubIssueSearchModel searchResultModel = new TestSubIssueSearchModel();
//            TestSubIssue testSubIssue = (TestSubIssue) object;
//
//            searchResultModel.id = testSubIssue.getId();
//            searchResultModel.description = testSubIssue.getDescription();
//            searchResultModel.approvalState = testSubIssue.getApprovalState();
//            searchResultModel.testUser = testSubIssue.getTestUser();
//
//            return searchResultModel;
//        });
//        List<TestSubIssueSearchModel> testSubIssueSearchModelList =
//                searchResultModelStream.collect(Collectors.<TestSubIssueSearchModel>toList());

        List<TestSubIssueSearchModel> testSubIssueSearchModelList = new ArrayList<>();
        for(Object object : resultObjects){
            TestSubIssueSearchModel searchResultModel = new TestSubIssueSearchModel();
            TestSubIssue testSubIssue = (TestSubIssue) object;
            searchResultModel.id = testSubIssue.getId();
            searchResultModel.description = testSubIssue.getDescription();
            searchResultModel.approvalState = testSubIssue.getApprovalState();
            searchResultModel.testUser = testSubIssue.getTestUser();

            testSubIssueSearchModelList.add(searchResultModel);

        }

        return testSubIssueSearchModelList;

    }

    public void saveOrUpdate(TestSubIssue testSubIssue, Session session) {
        if (testSubIssue.getId() == null) {
            testSubIssue.setCreationDate(DateTime.now());
            testSubIssue.setCreatorUser(PrincipalUtil.getCurrentUser());
        } else {
            testSubIssue.setLastModifyDate(DateTime.now());
            testSubIssue.setLastModifyUser(PrincipalUtil.getCurrentUser());
        }
        session.saveOrUpdate(testSubIssue);
    }

    public static class TestSubIssueSearchModel implements Serializable {
        Long id;
        public ApprovalState approvalState;
        public String description;
        public User testUser;

        public User getTestUser() {
            return testUser;
        }

        public void setTestUser(User testUser) {
            this.testUser = testUser;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public ApprovalState getApprovalState() {
            return approvalState;
        }

        public void setApprovalState(ApprovalState approvalState) {
            this.approvalState = approvalState;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }


        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestSubIssueSearchModel that = (TestSubIssueSearchModel) o;
            return id.equals(that.id);
        }
    }

}
