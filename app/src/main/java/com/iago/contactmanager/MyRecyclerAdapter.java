package com.iago.contactmanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ankurkumar on 17/09/15.
 */
public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> implements View.OnClickListener {

    List<ContactItem> mContacts;

    Context mContext;
    private ListListener mListener;

    public MyRecyclerAdapter(Context context, List<ContactItem> aContacts) {

        mContext = context;
        mContacts = aContacts;
    }

    @Override
    public void onClick(View view) {
        if (view.getTag() != null
                && view.getTag() instanceof ContactItem) {
            final ContactItem item = (ContactItem) view.getTag();
            // 1. Instantiate an AlertDialog.Builder with its constructor
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

            // 2. Chain together various setter methods to set the dialog characteristics
            builder.setMessage(mContext.getString(R.string.deleteCM, item.mName))
                    .setTitle(R.string.deleteC);

            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (mListener != null) {
                        mListener.onDeleteClicked(item);
                    }
                }
            });

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            // 3. Get the AlertDialog from create()
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameTV;
        ImageView deleteButton;
        LinearLayout phonesList;
        LinearLayout emailList;
        List<TextView> phoneTVs = new ArrayList<TextView>();
        List<TextView> emailTVs = new ArrayList<TextView>();

        public ViewHolder(View itemView) {
            super(itemView);
            nameTV = (TextView) itemView.findViewById(R.id.nameTV);
            phonesList = (LinearLayout) itemView.findViewById(R.id.phoneList);
            emailList = (LinearLayout) itemView.findViewById(R.id.emailList);
            deleteButton = (ImageView) itemView.findViewById(R.id.deleteIC);
        }
    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ContactItem item = mContacts.get(position);

        holder.nameTV.setText(item.mName);
        for (int i = 0; i < item.getPhones().size(); i++) {
            holder.phoneTVs.get(i).setText(item.getPhones().get(i));
        }
        for (int i = 0; i < item.getEmails().size(); i++) {
            holder.emailTVs.get(i).setText(item.getEmails().get(i));
        }
        holder.deleteButton.setTag(item);
        holder.deleteButton.setOnClickListener(this);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);

        ViewHolder vh = new ViewHolder(v);

        int phoneCount = viewType & 0x000F;
        int emailCount = viewType >> 4;

        for (int i = 0; i < phoneCount; i++) {
            TextView tv = (TextView) LayoutInflater.from(mContext)
                    .inflate(R.layout.phone_email_item, null)
                    .findViewById(R.id.phone_email_tv);
            vh.phonesList.addView(tv);
            vh.phoneTVs.add(tv);
        }

        for (int i = 0; i < emailCount; i++) {
            TextView tv = (TextView) LayoutInflater.from(mContext)
                    .inflate(R.layout.phone_email_item, null)
                    .findViewById(R.id.phone_email_tv);
            vh.emailList.addView(tv);
            vh.emailTVs.add(tv);
        }

        return vh;
    }

    @Override
    public int getItemViewType(int position) {
        ContactItem item = mContacts.get(position);
        int type = item.getPhoneCount() | (item.getEmailCount() << 4);
        return type;
    }

    public void setListener(ListListener listener) {
        mListener = listener;
    }
}