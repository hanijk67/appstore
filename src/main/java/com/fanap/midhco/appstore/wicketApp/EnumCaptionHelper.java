package com.fanap.midhco.appstore.wicketApp;

import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.AnouncementType;
import com.fanap.midhco.appstore.entities.helperClasses.Gender;
import com.fanap.midhco.appstore.entities.helperClasses.UserStatus;
import com.fanap.midhco.appstore.entities.issue.IssueState;
import com.fanap.midhco.appstore.entities.OSEnvironment;
import com.fanap.midhco.appstore.service.anouncement.AppSearchAnouncementActionDescriptor;
import com.fanap.midhco.appstore.service.anouncement.IAnouncementActionDescriptor;
import com.fanap.midhco.appstore.service.app.AppPackageService;
import com.fanap.midhco.appstore.service.comment.DislikeEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin123 on 6/19/2016.
 */
public class EnumCaptionHelper {
    public final static Map<UserStatus, String> USER_STATUS = new HashMap<UserStatus, String>();
    public final static Map<Gender, String> GENDER = new HashMap<Gender, String>();
    public final static Map<PublishState, String> PUBLISH_STATE = new HashMap<PublishState, String>();
    public final static Map<DeviceState, String> DEVICE_STATE = new HashMap<DeviceState, String>();
    public final static Map<ApprovalState, String> APPROVAL_STATE = new HashMap<ApprovalState, String>();
    public final static Map<TestPriority, String> PRIORITY = new HashMap<TestPriority, String>();
    public final static Map<RatingIndex, String> RATING_INDEX = new HashMap<RatingIndex, String>();
    public final static Map<ResultStatus, String> RESULT_STATUS = new HashMap<ResultStatus, String>();
    public final static Map<TimeLineFileType, String> TIME_LINE_FILE_TYPE = new HashMap<TimeLineFileType, String>();
    public final static Map<OSEnvironment, String> OS_ENVIRONMENT = new HashMap<OSEnvironment, String>();



    public final static Map<Class<? extends AppPackageService.APPPackageException>, String> APP_PACK_EXCEPTION = new HashMap<>();
    public final static Map<IssueState, String> ISSUE_STATE = new HashMap<>();

    public final static Map<Class<? extends IAnouncementActionDescriptor>, String> ANOUNCEMENTMAP = new HashMap<>();

    public final static Map<AnouncementType, String> ANOUNCEMENTTYPE = new HashMap<>();

    public final static Map<DislikeEnum, String> DISLIKEENUM = new HashMap<>();

    static {
        USER_STATUS.put(UserStatus.DISABLED, AppStorePropertyReader.getString("UserStatus.disabled"));
        USER_STATUS.put(UserStatus.ADMIN_BLOCKED, AppStorePropertyReader.getString("UserStatus.admin.blocked"));
        USER_STATUS.put(UserStatus.SECURITY_BLOCKED, AppStorePropertyReader.getString("UserStatus.security.blocked"));
        USER_STATUS.put(UserStatus.ENABLED, AppStorePropertyReader.getString("UserStatus.enabled"));

        GENDER.put(Gender.MALE, AppStorePropertyReader.getString("gender.male"));
        GENDER.put(Gender.FEMALE, AppStorePropertyReader.getString("gender.female"));

        DEVICE_STATE.put(DeviceState.FREE, AppStorePropertyReader.getString("DeviceState.free"));
        DEVICE_STATE.put(DeviceState.INUSED, AppStorePropertyReader.getString("DeviceState.inUsed"));

        APPROVAL_STATE.put(ApprovalState.APPROVED, AppStorePropertyReader.getString("ApprovalState.approved"));
        APPROVAL_STATE.put(ApprovalState.DISAPPROVED, AppStorePropertyReader.getString("ApprovalState.disApproved"));
        APPROVAL_STATE.put(ApprovalState.CANCELED, AppStorePropertyReader.getString("ApprovalState.canceled"));

        PRIORITY.put(TestPriority.LOW, AppStorePropertyReader.getString("Priority.low"));
        PRIORITY.put(TestPriority.MEDIUM, AppStorePropertyReader.getString("Priority.medium"));
        PRIORITY.put(TestPriority.HIGH, AppStorePropertyReader.getString("Priority.high"));

        PUBLISH_STATE.put(PublishState.PUBLISHED, AppStorePropertyReader.getString("PublishState.published"));
        PUBLISH_STATE.put(PublishState.UNPUBLISHED, AppStorePropertyReader.getString("PublishState.unPublished"));

        APP_PACK_EXCEPTION.put(AppPackageService.APPPackageGeneralException.class, "برز خطای کلی در پردازش بسته برنامه");
        APP_PACK_EXCEPTION.put(AppPackageService.PackageFileNotExistsException.class, "فایل بسته برنامه یافت نشد");
        APP_PACK_EXCEPTION.put(AppPackageService.PackageFileNotValidException.class, "فایل برنامه معتبر نیست");
        APP_PACK_EXCEPTION.put(AppPackageService.OSValidationScriptNotValid.class, "اسکریپت پردازش معتبر نیست");

        ISSUE_STATE.put(IssueState.NOTCONSIDERED, "بررسی نشده");
        ISSUE_STATE.put(IssueState.CANCELLED, "لغو شده");
        ISSUE_STATE.put(IssueState.DONE, "انجام شده");
        ISSUE_STATE.put(IssueState.FAILED, "رد شده");

        RATING_INDEX.put(RatingIndex.Excellent, "عالی");
        RATING_INDEX.put(RatingIndex.VeryGood, "خیلی خوب");
        RATING_INDEX.put(RatingIndex.Good, "خوب");
        RATING_INDEX.put(RatingIndex.intermediate, "متوسط");
        RATING_INDEX.put(RatingIndex.bad, "بد");

        RESULT_STATUS.put(ResultStatus.SUCCESSFUL,AppStorePropertyReader.getString("label.successfull"));
        RESULT_STATUS.put(ResultStatus.UNSUCCESSFUL,AppStorePropertyReader.getString("label.unsuccessfull"));
        RESULT_STATUS.put(ResultStatus.APP_NOT_FOUND,AppStorePropertyReader.getString("error.app.not.found"));
        RESULT_STATUS.put(ResultStatus.USER_NOT_IN_SSO,AppStorePropertyReader.getString("error.input.user.not.found.in.sso"));
        RESULT_STATUS.put(ResultStatus.COMMENT_IS_NULL,AppStorePropertyReader.getString("error.comment.is.null"));
        RESULT_STATUS.put(ResultStatus.DEVICE_ID_IS_NULL,AppStorePropertyReader.getString("error.device.id.is.null"));
        RESULT_STATUS.put(ResultStatus.RATING_INDEX_IS_NULL,AppStorePropertyReader.getString("error.rating.index.is.null"));
        RESULT_STATUS.put(ResultStatus.INVALID_TOKEN,AppStorePropertyReader.getString("error.invalid.token"));
        RESULT_STATUS.put(ResultStatus.PARENT_COMMENT_NOT_FOUND,AppStorePropertyReader.getString("error.parent.comment.not.found"));
        RESULT_STATUS.put(ResultStatus.OS_NOT_FOUND,AppStorePropertyReader.getString("error.os.not.found"));
        RESULT_STATUS.put(ResultStatus.INTERNAL_SERVER_ERROR,AppStorePropertyReader.getString("error.generalErr"));
        RESULT_STATUS.put(ResultStatus.PACKAGE_NOT_FOUND,AppStorePropertyReader.getString("no.appPackage.found"));
        RESULT_STATUS.put(ResultStatus.NULL_DATA,AppStorePropertyReader.getString("null.input.data"));
        RESULT_STATUS.put(ResultStatus.INVALID_ACCESS,AppStorePropertyReader.getString("invalid.access"));
        RESULT_STATUS.put(ResultStatus.ORGANIZATION_NOT_FOUND,AppStorePropertyReader.getString("organization.not.found"));
        RESULT_STATUS.put(ResultStatus.ORGANIZATION_EXIST,AppStorePropertyReader.getString("error.organization.exist"));
        RESULT_STATUS.put(ResultStatus.ENVIRONMENT_NOT_FOUND,AppStorePropertyReader.getString("osEnvironment.not.found"));
        RESULT_STATUS.put(ResultStatus.INVALID_USER,AppStorePropertyReader.getString("invalid.user"));
        RESULT_STATUS.put(ResultStatus.INVALID_DATA,AppStorePropertyReader.getString("invalid.data"));
        RESULT_STATUS.put(ResultStatus.NO_JWT_TOKEN_RECEIVED,AppStorePropertyReader.getString("no.jwtTokenUser.recieved.or.unknown.jwtTokenUser"));
        RESULT_STATUS.put(ResultStatus.INVALID_ROLE,AppStorePropertyReader.getString("invalid.role"));
        RESULT_STATUS.put(ResultStatus.INVALID_USER_STATE,AppStorePropertyReader.getString("invalid.user.state"));
        RESULT_STATUS.put(ResultStatus.ROLE_EXIST,AppStorePropertyReader.getString("error.role.exist"));
        RESULT_STATUS.put(ResultStatus.INVALID_APP_CATEGORY,AppStorePropertyReader.getString("invalid.app.category"));
        RESULT_STATUS.put(ResultStatus.APP_CATEGORY_EXIST,AppStorePropertyReader.getString("error.app.category.exist"));
        RESULT_STATUS.put(ResultStatus.OS_TYPE_NOT_FOUND,AppStorePropertyReader.getString("error.osType.not.found"));
        RESULT_STATUS.put(ResultStatus.DUPLICATE_HANDLER_APP,AppStorePropertyReader.getString("error.duplicate.handler.app"));
        RESULT_STATUS.put(ResultStatus.ANNOUNCEMENT_NOT_FOUND,AppStorePropertyReader.getString("error.announcement.not.found"));
        RESULT_STATUS.put(ResultStatus.UNDELETED_APP_FOUND,AppStorePropertyReader.getString("error.unDeleted.app.found"));
        RESULT_STATUS.put(ResultStatus.USER_NOT_DEFINED_IN_APPSTORE,AppStorePropertyReader.getString("error.user.notDefined.in.appStore"));
        RESULT_STATUS.put(ResultStatus.INVALID_APP,AppStorePropertyReader.getString("error.invalid.app"));
        RESULT_STATUS.put(ResultStatus.NOT_APP_DEVELOPER,AppStorePropertyReader.getString("error.not.app.developer"));
        RESULT_STATUS.put(ResultStatus.IMAGES_NOT_FOUND,AppStorePropertyReader.getString("error.images.not.found"));
        RESULT_STATUS.put(ResultStatus.COMPLAINT_EMAIL_NOT_FOUND,AppStorePropertyReader.getString("error.complaint.email.not.found"));
        RESULT_STATUS.put(ResultStatus.PROBLEM_IN_SENDING_EMAIL,AppStorePropertyReader.getString("error.sending.email"));
        RESULT_STATUS.put(ResultStatus.ALREADY_APPROVED,AppStorePropertyReader.getString("error.already.approved"));
        RESULT_STATUS.put(ResultStatus.ALREADY_DIS_APPROVED,AppStorePropertyReader.getString("error.already.dis.approved"));

        TIME_LINE_FILE_TYPE.put(TimeLineFileType.HTML,AppStorePropertyReader.getString("file.type.html"));
        TIME_LINE_FILE_TYPE.put(TimeLineFileType.VIDEO,AppStorePropertyReader.getString("file.type.video"));
        TIME_LINE_FILE_TYPE.put(TimeLineFileType.IMAGE,AppStorePropertyReader.getString("file.type.image"));
        TIME_LINE_FILE_TYPE.put(TimeLineFileType.IMAGE_AND_VIDEO,AppStorePropertyReader.getString("file.type.image.and.video"));
        TIME_LINE_FILE_TYPE.put(TimeLineFileType.TEXT,AppStorePropertyReader.getString("file.type.text"));
        TIME_LINE_FILE_TYPE.put(TimeLineFileType.ANY_FILE,AppStorePropertyReader.getString("file.type.any"));


        ANOUNCEMENTMAP.put(AppSearchAnouncementActionDescriptor.class,AppStorePropertyReader.getString("Application.search") );
        ANOUNCEMENTTYPE.put(AnouncementType.PRODUCTLISTTYPE, AppStorePropertyReader.getString("Anouncement.productVo"));
        ANOUNCEMENTTYPE.put(AnouncementType.VOID, AppStorePropertyReader.getString("Anouncement.void"));

        DISLIKEENUM.put(DislikeEnum.NOT_USEFUL, "مفید نیست");
        DISLIKEENUM.put(DislikeEnum.NOT_SUITABLE, "نامناسب");
        DISLIKEENUM.put(DislikeEnum.SPAM, "اسپم");


    }
}
