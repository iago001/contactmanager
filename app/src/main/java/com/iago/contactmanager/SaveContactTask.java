package com.iago.contactmanager;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ankurkumar on 21/09/15.
 */
public class SaveContactTask extends AsyncTask<Data, Void, Boolean> {

    private final String TAG = "SaveContactTask";

    private final Context mContext;
    private String mAccountName;
    private String mAccountType;
    private String mName;

    public SaveContactTask(Context context, String accountName, String accountType) {
        mContext = context;
        mAccountName = accountName;
        mAccountType = accountType;
    }

    @Override
    protected Boolean doInBackground(Data... arrayLists) {
        /*
         * Prepares the batch operation for inserting a new raw contact and its data. Even if
         * the Contacts Provider does not have any data for this person, you can't add a Contact,
         * only a raw contact. The Contacts Provider will then add a Contact automatically.
         */

        // Creates a new array of ContentProviderOperation objects.
        ArrayList<ContentProviderOperation> ops =
                new ArrayList<ContentProviderOperation>();

        /*
         * Creates a new raw contact with its account type (server type) and account name
         * (user's account). Remember that the display name is not stored in this row, but in a
         * StructuredName data row. No other data is required.
         */
        ContentProviderOperation.Builder op =
                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, mAccountType)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, mAccountName);

        // Builds the operation and adds it to the array of operations
        ops.add(op.build());

        for (Data data : arrayLists) {
            if (data instanceof NameData) {
                // Creates the display name for the new raw contact, as a StructuredName data row.
                op =
                            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                /*
                 * withValueBackReference sets the value of the first argument to the value of
                 * the ContentProviderResult indexed by the second argument. In this particular
                 * call, the raw contact ID column of the StructuredName data row is set to the
                 * value of the result returned by the first operation, which is the one that
                 * actually adds the raw contact row.
                 */
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)

                        // Sets the data row's MIME type to StructuredName
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)

                        // Sets the data row's display name to the name in the UI.
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, data.getData());

                mName = data.getData();

                // Builds the operation and adds it to the array of operations
                ops.add(op.build());
            } else if (data instanceof PhoneData) {
                // Inserts the specified phone number and type as a Phone data row
                op =
                        ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                /*
                 * Sets the value of the raw contact id column to the new raw contact ID returned
                 * by the first operation in the batch.
                 */
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)

                            // Sets the data row's MIME type to Phone
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)

                            // Sets the phone number and type
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, data.getData())
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);

                // Builds the operation and adds it to the array of operations
                ops.add(op.build());
            } else if (data instanceof EmailData) {
                // Inserts the specified email and type as a Phone data row
                op =
                        ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                /*
                 * Sets the value of the raw contact id column to the new raw contact ID returned
                 * by the first operation in the batch.
                 */
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)

                        // Sets the data row's MIME type to Email
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)

                        // Sets the email address and type
                .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, data.getData())
                .withValue(ContactsContract.CommonDataKinds.Email.TYPE,
                                    ContactsContract.CommonDataKinds.Email.TYPE_WORK);

                /*
                 * Demonstrates a yield point. At the end of this insert, the batch operation's thread
                 * will yield priority to other threads. Use after every set of operations that affect a
                 * single contact, to avoid degrading performance.
                 */
                op.withYieldAllowed(true);

                // Builds the operation and adds it to the array of operations
                ops.add(op.build());
            }
        }

        // Ask the Contacts Provider to create a new contact
        Log.d(TAG,"Selected account: " + mAccountName + " (" +
                mAccountType + ")");

        /*
         * Applies the array of ContentProviderOperation objects in batch. The results are
         * discarded.
         */
        try {
            mContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public void onPostExecute(Boolean result) {
        String resultStr = "";

        if (result) {
            resultStr = mContext.getString(R.string.createSuccess, mName);
        } else {
            resultStr = mContext.getString(R.string.createFailed, mName);
        }

        Toast.makeText(mContext, resultStr, Toast.LENGTH_LONG).show();
    }
}
