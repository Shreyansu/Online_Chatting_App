package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity {
    private Button createAccBtn;
    private EditText Registeremail, RegisterPassword;
    private TextView AlreadyAcc;
    private FirebaseAuth mAuth;
    private ProgressDialog LoadingBar;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        initializeFields();

        AlreadyAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLoginActivity();
            }
        });
        createAccBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createNewAccount();
            }
        });
    }

    private void createNewAccount()
    {
        String email = Registeremail.getText().toString();
        String password =RegisterPassword.getText().toString();

        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(RegisterActivity.this,"Please Enter an Email",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(RegisterActivity.this,"Please Enter your Password",Toast.LENGTH_SHORT).show();
        }
        else
        {
            LoadingBar.setTitle("Creating New Account");
            LoadingBar.setMessage("PlEase wait while we are Creating Your Account");
            LoadingBar.setCanceledOnTouchOutside(true);
            LoadingBar.show();


            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                String deviceToken  = FirebaseInstanceId.getInstance().getToken();
                                String CurrentUserID = mAuth.getCurrentUser().getUid();
                                RootRef.child("Users").child(CurrentUserID).setValue("");

                                RootRef.child("Users").child(CurrentUserID).child("device_Token")
                                        .setValue(deviceToken);
                                sendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this,"Account created SuccessFully",Toast.LENGTH_SHORT).show();
                                LoadingBar.dismiss();
                            }
                            else
                            {

                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this,"Error" + message,Toast.LENGTH_SHORT).show();
                                LoadingBar.dismiss();
                            }

                        }
                    });
        }
    }

    private void initializeFields() {
        createAccBtn = (Button) findViewById(R.id.register_button);
        Registeremail = (EditText) findViewById(R.id.register_Email);
        RegisterPassword = (EditText) findViewById(R.id.signup_password);
        AlreadyAcc = (TextView) findViewById(R.id.already_Account_btn);

        LoadingBar = new ProgressDialog(this);
    }

    private void sendUserToLoginActivity() {
        Intent LoginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(LoginIntent);
    }

    private void sendUserToMainActivity() {
        Intent MainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }

}
