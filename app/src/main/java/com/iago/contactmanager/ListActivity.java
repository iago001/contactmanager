package com.iago.contactmanager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.List;


public class ListActivity extends Activity
        implements SearchView.OnQueryTextListener,
        ListListener {

    private RecyclerView recyclerView;
    private String mSearchString;
    private Handler mHandler;
    private MyRecyclerAdapter mAdapter;
    private MenuItem mSearchMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        mHandler = new Handler();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSearchString = "";
        new GetContactsTask(this).execute(mSearchString);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(this);

        mSearchMenuItem = menu.getItem(0);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_add) {
            Intent intent = new Intent(this, CreateContact.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void onContactsLoaded(List<ContactItem> contacts) {
        mAdapter = new MyRecyclerAdapter(this, contacts);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {

        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {

        if (!(s  + "").equals(mSearchString)) {
            // start search
            mSearchString = s;
            new GetContactsTask(this).execute(mSearchString);

        }

        return false;
    }

    @Override
    public void onDeleteClicked(ContactItem item) {
        new DeleteContactTask().execute(item);
        if (mSearchMenuItem != null) {
            mSearchMenuItem.collapseActionView();
        }
    }



    private class DeleteContactTask extends AsyncTask<ContactItem, Void, Boolean> {
        @Override
        protected Boolean doInBackground(ContactItem... contactItems) {

            Uri uri = ContactsContract.RawContacts.CONTENT_URI;
            int i = getContentResolver().delete(uri, ContactsContract.RawContacts.CONTACT_ID + "=" + contactItems[0].mId, null);

            if (i > 0) {
                return true;
            }

            return false;
        }

        public void onPostExecute(Boolean result) {
            String resultStr = "";

            if (result) {
                resultStr = getString(R.string.deleteSuccess);
            } else {
                resultStr = getString(R.string.deleteFailed);
            }

            Toast.makeText(ListActivity.this, resultStr, Toast.LENGTH_LONG).show();

            mSearchString = "";
            new GetContactsTask(ListActivity.this).execute(mSearchString);
        }
    }
}