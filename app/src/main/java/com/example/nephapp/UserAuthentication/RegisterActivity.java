package com.example.nephapp.UserAuthentication;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nephapp.MainActivity;
import com.example.nephapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    //Widgets
    private EditText registerEmail;
    private EditText newPassword;
    private EditText confirmPassword;
    private TextView linkLogin;
    private Button regButton;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        hideSoftKeyboard();

        //Initialize firebase variables
        mAuth=FirebaseAuth.getInstance();
        rootRef=FirebaseDatabase.getInstance().getReference();

        initializeFields();

        linkLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendUserToLoginActivity();
            }
        });

        regButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                createNewAccount();
            }
        });
    }

    private void createNewAccount()
    {
        String email=registerEmail.getText().toString();
        String password=newPassword.getText().toString();
        String confirmPass=confirmPassword.getText().toString();

        if (!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password)
            &&!TextUtils.isEmpty(confirmPass))
        {
            if (password.equals(confirmPass))
            {
                Log.d(TAG, "createNewAccount: Creating new account");
                progressDialog.setTitle("Creating new Account");
                progressDialog.setMessage("We are getting things ready for you");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            Log.d(TAG, "onComplete: Account created successfully");
                            String userID=mAuth.getCurrentUser().getUid();
                            rootRef.child("Users").child(userID).setValue("");
                            sendUserToMainActivity();
                            toastMessage("Your account was created successfully");
                            progressDialog.dismiss();
                        }
                        else
                        {
                            Log.d(TAG, "onComplete: Something went wrong");
                            String errorMessage= Objects.requireNonNull(task.getException()).getMessage();
                            toastMessage("Error" +errorMessage);
                            progressDialog.dismiss();
                        }
                    }
                });
            }
            else
            {
                toastMessage("Passwords Fields do not match");
            }
        }
        else
        {
            toastMessage("Please fill out all the Fields");
        }
    }

    private void initializeFields()
    {
        registerEmail=findViewById(R.id.register_email);
        newPassword=findViewById(R.id.register_password);
        confirmPassword=findViewById(R.id.confirm_pass);
        regButton=findViewById(R.id.register_button);
        linkLogin=findViewById(R.id.existing_account);

        progressDialog=new ProgressDialog(this);
    }

    private void sendUserToLoginActivity()
    {
        Intent intent= new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }
    private void sendUserToMainActivity()
    {
        Intent intent=new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
