package com.sunil.contactsloadapp.task;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.widget.Toast;

import com.sunil.contactsloadapp.adapter.ContactAdapter;
import com.sunil.contactsloadapp.model.Contact;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sunil on 20-Aug-16.
 */
public class AsyncContactTask implements Runnable{

    StringBuffer bufferChange;
    Context context;
    List<Contact> list;
    ContactAdapter adapter;

    public AsyncContactTask(Context context, List<Contact> list, ContactAdapter adapter) {
        this.context = context;
        this.list = list;
        this.adapter=adapter;
    }

    @Override
    public void run() {

        bufferChange = new StringBuffer();
        HashMap<String, Contact> hashMapContact = new HashMap<String, Contact>();
        HashMap<String, String> hashMapDB = new HashMap<String, String>();
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = null;
        int version_sdk;
        try {
            version_sdk = Integer.valueOf(android.os.Build.VERSION.SDK);
        } catch (NumberFormatException e) {
            version_sdk = 0;
        }

        if (version_sdk > 11) {
            cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            new String[] {
                                    ContactsContract.CommonDataKinds.Phone._ID,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                    ContactsContract.CommonDataKinds.Phone.PHOTO_URI },
                            null, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " ASC");
        } else {
            cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            new String[] {
                                    ContactsContract.CommonDataKinds.Phone._ID,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME },
                            null, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " ASC");
        }
        while (cursor.moveToNext()) {
            String contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
            String contact_name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String contact_name_order = "";

            String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            int phoneId = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
            String profilePic = "";
            if (version_sdk > 11) {
                profilePic = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
            }
            if (profilePic == null) {
                profilePic = "";
            }

            Contact contact = new Contact(contact_id, contact_name, phone, phoneId, profilePic);
            hashMapContact.put(contact_id + phone, contact);
        }
        getAllContactCompare(context, hashMapDB, hashMapContact, bufferChange);

        cursor.close();
        for (Map.Entry<String, Contact> entry : hashMapContact.entrySet()) {
            String key = entry.getKey();
            if (!hashMapDB.containsKey(key)) {

                bufferChange.append(entry.getValue().getPhone());
                bufferChange.append("|");
                bufferChange.append(entry.getValue().getContact_name());
                bufferChange.append("|");
                bufferChange.append("Inser");
                bufferChange.append("-;-");
                list.add(entry.getValue());

            }

        }
        onPostExecute();
    }

    public void getAllContactCompare(Context context, HashMap<String, String> hashMapDB,
                                     HashMap<String, Contact> hashMapContact, StringBuffer bufferContacts) {

        for (int i = 0; i < list.size(); i++) {

            String contactId = list.get(i).getContact_id();
            String phone = list.get(i).getPhone();
            String key = contactId + phone;
            String contactName1 = list.get(i).getContact_name();

            if (!hashMapContact.containsKey(key)) {
                // deleteById(context, id);
                bufferContacts.append(phone);
                bufferContacts.append("|");
                bufferContacts.append(contactName1);
                bufferContacts.append("|");
                bufferContacts.append("Delete");
                bufferContacts.append("-;-");
            } else {
                Contact contact = hashMapContact.get(key);
                if (!compareTwoString(contact.getContact_name(), contactName1)) {
                    // update

                    bufferContacts.append(phone);
                    bufferContacts.append("|");
                    bufferContacts.append(contact.getContact_name());
                    bufferContacts.append("|");
                    bufferContacts.append("Update name");
                    bufferContacts.append("-;-");
                    list.get(i).setContact_name(contact.getContact_name());
                }
                if (!compareTwoString(contact.getProfilePic(), list.get(i).getProfilePic())) {
                    // update
                    bufferContacts.append(phone);
                    bufferContacts.append("|");
                    bufferContacts.append(contact.getProfilePic());
                    bufferContacts.append("|");
                    bufferContacts.append("Update pic");
                    bufferContacts.append("-;-");
                    list.get(i).setProfilePic(contact.getProfilePic());
                }
                hashMapDB.put(key, contact.getContact_name());
            }
        }
    }


    protected void onPostExecute() {
        if (bufferChange.length() > 0) {
            final String dataContacts = bufferChange.substring(0, bufferChange.length() - 1);
            ((Activity)context).runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(context, dataContacts, Toast.LENGTH_LONG).show();
                    adapter.notifyDataSetChanged();

                }
            });


        }

    }

    public static boolean compareTwoString(String s1, String s2) {
        if (s1 == null) {
            if (s2 == null) {
                return true;
            } else {
                return false;
            }
        } else {
            if (s2 == null) {
                return false;
            } else {
                return s1.equals(s2);
            }

        }
    }
}
