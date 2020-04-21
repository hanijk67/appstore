package com.fanap.midhco.appstore.applicationUtils;

import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.apache.wicket.model.ResourceModel;

/**
 * Created by A.Moshiri on 7/17/2017.
 */
public class HistoryUtil {

    public static String createChangeDifferenceMessage(String fromString, String toString, DateTime changeDate, String title, String inDate, String from, String to, String action ,String changer) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(inDate).append(" ").append(changeDate).append(" ").append(title).append(" ")
                .append(from).append(" ").append(fromString).append(" ").append(to).append(" ")
                .append(toString).append(" ").append(new ResourceModel("label.by").getObject()).append(" ").append(changer).append(" ").append(action);
        return stringBuilder.toString();
    }

    public static String createAddDifferenceMessage(String addedItemString, DateTime changeDate, String title , String content, String inDate, String to, String action , String changer) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(inDate).append(" ").append(changeDate).append(" ").append(to).append(" ").append(title).append(" ").append(" ، ")
                .append(content).append(" ").append(addedItemString).append(" ").append(new ResourceModel("label.by").getObject()).append(" ").append(changer).append(" ").append(action);
        return stringBuilder.toString();
    }

    public static String createChangeDifferenceTestIssueMessage(String fromString, String testIssueTitle, String toString, DateTime changeDate, String title,
                                                                String inDate, String from, String to, String action ,String changer) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(inDate).append(" ").append(changeDate).append(" ").append(title).append(" ") .append(new ResourceModel("label.test").getObject()).append(" ، ").append(testIssueTitle)
                .append(" ").append(from).append(" ").append(fromString).append(" ").append(to).append(" ")
                .append(toString).append(" ").append(new ResourceModel("label.by").getObject()).append(" ").append(changer).append(" ").append(action);
        return stringBuilder.toString();
    }


    public static String createAddDifferenceTestIssueMessage(String addedItemString, String testIssueTitle,
                                                             DateTime changeDate, String title , String content, String inDate, String to, String action , String changer) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(inDate).append(" ").append(changeDate).append(" ").append(to).append(" ").append(title).append(" ").append(new ResourceModel("label.test").getObject()).append("، ")
                .append(testIssueTitle).append(" ، ").append(content).append(" ").append(addedItemString).append(" ").append(new ResourceModel("label.by").getObject()).append(" ")
                .append(changer).append(" ").append(action);
        return stringBuilder.toString();
    }

    public static String removeBackslash (String inputString){

        return (inputString==null || inputString.trim().equals("")) ?"  " : inputString.replaceAll("(\\r|\\n)", "");
    }

}
