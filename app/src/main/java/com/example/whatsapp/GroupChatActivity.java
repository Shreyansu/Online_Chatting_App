package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
    private ImageButton sendMessageBtn;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessage;
    private Toolbar mToolbar;
    private DatabaseReference UsersRef ,GroupNameRef ,GroupMessageKeyRef;

    private FirebaseAuth mAuth;

    private String CurrentGroupName, CurrentUserId,CurrentUserName, CurrentDate ,CurrentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        CurrentGroupName = getIntent().getExtras().get("Group Name").toString();
        Toast.makeText(GroupChatActivity.this,CurrentGroupName,Toast.LENGTH_SHORT).show();

        mAuth =FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef =FirebaseDatabase.getInstance().getReference().child("Groups").child(CurrentGroupName);



        initializeFields();

        getUserInfo();
        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                saveMessageInfoToDatabse();
                //use to clear the messsage edit text after sending the message.
                userMessageInput.setText("");
//          For scroll view
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

            }
        });

    }

    @Override
    protected void onStart()
    {
        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                if(dataSnapshot.exists())
                {
                    displayMessage(dataSnapshot);
                }


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                    displayMessage(dataSnapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        super.onStart();
    }



    private void initializeFields()
    {
        mToolbar =(Toolbar)findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(CurrentGroupName);


        sendMessageBtn = (ImageButton)findViewById(R.id.send_message_button);
        userMessageInput = (EditText)findViewById(R.id.input_Group_Message);
        mScrollView = (ScrollView)findViewById(R.id.my_scroll_view);
        displayTextMessage =(TextView)findViewById(R.id.group_chat_text_display);

    }

    private void getUserInfo()
    {
        UsersRef.child(CurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    CurrentUserName = dataSnapshot.child("name").getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveMessageInfoToDatabse()
    {
        String message = userMessageInput.getText().toString();
        String MessageKEY = GroupNameRef.push().getKey();
        if(TextUtils.isEmpty(message))
        {
            Toast.makeText(this,"Please Write Message First",Toast.LENGTH_SHORT).show();
        }
        else
        {
            //used to get current Date and Time
            Calendar ccalForDate =  Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            CurrentDate = currentDateFormat.format(ccalForDate.getTime()).toString();

            Calendar ccalForTime =  Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm:ss a");
            CurrentTime = currentTimeFormat.format(ccalForTime.getTime()).toString();

            //Hashmap is used to store the data or other message info to firebase Database
            HashMap<String, Object> groupMessagekey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessagekey);

            GroupMessageKeyRef =GroupNameRef.child(MessageKEY);

                HashMap<String, Object> MessageInfoMap = new HashMap<>();
                MessageInfoMap.put("name",CurrentUserName);
                MessageInfoMap.put("message",message);
                MessageInfoMap.put("date",CurrentDate);
                MessageInfoMap.put("time",CurrentTime);

                GroupMessageKeyRef.updateChildren(MessageInfoMap);



        }
    }

    private void displayMessage(DataSnapshot dataSnapshot)
    {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while(iterator.hasNext())
        {
            String Chatdate = (String) (((DataSnapshot)iterator.next()).getValue());
            String ChatMessage = (String) (((DataSnapshot)iterator.next()).getValue());
            String ChatName = (String) (((DataSnapshot)iterator.next()).getValue());
            String ChatTime = (String) (((DataSnapshot)iterator.next()).getValue());

            displayTextMessage.append(Chatdate + "     " + ChatTime + "\n" + ChatName + ":\n" + ChatMessage + "\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

        }
    }

}
