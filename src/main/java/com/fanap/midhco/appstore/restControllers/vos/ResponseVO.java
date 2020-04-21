package com.fanap.midhco.appstore.restControllers.vos;

import com.fanap.midhco.appstore.wicketApp.ResultStatus;

/**
 * Created by admin123 on 8/12/2017.
 */
public class ResponseVO {
    ResultStatus resultStatus;
    String result;

    public ResponseVO() {
        resultStatus = ResultStatus.UNSUCCESSFUL;
    }

    public ResultStatus getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(ResultStatus resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
