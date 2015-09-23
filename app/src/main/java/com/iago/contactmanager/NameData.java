package com.iago.contactmanager;

/**
 * Created by ankurkumar on 21/09/15.
 */
public class NameData implements Data {

    private String mData;

    public NameData(String data) {
        mData = data;
    }

    @Override
    public String getData() {
        return mData;
    }

    @Override
    public void storeData(String data) {
        mData = data;
    }
}
