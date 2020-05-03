package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {


    private Button loginBtn,PhoneLoginBtn;
    private TextView NeedNewAcc,ForgetPassword;
    private EditText Useremail,UserPassword;

    private FirebaseAuth mAuth;
    private ProgressDialog LoadingBar;
    private DatabaseReference UsersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        mAuth =FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        initializeFields();



        NeedNewAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                sendUserToRegisterActivity();
            }
        });
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                AllowUserToLogin();
            }
        });

        PhoneLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent PhoneLoginIntent = new Intent(LoginActivity.this,PhoneLoginActivity.class);
                startActivity(PhoneLoginIntent);

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null)
        {
            sendUserToMainActivity();
        }
    }

    private void AllowUserToLogin()
    {
        String email = Useremail.getText().toString();
        String password =UserPassword.getText().toString();

        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(LoginActivity.this,"Please Enter an Email",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty((password)))
        {
            Toast.makeText(LoginActivity.this,"Please Enter your Password",Toast.LENGTH_SHORT).show();
        }
        else
        {
            LoadingBar.setTitle("Sign In");
            LoadingBar.setMessage("Please Wait");
            LoadingBar.setCanceledOnTouchOutside(true);
            LoadingBar.show();

            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                String currentUserId = mAuth.getCurrentUser().getUid();
                                String deviceToken  = FirebaseInstanceId.getInstance().getToken();
                                UsersRef.child(currentUserId).child("device_Token")
                                        .setValue(deviceToken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if(task.isSuccessful())
                                                {
                                                    sendUserToMainActivity();
                                                    Toast.makeText(LoginActivity.this,"LoggedIn Successful",Toast.LENGTH_SHORT).show();
                                                    LoadingBar.dismiss();

                                                }

                                            }
                                        });


                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(LoginActivity.this,"Error" + message,Toast.LENGTH_SHORT).show();
                                LoadingBar.dismiss();
                            }

                        }
                    });
        }
    }

    private void initializeFields()
    {

        loginBtn = (Button)findViewById(R.id.Login_button);
        PhoneLoginBtn = (Button)findViewById(R.id.Phone_login);
        loginBtn = (Button)findViewById(R.id.Login_button);
        NeedNewAcc = (TextView) findViewById(R.id.Need_New_Account);
        ForgetPassword = (TextView)findViewById(R.id.Forget_Password);
        Useremail = (EditText)findViewById(R.id.Login_Email);
        UserPassword = (EditText)findViewById(R.id.Login_Password);
        LoadingBar = new ProgressDialog(this);

    }

    private void sendUserToMainActivity() {
        Intent MainIntent = new Intent(LoginActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }

    private void sendUserToRegisterActivity()
    {
        Intent RegisterIntent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(RegisterIntent);
    }
}
