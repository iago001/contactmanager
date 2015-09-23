package com.iago.contactmanager;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ankurkumar on 17/09/15.
 */
public class GetContactsTask extends AsyncTask<String, Void, List<ContactItem>> {

    private final int INDEX_ID = 0;
    private final int INDEX_NAME = 1;
    private final int INDEX_MIMETYPE = 2;
    private final int INDEX_DATA = 3;

    private ListActivity mActivity;
    private HashMap<Long, ContactItem> contactItemHashMap = new HashMap<Long, ContactItem>();

    public GetContactsTask(ListActivity aActivity) {
        mActivity = aActivity;
    }

    @Override
    protected List<ContactItem> doInBackground(String... term) {

        StringBuilder filterString = new StringBuilder("("
                + ContactsContract.Data.MIMETYPE + "='"
                + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                + "' OR "
                + ContactsContract.Data.MIMETYPE + "='"
                + ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                + "')");

        if (!TextUtils.isEmpty(term[0])) {
            filterString.append(" AND (" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                    + " LIKE '%" + term[0] + "%' OR ");
            filterString.append(" REPLACE(REPLACE(" + ContactsContract.CommonDataKinds.Phone.DATA1
                    + ",' ',''),'-','') LIKE '%" + term[0] + "%' OR ");
            filterString.append(" " + ContactsContract.CommonDataKinds.Phone.DATA1
                    + " LIKE '%" + term[0] + "%')");
        }

        Cursor cursor = mActivity.getContentResolver()
                        .query(ContactsContract.Data.CONTENT_URI,
                                new String[]{
                                        ContactsContract.Data.CONTACT_ID,
                                        ContactsContract.Data.DISPLAY_NAME,
                                        ContactsContract.Data.MIMETYPE,
                                        ContactsContract.Data.DATA1
                                },
                                filterString.toString(),
                                null,
                                ContactsContract.Data.DISPLAY_NAME);

        while(cursor.moveToNext()) {
            long id = cursor.getLong(INDEX_ID);
            String name = cursor.getString(INDEX_NAME);
            ContactItem item;
            if (contactItemHashMap.containsKey(id)) {
                item = contactItemHashMap.get(id);
            } else {
                item = new ContactItem(id, name);
                contactItemHashMap.put(id, item);
            }

            String mimetype = cursor.getString(INDEX_MIMETYPE);
            String data = cursor.getString(INDEX_DATA);
            if (mimetype.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                item.addPhone(data);
            } else {
                item.addEmail(data);
            }

        }
        cursor.close();

        ArrayList<ContactItem> contacts = new ArrayList<ContactItem>();
        for (Long id : contactItemHashMap.keySet()) {
            contacts.add(contactItemHashMap.get(id));
        }

        Collections.sort(contacts);

        return contacts;
    }

    public void onPostExecute(List<ContactItem> contacts) {
        mActivity.onContactsLoaded(contacts);
    }
}
