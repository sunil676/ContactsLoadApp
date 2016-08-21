package com.sunil.contactsloadapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.sunil.contactsloadapp.adapter.ContactAdapter;
import com.sunil.contactsloadapp.model.Contact;
import com.sunil.contactsloadapp.observer.ContactsObserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    private final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
   // private EditText mEditTextSearch;
    private SearchView mSearchView;

    List<Contact> mContactList;
    List<Contact> mContactOriginalList;
    ContactAdapter contactAdapter;
    private static final int REQUET_CODE_WRITE = 101;
    private static final int REQUET_CODE_READ = 102;
    private List<Contact> filteredList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        mSearchView = (SearchView) findViewById(R.id.searchView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mContactList = new ArrayList<Contact>();
        mContactOriginalList = new ArrayList<>();

        contactAdapter = new ContactAdapter(this, mContactList);
        mRecyclerView.setAdapter(contactAdapter);

        if(checkPermissionForReadContact()) {
            if(checkPermissionForWriteContact()) {
                startReadingContacts();
            }
        }

        setupSearchView();

    }

    private void setupSearchView() {
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setQueryHint("Search Here");
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean checkPermissionForReadContact() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUET_CODE_READ);
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean checkPermissionForWriteContact() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS}, REQUET_CODE_WRITE);
            return false;
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        //filter(s);
       // contactAdapter.getFilter().filter(s);
        final List<Contact> filteredModelList = filter(mContactOriginalList, s);
        contactAdapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        final List<Contact> filteredModelList = filter(mContactOriginalList, s);
        contactAdapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return true;
    }

    private List<Contact> filter(List<Contact> models, String query) {
        query = query.toLowerCase();

        final List<Contact> filteredModelList = new ArrayList<>();
        if (TextUtils.isEmpty(query)) {
            filteredModelList.addAll(models);

        } else {
            for (Contact model : models) {
                final String text = model.getContact_name().toLowerCase();
                if (text.contains(query)) {
                    filteredModelList.add(model);
                }
            }
        }
        return filteredModelList;
    }

    // Do Search...
    public void filter(final String text) {
        filteredList = new ArrayList<>();
        filteredList.clear();
        if (TextUtils.isEmpty(text)) {
            filteredList.addAll(mContactList);

        } else {
            // Iterate in the original List and add it to filter list...
            for (Contact item : mContactList) {
                if (item.getContact_name().toLowerCase().contains(text.toLowerCase())) {
                    // Adding Matched items
                    filteredList.add(item);
                }
            }
        }

        contactAdapter.notifyDataSetChanged();

    }



    public class LoadContactAsync extends AsyncTask<Void, Void, Void> {
        boolean isSynchronous = false;

        @Override
        protected Void doInBackground(Void... params) {

            // if (list.size() == 0) {
            StringBuffer buffer = new StringBuffer();
            int version_sdk;
            try {
                version_sdk = Integer.valueOf(android.os.Build.VERSION.SDK);
            } catch (NumberFormatException e) {
                version_sdk = 0;
            }
            ContentResolver cr = getContentResolver();
            Cursor pCur = null;
            if (version_sdk > 11) {
                pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " ASC");
            } else {

                pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                new String[] {
                                        ContactsContract.CommonDataKinds.Phone._ID,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME },
                                null, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " ASC");
            }
            while (pCur.moveToNext()) {
                String contact_id = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                String contact_name = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String contact_name_order = "";

                String phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                int phoneId = pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
                String profilePic = "";
                if (version_sdk > 11) {
                    profilePic = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                }
                if (profilePic == null)
                    profilePic = "";

                Contact contact = new Contact(contact_id, contact_name, phone, phoneId, profilePic);
                mContactList.add(contact);
                mContactOriginalList.add(contact);

            }

            ContactsComparator contactsComparator = new ContactsComparator();
            Collections.sort(mContactList, contactsComparator);
            Collections.sort(mContactOriginalList, contactsComparator);

            pCur.close();

            // }

            return null;

        }

        @Override
        protected void onPostExecute(Void result) {
            mProgressBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            contactAdapter.notifyDataSetChanged();
            super.onPostExecute(result);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUET_CODE_READ:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(checkPermissionForReadContact()) {
                        startReadingContacts();
                    }
                } else {
                    Toast.makeText(this, "You have denied permission to access read contacts. Please go to settings and enable access to use this feature", Toast.LENGTH_LONG).show();
                }
                break;
            case REQUET_CODE_WRITE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(checkPermissionForWriteContact()) {
                        startReadingContacts();
                    }
                } else {
                    Toast.makeText(this, "You have denied permission to access write contacts. Please go to settings and enable access to use this feature", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void startReadingContacts() {
        ContactsObserver contentObserver = new ContactsObserver(MainActivity.this, mContactList, contactAdapter);
        new LoadContactAsync().execute();
        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        this.getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, contentObserver);
    }

    class ContactsComparator implements Comparator<Contact> {
        public int compare(Contact c1, Contact c2) {
            final String name1 = c1.getContact_name();
            final String name2 = c2.getContact_name();

            if (!name1.contains("@") && name2.contains("@")) {
                return -1;
            } else if (name1.contains("@") && !name2.contains("@")) {
                return 1;
            } else {
                return name1.compareToIgnoreCase(name2);
            }
        }
    }
}
