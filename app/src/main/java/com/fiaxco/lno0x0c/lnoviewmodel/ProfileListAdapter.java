package com.fiaxco.lno0x0c.lnoviewmodel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fiaxco.lno0x0c.R;
import com.fiaxco.lno0x0c.roomstuff.Profile;
import com.fiaxco.lno0x0c.roomstuff.ProfileContract;

import java.util.Collections;
import java.util.List;

public class ProfileListAdapter extends RecyclerView.Adapter<ProfileListAdapter.ProfileViewHolder> {

    private final LayoutInflater mLayoutInflater;
    private List<Profile> mAllProfiles;
    private OnProfileListener mOnProfileListener;

    // Constructor
    public ProfileListAdapter(Context context, OnProfileListener onProfileListener) {
        mLayoutInflater = LayoutInflater.from(context);
        this.mOnProfileListener = onProfileListener;
    }

    // 1 View holder for a single item in recycler view
    public static class ProfileViewHolder
            extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView listItemName, listItemAge, listItemGender;
        OnProfileListener onProfileListener;

        public ProfileViewHolder(@NonNull View itemView, OnProfileListener onProfileListener) {
            super(itemView);
            listItemName = itemView.findViewById(R.id.list_item_text_view_name);
            listItemAge = itemView.findViewById(R.id.list_item_text_view_age);
            listItemGender = itemView.findViewById(R.id.list_item_text_view_gender);

            itemView.setOnClickListener(this);
            this.onProfileListener = onProfileListener;
        }

        @Override
        public void onClick(View v) {
            onProfileListener.onProfileClick(getAdapterPosition());
        }
    }


    // 2 Inflate a list item layout and return a view holder
    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = mLayoutInflater.inflate(R.layout.recyclerview_listitem, parent, false);
        return new ProfileViewHolder(itemView, mOnProfileListener);
    }

    // 3 Set values to the list item view children
    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {

        if (mAllProfiles != null) {
            Profile currentProfile = mAllProfiles.get(position);
            holder.listItemName
                    .setText(currentProfile.mName);
            holder.listItemAge
                    .setText(String.format("%s years   |", currentProfile.mAge.toString()));
            holder.listItemGender
                    .setText(ProfileContract.ProfileEntry.genderType(currentProfile.mGender));
        }
    }

    // Pass the list of profile to the adapter
    public void setProfile(List<Profile> profiles) {
        Collections.reverse(profiles);
        mAllProfiles = profiles;
        notifyDataSetChanged();
    }

    // method to get a profile at a position
    public Profile getProfileAtPosition(int position) {
        if(mAllProfiles != null) {
            return mAllProfiles.get(position);
        } else {
            return null;
        }
    }

    @Override
    public int getItemCount() {
        if (mAllProfiles != null) {
            return mAllProfiles.size();
        } else {
            return 0;
        }
    }

    // click listener for the recycler view list item
    public interface OnProfileListener {
        void onProfileClick(int position);
    }


}
