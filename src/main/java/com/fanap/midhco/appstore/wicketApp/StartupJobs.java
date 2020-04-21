package com.fanap.midhco.appstore.wicketApp;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.service.AppInstallReportQueue.AppInstallReportQueueService;
import com.fanap.midhco.appstore.service.app.AppService;
import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import com.fanap.midhco.appstore.service.packagePublish.PackagePublishService;
import org.apache.log4j.Logger;
import org.quartz.*;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by admin123 on 8/26/2017.
 */
public class StartupJobs {
    static Scheduler sched;
    static Logger logger = Logger.getLogger(StartupJobs.class);

    public static void init() {
        try {
            SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();

            sched = schedFact.getScheduler();

            sched.start();

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            JobDetail relatedAppCalculatorJob = newJob(AppService.RelatedAppCalculatorJob.class)
                    .withIdentity("relatedAppCalculator", "group1")
                    .build();

            String appRelevencyRateCronScheduler = ConfigUtil.getProperty(ConfigUtil.APP_RELEVENCYRATE_CRON);

            Trigger relatedAppTrigger = newTrigger()
                    .withIdentity("relatedAppCalculator_Trigger", "group1")
                    .startNow()
                    .withSchedule(cronSchedule(appRelevencyRateCronScheduler))
                            .build();

            sched.scheduleJob(relatedAppCalculatorJob, relatedAppTrigger);

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            String installedAppCronScheduler = ConfigUtil.getProperty(ConfigUtil.APP_INSTALLATION_CRON);

            JobDetail installedAppCalculatorJob = newJob(AppInstallReportQueueService.AppInstallationReportJob.class)
                    .withIdentity("installedAppCalculator", "group2")
                    .build();

            Trigger installedAppTrigger = newTrigger()
                    .withIdentity("installedAppCalculator_Trigger", "group2")
                    .startNow()
                    .withSchedule(cronSchedule(installedAppCronScheduler))
                    .build();

            sched.scheduleJob(installedAppCalculatorJob, installedAppTrigger);

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            String packagePublishingCronScheduler = ConfigUtil.getProperty(ConfigUtil.PACKAGE_PUBLISHING_CRON);

            JobDetail publishPackageJob = newJob(PackagePublishService.PackagePublishJob.class)
                    .withIdentity("publishPackage","group3")
                    .build();


            Trigger publishPackageTrigger = newTrigger()
                    .withIdentity("publishPackage_Trigger","group3")
                    .startNow()
                    .withSchedule(cronSchedule(packagePublishingCronScheduler))
                    .build();

            sched.scheduleJob(publishPackageJob , publishPackageTrigger);

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            String deleteTempFolderCronScheduler = ConfigUtil.getProperty(ConfigUtil.DELETE_TEMP_FOLDER_CRON);

            JobDetail deleteTempFolderJob = newJob(FileServerService.DeleteTempFolderJob.class)
                    .withIdentity("deleteTempFolder","group4")
                    .build();


            Trigger deleteTempFolderTrigger = newTrigger()
                    .withIdentity("deleteTempFolder_Trigger","group4")
                    .startNow()
                    .withSchedule(cronSchedule(deleteTempFolderCronScheduler))
                    .build();

            sched.scheduleJob(deleteTempFolderJob , deleteTempFolderTrigger);



        } catch (Exception ex) {
            logger.error("Error starting startup jobs: ", ex);
        }
    }

    public static void shutDown() {
        try {
            if(sched != null)
                sched.shutdown();
        } catch (SchedulerException e) {
            logger.error("Error occured shutting down job scheduler ", e);
        }
    }
}
