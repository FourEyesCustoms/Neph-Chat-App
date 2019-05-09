package com.example.nephapp.Chats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import android.widget.Toast;

import com.example.nephapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity
{

    private static final String TAG = "GroupChatActivity";

    //Widgets
    private Toolbar mToolbar;
    private ImageButton mImageButton;
    private EditText mUserMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;

    private String currentGroupName,currentUserID,currentUserName, currentDate, currentTime;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, groupNameRef,groupMessageKeyRef;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        hideSoftKeyboard();

        //Firebase

        currentGroupName=getIntent().getExtras().get("groupName").toString();
        //Display Toast
        toastMessage(currentGroupName);
        //Get Current user
        mAuth=FirebaseAuth.getInstance();
        //Get User ID
        currentUserID=mAuth.getCurrentUser().getUid();
        //Database reference
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        initializeFields();
        getUserInfo();
        mImageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendMessageToDatabase();
                mUserMessageInput.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private void initializeFields()
    {
        mToolbar=findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        mImageButton=findViewById(R.id.send_message_button);
        mUserMessageInput=findViewById(R.id.input_group_message);
        mScrollView=findViewById(R.id.my_scroll_view);
        displayTextMessages=findViewById(R.id.group_chat_display);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        groupNameRef.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                if (dataSnapshot.exists())
                {
                    displayGroupMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                if (dataSnapshot.exists())
                {
                    displayGroupMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
            {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void getUserInfo()
    {

        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    currentUserName=dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void sendMessageToDatabase()
    {
        String messageKey= groupNameRef.push().getKey();
        String message=mUserMessageInput.getText().toString();
        if (!TextUtils.isEmpty(message))
        {
            Calendar messageDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat= new SimpleDateFormat("MMM dd,yyyy");
            currentDate=currentDateFormat.format(messageDate.getTime());

            Calendar messageTime=Calendar.getInstance();
            SimpleDateFormat currentTimeFormat=new SimpleDateFormat("hh:mm a");
            currentTime=currentTimeFormat.format(messageTime.getTime());

            HashMap<String ,Object> groupMessageKey=new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);
            groupMessageKeyRef=groupNameRef.child(messageKey);

            HashMap<String,Object>messageInfoMap= new HashMap<>();
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            groupMessageKeyRef.updateChildren(messageInfoMap);

        }
        else
        {
            toastMessage("Please Write a message");
        }
    }

    private void displayGroupMessages(DataSnapshot dataSnapshot)
    {
        Iterator iterator=dataSnapshot.getChildren().iterator();
        while (iterator.hasNext())
        {
            String chatDate=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatName=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime=(String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessages.append(chatName+ ":\n" +chatMessage + "\n" +chatTime+"    "+chatDate+ "\n\n");
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    private void toastMessage(String message)
    {
        Toast.makeText(GroupChatActivity.this,message,Toast.LENGTH_SHORT).show();
    }

    private void hideSoftKeyboard()
    {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
