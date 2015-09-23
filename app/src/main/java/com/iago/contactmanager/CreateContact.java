package com.iago.contactmanager;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.accounts.OnAccountsUpdateListener;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class CreateContact extends Activity implements OnAccountsUpdateListener, AdapterView.OnItemSelectedListener {

    private static String TAG = "CreateContact";

    private LinearLayout mPhoneLayout;
    private LinearLayout mEmailLayout;

    private EditText mNameText;

    private ArrayList<EditText> mPhoneTexts = new ArrayList<EditText>();
    private ArrayList<EditText> mEmailTexts = new ArrayList<EditText>();

    private ArrayList<AccountData> mAccounts = new ArrayList<AccountData>();
    private AccountAdapter mAccountAdapter;
    private Spinner mAccountSpinner;
    private AccountData mSelectedAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_contact);

        AccountManager.get(this).addOnAccountsUpdatedListener(this, null, true);

        mNameText = (EditText) findViewById(R.id.editName);

        mPhoneLayout = (LinearLayout) findViewById(R.id.phoneLayout);
        mEmailLayout = (LinearLayout) findViewById(R.id.emailLayout);

        mPhoneTexts.add((EditText) findViewById(R.id.phoneEditText));
        mEmailTexts.add((EditText) findViewById(R.id.emailEditText));

        mAccountAdapter = new AccountAdapter(this, mAccounts);
        mAccountSpinner = (Spinner) findViewById(R.id.accountSpinner);

        mAccountSpinner.setAdapter(mAccountAdapter);

        mAccountSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_done) {
            String accountName = null;
            String accountType = null;
            if (mSelectedAccount != null) {
                accountName = mSelectedAccount.getName();
                accountType = mSelectedAccount.getType();
            }

            ArrayList<Data> datas = new ArrayList<Data>();
            datas.add(new NameData(mNameText.getEditableText().toString()));

            for (EditText phones : mPhoneTexts) {
                if (!TextUtils.isEmpty(phones.getEditableText().toString())) {
                    datas.add(new PhoneData(phones.getEditableText().toString()));
                }
//                Log.d(TAG, "phones " + phones.getEditableText().toString());
            }

            for (EditText emails : mEmailTexts) {
                if (!TextUtils.isEmpty(emails.getEditableText().toString())) {
                    datas.add(new EmailData(emails.getEditableText().toString()));
                }
//                Log.d(TAG, "emails " + emails.getEditableText().toString());
            }

            new SaveContactTask(this, accountName, accountType)
                    .execute(datas.toArray(new Data[datas.size()]));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addPhoneBox(View view) {
        view.setVisibility(View.GONE);

        View phoneView = LayoutInflater.from(this).inflate(R.layout.phone_edit, null);
        EditText phoneEditText = (EditText) phoneView.findViewById(R.id.phoneEditText);
        mPhoneTexts.add(phoneEditText);
        mPhoneLayout.addView(phoneView, mPhoneLayout.getChildCount());
    }

    public void addEmailBox(View view) {
        view.setVisibility(View.GONE);

        View emailView = LayoutInflater.from(this).inflate(R.layout.email_edit, null);
        EditText emailEditText = (EditText) emailView.findViewById(R.id.emailEditText);
        mEmailTexts.add(emailEditText);
        mEmailLayout.addView(emailView, mEmailLayout.getChildCount());
    }

    /**
     * Called when this activity is about to be destroyed by the system.
     */
    @Override
    public void onDestroy() {
        // Remove AccountManager callback
        AccountManager.get(this).removeOnAccountsUpdatedListener(this);
        super.onDestroy();
    }
    /**
     * Updates account list spinner when the list of Accounts on the system changes. Satisfies
     * OnAccountsUpdateListener implementation.
     */
    public void onAccountsUpdated(Account[] a) {
//        Log.i(TAG, "Account list update detected");
        // Clear out any old data to prevent duplicates
        if (mAccounts != null) {
            mAccounts.clear();
        }
        // Get account data from system
        AuthenticatorDescription[] accountTypes = AccountManager.get(this).getAuthenticatorTypes();
        // Populate tables
        for (int i = 0; i < a.length; i++) {
            // The user may have multiple accounts with the same name, so we need to construct a
            // meaningful display name for each.
            String systemAccountType = a[i].type;
            AuthenticatorDescription ad = getAuthenticatorDescription(systemAccountType,
                    accountTypes);
            Log.d(TAG, "acc " + a[i].type);

            boolean syncable = ContentResolver.getIsSyncable(a[i], ContactsContract.AUTHORITY) > 0;
            if (syncable) {
                AccountData data = new AccountData(a[i].name, ad);
                mAccounts.add(data);
            }
        }
        // Update the account spinner
        mAccountAdapter.notifyDataSetChanged();
    }
    /**
     * Obtain the AuthenticatorDescription for a given account type.
     * @param type The account type to locate.
     * @param dictionary An array of AuthenticatorDescriptions, as returned by AccountManager.
     * @return The description for the specified account type.
     */
    private static AuthenticatorDescription getAuthenticatorDescription(String type,
                                                                        AuthenticatorDescription[] dictionary) {
        for (int i = 0; i < dictionary.length; i++) {
            if (dictionary[i].type.equals(type)) {
                return dictionary[i];
            }
        }
        // No match found
        throw new RuntimeException("Unable to find matching authenticator");
    }
    /**
     * Update account selection. If NO_ACCOUNT is selected, then we prohibit inserting new contacts.
     */
    private void updateAccountSelection() {
        // Read current account selection
        mSelectedAccount = (AccountData) mAccountSpinner.getSelectedItem();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        updateAccountSelection();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    /**
     * A container class used to repreresent all known information about an account.
     */
    private class AccountData {
        private String mName;
        private String mType;
        private CharSequence mTypeLabel;
        private Drawable mIcon;
        /**
         * @param name The name of the account. This is usually the user's email address or
         *        username.
         * @param description The description for this account. This will be dictated by the
         *        type of account returned, and can be obtained from the system AccountManager.
         */
        public AccountData(String name, AuthenticatorDescription description) {
            mName = name;
            if (description != null) {
                mType = description.type;
                // The type string is stored in a resource, so we need to convert it into something
                // human readable.
                String packageName = description.packageName;
                PackageManager pm = getPackageManager();
                if (description.labelId != 0) {
                    mTypeLabel = pm.getText(packageName, description.labelId, null);
                    if (mTypeLabel == null) {
                        throw new IllegalArgumentException("LabelID provided, but label not found");
                    }
                } else {
                    mTypeLabel = "";
                }
                if (description.iconId != 0) {
                    mIcon = pm.getDrawable(packageName, description.iconId, null);
                    if (mIcon == null) {
                        throw new IllegalArgumentException("IconID provided, but drawable not " +
                                "found");
                    }
                } else {
                    mIcon = getResources().getDrawable(android.R.drawable.sym_def_app_icon);
                }
            }
        }
        public String getName() {
            return mName;
        }
        public String getType() {
            return mType;
        }
        public CharSequence getTypeLabel() {
            return mTypeLabel;
        }
        public Drawable getIcon() {
            return mIcon;
        }
        public String toString() {
            return mName;
        }
    }
    /**
     * Custom adapter used to display account icons and descriptions in the account spinner.
     */
    private class AccountAdapter extends ArrayAdapter<AccountData> {
        public AccountAdapter(Context context, ArrayList<AccountData> accountData) {
            super(context, android.R.layout.simple_spinner_item, accountData);
            setDropDownViewResource(R.layout.account_entry);
        }
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            // Inflate a view template
            if (convertView == null) {
                LayoutInflater layoutInflater = getLayoutInflater();
                convertView = layoutInflater.inflate(R.layout.account_entry, parent, false);
            }
            TextView firstAccountLine = (TextView) convertView.findViewById(R.id.firstAccountLine);
            TextView secondAccountLine = (TextView) convertView.findViewById(R.id.secondAccountLine);
            ImageView accountIcon = (ImageView) convertView.findViewById(R.id.accountIcon);
            // Populate template
            AccountData data = getItem(position);
            firstAccountLine.setText(data.getName());
            secondAccountLine.setText(data.getTypeLabel());
            Drawable icon = data.getIcon();
            if (icon == null) {
                icon = getResources().getDrawable(android.R.drawable.ic_menu_search);
            }
            accountIcon.setImageDrawable(icon);
            return convertView;
        }
    }
}
