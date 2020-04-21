package com.fanap.midhco.appstore.service.email;

import com.fanap.midhco.appstore.restControllers.vos.ResponseVO;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;

import java.util.List;

/**
 * Created by khashayar on 1/4/2017.
 */
public class SendEmailVo {
    private String fromAddress;
    private List<String> to;
    private String replyAddress;
    private String content;
    private String Subject;
    private List<String> cc;
    private List<String> bcc;
    private int mailType=0;
    private long messageId;
    public String getReplyAddress() {
        return replyAddress;
    }

    public String getContent() {
        return content;
    }

    public String getSubject() {
        return Subject;
    }


    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public List<String> getCc() {
        return cc;
    }

    public List<String> getBcc() {
        return bcc;
    }

    public int getMailType() {
        return mailType;
    }

    public String getFromAddress() {

        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public List<String> getTo() {
        return to;
    }

    public void setTo(List<String> to) {
        this.to = to;
    }

    public void setReplyAddress(String replyAddress) {
        this.replyAddress = replyAddress;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSubject(String subject) {
        Subject = subject;
    }

    public void setCc(List<String> cc) {
        this.cc = cc;
    }

    public void setBcc(List<String> bcc) {
        this.bcc = bcc;
    }

    public void setMailType(int mailType) {
        this.mailType = mailType;
    }

    public static ResponseVO checkInputData(SendEmailVo sendEmailVo) {
        ResponseVO responseVO = new ResponseVO();
        responseVO.setResult(ResultStatus.NULL_DATA.toString());
        responseVO.setResultStatus(ResultStatus.NULL_DATA);
        if (sendEmailVo != null) {
            if (sendEmailVo.getSubject() != null && !sendEmailVo.getSubject().trim().equals("") && sendEmailVo.getContent() != null && !sendEmailVo.getContent().trim().equals("")  &&
                    sendEmailVo.getTo()!=null && !sendEmailVo.getTo().isEmpty()) {
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                responseVO.setResult(ResultStatus.SUCCESSFUL.toString());
            }
        }
        return responseVO;

    }
}
