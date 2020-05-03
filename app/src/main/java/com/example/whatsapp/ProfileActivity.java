package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private String recieverUserId,senderUserId,  currentState;
    private CircleImageView userProfileImage;
    private TextView UserProfileName,UserProfileStatus;
    private Button SendMessagerequestButton,DeclineMessageRequestButton;
    private DatabaseReference UserRef,chatRequestRef,contactRef,notificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requsets");
        contactRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef= FirebaseDatabase.getInstance().getReference().child("Notification");

        userProfileImage = (CircleImageView)findViewById(R.id.visit_profile_image);
        UserProfileName = (TextView) findViewById(R.id.visit_user_name);
        UserProfileStatus = (TextView)findViewById(R.id.visit_user_status);
        SendMessagerequestButton = (Button) findViewById(R.id.send_message_request_button);
        DeclineMessageRequestButton = (Button) findViewById(R.id.decline_message_request_button);
        currentState = "new";
        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();

        recieverUserId = getIntent().getExtras().get("visit_user_id").toString();

        retrieveUserInfo();

    }

    private void ManageChatRequests()
    {
        chatRequestRef.child(senderUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.hasChild(recieverUserId))
                        {
                            String request_type = dataSnapshot.child(recieverUserId).child("Request Type").getValue().toString();

                            if (request_type.equals("sent"))
                            {
                                currentState ="request_sent";
                                SendMessagerequestButton.setText("Cancel Chat Request");
                            }
                            else if (request_type.equals("recieved"))
                            {
                                currentState = "request_received";
                                SendMessagerequestButton.setText("Accept Chat Request");

                                DeclineMessageRequestButton.setVisibility(View.VISIBLE);
                                DeclineMessageRequestButton.setEnabled(true);

                                DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        }
                        else
                        {
                            contactRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                        {
                                            if(dataSnapshot.hasChild(recieverUserId))
                                            {
                                                currentState = "friends";
                                                SendMessagerequestButton.setText("Remove This Contact");
                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError)
                                        {


                                        }
                                    });
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



        if (!senderUserId.equals(recieverUserId))
        {
            SendMessagerequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    SendMessagerequestButton.setEnabled(false);

                    if (currentState.equals("new"))
                    {
                        sendMessageChatRequsets();
                    }
                    if (currentState.equals("request_sent"))
                    {
                        CancelChatRequest();
                    }
                    if (currentState.equals("request_received"))
                    {
                        AcceptChatRequest();
                    }
                    if (currentState.equals("friends"))
                    {
                        removeSpecificContact();

                    }
                }
            });
        }
        else
        {
            SendMessagerequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void removeSpecificContact()
    {
        contactRef.child(senderUserId).child(recieverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            contactRef.child(recieverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                SendMessagerequestButton.setEnabled(true);
                                                currentState = "new";
                                                SendMessagerequestButton.setText("Send Message");

                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void AcceptChatRequest()
    {
        contactRef.child(senderUserId).child(recieverUserId)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            contactRef.child(recieverUserId).child(senderUserId)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            chatRequestRef.child(senderUserId).child(recieverUserId)
                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    if(task.isSuccessful())
                                                    {
                                                        chatRequestRef.child(recieverUserId).child(senderUserId)
                                                                .removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                    {
                                                                        SendMessagerequestButton.setEnabled(true);
                                                                        currentState = "friends";
                                                                        SendMessagerequestButton.setText("Remove This contact");

                                                                        DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                        DeclineMessageRequestButton.setEnabled(false);

                                                                    }
                                                                });
                                                    }


                                                }
                                            });

                                        }
                                    });

                        }

                    }
                });
    }

    private void CancelChatRequest()
    {
        chatRequestRef.child(senderUserId).child(recieverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            chatRequestRef.child(recieverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                SendMessagerequestButton.setEnabled(true);
                                                currentState = "new";
                                                SendMessagerequestButton.setText("Send Message");

                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void retrieveUserInfo()
    {
        UserRef.child(recieverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if ((dataSnapshot.exists())  &&  (dataSnapshot.hasChild("image")))
                {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userstatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    UserProfileName.setText(userName);
                    UserProfileStatus.setText(userstatus);


                    ManageChatRequests();
                }
                else
                {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userstatus = dataSnapshot.child("status").getValue().toString();

                    UserProfileName.setText(userName);
                    UserProfileStatus.setText(userstatus);


                    ManageChatRequests();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void sendMessageChatRequsets()
    {
        chatRequestRef.child(senderUserId).child(recieverUserId)
                .child("Request Type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful()) {
                            chatRequestRef.child(recieverUserId).child(senderUserId)
                                    .child("Request Type").setValue("recieved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                chatNotificationMap.put("from",senderUserId);
                                                chatNotificationMap.put("type","request");

                                                notificationRef.child(recieverUserId).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if(task.isSuccessful())
                                                                {

                                                                    SendMessagerequestButton.setEnabled(true);
                                                                    currentState = "request_sent";
                                                                    SendMessagerequestButton.setText("cancel chat Request");
                                                                }

                                                            }
                                                        });

                                            }
                                        }


                                    });
                        }
                    }
                });

    }


}

