package com.example.nephapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nephapp.Adapters.TabsAdapter;
import com.example.nephapp.UserAuthentication.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    //objects to access different classes
    private TabsAdapter mTabsAdapter;

    //firebase
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        rootRef= FirebaseDatabase.getInstance().getReference();

        mToolbar=findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Neph ChatApp");
        Log.d(TAG, "onCreate: App has started");

        mViewPager=findViewById(R.id.main_tabs_pager);
        mTabsAdapter=new TabsAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTabsAdapter);

        mTabLayout=findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
       super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
         super.onOptionsItemSelected(item);

         if (item.getItemId()==R.id.logout_option)
         {
             mAuth.signOut();
             sendUserToLoginActivity();
         }
        if (item.getItemId()==R.id.settings_option)
        {
            sendUserToSettingsActivity();
        }
        if (item.getItemId()==R.id.find_friends_option)
        {
            sendUserToFindFriendsActivity();
        }
        if (item.getItemId()==R.id.create_group_option)
        {
            requestNewGroup();
        }

        return true;
    }

    private void requestNewGroup()
    {
        hideSoftKeyboard();
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name:");

        final EditText groupNameField=  new EditText(MainActivity.this);
        groupNameField.setHint("e.g TeOH Kenya");
        builder.setView(groupNameField);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String groupName=groupNameField.getText().toString();

                if (!TextUtils.isEmpty(groupName))
                {
                    createNewGroup(groupName);
                }
                else
                {
                    toastMessage("Please enter a group name");
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void createNewGroup(final String groupName)
    {
        hideSoftKeyboard();
        rootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            hideSoftKeyboard();
                            toastMessage(groupName +" Created Successfully");
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser==null)
        {
            sendUserToLoginActivity();
        }
        else
        {
            verifyUserExistence();
        }
    }

    private void verifyUserExistence()
    {
        String currentUserID=mAuth.getCurrentUser().getUid();
        rootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if ((dataSnapshot.child("name").exists()))
                {
                   /* toastMessage("Welcome...");*/
                    Log.d(TAG, "onDataChange: User Exists");
                }
                else
                {
                    sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToLoginActivity()
    {
        Intent intent=new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendUserToSettingsActivity()
    {
        Intent intent=new Intent(MainActivity.this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendUserToFindFriendsActivity()
    {
        Intent intent=new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(intent);
    }

    private void toastMessage(String message)
    {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    private void hideSoftKeyboard()
    {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
