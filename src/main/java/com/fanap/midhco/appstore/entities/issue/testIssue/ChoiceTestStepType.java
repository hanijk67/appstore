package com.fanap.midhco.appstore.entities.issue.testIssue;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;

/**
 * Created by admin123 on 12/14/2016.
 */
@DiscriminatorValue("CHOICE")
public class ChoiceTestStepType extends TestStepType {
    @Column(name = "PISMULTIPLE")
    Boolean isMultiple;

    @Column(name="PCHOICESXML")
    String choicesXML;

    public Boolean getMultiple() {
        return isMultiple;
    }

    public void setMultiple(Boolean multiple) {
        isMultiple = multiple;
    }

    public String getChoicesXML() {
        return choicesXML;
    }

    public void setChoicesXML(String choicesXML) {
        this.choicesXML = choicesXML;
    }
}
