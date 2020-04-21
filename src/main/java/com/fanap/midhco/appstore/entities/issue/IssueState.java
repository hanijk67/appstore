package com.fanap.midhco.appstore.entities.issue;

import com.fanap.midhco.appstore.wicketApp.EnumCaptionHelper;

import javax.persistence.Embeddable;

/**
 * Created by admin123 on 12/14/2016.
 */
@Embeddable
public class IssueState {
    public static final IssueState NOTCONSIDERED = new IssueState(0);
    public static final IssueState CANCELLED = new IssueState(1);
    public static final IssueState DONE = new IssueState(2);
    public static final IssueState FAILED = new IssueState(3);

    int state;

    public IssueState() {}

    public IssueState(int state) {
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

        IssueState that = (IssueState) o;

        return state == that.getState();

    }

    @Override
    public int hashCode() {
        return state;
    }

    @Override
    public String toString() {
        return EnumCaptionHelper.GENDER.get(this);
    }

}
