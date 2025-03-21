package com.example.tradesphere.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tradesphere.FilterChats;
import com.example.tradesphere.R;
import com.example.tradesphere.Utils;
import com.example.tradesphere.activities.ChatActivity;
import com.example.tradesphere.databinding.RowChatsBinding;
import com.example.tradesphere.models.ModelChats;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterChats extends RecyclerView.Adapter<AdapterChats.HolderChats> implements Filterable {

    private RowChatsBinding binding;

    //context of activity/fragment where instance of AdapterChats class is created
    private Context context;

    //chatsArrayList - the list of chats
    public ArrayList<ModelChats> chatsArrayList;
    private ArrayList<ModelChats> filterList;
    private FilterChats filter;
    private FirebaseAuth firebaseAuth;
    private String myUid;

    public AdapterChats(Context context, ArrayList<ModelChats> chatsArrayList) {
        this.context = context;
        this.chatsArrayList = chatsArrayList;
        this.filterList = chatsArrayList;

        firebaseAuth = FirebaseAuth.getInstance();

        myUid = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public HolderChats onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = RowChatsBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderChats(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderChats holder, int position) {
        //get data from particular position of list and set to the UI views of row_chats.xml
        //and handle clicks
        ModelChats modelChats = chatsArrayList.get(position);

        loadLastMessage(modelChats, holder);

        //handle chat item click, open ChatActivity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String receiptUid = modelChats.getReceiptUid();

                if (receiptUid != null) {

                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("receiptUid", receiptUid);
                    context.startActivity(intent);

                }
            }
        });
    }

    private void loadLastMessage(ModelChats modelChats, HolderChats holder) {

        String chatKey = modelChats.getChatKey();

        //database reference to load last messages info e.g. Chats > ChatKey > LastMessage
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.child(chatKey).limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot ds: snapshot.getChildren()) {

                            //get message data
                            String fromUid = ""+ds.child("fromUid").getValue();
                            String message = ""+ds.child("message").getValue();
                            String messageId = ""+ds.child("messageId").getValue();
                            String messageType = ""+ds.child("messageType").getValue();
                            String toUid = ""+ ds.child("toUid").getValue();
                            long timestamp = (Long) ds.child("timestamp").getValue();

                            String formattedDate = Utils.formatTimeStampDateTime(timestamp);

                            //set data to current instance of modelChats using setters
                            modelChats.setMessage(message);
                            modelChats.setMessageId(messageId);
                            modelChats.setMessageType(messageType);
                            modelChats.setFromUid(fromUid);
                            modelChats.setToUid(toUid);
                            modelChats.setTimestamp(timestamp);

                            //set formatted date and time
                            holder.dateTimeTv.setText(formattedDate);

                            //check message type
                            if (messageType.equals(Utils.MESSAGE_TYPE_TEXT)) {
                                //message type is text, set last message
                                holder.lastMessageTv.setText(message);

                            } else {
                                //message type is IMAGE, just set hardcoded string
                                //e.g. "sends attachment"
                                holder.lastMessageTv.setText("Sends Attachment");
                            }
                        }

                        loadReceiptUserInfo(modelChats, holder);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private void loadReceiptUserInfo(ModelChats modelChats, HolderChats holder) {

        String fromUid = modelChats.getFromUid();
        String toUid = modelChats.getToUid();

        //to identify either fromUid or toUid is the Uid of the receipt we need to validate
        //e.g. if fromUid == UID_OF_CURRENT_USER then receiptUid = toUid
        String receiptUid;
        if (fromUid.equals(myUid)) {
            //fromUid = uid of current user
            receiptUid = toUid;

        } else {
            //fromUid != uid of current user
            receiptUid = fromUid;
        }

        //set receipt uid to current instance of model chats using setters
        modelChats.setReceiptUid(receiptUid);

        //database reference to load receipt user info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(receiptUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //set message data
                        String name = ""+ snapshot.child("name").getValue();
                        String profileImageUrl = ""+ snapshot.child("profileImageUrl").getValue();


                        //set data to current instance of modelChats using setters
                        modelChats.setName(name);
                        modelChats.setProfileImageUrl(profileImageUrl);


                        //set/show receipt name and profile image to UI
                        holder.nameTv.setText(name);
                        try {
                            Glide.with(context)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.person)
                                    .into(holder.profileIv);

                        } catch (Exception e) {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return chatsArrayList.size();
    }

    @Override
    public Filter getFilter() {
        //init the filter object only if it is null
        if (filter == null) {
            //filter obj is null, init
            filter = new FilterChats(this, filterList);
        }
        return null;
    }

    class HolderChats extends RecyclerView.ViewHolder{

        ShapeableImageView profileIv;
        TextView nameTv, lastMessageTv, dateTimeTv;
        public HolderChats(@NonNull View itemView) {
            super(itemView);

            profileIv = binding.profileIv;
            nameTv = binding.nameTv;
            lastMessageTv = binding.lastMessageTv;
            dateTimeTv = binding.dateTimeTv;
        }
    }
}
