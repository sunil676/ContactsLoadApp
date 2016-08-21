package com.sunil.contactsloadapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunil.contactsloadapp.R;
import com.sunil.contactsloadapp.model.Contact;

import java.util.List;

/**
 * Created by sunil on 20-Aug-16.
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder>{

    private List<Contact> mListContacts;
    private Context mContext;
   // private final ItemFilter itemFilter = new ItemFilter();
    private List<Contact> filteredList;


    public ContactAdapter(Context context, List<Contact> list){
        mListContacts = list;
        mContext = context;
        filteredList = mListContacts;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contacts_list_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        Contact contact = mListContacts.get(position);
        holder.mNameTv.setText(contact.getContact_name());
        holder.mPhoneTv.setText(contact.getPhone());
        if (contact.getContact_name()!= null && !contact.getContact_name().isEmpty()){
            holder.mShortName.setText(contact.getContact_name().substring(0,1));
            holder.mProfileView.setVisibility(View.GONE);
            holder.mRelative.setVisibility(View.VISIBLE);
        }

        if (contact.getProfilePic()!= null && !contact.getProfilePic().isEmpty()){
           holder.mProfileView.setImageURI(Uri.parse(contact.getProfilePic()));
            holder.mProfileView.setVisibility(View.VISIBLE);
            holder.mRelative.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mListContacts.size();
    }

    /*@Override
    public Filter getFilter() {
        return itemFilter;
    }*/

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView mNameTv, mPhoneTv, mShortName;
        ImageView mProfileView;
        RelativeLayout mRelative;

        public MyViewHolder(View view) {
            super(view);
            mNameTv = (TextView) view.findViewById(R.id.name);
            mPhoneTv = (TextView) view.findViewById(R.id.phone);
            mShortName = (TextView)view.findViewById(R.id.initials);
            mProfileView = (ImageView)view.findViewById(R.id.profilePic);
            mRelative = (RelativeLayout)view.findViewById(R.id.icon);
        }
    }

   /* private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<Contact> list = mListContacts;

            int count = list.size();
            final ArrayList<Contact> nlist = new ArrayList<Contact>(count);

            for (Contact filteredSite : list) {
                final String filteredSiteName = filteredSite.getContact_name();
                if (filteredSiteName.toLowerCase(Locale.getDefault()).contains(filterString)) {
                    nlist.add(filteredSite);
                }
            }
            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked") @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList = (ArrayList<Contact>) results.values;
            notifyDataSetChanged();
        }
    }
*/

    public void animateTo(List<Contact> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<Contact> newModels) {
        for (int i = mListContacts.size() - 1; i >= 0; i--) {
            final Contact model = mListContacts.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<Contact> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final Contact model = newModels.get(i);
            if (!mListContacts.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<Contact> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final Contact model = newModels.get(toPosition);
            final int fromPosition = mListContacts.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public Contact removeItem(int position) {
        final Contact model = mListContacts.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, Contact model) {
        mListContacts.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final Contact model = mListContacts.remove(fromPosition);
        mListContacts.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

}
