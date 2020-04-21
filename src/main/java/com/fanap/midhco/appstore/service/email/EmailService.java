//package com.fanap.midhco.appstore.service.email;
//
//import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
//import com.fanap.midhco.appstore.applicationUtils.JsonUtil;
//import com.fanap.midhco.appstore.entities.async.MessageVO;
//import com.fanap.midhco.appstore.entities.async.MessageWrapperVO;
//import com.fanap.midhco.appstore.restControllers.vos.ResponseVO;
//import com.fanap.midhco.appstore.service.async.AsyncService;
//import com.fanap.midhco.appstore.wicketApp.ResultStatus;
//import org.apache.http.NameValuePair;
//import org.apache.http.message.BasicNameValuePair;
//import org.apache.qpid.amqp_1_0.jms.impl.QueueImpl;
//
//import javax.jms.Connection;
//import javax.jms.Destination;
//import javax.jms.MessageProducer;
//import javax.jms.Session;
//import java.io.BufferedWriter;
//import java.io.OutputStream;
//import java.io.OutputStreamWriter;
//import java.io.UnsupportedEncodingException;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.net.URLEncoder;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by admin123 on 4/5/2017.
// */
//public class EmailService {
//    public static EmailService Instance = new EmailService();
//
//    private EmailService() {
//    }
//
//    public void sendEmail(SendEmailVo sendEmailVO) throws Exception {
//        Connection proConnection = null;
//
//        try {
//            Request mailRequest = new Request();
//            RequestHeader requestHeader = new RequestHeader();
//            mailRequest.setMessageType(518);
//
//            String serviceName = ConfigUtil.getProperty(ConfigUtil.NOTIFICATION_SERVICE_NAME);
//            String apiToken = ConfigUtil.getProperty(ConfigUtil.OAUTH_CLIENT_API_TOKEN);
//
//            mailRequest.setServiceName(serviceName);
//            mailRequest.setContent(JsonUtil.getJson(sendEmailVO));
//            requestHeader.setApiToken(apiToken);
//            mailRequest.setRequestHeader(requestHeader);
//
//            String mailJson = JsonUtil.getJson(mailRequest);
//
//            MessageVO messageVO = new MessageVO();
//            long[] recievers = new long[1];
//            messageVO.setContent(mailJson);
//            recievers[0] = Long.parseLong(ConfigUtil.getProperty(ConfigUtil.NOTIFICATION_ENDPOINT_PEERID));
//            messageVO.setReceivers(recievers);
//
//            MessageWrapperVO messageWrapperVO = new MessageWrapperVO();
//            messageWrapperVO.setContent(JsonUtil.getJson(messageVO));
//            messageWrapperVO.setType(AsyncService.AsyncMessageType.MESSAGE);
//
//            proConnection = AsyncService.Instance.getConnection();
//            proConnection.start();
//
//            Session producerSession = proConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//
//            Destination outputQueue = new QueueImpl(ConfigUtil.getProperty(ConfigUtil.QUEUE_OUTPUT_QUEUE));
//            MessageProducer producer = producerSession.createProducer(outputQueue);
//
//            AsyncService.Instance.sendMessage(messageWrapperVO, producer, producerSession);
//        } catch (Exception ex) {
//            throw ex;
//        } finally {
//            if(proConnection != null)
//                proConnection.close();
//        }
//    }
//
//
//    public ResponseVO sendEmailByPod(SendEmailVo sendEmailVO, String token) throws Exception {
//
//        ResponseVO responseVO = new ResponseVO();
//        responseVO = SendEmailVo.checkInputData(sendEmailVO);
//        try {
//
////http://core.pod.land/nzh/biz/sendEmailByEmailAddress?toEmails=a.moshiri.a%40gmail.com&subject=shekayat&htmlContent=salam%20boro%20barnamat%20ro%20bebin
//            StringBuffer sendMailUrl = new StringBuffer(ConfigUtil.getProperty(ConfigUtil.POD_SERVER_URL));
//            URL url = new URL(sendMailUrl.toString());
//            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//            httpURLConnection.setRequestMethod("GET");
//            List<NameValuePair> params = new ArrayList<NameValuePair>();
//
//            httpURLConnection.setRequestMethod("GET");
//            httpURLConnection.setRequestProperty("_token_", ConfigUtil.getProperty(ConfigUtil.POD_API_TOKEN));
//            httpURLConnection.setRequestProperty("_token_issuer_", "1");
//            httpURLConnection.setDoOutput(true);
//
//            for (String emailStr : sendEmailVO.getTo()) {
//                params.add(new BasicNameValuePair("toEmails", emailStr));
//            }
//            params.add(new BasicNameValuePair("subject", sendEmailVO.getSubject()));
//            params.add(new BasicNameValuePair("htmlContent", sendEmailVO.getContent()));
//
//            OutputStream os = httpURLConnection.getOutputStream();
//            BufferedWriter writer = new BufferedWriter(
//                    new OutputStreamWriter(os, "UTF-8"));
//            writer.write(getQuery(params));
//            writer.flush();
//            writer.close();
//            os.close();
//
//            httpURLConnection.connect();
//
//            int responseCode = httpURLConnection.getResponseCode();
//            if (responseCode == HttpURLConnection.HTTP_OK) {
//                responseVO.setResult(ResultStatus.SUCCESSFUL.toString());
//                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
//            } else {
//                StringBuffer result = new StringBuffer(ResultStatus.UNSUCCESSFUL.toString()).append(" beacauseof responseCode is ").append(responseCode);
//                responseVO.setResult(result.toString());
//                responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
//            }
//
//        } catch (Exception ex) {
//            responseVO.setResult(ex.getMessage());
//            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
//        }
//        return responseVO;
//    }
//
//    private static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
//        StringBuilder result = new StringBuilder();
//        boolean first = true;
//
//        for (NameValuePair pair : params) {
//            if (first)
//                first = false;
//            else
//                result.append("&");
//
//            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
//            result.append("=");
//            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
//        }
//
//        return result.toString();
//    }
//
//    public static void main(String[] args) {
//
//        try {
//            String intendedEmail = "a.moshiri.a@gmail.com";
//
//            String subject = " باز کن";
//            String content = "from Pod";
//
//            SendEmailVo sendEmailVo = new SendEmailVo();
//            sendEmailVo.setContent(content);
//            sendEmailVo.setSubject(subject);
//            sendEmailVo.setMailType(0);
//            ArrayList<String> to = new ArrayList<>();
//            to.add(intendedEmail);
////            to.add("a.moshiri.a@gmail.com");
//            sendEmailVo.setTo(to);
//
//
////http://core.pod.land/nzh/biz/sendEmailByEmailAddress?toEmails=a.moshiri.a%40gmail.com&subject=shekayat&htmlContent=salam%20boro%20barnamat%20ro%20bebin
//            StringBuffer sendMailUrl = new StringBuffer(ConfigUtil.getProperty(ConfigUtil.POD_SERVER_URL));
//            URL url = new URL(sendMailUrl.toString());
//            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//            httpURLConnection.setRequestMethod("GET");
//            httpURLConnection.setRequestProperty("_token_", ConfigUtil.getProperty(ConfigUtil.POD_API_TOKEN));
//            httpURLConnection.setRequestProperty("_token_issuer_", "1");
//            httpURLConnection.setDoOutput(true);
//            List<NameValuePair> params = new ArrayList<NameValuePair>();
//
//            for (String emailStr : sendEmailVo.getTo()) {
//                params.add(new BasicNameValuePair("toEmails", emailStr));
//            }
//            params.add(new BasicNameValuePair("subject", subject));
//            params.add(new BasicNameValuePair("htmlContent", content));
//
//            OutputStream os = httpURLConnection.getOutputStream();
//            BufferedWriter writer = new BufferedWriter(
//                    new OutputStreamWriter(os, "UTF-8"));
//            writer.write(getQuery(params));
//            writer.flush();
//            writer.close();
//            os.close();
//
//
//            int responseCode = httpURLConnection.getResponseCode();
//            if (responseCode == HttpURLConnection.HTTP_OK) {
//
//            } else {
//
//            }
//
//        } catch (Exception ex) {
//            System.out.printf(ex.getMessage());
//        } finally {
//        }
//    }
//}
