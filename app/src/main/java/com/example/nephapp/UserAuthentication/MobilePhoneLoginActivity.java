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
import android.widget.Toast;

import com.example.nephapp.MainActivity;
import com.example.nephapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MobilePhoneLoginActivity extends AppCompatActivity
{

    private static final String TAG = "MobilePhoneLogin";
    private ProgressDialog progressDialog;
    //Widgets
    private EditText enterPhoneNumber;
    private EditText inputVerificationCode;
    private Button btnSendVerificationCode;
    private Button btnVerify;

    //Firebase
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_phone_login);

        mAuth=FirebaseAuth.getInstance();

        hideSoftKeyboard();
        initializeFields();

        btnSendVerificationCode.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String phoneNumber=enterPhoneNumber.getText().toString();
                if (!TextUtils.isEmpty(phoneNumber))
                {
                    progressDialog.setTitle("Phone Verification");
                    progressDialog.setMessage("Wait as your phone is being authenticated");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            MobilePhoneLoginActivity.this,  // Activity (for callback binding)
                            mCallbacks);        // OnVerificationStateChangedCallbacks
                }
                else
                {
                    toastMessage("Please Enter Phone Number");
                }
            }
        });

        btnVerify.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                btnSendVerificationCode.setVisibility(View.INVISIBLE);
                enterPhoneNumber.setVisibility(View.INVISIBLE);
                String verificationCode= inputVerificationCode.getText().toString();
                if (!TextUtils.isEmpty(verificationCode))
                {
                    progressDialog.setTitle("Phone Verification");
                    progressDialog.setMessage("Wait as your phone is being authenticated");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
                else
                {
                    toastMessage("Enter the verification code");
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
        {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential)
            {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e)
            {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);
                progressDialog.dismiss();
                toastMessage("Invalid phone number. Please Include the country code");
                btnSendVerificationCode.setVisibility(View.VISIBLE);
                enterPhoneNumber.setVisibility(View.VISIBLE);
                btnVerify.setVisibility(View.INVISIBLE);
                inputVerificationCode.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token)
            {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                progressDialog.dismiss();
                toastMessage("Verification code has been sent");

                btnSendVerificationCode.setVisibility(View.INVISIBLE);
                enterPhoneNumber.setVisibility(View.INVISIBLE);
                btnVerify.setVisibility(View.VISIBLE);
                inputVerificationCode.setVisibility(View.VISIBLE);
                // ...
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            progressDialog.dismiss();
                            toastMessage("Logged in successfully");
                            sendUserToMainActivity();

                        } else
                            {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            String exception = Objects.requireNonNull(task.getException()).toString();
                            toastMessage("Error: "+exception);
                        }
                    }
                });
    }

    private void initializeFields()
    {
        enterPhoneNumber=findViewById(R.id.input_phone_number);
        inputVerificationCode=findViewById(R.id.input_verification_code);
        btnSendVerificationCode=findViewById(R.id.btn_send_verification_code);
        btnVerify=findViewById(R.id.btn_verify_phone);
        progressDialog=new ProgressDialog(this);
    }

    private void sendUserToMainActivity()
    {
        Intent intent=new Intent(MobilePhoneLoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void hideSoftKeyboard()
    {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void toastMessage(String message)
    {
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }
}
