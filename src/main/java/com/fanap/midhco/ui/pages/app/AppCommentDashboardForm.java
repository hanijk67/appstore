package com.fanap.midhco.ui.pages.app;

import com.fanap.midhco.appstore.applicationUtils.DateUtil;
import com.fanap.midhco.appstore.applicationUtils.JsonUtil;
import com.fanap.midhco.appstore.applicationUtils.MyCalendarUtil;
import com.fanap.midhco.appstore.entities.App;
import com.fanap.midhco.appstore.entities.AppPackage;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.entities.helperClasses.DayDate;
import com.fanap.midhco.appstore.entities.helperClasses.DayTime;
import com.fanap.midhco.appstore.restControllers.vos.componentVos.*;
import com.fanap.midhco.appstore.service.app.AppElasticService;
import com.fanap.midhco.appstore.service.comment.CommentService;
import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import com.fanap.midhco.appstore.service.myException.AppStoreRuntimeException;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.circleBadge.CircleBadge;
import com.fanap.midhco.ui.component.drillDown.DrillDown;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.ibm.icu.text.DecimalFormat;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.devocative.adroit.CalendarUtil;
import org.devocative.adroit.vo.DateFieldVO;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Created by M.Sajadi on 9/16/2017.
 */
public class AppCommentDashboardForm extends BasePanel{
    BootStrapModal modal = new BootStrapModal("modal");

    Form form;
    FeedbackPanel feedbackPanel;
    AjaxFormButton moreComments;
    Label favoriteCountInCurrentDateLbl;
    Label todayCommentsLbl;
    Label yearlyRateLbl;
    String elasticCommentVoJSON = null;
    DrillDown favoriteDrillDown;
    WebMarkupContainer favoriteReportDiv = null;

    DrillDown rateDrillDownInTab;
    DrillDown favoriteDrillDownInTab;
    private static ResourceReference App_Comment_DashboardSCRIPT =
            new JavaScriptResourceReference(AppCommentDashboardForm.class, "res/appCommentDashboard.js");

    protected AppCommentDashboardForm(String id, App app) {
        super(id);
        add(modal);

        feedbackPanel = new FeedbackPanel("feedbackPanel");
        feedbackPanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(feedbackPanel);

        form = new Form("form");
        form.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);


        try {
            moreComments = new AjaxFormButton("groupComments", form) {
                @Override
                protected void onSubmit(Form form, AjaxRequestTarget target) {
                    try {
                        AppGroupCommentImportForm appGroupCommentImportForm = new AppGroupCommentImportForm(modal.getContentId());
                        appGroupCommentImportForm.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
                        modal.setContent(appGroupCommentImportForm);
                        modal.setParent(AppCommentDashboardForm.this);
                        modal.setTitle(getString("app.comment.excel.file"));
                        target.appendJavaScript("$('.modal-body').css('overflow-x', 'hidden')");
                        modal.show(target);
                    } catch (Exception ex) {
                        throw new AppStoreRuntimeException(ex);
                    }
                }
            };
            moreComments.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
            form.add(moreComments);

            moreComments = new AjaxFormButton("moreComments", form) {
                @Override
                protected void onSubmit(Form form, AjaxRequestTarget target) {
                    CommentService.CommentVOCriteria commentVOCriteria = new CommentService.CommentVOCriteria();
                    try {
                        if (app != null) {
                            commentVOCriteria.appId = app.getId();
                            commentVOCriteria.approved = false;
                            AppCommentForm appCommentForm = new AppCommentForm(modal.getContentId(), commentVOCriteria, SelectionMode.Multiple);
                            appCommentForm.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
                            modal.setContent(appCommentForm);
                            modal.setParent(AppCommentDashboardForm.this);
                            modal.setTitle(getString("app.comment.info"));
                            target.appendJavaScript("$('.modal-body').css('overflow-x', 'hidden')");
                        }
                        modal.show(target);
                    } catch (Exception ex) {
                        throw new AppStoreRuntimeException(ex);
                    }
                }
            };
            moreComments.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
            form.add(moreComments);


            CommentService.CommentVOCriteria commentVOCriteria = new CommentService.CommentVOCriteria();
            commentVOCriteria.appId = app.getId();

            double averageRate = CommentService.Instance.getAppRateAverage(commentVOCriteria, 0, -1, null, false);

            Date currentDate = new Date();
            DateFieldVO firstDateFieldVO = CalendarUtil.toPersianDateField(currentDate).setDay(1);

            int monthField = firstDateFieldVO.getMonth();
            DateFieldVO lastDateFieldVO = CalendarUtil.toPersianDateField(currentDate).setDay(30);
            if(monthField <=6)
                lastDateFieldVO.setDay(31);

            DateTime firstPersianDateTime = new DateTime(new DayDate(firstDateFieldVO.getYear(), firstDateFieldVO.getMonth(), firstDateFieldVO.getDay()), DayTime.MIN_DAY_TIME);
            DateTime secondPersianDateTime = new DateTime(new DayDate(lastDateFieldVO.getYear(), lastDateFieldVO.getMonth(), lastDateFieldVO.getDay()), DayTime.MAX_DAY_TIME);

            commentVOCriteria.modifyTimeForm = MyCalendarUtil.toGregorian(firstPersianDateTime).toDate().getTime();
            commentVOCriteria.modifyTimeTo = MyCalendarUtil.toGregorian(secondPersianDateTime).toDate().getTime();

            DecimalFormat df = new DecimalFormat("##.##");
            if(averageRate < 0)
                averageRate = 0;
            CircleBadge averageRateCircleBadge = new CircleBadge(
                    "averageRateCircleBadge"
                    , new ResourceModel("app.average.rating").getObject()
                    , df.format(averageRate));
            form.add(averageRateCircleBadge);


            AppPackage appMainPackage = app.getMainPackage();
            String fileKey = appMainPackage.getPackFile().getFilePath();

            Integer fileDownloadCount = FileServerService.Instance.getFileDownloadCount(fileKey);
            CircleBadge downloadCountCircleBadge = new CircleBadge(
                    "downloadCountCircleBadge",
                    new ResourceModel("app.download.count").getObject(),
                    String.valueOf(fileDownloadCount)
            );
            form.add(downloadCountCircleBadge);


            // like and DisLike
            Model<String> favoriteCountMdl = Model.of(AppStorePropertyReader.getString("favoriteCount.null"));

            try {
                AppElasticService.FavoriteAppCriteria favoriteAppCriteria = new AppElasticService.FavoriteAppCriteria();
                favoriteAppCriteria.setFromDate(DateUtil.getBaseTimeForInputDate(DateTime.now()));
                favoriteAppCriteria.setToDate(DateUtil.getBaseTimeForInputDate(DateTime.afterNow(1)));
                List<AppElasticService.FavoriteAppVO> favoriteAppVOS = AppElasticService.Instance.getFavoriteApp(favoriteAppCriteria, 0, -1, "userId", false);
                if (favoriteAppVOS != null && !favoriteAppVOS.isEmpty()) {
                    String favoriteCountStr = AppStorePropertyReader.getString("favoriteCountInToDay").replace("${count}", String.valueOf(favoriteAppVOS.size()));
                    favoriteCountMdl.setObject(favoriteCountStr);
                }
            } catch (Exception e) {
//                throw new AppStoreRuntimeException(e);
            }

            favoriteCountInCurrentDateLbl = new Label("favoriteCountInCurrentDate", favoriteCountMdl);
            favoriteCountInCurrentDateLbl.setOutputMarkupId(true);
            form.add(favoriteCountInCurrentDateLbl);


            //appInstallation Count

            try {
                AppElasticService.AppInstallCriteria appInstallCriteria = new AppElasticService.AppInstallCriteria();
                appInstallCriteria.installAction = AppElasticService.InstallAction.INSTALL.toString();
                appInstallCriteria.appId = app.getId();
                Integer installationCount = AppElasticService.Instance.getInstallationCountForApp(appInstallCriteria).intValue();

                CircleBadge installationCountCircleBadge = new CircleBadge(
                        "installationCountCircleBadge",
                        new ResourceModel("app.installation.count").getObject(),
                        String.valueOf(installationCount)
                );
                form.add(installationCountCircleBadge);

            } catch (Exception e) {
//                throw new AppStoreRuntimeException(e);
            }

            // comments

            // today's comments
            Model<String> todayCommentsMdl = Model.of(AppStorePropertyReader.getString("commentCount.null"));
            CommentService.CommentVOCriteria todayCommentVOCriteria = null;
            if (app != null) {
                todayCommentVOCriteria = new CommentService.CommentVOCriteria();
                todayCommentVOCriteria.appId = app.getId();
                DateTime currentDateTime = DateTime.now();
                todayCommentVOCriteria.modifyTimeForm = DateUtil.getBaseTimeForInputDate(currentDateTime);
                todayCommentVOCriteria.modifyTimeTo = DateUtil.getBaseTimeForInputDate(DateTime.afterFrom(currentDateTime, 1));
                List<CommentService.ElasticCommentVO> elasticCommentVOs = CommentService.Instance.getCommentsForApp(todayCommentVOCriteria, 0, -1, "lastModifyDate", false);
                if (elasticCommentVOs != null && !elasticCommentVOs.isEmpty()) {
                    todayCommentsMdl.setObject(String.valueOf(elasticCommentVOs.size()));
                }


                todayCommentsLbl = new Label("todayComments", todayCommentsMdl);
                todayCommentsLbl.setOutputMarkupId(true);
                form.add(todayCommentsLbl);

            }

            // number of new comments
            CommentService.CommentVOCriteria newCommentVOCriteria = null;
            if (app != null) {
                newCommentVOCriteria = new CommentService.CommentVOCriteria();
                newCommentVOCriteria.appId = app.getId();
                newCommentVOCriteria.approved = true;
                List<CommentService.ElasticCommentVO> elasticCommentVOs = CommentService.Instance.getCommentsForApp(newCommentVOCriteria, 0, 3, "lastModifyDate", false);
                elasticCommentVoJSON = JsonUtil.getJson(elasticCommentVOs);
            }


            //rate Average
            try {

                //yearly Rate
                Model<String> yearlyRateMdl = Model.of("....");

                DateTime currentDateTime = DateTime.now();

                Long currentDateTimeLong = currentDateTime.getTime();
                DateTime lastYearDateTime = DateTime.beforeNow(365);
                Long lastYearDateTimeLong = lastYearDateTime.getTime();

                CommentService.CommentVOCriteria commentVOCriteriaForYearRate = new CommentService.CommentVOCriteria();
                commentVOCriteriaForYearRate.modifyTimeForm = lastYearDateTimeLong;
                commentVOCriteriaForYearRate.modifyTimeTo = currentDateTimeLong;
                commentVOCriteriaForYearRate.appId = app.getId();
                double yearAverageRate = CommentService.Instance.getAppRateAverage(commentVOCriteriaForYearRate, 0, -1, null, false);
                Double roundedYearAverageRate = new BigDecimal(yearAverageRate).setScale(2, RoundingMode.HALF_UP).doubleValue();
                roundedYearAverageRate = roundedYearAverageRate < 0 ? 0 : roundedYearAverageRate;
                yearlyRateMdl.setObject(String.valueOf(roundedYearAverageRate));
                yearlyRateLbl = new Label("yearlyRateLbl", yearlyRateMdl);
                yearlyRateLbl.setOutputMarkupId(true);
//                form.add(yearlyRateLbl);


                // todo get calendar type from user Request
                CalendarType currentCalendarType = CalendarType.PERSIAN;
//                CalendarType currentCalendarType = CalendarType.GREGORIAN;

                //favorite drill down chart
                WebMarkupContainer favoriteMasterContainer = new WebMarkupContainer("favoriteDrillDownChartMasterContainer", new Model<>());
                favoriteMasterContainer.setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true);


                String favoriteDrillDownChartDataSourceStrSevenDays = getDataSourceFor(app.getId(), currentCalendarType, DateHistogramInterval.WEEK);
                String favoriteDrillDownChartDataSourceStrLastMonth = getDataSourceFor(app.getId(), currentCalendarType, DateHistogramInterval.MONTH);


                String favoriteDrillDownChartDataSourceStrLastYear = getDataSourceFor(app.getId(), currentCalendarType, DateHistogramInterval.YEAR);
                favoriteDrillDown = new DrillDown("favoriteDrillDownChartContent", favoriteDrillDownChartDataSourceStrSevenDays, Integer.valueOf(2));

                favoriteDrillDown.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

                favoriteDrillDown.setModel(new Model());

                favoriteReportDiv = new WebMarkupContainer("favoriteReportDiv");

                AjaxFormButton lastSevenDaysBtn = new AjaxFormButton("lastSevenDaysBtn", form) {
                    @Override
                    protected void onSubmit(Form form, AjaxRequestTarget target) {
                        favoriteDrillDownInTab = new DrillDown("favoriteReportPanel", favoriteDrillDownChartDataSourceStrSevenDays, Integer.valueOf(2));
                        favoriteDrillDownInTab.setModel(new Model());

                        favoriteDrillDownInTab.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
                        favoriteReportDiv.get("favoriteReportPanel").replaceWith(favoriteDrillDownInTab);
                        favoriteReportDiv.setVisible(true);
                        favoriteReportDiv.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

                        target.add(favoriteReportDiv);

                    }
                };

                lastSevenDaysBtn.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

                form.add(lastSevenDaysBtn);
                AjaxFormButton lastMonthBtn = new AjaxFormButton("lastMonthBtn", form) {
                    @Override
                    protected void onSubmit(Form form, AjaxRequestTarget target) {

                        favoriteDrillDownInTab = new DrillDown("favoriteReportPanel", favoriteDrillDownChartDataSourceStrLastMonth, Integer.valueOf(2));
                        favoriteDrillDownInTab.setModel(new Model());

                        favoriteDrillDownInTab.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
                        favoriteReportDiv.get("favoriteReportPanel").replaceWith(favoriteDrillDownInTab);
                        favoriteReportDiv.setVisible(true);
                        favoriteReportDiv.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

                        target.add(favoriteReportDiv);

                    }
                };

                lastMonthBtn.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

                form.add(lastMonthBtn);

                AjaxFormButton lastYearBtn = new AjaxFormButton("lastYearBtn", form) {
                    @Override
                    protected void onSubmit(Form form, AjaxRequestTarget target) {

                        favoriteDrillDownInTab = new DrillDown("favoriteReportPanel", favoriteDrillDownChartDataSourceStrLastYear, Integer.valueOf(2));
                        favoriteDrillDownInTab.setModel(new Model());

                        favoriteDrillDownInTab.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
                        favoriteReportDiv.get("favoriteReportPanel").replaceWith(favoriteDrillDownInTab);
                        favoriteReportDiv.setVisible(true);
                        favoriteReportDiv.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

                        target.add(favoriteReportDiv);
                    }
                };
                lastYearBtn.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

                form.add(lastYearBtn);

                form.add(favoriteMasterContainer);


                WebMarkupContainer rateReport = new WebMarkupContainer("rateReportLbl");
                form.add(rateReport);
                WebMarkupContainer favoriteReport = new WebMarkupContainer("favoriteReportLbl");
                form.add(favoriteReport);

                WebMarkupContainer rateReportDiv = new WebMarkupContainer("rateReportDiv");

// rate Drill down
                String rateDrillDownChartDataSourceStr = createRateDrillDownDataSource(app, currentCalendarType);

                rateDrillDownInTab = new DrillDown("rateReportPanel", rateDrillDownChartDataSourceStr, Integer.valueOf(1));
                rateDrillDownInTab.setModel(new Model());
                rateDrillDownInTab.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
                rateReportDiv.add(rateDrillDownInTab);
                form.add(rateReportDiv);

                favoriteDrillDownInTab = new DrillDown("favoriteReportPanel", favoriteDrillDownChartDataSourceStrSevenDays, Integer.valueOf(2));
                favoriteDrillDownInTab.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
                favoriteDrillDownInTab.setModel(new Model());
                favoriteReportDiv.add(favoriteDrillDownInTab);
                favoriteReportDiv.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

                form.add(favoriteReportDiv);

        } catch (Exception ex) {
//                throw new AppStoreRuntimeException(ex);
        }

        } catch (Exception ex) {
//            throw new AppStoreRuntimeException(ex);
        }

        form.add(new AjaxLink("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                modal.close(target);
            }
        });

        add(form);
    }

    private String getDataSourceFor(Long appId, CalendarType calendarType, DateHistogramInterval dateHistogramInterval) {
        String dataSource = null;
        DrillDownChartDataSourceVo drillDownChartDataSourceVo = new DrillDownChartDataSourceVo();
        try {
            if (dateHistogramInterval != null && appId != null && calendarType != null) {

                if (dateHistogramInterval.equals(DateHistogramInterval.WEEK)) {
                    DateTime beginDateTimeForCalculation = DateTime.beforeNow(6);

                    if (calendarType != null) {
                        StringBuffer title = new StringBuffer();
                        title.append("در هفته گذشته");
                        Chart firstChart = getFirstChartVo(title.toString(), "گزارش میزان لایک ثبت شده");
                        AppElasticService.FavoriteAppCriteria favoriteAppCriteria = new AppElasticService.FavoriteAppCriteria();
                        favoriteAppCriteria.setFromDate(DateUtil.getBaseTimeForInputDate(beginDateTimeForCalculation));
                        favoriteAppCriteria.setToDate(DateUtil.getBaseTimeForInputDate(DateTime.afterNow(1)));
                        favoriteAppCriteria.setAppId(String.valueOf(appId));

                        List<CommentService.HistogramBucketVO> favoriteHistogram = AppElasticService.Instance.getFavoriteHistogram(favoriteAppCriteria, DateHistogramInterval.DAY);
                        List<Data> firstChartDataList = getFirstChartDataVos(favoriteHistogram, calendarType, dateHistogramInterval);
                        drillDownChartDataSourceVo.setChart(firstChart);
                        drillDownChartDataSourceVo.setData(firstChartDataList);
                        drillDownChartDataSourceVo.setLinkedData(null);
                    }

                } else if (dateHistogramInterval.equals(DateHistogramInterval.MONTH)) {
                    DateTime beginDateTimeForCalculation = DateTime.beforeNow(30);

                    if (calendarType != null) {
                        StringBuffer title = new StringBuffer();
                        title.append("در ماه گذشته");
                        Chart firstChart = getFirstChartVo(title.toString(), "گزارش میزان لایک ثبت شده");
                        AppElasticService.FavoriteAppCriteria favoriteAppCriteria = new AppElasticService.FavoriteAppCriteria();
                        favoriteAppCriteria.setFromDate(DateUtil.getBaseTimeForInputDate(beginDateTimeForCalculation));
                        favoriteAppCriteria.setToDate(DateUtil.getBaseTimeForInputDate(DateTime.afterNow(1)));
                        favoriteAppCriteria.setAppId(String.valueOf(appId));
                        List<CommentService.HistogramBucketVO> favoriteHistogram = AppElasticService.Instance.getFavoriteHistogram(favoriteAppCriteria, DateHistogramInterval.DAY);
                        List<Data> firstChartDataList = getFirstChartDataVos(favoriteHistogram, calendarType, dateHistogramInterval);
                        drillDownChartDataSourceVo.setChart(firstChart);
                        drillDownChartDataSourceVo.setData(firstChartDataList);
                        drillDownChartDataSourceVo.setLinkedData(null);
                    }

                } else if (dateHistogramInterval.equals(DateHistogramInterval.YEAR)) {

                    DateTime previousYearDateTime = DateTime.beforeNow(365);
                    DateTime convertedPreviousYearDateTime = null;

                    if (calendarType != null) {
                        if (calendarType.equals(CalendarType.PERSIAN)) {
                            convertedPreviousYearDateTime = MyCalendarUtil.toPersian(previousYearDateTime);
                        } else {
                            convertedPreviousYearDateTime = previousYearDateTime;
                        }
                    }
                    int convertedYearInt = MyCalendarUtil.getYearIndexFromDateTime(convertedPreviousYearDateTime);
                    int convertedMonthIndex = MyCalendarUtil.getMonthIndexFromDateTime(convertedPreviousYearDateTime);

                    StringBuffer title = new StringBuffer();
                    title.append(convertedYearInt);
                    if (convertedMonthIndex != 12) {
                        title.append(" - ").append(++convertedYearInt);
                    }
                    Chart firstChart = getFirstChartVo(title.toString(), "گزارش میزان لایک ثبت شده");
                    ArrayList<Double> monthRates = getMonthFavoriteFromLastYearTillNow(appId, calendarType);
                    List<Data> firstChartDataList = getFirstChartDataVos(convertedMonthIndex, appId, monthRates, calendarType);

                    drillDownChartDataSourceVo.setChart(firstChart);
                    drillDownChartDataSourceVo.setData(firstChartDataList);

                }
            }

        } catch (Exception ex) {
            throw new AppStoreRuntimeException(ex);
        }

        dataSource = JsonUtil.getJson(drillDownChartDataSourceVo);
        return dataSource;
    }

    private ArrayList<Double> getMonthFavoriteFromLastYearTillNow(Long appId, CalendarType calendarType) throws Exception {
        ArrayList<Double> monthFavorites = new ArrayList<>();

        ArrayList<Long> monthlyDateTimeLong = getMonthStartTime(calendarType);

        AppElasticService.FavoriteAppCriteria favoriteAppCriteria = new AppElasticService.FavoriteAppCriteria();
        favoriteAppCriteria.setAppId(appId.toString());
        if (monthlyDateTimeLong != null) {
            for (int i = 0; i < monthlyDateTimeLong.size() - 1; i++) {
                favoriteAppCriteria.setFromDate(monthlyDateTimeLong.get(i));
                favoriteAppCriteria.setToDate(monthlyDateTimeLong.get(i + 1));
                List<AppElasticService.FavoriteAppVO> favoriteAppVOList = AppElasticService.Instance.getFavoriteApp(favoriteAppCriteria, 0, -1, null, false);

                monthFavorites.add(Double.valueOf(favoriteAppVOList != null ? favoriteAppVOList.size() : 0));
            }
        }
        return monthFavorites;
    }

    private List<Data> getFirstChartDataVos(List<CommentService.HistogramBucketVO> inputHistogram, CalendarType calendarType, DateHistogramInterval dateHistogramInterval) {

        List<Data> dateList = new ArrayList<>();
        if (dateHistogramInterval != null) {
            int size = 0;
            if (dateHistogramInterval.equals(DateHistogramInterval.WEEK)) {
                size = 6;
            } else if (dateHistogramInterval.equals(DateHistogramInterval.MONTH)) {
                size = 29;
            }
            Double[] selectedDurationFavorite = getFavoriteInSelectedDuration(inputHistogram, calendarType, dateHistogramInterval, size + 1);

            for (int i = 1; i < size + 1; i++) {
                DateTime convertedDateTime = getDateTimeByCalendarType(calendarType, DateTime.beforeNow(i));
                Data data = new Data();
                double value = (selectedDurationFavorite[i - 1] == null || selectedDurationFavorite[i - 1] < 0) ? 0 : selectedDurationFavorite[i - 1];
                int monthIndex = MyCalendarUtil.getMonthIndexFromDateTime(convertedDateTime);
                int dateIndex = MyCalendarUtil.getDateIndexFromDateTime(convertedDateTime);
                StringBuffer label = new StringBuffer();
                label.append(monthIndex).append("/").append(dateIndex);

                data.setLabel(label.toString());
                data.setValue(String.valueOf(value));
                dateList.add(data);
            }

            //for current date
            Data data = new Data();
            DateTime currentDateTime = getDateTimeByCalendarType(calendarType, DateTime.now());

            int monthIndex = MyCalendarUtil.getMonthIndexFromDateTime(currentDateTime);
            int dateIndex = MyCalendarUtil.getDateIndexFromDateTime(currentDateTime);
            StringBuffer label = new StringBuffer();
            label.append(monthIndex).append("/").append(dateIndex);

            data.setLabel(label.toString());
            double value = (selectedDurationFavorite[size] == null || selectedDurationFavorite[size] < 0) ? 0 : selectedDurationFavorite[size];
            data.setValue(String.valueOf(value));
            Collections.reverse(dateList);
            dateList.add(data);
        }
        return dateList;
    }

    private Double[] getFavoriteInSelectedDuration(List<CommentService.HistogramBucketVO> favoriteHistogramBucketVOS, CalendarType calendarType, DateHistogramInterval dateHistogramInterval, int size) {

        Double[] weeklyFavorite = new Double[size];
        DateTime currentDateTime = DateTime.now();
        Arrays.fill(weeklyFavorite, 0.0);
        for (CommentService.HistogramBucketVO favoriteHistogram : favoriteHistogramBucketVOS) {
            DateTime histogramDateTime = favoriteHistogram.time;

            DateTime convertedDateTime = null;
//            int calculatedDateIndex = 0;
//            if (calendarType != null) {
//                convertedDateTime = getDateTimeByCalendarType(calendarType, histogramDateTime);
//                calculatedDateIndex = MyCalendarUtil.getDateIndexFromDateTime(convertedDateTime);
//            }
//
            int dif = (int) ((currentDateTime.getTime() - histogramDateTime.getTime()) / (DateTime.ONE_DAY_MILLIS));
            weeklyFavorite[dif - 1] += Double.valueOf(favoriteHistogram.count);
        }
        return weeklyFavorite;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(App_Comment_DashboardSCRIPT));
        response.render(OnDomReadyHeaderItem.forScript("makeReady('" + elasticCommentVoJSON + "');"));
    }


    private String createRateDrillDownDataSource(App app, CalendarType calendarType) {
        if (app == null || app.getId() == null) {
            return null;
        } else {
            DrillDownChartDataSourceVo drillDownChartDataSourceVo = new DrillDownChartDataSourceVo();
            try {
                DateTime previousYearDateTime = DateTime.beforeNow(365);
                DateTime convertedPreviousYearDateTime = null;
                DateTime beginDateTimeForCalculation = null;

                int previousYearMonthIndex = MyCalendarUtil.getMonthIndexFromDateTime(previousYearDateTime);
                int previousYearDateIndex = MyCalendarUtil.getDateIndexFromDateTime(previousYearDateTime);
                beginDateTimeForCalculation = DateTime.afterFrom(DateTime.beforeNow(365), (MyCalendarUtil.gregDaysInMonth[previousYearMonthIndex - 1] - previousYearDateIndex) + 1);

                if (calendarType != null) {
                    if (calendarType.equals(CalendarType.PERSIAN)) {
                        convertedPreviousYearDateTime = MyCalendarUtil.toPersian(previousYearDateTime);
                    } else {
                        convertedPreviousYearDateTime = previousYearDateTime;
                    }
                }
                int convertedYearInt = MyCalendarUtil.getYearIndexFromDateTime(convertedPreviousYearDateTime);
                int convertedMonthIndex = MyCalendarUtil.getMonthIndexFromDateTime(convertedPreviousYearDateTime);

                StringBuffer title = new StringBuffer();
                title.append(convertedYearInt);
                if (convertedMonthIndex != 12) {
                    title.append(" - ").append(++convertedYearInt);
                }
                Chart firstChart = getFirstChartVo(title.toString(), "گزارش آماری میزان محبوبیت برنامه در سال");
                ArrayList<Double> monthRates = getMonthRateFromLastYearTillNow(app.getId(), calendarType);
                List<Data> firstChartDataList = getFirstChartDataVos(convertedMonthIndex, app.getId(), monthRates, calendarType);

                drillDownChartDataSourceVo.setChart(firstChart);
                drillDownChartDataSourceVo.setData(firstChartDataList);

                CommentService.CommentVOCriteria commentVOCriteria = new CommentService.CommentVOCriteria();
                commentVOCriteria.appId = app.getId();

                commentVOCriteria.modifyTimeTo = DateTime.now().getTime();

                commentVOCriteria.modifyTimeForm = beginDateTimeForCalculation.getTime();

                List<CommentService.HistogramBucketVO> rateHistogramList = CommentService.Instance.getRateHistogram(commentVOCriteria, DateHistogramInterval.DAY);

                Double[][] rate = getMontRate(calendarType, rateHistogramList);

                List<LinkedData> linkedDataList = getLinkedDataVo(firstChartDataList, convertedMonthIndex, calendarType, rate);
                drillDownChartDataSourceVo.setLinkedData(linkedDataList);

            } catch (Exception ex) {
                throw new AppStoreRuntimeException(ex);
            }

            String drillDownChartDataSourceStr = JsonUtil.getJson(drillDownChartDataSourceVo);
            return drillDownChartDataSourceStr;
        }
    }

    /**
     * get a matrix of rate in whole year.
     * for month we use original month index that obtain from inserted date
     * for days we calculated to selected calendar type and then
     * we put avgRate for  matrix[month][date]
     *
     * @param calendarType      specific calendar type.
     * @param rateHistogramList rateHistogram is a Vo that contain all avgRate for whole year with it's date.
     */
    private Double[][] getMontRate(CalendarType calendarType, List<CommentService.HistogramBucketVO> rateHistogramList) {
        Double[][] monthRate = new Double[12][31];
        for (CommentService.HistogramBucketVO rateHistogram : rateHistogramList) {
            DateTime histogramDateTime = rateHistogram.time;

            DateTime convertedDateTime = null;
            int calculatedDateIndex = 0;
            int calculatedMonthIndex = 0;
            if (calendarType != null) {
                convertedDateTime = getDateTimeByCalendarType(calendarType, histogramDateTime);
                calculatedDateIndex = MyCalendarUtil.getDateIndexFromDateTime(convertedDateTime);
                calculatedMonthIndex = MyCalendarUtil.getMonthIndexFromDateTime(convertedDateTime);
            }
            monthRate[calculatedMonthIndex - 1][calculatedDateIndex - 1] = rateHistogram.avgRate;
        }
        return monthRate;
    }

    private DateTime getDateTimeByCalendarType(CalendarType calendarType, DateTime histogramDateTime) {
        DateTime convertedDateTime;
        if (calendarType.equals(CalendarType.PERSIAN)) {
            convertedDateTime = MyCalendarUtil.toPersian(histogramDateTime);
        } else {
            convertedDateTime = histogramDateTime;
        }
        return convertedDateTime;
    }

    private ArrayList<Double> getMonthRateFromLastYearTillNow(Long appId, CalendarType calendarType) throws Exception {
        ArrayList<Double> monthRates = new ArrayList<>();

        ArrayList<Long> monthlyDateTimeLong = getMonthStartTime(calendarType);

        CommentService.CommentVOCriteria commentVOCriteria = new CommentService.CommentVOCriteria();
        commentVOCriteria.appId = appId;
        if (monthlyDateTimeLong != null) {
            for (int i = 0; i < monthlyDateTimeLong.size() - 1; i++) {
                commentVOCriteria.modifyTimeForm = monthlyDateTimeLong.get(i);
                commentVOCriteria.modifyTimeTo = monthlyDateTimeLong.get(i + 1);
                Double monthRate = CommentService.Instance.getAppRateAverage(commentVOCriteria, 0, -1, null, false);
                if (monthRate < 0) {
                    monthRate = 0d;
                }
                monthRates.add(monthRate);
            }
        }
        return monthRates;
    }

    private ArrayList<Long> getMonthStartTime(CalendarType calendarType) {
        DateTime previousYear = DateTime.beforeNow(365);

        DateTime previousYearDate = null;
        int previousYearInt = MyCalendarUtil.getYearIndexFromDateTime(previousYear);
        int currentMonthIndex = MyCalendarUtil.getMonthIndexFromDateTime(previousYear);
        if (currentMonthIndex != 12) {
            previousYear = new DateTime(previousYearInt, currentMonthIndex + 1, 1);
        } else {
            previousYear = new DateTime(previousYearInt + 1, 1, 1);
        }

        if (calendarType != null) {
            ArrayList<Long> monthlyDateTimeLong = new ArrayList<>();

            previousYearDate = getDateTimeByCalendarType(calendarType, previousYear);

            int previousYearYearInt = MyCalendarUtil.getYearIndexFromDateTime(previousYearDate);
            int previousYearMonthInt = MyCalendarUtil.getMonthIndexFromDateTime(previousYearDate);
            if (calendarType != null && calendarType.equals(CalendarType.PERSIAN)) {
                previousYearMonthInt++;
            }
            int previousYearDayInt = 1;

            if (calendarType != null) {

                for (int i = previousYearMonthInt; i <= 12; i++) {
                    Date lastYearDate = MyCalendarUtil.getDateFromCalendar(MyCalendarUtil.getDateByInputData(previousYearYearInt, i, previousYearDayInt, calendarType));
                    monthlyDateTimeLong.add(lastYearDate.getTime());
                }

                previousYearYearInt++;
                for (int i = 1; i < previousYearMonthInt; i++) {
                    Date lastYearDate = MyCalendarUtil.getDateFromCalendar(MyCalendarUtil.getDateByInputData(previousYearYearInt, i, previousYearDayInt, calendarType));
                    monthlyDateTimeLong.add(lastYearDate.getTime());
                }

            }

            //add current Date Time Long
            Long currentMontStartLongTime = MyCalendarUtil.getDateFromCalendar(MyCalendarUtil.getDateByInputData(previousYearYearInt, previousYearMonthInt, previousYearDayInt, calendarType)).getTime();

            monthlyDateTimeLong.add(new Date().getTime());

            return monthlyDateTimeLong;
        }
        return null;
    }

    private List<LinkedData> getLinkedDataVo(List<Data> firstChartDataList, int inputMonthIndex, CalendarType calendarType, Double[][] rate) {

        List<LinkedData> linkedDataArrayList = new ArrayList<>();
        try {
            int firstChildLength = (firstChartDataList != null && !firstChartDataList.isEmpty()) ? firstChartDataList.size() : 0;
            for (int i = 0; i < firstChildLength; i++) {
                LinkedData linkedData = new LinkedData();
                if (i == 0) {
                    linkedData.setId(String.valueOf(12));
                } else {
                    linkedData.setId(String.valueOf(i));

                }
                LinkedChart linkedChart = new LinkedChart();
                int monthIndex = (inputMonthIndex + i) % 12;
                if (monthIndex == 0) {
                    monthIndex = 12;
                }
                String monthName = MyCalendarUtil.getMonthName(calendarType, monthIndex);
                Chart secondChart = getSecondChartVo(monthName);
                linkedChart.setChart(secondChart);
                int calculateMonthIndex = MyCalendarUtil.getMontIndexByMonthName(calendarType, monthName);
                ArrayList<Data> secondData = getSecondChartDataVos(rate[calculateMonthIndex - 1]);
                linkedChart.setData(secondData);
                linkedData.setLinkedChart(linkedChart);
                linkedDataArrayList.add(linkedData);
            }
        } catch (Exception ex) {
            throw new AppStoreRuntimeException(ex);
        }
        return linkedDataArrayList;

    }

    private ArrayList<Data> getSecondChartDataVos(Double[] doubles) {
        ArrayList<Data> dataArrayList = new ArrayList<>();
        if (doubles != null && doubles.length != 0) {
            for (int i = 0; i < doubles.length; i++) {
                Double db = doubles[i];
                if (db != null) {
                    Data data = new Data();
                    data.setValue(String.valueOf(db));
                    data.setLabel(String.valueOf(i + 1));
                    dataArrayList.add(data);
                }
            }
        }
        return dataArrayList;
    }

    private Chart getFirstChartVo(String title, String caption) {
        Chart firstChart = new Chart();
        firstChart.setCaption(caption);
        firstChart.setSubCaption(title);
        firstChart.setxAxisName("ماه");
        firstChart.setyAxisName("تعداد");
        firstChart.setNumberPrefix("");
        firstChart.setPaletteColors("#0075c2");
        firstChart.setBgColor("#ffffff");
        firstChart.setBorderAlpha("30");
        firstChart.setCanvasBorderAlpha("0");
        firstChart.setUsePlotGradientColor("0");
        firstChart.setPlotBorderAlpha("10");
        firstChart.setPlacevaluesInside("1");
        firstChart.setRotatevalues("1");
        firstChart.setValueFontColor("#ffffff");
        firstChart.setShowXAxisLine("2");
        firstChart.setxAxisLineColor("#999999");
        firstChart.setDivlineColor("#999999");
        firstChart.setDivLineIsDashed("1");
        firstChart.setShowAlternateHGridColor("0");
        firstChart.setSubcaptionFontBold("0");
        firstChart.setSubcaptionFontSize("48");
        return firstChart;
    }

    private List<Data> getFirstChartDataVos(int currentMonthIndex, Long appId, ArrayList<Double> monthRates, CalendarType calendarType) {

        List<Data> dataArrayList = new ArrayList<>();

        CommentService.CommentVOCriteria commentVOCriteria = new CommentService.CommentVOCriteria();
        commentVOCriteria.appId = appId;
        for (int i = 0; i < 12; i++) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("newchart-json-");
            stringBuffer.append(i + 1);
            Data data = new Data();
            int mountId = (currentMonthIndex + i + 1) % 12;
            if (mountId == 0) {
                mountId = 12;
            }

            if (calendarType != null) {
                if (calendarType.equals(CalendarType.PERSIAN)) {
                    data.setLabel(MyCalendarUtil.getPersianMonthName(mountId));
                } else if (calendarType.equals(CalendarType.GREGORIAN)) {
                    data.setLabel(MyCalendarUtil.getGregorianMonthName(mountId));
                }
            }

            //in bood
//            Double roundedMonthRate = new BigDecimal(monthRates.get(i)).setScale(2, RoundingMode.HALF_UP).doubleValue();

            // in shod
            int tmpCounter = i;
            if (tmpCounter == 0) {
                tmpCounter = 12;
            }
            Double roundedMonthRate = new BigDecimal(monthRates.get(tmpCounter - 1)).setScale(2, RoundingMode.HALF_UP).doubleValue();
            data.setValue(String.valueOf(roundedMonthRate));
            data.setLink(stringBuffer.toString());
            dataArrayList.add(data);
        }

        return dataArrayList;
    }

    public Chart getSecondChartVo(String monthName) {
        Chart secondChart = new Chart();
        StringBuffer caption = new StringBuffer();
        caption.append("میزان محبوبیت در ");
        caption.append(monthName);
        caption.append(" ماه");
        secondChart.setCaption(caption.toString());
        secondChart.setSubCaption("");
        secondChart.setxAxisName("تاریخ");
        secondChart.setyAxisName("میزان محبوبیت");
        secondChart.setNumberPrefix("");
        secondChart.setPaletteColors("#0075c2");
        secondChart.setBgColor("#ffffff");
        secondChart.setBorderAlpha("20");
        secondChart.setCanvasBorderAlpha("0");
        secondChart.setUsePlotGradientColor("0");
        secondChart.setPlotBorderAlpha("10");
        secondChart.setPlacevaluesInside("1");
        secondChart.setRotatevalues("1");
        secondChart.setValueFontColor("#ffffff");
        secondChart.setShowXAxisLine("1");
        secondChart.setxAxisLineColor("#999999");
        secondChart.setDivlineColor("#999999");
        secondChart.setDivLineIsDashed("1");
        secondChart.setShowAlternateHGridColor("0");
        secondChart.setSubcaptionFontBold("0");
        secondChart.setSubcaptionFontSize("38");
        return secondChart;


    }

}
