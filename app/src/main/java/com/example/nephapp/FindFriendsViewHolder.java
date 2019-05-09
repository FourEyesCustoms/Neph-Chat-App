package com.example.nephapp;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsViewHolder extends RecyclerView.ViewHolder
{

    TextView userName, userStatus;
    CircleImageView profileImage;
    public FindFriendsViewHolder(@NonNull View itemView)
    {
        super(itemView);

        userName=itemView.findViewById(R.id.user_profile_name);
        userStatus=itemView.findViewById(R.id.user_status);
        profileImage=itemView.findViewById(R.id.users_profile_image);

    }
}
