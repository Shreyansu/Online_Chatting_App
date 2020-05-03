package com.example.whatsapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment
{
    private View ContactsView;
    private RecyclerView myContactList;
    private DatabaseReference contactsRef,UserRef;
    private FirebaseAuth mAuth;
    private String CurrentUserId;

    public ContactsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();
        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);

        myContactList = (RecyclerView) ContactsView.findViewById(R.id.contact_list);
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(CurrentUserId);
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        // Inflate the layout for this fragment

        myContactList.setLayoutManager(new LinearLayoutManager(getContext()));

        return ContactsView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contact>()
                .setQuery(contactsRef,Contact.class)
                .build();

        FirebaseRecyclerAdapter<Contact,ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contact, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contact model)
            {
                final String UserId = getRef(position).getKey();
                UserRef.child(UserId).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            if(dataSnapshot.child("UserState").hasChild("state"))
                            {
                                String date = dataSnapshot.child("UserState").child("date").getValue().toString();
                                String time = dataSnapshot.child("UserState").child("time").getValue().toString();
                                String state = dataSnapshot.child("UserState").child("state").getValue().toString();




                                if(state.equals("online"))
                                {
                                    holder.OnlineIcon.setVisibility(View.VISIBLE);

                                }
                                else if(state.equals("offline"))
                                {
                                    holder.OnlineIcon.setVisibility(View.INVISIBLE);
                                }
                            }
                            else
                            {
                                holder.OnlineIcon.setVisibility(View.INVISIBLE);

                            }





                            if (dataSnapshot.hasChild("image")) {
                                String userImage = dataSnapshot.child("image").getValue().toString();
                                String userName = dataSnapshot.child("name").getValue().toString();
                                String userStatus = dataSnapshot.child("status").getValue().toString();


                                holder.UserName.setText(userName);
                                holder.UserStatus.setText(userStatus);
                                Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.ProfileImage);
                            } else {
                                String userName = dataSnapshot.child("name").getValue().toString();
                                String userStatus = dataSnapshot.child("status").getValue().toString();


                                holder.UserName.setText(userName);
                                holder.UserStatus.setText(userStatus);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_list_layout, parent, false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };
        myContactList.setAdapter(adapter);
        adapter.startListening();

    }


    public static class ContactsViewHolder extends RecyclerView.ViewHolder
    {
        TextView UserName,UserStatus;
        CircleImageView ProfileImage;
        ImageView OnlineIcon;
        public ContactsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            UserName = itemView.findViewById(R.id.User_profile_name);
            UserStatus = itemView.findViewById(R.id.user_status);
            ProfileImage = itemView.findViewById(R.id.Users_profile_image);
            OnlineIcon = (ImageView) itemView.findViewById(R.id.User_online_status);


        }
    }
}
