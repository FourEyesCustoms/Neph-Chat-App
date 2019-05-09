package com.example.nephapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID;

    //Widgets
    private CircleImageView userProfileImage;
    private TextView userProfileName;
    private TextView userProfileStatus;
    private Button sendMessageRequestButton;

    //Variables
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        receiverUserID = Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("visitUserID")).toString();

        userProfileImage = (CircleImageView)findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_user_status);
        sendMessageRequestButton = findViewById(R.id.send_message_request_button);

        retrieveUserInfo();
    }

    private void retrieveUserInfo() {
        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("images"))) {
                    String userImage = dataSnapshot.child("images").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage)
                            .placeholder(R.drawable.profile)
                            .into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                }
                else {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}




