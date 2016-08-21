package com.sunil.contactsloadapp.observer;

import android.content.Context;
import android.database.ContentObserver;

import com.sunil.contactsloadapp.adapter.ContactAdapter;
import com.sunil.contactsloadapp.model.Contact;
import com.sunil.contactsloadapp.task.AsyncContactTask;

import java.util.List;

/**
 * Created by sunil on 20-Aug-16.
 */
public class ContactsObserver extends ContentObserver {
    List<Contact> mList;
    ContactAdapter mAdapter;
    Context mContext;
    public ContactsObserver(Context context, List<Contact> list, ContactAdapter contactAdapter) {
        super(null);
        mList = list;
        mAdapter = contactAdapter;
        mContext = context;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        new Thread(new AsyncContactTask(mContext, mList, mAdapter)).start();
    }

}
