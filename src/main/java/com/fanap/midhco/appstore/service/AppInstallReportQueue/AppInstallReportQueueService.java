package com.fanap.midhco.appstore.service.AppInstallReportQueue;

import com.fanap.midhco.appstore.applicationUtils.JsonUtil;
import com.fanap.midhco.appstore.entities.App;
import com.fanap.midhco.appstore.entities.AppInstallReportQueue;
import com.fanap.midhco.appstore.entities.OSType;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.restControllers.vos.AppInstalledVO;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.app.AppElasticService;
import com.fanap.midhco.appstore.service.app.AppService;
import com.fanap.midhco.appstore.service.login.JWTService;
import io.searchbox.client.JestResult;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

/**
 * Created by admin123 on 9/11/2017.
 */
public class AppInstallReportQueueService {
    public static AppInstallReportQueueService Instance = new AppInstallReportQueueService();
    public static Logger logger = Logger.getLogger(AppInstallReportQueueService.class);


    private AppInstallReportQueueService() {
    }

    public void save(AppInstalledVO appInstallVO, OSType osType, JWTService.JWTUserClass jwtUser, Session session) {
        AppInstallReportQueue appInstallReportQueue = new AppInstallReportQueue();
        appInstallReportQueue.setDeleted(false);
        appInstallReportQueue.setDeviceId(appInstallVO.getDeviceId());
        appInstallReportQueue.setOsType(osType);
        appInstallReportQueue.setAppInstallJsonString(JsonUtil.getJson(appInstallVO));
        appInstallReportQueue.setCreationDate(DateTime.now());
        if (jwtUser != null) {
            appInstallReportQueue.setSsoUserId(Long.parseLong(jwtUser.getUserId()));
            appInstallReportQueue.setSsoUserName(jwtUser.getUserName());
        }
        session.saveOrUpdate(appInstallReportQueue);
    }

    public List<AppInstallReportQueue> list(int from, int count, Session session) {
        String queryString = "select appInstallReportQueue from AppInstallReportQueue appInstallReportQueue where appInstallReportQueue.isDeleted = :isDeleted_";
        Query query = session.createQuery(queryString);
        query.setParameter("isDeleted_", false);

        query.setFirstResult(from);
        if (count != -1)
            query.setMaxResults(count);

        return query.list();
    }

    public Long count(Session session) {
        String queryString = "select count(appInstallReportQueue.id) from AppInstallReportQueue appInstallReportQueue where appInstallReportQueue.isDeleted = :isDeleted_";
        Query query = session.createQuery(queryString);
        query.setParameter("isDeleted_", false);

        return (Long) query.uniqueResult();
    }

    public static class AppInstallationReportJob implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            Session session = HibernateUtil.getCurrentSession();
            try {
                Long queryCount = Instance.count(session);

                if (queryCount > 0) {
                    int first = 0;
                    int to = 10;

                    Transaction tx = null;

                    try {
                        while (true) {
                            List<AppInstallReportQueue> reportQueueList =
                                    Instance.list(first, to, session);

                            if (reportQueueList.isEmpty())
                                break;

                            tx = session.beginTransaction();

                            for (AppInstallReportQueue appInstallReportQueue : reportQueueList) {
                                String installationReportContent = appInstallReportQueue.getAppInstallJsonString();
                                String deviceId = appInstallReportQueue.getDeviceId();

                                if (deviceId != null && !deviceId.trim().isEmpty() && installationReportContent != null && !installationReportContent.isEmpty()) {
                                    AppInstalledVO appInstalledVO = JsonUtil.getObject(installationReportContent, AppInstalledVO.class);
                                    OSType osType = appInstallReportQueue.getOsType();

                                    if (appInstalledVO.getPackageList() != null && !appInstalledVO.getPackageList().isEmpty()) {
                                        List<AppElasticService.AppInstallVO> uninstalledPackageList =
                                                AppElasticService.Instance.getNotIncludedPackagesForOneDevice(deviceId, osType, appInstalledVO.getPackageList());

                                        for (String packageName : appInstalledVO.getPackageList()) {
                                            App app = AppService.Instance.getAppByPackageName(osType, packageName, false, session);

                                            if (app != null) {
                                                AppElasticService.AppInstallVO appInstallVO =
                                                        AppElasticService.Instance.buildAppInstallVO(
                                                                app, appInstalledVO.getDeviceId(),
                                                                appInstallReportQueue.getCreationDate().getTime(),
                                                                appInstallReportQueue.getSsoUserName(),
                                                                appInstallReportQueue.getSsoUserId()
                                                        );

                                                appInstallVO.setAction(AppElasticService.InstallAction.INSTALL.toString());
                                                JestResult jestResult = AppElasticService.Instance.insertAppInstallVO(appInstallVO);
                                                if (!jestResult.isSucceeded()) {
                                                    throw new Exception(jestResult.getErrorMessage());
                                                }
                                            }
                                        }

                                        for (AppElasticService.AppInstallVO appInstallVO : uninstalledPackageList) {
                                            appInstallVO.setAction(AppElasticService.InstallAction.UNINSTALL.toString());
                                            JestResult jestResult = AppElasticService.Instance.insertAppInstallVO(appInstallVO);
                                            if (!jestResult.isSucceeded()) {
                                                throw new Exception(jestResult.getErrorMessage());
                                            }
                                        }
                                    }
                                }

                                appInstallReportQueue.setDeleted(true);
                                session.saveOrUpdate(appInstallReportQueue);
                            }

                            tx.commit();
                        }
                    } catch (Exception ex) {
                        if (tx != null)
                            tx.rollback();
                        logger.error("error occured evaluation appInstallationReport ", ex);
                    }
                }
            } catch (Exception ex) {
                logger.error("error occured evaluation appInstallationReport ", ex);
            } finally {
                session.close();
            }
        }
    }

}
