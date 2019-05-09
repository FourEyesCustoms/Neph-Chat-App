package com.example.nephapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.nephapp.models.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FindFriendsActivity extends AppCompatActivity {

    private static final String TAG = "FindFriendsActivity";

    //Widgets
    private Toolbar mToolbar;
    private RecyclerView mRecyclerList;

    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        hideSoftInputKeyboard();

        userRef= FirebaseDatabase.getInstance().getReference().child("Users");

        mRecyclerList=findViewById(R.id.find_friends_recycler_list);
        mRecyclerList.setLayoutManager(new LinearLayoutManager(this));

        mToolbar=findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");
    }

    @Override
    protected void onStart()
    {
        Log.d(TAG, "onStart: Retrieving friends list from database");
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(userRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,FindFriendsViewHolder> adapter=
                new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder,
                                                    final int position, @NonNull Contacts model)
                    {
                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());
                        Picasso.get()
                                .load(model.getImages())
                                .placeholder(R.drawable.profile)
                                .into(holder.profileImage);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                String visitUserID=getRef(position).getKey();
                                Intent intent=new Intent(FindFriendsActivity.this, ProfileActivity.class);
                                intent.putExtra("visitUserID",visitUserID);
                                startActivity(intent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view= LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.display_users_layout,parent,false);
                        FindFriendsViewHolder viewHolder=new FindFriendsViewHolder(view);
                        return viewHolder;
                    }
                };

        mRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }


    private void toastMessage(String message)
    {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
    private void hideSoftInputKeyboard()
    {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

}


