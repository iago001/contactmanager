package com.iago.contactmanager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ankurkumar on 17/09/15.
 */
public class ContactItem implements Comparable {

    long mId;
    String mName;
    List<String> mPhones = new ArrayList<String>();
    List<String> mEmails = new ArrayList<String>();

    public ContactItem(long aId, String aName) {
        mId = aId;
        mName = aName;
    }

    public void addPhone(String aPhone) {
        mPhones.add(aPhone);
    }

    public void addEmail(String aEmail) {
        mEmails.add(aEmail);
    }

    public int getPhoneCount() {
        return mPhones.size();
    }

    public int getEmailCount() {
        return mEmails.size();
    }

    public List<String> getPhones() {
        return mPhones;
    }

    public List<String> getEmails() {
        return mEmails;
    }

    @Override
    public int compareTo(Object o) {

        return (this.mName + "").toLowerCase().compareTo((((ContactItem) o).mName + "").toLowerCase());
    }
}
