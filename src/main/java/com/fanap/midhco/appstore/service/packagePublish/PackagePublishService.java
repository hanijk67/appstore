package com.fanap.midhco.appstore.service.packagePublish;

import com.fanap.midhco.appstore.entities.App;
import com.fanap.midhco.appstore.entities.AppPackage;
import com.fanap.midhco.appstore.entities.PackagePublish;
import com.fanap.midhco.appstore.entities.PublishState;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.service.HQLBuilder;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.app.AppPackageService;
import com.fanap.midhco.appstore.service.app.AppService;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.Serializable;
import java.util.List;

/**
 * Created by A.Moshiri on 10/17/2017.
 */
public class PackagePublishService {
    public static PackagePublishService Instance = new PackagePublishService();
    public static Logger logger = Logger.getLogger(PackagePublishService.class);

    private PackagePublishService() {
    }


    public static class PackagePublishCriteria implements Serializable {
        public Long id;
        public Long appId;
        public Long packId;
        public Boolean isApplied;
        public DateTime[] publishDateTime = new DateTime[2];
    }

    private void applyCriteria(HQLBuilder builder, PackagePublishCriteria criteria) throws Exception {
        if (criteria.id != null)
            builder.addClause("and packagePublish.id = :id_", "id_", criteria.id);
        if (criteria.appId != null)
            builder.addClause("and packagePublish.appId=:appId_", "appId_", criteria.appId);
        if (criteria.packId != null)
            builder.addClause("and packagePublish.packId=:packId_", "packId_", criteria.packId);

        if (criteria.publishDateTime != null) {
            builder.addDateTimeRange("packagePublish", "publishDateTime", "lPublishDateTime", "uPublishDateTime", criteria.publishDateTime);
        }
        if (criteria.isApplied != null) {
            builder.addClause("and packagePublish.isApplied = :isApplied_", "isApplied_", criteria.isApplied);
        }


    }

    public void saveOrUpdate(PackagePublish packagePublish, Session session) throws Exception {

        if (packagePublish.getId() == null) {
            packagePublish.setCreationDate(DateTime.now());
            packagePublish.setCreatorUser(PrincipalUtil.getCurrentUser());
        } else {
            packagePublish.setLastModifyDate(DateTime.now());
            packagePublish.setLastModifyUser(PrincipalUtil.getCurrentUser());
        }
        session.saveOrUpdate(packagePublish);
//        App relatedApp = (App) session.load(App.class , packagePublish.getAppId());
//        List<App> appList = new ArrayList<>();
//        appList.add(relatedApp);
//        AppService.Instance.updateAppToNoSqlWithJob(appList,session);
    }


    public List<PackagePublish> list(PackagePublishCriteria packJobCriteria, int first, int count) throws Exception {
        Session session = HibernateUtil.getNewSession();
        List<PackagePublish> packagePublishJobList = null;
        try {
            packagePublishJobList = list(packJobCriteria, first, count, session);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return packagePublishJobList;
    }

    public List<PackagePublish> list(PackagePublishCriteria criteria, int first, int count, Session session) throws Exception {

        HQLBuilder builder = new HQLBuilder(session, "select packagePublish ", " from PackagePublish packagePublish ");

        if (criteria != null)
            applyCriteria(builder, criteria);

        Query query = builder.createQuery();
        query.setFirstResult(first);
        if (count != -1)
            query.setMaxResults(count);

        List<PackagePublish> resultObjects = query.list();

        return resultObjects;
    }

    public Long count(PackagePublishCriteria criteria, Session session) throws Exception {
        HQLBuilder builder = new HQLBuilder(session, "select count(packagePublish.id) ", " from PackagePublish packagePublish ");

        if (criteria != null)
            applyCriteria(builder, criteria);

        Query query = builder.createQuery();
        Long result = (Long) query.uniqueResult();
        return result;

    }

    public Boolean isExisted(PackagePublishCriteria packagePublishCriteria, Session session) throws Exception {
        HQLBuilder builder = new HQLBuilder(session, "select packagePublish ", " from PackagePublish packagePublish ");

        if (packagePublishCriteria != null) {
            applyCriteria(builder, packagePublishCriteria);
        }
        Query query = builder.createQuery();
        query.setFirstResult(0).setMaxResults(-1);
        List<PackagePublish> result = query.list();
        if (result != null && !result.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public static class PackagePublishJob implements Job {
        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            Session session = HibernateUtil.getCurrentSession();
            DateTime currentDateTime = DateTime.now();
            try {
                PackagePublishCriteria packagePublishCriteria = new PackagePublishCriteria();
                packagePublishCriteria.isApplied = false;
                packagePublishCriteria.publishDateTime[0] = DateTime.MIN_DATE_TIME;
                packagePublishCriteria.publishDateTime[1] = currentDateTime;
                List<PackagePublish> packagePublishList = Instance.list(packagePublishCriteria, 0, -1, session);
                Transaction tx = null;
                try {
                    for (PackagePublish packagePublishInList : packagePublishList) {
                        tx = session.beginTransaction();
                        App app = (App) session.load(App.class, packagePublishInList.getAppId());
                        AppPackage mainPackage = (AppPackage) session.load(AppPackage.class, packagePublishInList.getPackId());

                        app.setHasScheduler(false);
                        mainPackage.setPublishState(PublishState.PUBLISHED);
                        mainPackage.setLastModifyUser(packagePublishInList.getLastModifyUser() != null ? packagePublishInList.getLastModifyUser() : packagePublishInList.getCreatorUser());
                        App changedApp = AppPackageService.Instance.publishAppPackage(app, mainPackage, session);
                        PackagePublish loadedPackagePublish = (PackagePublish) session.load(PackagePublish.class , packagePublishInList.getId());
                        loadedPackagePublish.setApplied(true);
                        Instance.saveOrUpdate(loadedPackagePublish, session);
                        AppPackageService.Instance.saveOrUpdate(mainPackage,session);
                        if (!changedApp.getMainPackage().equals(mainPackage)) {
                            AppService.Instance.saveOrUpdate(changedApp, session);
                        }
                        tx.commit();
                    }
                } catch (Exception e) {
                    if (tx != null) {
                        tx.rollback();
                    }
                    logger.error("in PublishPackage Job == > " + e.getMessage());
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            }
        }
    }

    public static void main(String[] args) {
        Session session = HibernateUtil.getCurrentSession();
        DateTime currentDateTime = DateTime.now();
        try {
            PackagePublishCriteria packagePublishCriteria = new PackagePublishCriteria();
            packagePublishCriteria.isApplied = false;
            packagePublishCriteria.publishDateTime[0] = DateTime.MIN_DATE_TIME;
            packagePublishCriteria.publishDateTime[1] = currentDateTime;
            List<PackagePublish> packagePublishList = Instance.list(packagePublishCriteria, 0, -1, session);
            Transaction tx = null;
            try {
                for (PackagePublish packagePublishInList : packagePublishList) {
                    tx = session.beginTransaction();
                    App app = (App) session.load(App.class, packagePublishInList.getAppId());
                    AppPackage mainPackage = (AppPackage) session.load(AppPackage.class, packagePublishInList.getPackId());

                    app.setHasScheduler(false);
                    mainPackage.setPublishState(PublishState.PUBLISHED);
                    mainPackage.setCreatorUser(packagePublishInList.getCreatorUser());
                    if (packagePublishInList.getLastModifyUser() != null) {
                        mainPackage.setLastModifyUser(packagePublishInList.getLastModifyUser());
                    } else {
                        mainPackage.setLastModifyUser(packagePublishInList.getCreatorUser());
                    }
                    App changedApp = AppPackageService.Instance.publishAppPackage(app, mainPackage, session);
                    PackagePublish loadedPackagePublish = (PackagePublish) session.load(PackagePublish.class , packagePublishInList.getId());
                    loadedPackagePublish.setApplied(true);

                    Instance.saveOrUpdate(loadedPackagePublish, session);
                    AppPackageService.Instance.saveOrUpdate(mainPackage,session);
                        if (!changedApp.getMainPackage().equals(mainPackage)) {

                            AppService.Instance.saveOrUpdate(changedApp, session);
                        }
                        tx.commit();
                    }
                } catch (Exception e) {
                    if (tx != null) {
                        tx.rollback();
                    }
                    logger.error("in PublishPackage Job == > " + e.getMessage());
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            }
        }
    }







