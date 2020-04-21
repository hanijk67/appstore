package com.fanap.midhco.appstore.restControllers.vos;

import com.fanap.midhco.appstore.wicketApp.EnumCaptionHelper;

/**
 * Created by admin123 on 8/12/2017.
 */
public class ResultStatus {
    public static ResultStatus SUCCESSFUL = new ResultStatus(0);
    public static ResultStatus UNSUCCESSFUL = new ResultStatus(1);
    public static ResultStatus APP_NOT_FOUND = new ResultStatus(2);
    public static ResultStatus USER_NOT_IN_SSO = new ResultStatus(3);
    public static ResultStatus COMMENT_IS_NULL = new ResultStatus(4);
    public static ResultStatus DEVICE_ID_IS_NULL = new ResultStatus(5);
    public static ResultStatus RATING_INDEX_IS_NULL = new ResultStatus(6);
    public static ResultStatus FILE_KEY_NULL = new ResultStatus(7);
    public static ResultStatus OS_NOT_FOUND = new ResultStatus(8);
    public static ResultStatus INVALID_TOKEN = new ResultStatus(9);
    public static ResultStatus PARENT_COMMENT_NOT_FOUND = new ResultStatus(10);
    public static ResultStatus USER_NOT_DEFINED_IN_APPSTORE = new ResultStatus(11);
    public static ResultStatus INTERNAL_SERVER_ERROR = new ResultStatus(12);
    public static ResultStatus PACKAGE_NOT_FOUND = new ResultStatus(13);
    public static ResultStatus NULL_DATA = new ResultStatus(14);
    public static ResultStatus INVALID_ACCESS = new ResultStatus(15);
    public static ResultStatus ORGANIZATION_NOT_FOUND = new ResultStatus(16);
    public static ResultStatus ENVIRONMENT_NOT_FOUND = new ResultStatus(17);
    public static ResultStatus INVALID_USER = new ResultStatus(18);
    public static ResultStatus INVALID_DATA = new ResultStatus(19);
    public static ResultStatus NO_JWT_TOKEN_RECEIVED = new ResultStatus(20);
    public static ResultStatus INVALID_ROLE = new ResultStatus(21);
    public static ResultStatus INVALID_USER_STATE = new ResultStatus(22);
    public static ResultStatus ROLE_EXIST = new ResultStatus(23);
    public static ResultStatus INVALID_APP_CATEGORY = new ResultStatus(24);
    public static ResultStatus APP_CATEGORY_EXIST = new ResultStatus(25);
    public static ResultStatus ORGANIZATION_EXIST = new ResultStatus(26);
    public static ResultStatus DUPLICATE_HANDLER_APP = new ResultStatus(27);
    public static ResultStatus OS_TYPE_NOT_FOUND = new ResultStatus(28);
    public static ResultStatus INVALID_OS_TYPE = new ResultStatus(29);
    public static ResultStatus ANNOUNCEMENT_NOT_FOUND = new ResultStatus(30);
    public static ResultStatus APP_EXIST = new ResultStatus(31);

    int state;

    public ResultStatus(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResultStatus that = (ResultStatus) o;

        return state == that.state;

    }

    @Override
    public int hashCode() {
        return state;
    }

    @Override
    public String toString() {
        return EnumCaptionHelper.RESULT_STATUS.get(this);
    }
}
