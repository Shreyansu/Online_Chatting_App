package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity
{
    private Button SendVerificationCodeButton,VerifyBtn;
    private EditText InputPhoneNumber, InputVerificationCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;

    private ProgressDialog LoadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);


        SendVerificationCodeButton = (Button)findViewById(R.id.send_ver_code_button);
        VerifyBtn = (Button)findViewById(R.id.Verify_button);
        InputPhoneNumber = (EditText)findViewById(R.id.Phone_number_input);
        InputVerificationCode = (EditText)findViewById(R.id.Verification_code_input);
        mAuth = FirebaseAuth.getInstance();
        LoadingBar = new ProgressDialog(this);

        SendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String PhoneNumber = InputPhoneNumber.getText().toString();

                if(TextUtils.isEmpty(PhoneNumber))
                {
                    Toast.makeText(PhoneLoginActivity.this," Phone Number is Required",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    LoadingBar.setTitle("Phone Verification");
                    LoadingBar.setMessage("Please Wait...while we are authenticating your Phone");
                    LoadingBar.setCanceledOnTouchOutside(false);
                    LoadingBar.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            PhoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks

                }
            }
        });
        VerifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                String VerificationCode = InputVerificationCode.getText().toString();
                if(TextUtils.isEmpty(VerificationCode))
                {
                    Toast.makeText(PhoneLoginActivity.this,"please Enter Verification Code",Toast.LENGTH_SHORT).show();
                }
                else
                {

                    LoadingBar.setTitle("Code Verification");
                    LoadingBar.setMessage("Please Wait...while we are Verifying your Code");
                    LoadingBar.setCanceledOnTouchOutside(false);
                    LoadingBar.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, VerificationCode);
                    signInWithPhoneAuthCredential(credential);

                }
            }
        });




        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential)
            {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e)
            {
                Toast.makeText(PhoneLoginActivity.this,"Invalid ....please Enter correct phone Number",Toast.LENGTH_SHORT).show();

                SendVerificationCodeButton.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);

                VerifyBtn.setVisibility(View.INVISIBLE);
                InputVerificationCode.setVisibility(View.INVISIBLE);
            }

            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {


                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                Toast.makeText(PhoneLoginActivity.this," Code has been sent...Please check and Verify",Toast.LENGTH_SHORT).show();


                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                VerifyBtn.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);


            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            LoadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this,"Logged In Successfully",Toast.LENGTH_SHORT).show();
                            senduserToMainActivity();

                        }
                        else
                        {
                            String Message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this,"Error : " + Message,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void senduserToMainActivity()
    {
        Intent MainIntent = new Intent(PhoneLoginActivity.this,MainActivity.class);
        startActivity(MainIntent);
        finish();
    }

}
