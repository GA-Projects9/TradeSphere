package com.example.tradesphere.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tradesphere.R;
import com.example.tradesphere.adapters.AdapterChats;
import com.example.tradesphere.databinding.FragmentChatsBinding;
import com.example.tradesphere.models.ModelChat;
import com.example.tradesphere.models.ModelChats;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class ChatsFragment extends Fragment {

    private FragmentChatsBinding binding;
    private FirebaseAuth firebaseAuth;
    private String myUid;

    //context for this fragment class
    private Context mContext;

    //chatsArrayList to hold chats list by currently logged in user to show in recylcer view
    private ArrayList<ModelChats> chatsArrayList;
    //adapter chats class instance to set to recycler view to show chats list
    private AdapterChats adapterChats;

    @Override
    public void onAttach(@NonNull Context context) { //get and init the context for this fragment class
        this.mContext = context;
        super.onAttach(context);
    }

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        myUid = firebaseAuth.getUid();

        loadChats();

        //add text changed listener to searchEt to search using filter applied
        //in adapterChats class
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //this function is called whenever the user type a letter
                //search based on what user typed
                try {

                    String query = s.toString();
                    adapterChats.getFilter().filter(query);

                } catch (Exception e) {


                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void loadChats() {

        chatsArrayList = new ArrayList<>();

        //firebase db listener to get the chats of logged-in user
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear chatsArrayList each time, starting adding data into it
                chatsArrayList.clear();

                //load chats, only need chatKey
                //e.g. uid1_uid2 here, we have to get (already done) the chat data
                //and receipt user data in adapter class
                for (DataSnapshot ds: snapshot.getChildren()) {
                    //the chat key e.g. uid1_uid2
                    String chatKey = ""+ds.getKey();

                    //if the chatkey uid1_uid2 contains the uid of currently logged in user
                    //will be considered as chat of currently logged in user
                    if (chatKey.contains(myUid)) {

                        //create instance of modelchats in arraylist
                        ModelChats modelChats = new ModelChats();
                        modelChats.setChatKey(chatKey);

                        //add the instance of modelchats in chatsArrayList
                        chatsArrayList.add(modelChats);

                    }
                }

                //init/setup adapter class and set to recyclerview
                adapterChats = new AdapterChats(mContext, chatsArrayList);
                binding.chatsRv.setAdapter(adapterChats);

                //after loading data in list, we will sort the list using timestamp of each
                //last message of chat, to show the newest chat first
                sort();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sort() {

        //delay of 1 second before sorting the list
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //sort chats arrayList
                Collections.sort(chatsArrayList, (model1, model2) -> Long.compare(model2.getTimestamp(), model1.getTimestamp()));
                //notify changes
                adapterChats.notifyDataSetChanged();
            }
        }, 1000);
    }
}