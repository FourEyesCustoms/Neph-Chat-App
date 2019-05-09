package com.example.nephapp.UserAuthentication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity
{
    private static final String TAG = "LoginActivity";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDb;

    //Widgets
    private Button btnLogin;
    private Button btnPhoneLogin;
    private EditText loginEmail;
    private EditText loginPassword;
    private TextView forgotPassword;
    private TextView registerNewAccount;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        hideSoftKeyboard();

        //Initialize firebase variables
        mAuth=FirebaseAuth.getInstance();
        mDb=FirebaseDatabase.getInstance();

        //Initialize fields
        initializeFields();

        registerNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                sendUserToRegisterActivity();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
               allowUserToLogin();
            }
        });

        btnPhoneLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToMobilePhoneLoginActivity();
            }
        });

    }

    private void initializeFields()
    {
        //Initialize fields
        btnLogin=findViewById(R.id.login_button);
        btnPhoneLogin=findViewById(R.id.btn_phone_login);
        loginEmail=findViewById(R.id.login_email);
        loginPassword=findViewById(R.id.login_password);
        forgotPassword=findViewById(R.id.forgot_password_link);
        registerNewAccount=findViewById(R.id.no_account);

        progressDialog=new ProgressDialog(this);
    }

    private void allowUserToLogin()
    {

        String email=loginEmail.getText().toString();
        String password=loginPassword.getText().toString();

        if (!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password))
        {
            progressDialog.setTitle("Sign In");
            progressDialog.setMessage("Signing in to your account...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {
                                Log.d(TAG, "onComplete: Logging in...");
                                sendUserToMainActivity();
                                progressDialog.dismiss();
                            }
                            else
                            {
                                Log.d(TAG, "onComplete: Failed to login");
                                String errorMessage= Objects.requireNonNull(task.getException()).getMessage();
                                toastMessage("Error" +errorMessage);
                                progressDialog.dismiss();
                            }
                        }
                    });
        }
        else
        {
            toastMessage("Fill out all fields");
        }
    }


    private void sendUserToMainActivity()
    {
        Intent intent=new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    private void sendUserToRegisterActivity()
    {
        Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(intent);
    }

    private void sendUserToMobilePhoneLoginActivity()
    {
        Intent intent=new Intent(LoginActivity.this,MobilePhoneLoginActivity.class);
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
