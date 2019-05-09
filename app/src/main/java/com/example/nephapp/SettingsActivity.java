package com.example.nephapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity
{
    private static final String TAG = "SettingsActivity";
    private static final int REQUEST_CODE=1234;

    //Widgets
    private CircleImageView accountImage;
    private EditText userName;
    private EditText userStatus;
    private Button btnSetupAccount;
    private Uri mainImageURI=null;

    //Variables
    private ProgressDialog progressDialog;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef;
    private String currentUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        hideSoftKeyboard();

        firebaseAuth=FirebaseAuth.getInstance();
        currentUserID=firebaseAuth.getCurrentUser().getUid();
        storageReference= FirebaseStorage.getInstance().getReference();
        rootRef= FirebaseDatabase.getInstance().getReference();

        accountImage=findViewById(R.id.set_profile_image);
        userName=findViewById(R.id.set_user_name);
        userStatus=findViewById(R.id.set_profile_status);
        btnSetupAccount=findViewById(R.id.btn_update_settings);
        progressDialog=new ProgressDialog(this);

        btnSetupAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
             /*   progressDialog.setTitle("Updating settings");
                progressDialog.setMessage("The changes are being updated");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();*/
                updateAccountSettings();
                String user_name= userName.getText().toString();
                String user_status=userStatus.getText().toString();
                if (!TextUtils.isEmpty(user_name)&&!TextUtils.isEmpty(user_status)) {
                    StorageReference image_path=storageReference.child("profile_images").child(currentUserID+".jpg");
                    image_path.putFile(mainImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()){
                                final String download_uri= Objects.requireNonNull(task.getResult().getDownloadUrl()).toString();
                                rootRef.child("Users").child(currentUserID).child("images")
                                        .setValue(download_uri)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                  //  progressDialog.dismiss();
                                                    toastMessage("Image saved to the database");
                                                }
                                                else {
                                                  //  progressDialog.dismiss();
                                                    String message= Objects.requireNonNull(task.getException()).toString();
                                                    toastMessage(message);
                                                }
                                            }
                                        });
                                toastMessage("Image is uploaded");
                            }
                            else {
                                String error= Objects.requireNonNull(task.getException()).getMessage();
                                toastMessage("Error: "+error);
                            }
                        }
                    });
                }
            }
        });


        accountImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    if (ContextCompat.checkSelfPermission(SettingsActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(SettingsActivity.this,
                                new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_CODE);
                    }
                    else {
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(SettingsActivity.this);
                    }
                }
                else {
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1,1)
                            .start(SettingsActivity.this);
                }
            }
        });

        retrieveUserInfo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageURI=result.getUri();
                accountImage.setImageURI(mainImageURI);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                toastMessage("Error: "+error);
            }
        }
    }

    private void updateAccountSettings(){
        String setUserName=userName.getText().toString();
        String setStatus=userStatus.getText().toString();
        HashMap<String, String> profileMap=new HashMap<>();
        profileMap.put("uid",currentUserID);
        profileMap.put("name",setUserName);
        profileMap.put("status",setStatus);

        rootRef.child("Users").child(currentUserID).setValue(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    toastMessage("Profile updated successfully");
                    sendUserToMainActivity();
                }
                else {
                    String message= Objects.requireNonNull(task.getException()).toString();
                    toastMessage(message);
                }
            }
        });

    }

    private void retrieveUserInfo(){
        rootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if ((dataSnapshot.exists())&&(dataSnapshot.hasChild("name")&&(dataSnapshot.hasChild("images")))){
                            String retrieveUserName= Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                            String retrieveStatus= Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();
                            String retrieveProfileImage= Objects.requireNonNull(dataSnapshot.child("images").getValue()).toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
                            Picasso.get().load(retrieveProfileImage).into(accountImage);
                        }
                        else if ((dataSnapshot.exists())&&(dataSnapshot.hasChild("name"))){
                            String retrieveUserName= Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                            String retrieveStatus= Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
                        }
                        else {
                            toastMessage("Update your profile information");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent intent=new Intent(SettingsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void toastMessage(String message) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}






