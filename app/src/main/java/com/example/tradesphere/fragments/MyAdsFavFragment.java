package com.example.tradesphere.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tradesphere.adapters.AdapterAd;
import com.example.tradesphere.databinding.FragmentMyAdsFavBinding;
import com.example.tradesphere.models.ModelAd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyAdsFavFragment extends Fragment {


    private FragmentMyAdsFavBinding binding;
    private static final String TAG = "FAV_TAG";
    private Context mContext;
    private FirebaseAuth firebaseAuth;

    //adArrayList to hold ads list added to favorite by currently logged in user
    //to show in recycler view
    private ArrayList<ModelAd> adArrayList;

    //adapter class instance to set to recycler view to show ads list
    private AdapterAd adapterAd;


    public MyAdsFavFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMyAdsFavBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        loadAds();

        //add text changed listener to searchEt to seach ads using filter
        //in adapter class
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //this function is called whenever user type a letter
                //search based on what user typed
                try {
                    String query = s.toString();
                    adapterAd.getFilter().filter(query);
                } catch (Exception e) {
                    Log.e(TAG, "onTextChanged: ", e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void loadAds(){

        adArrayList = new ArrayList<>();

        //firebae db listener to get the ids of the ads added to favorite by currently
        //logged in user e.g. Users > Uid > Favorites > Fav_Ads_Data
        DatabaseReference favRef = FirebaseDatabase.getInstance().getReference("Users");
        favRef.child(firebaseAuth.getUid()).child("Favorites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        adArrayList.clear();

                        //load favorite ad ids
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            //get the id of the ad, that is, Users > Uid > Favorites > adId
                            String adId = ""+ ds.child("adId").getValue();

                            //Firebase DB listener to load ad details based on the id
                            //of the ad we just got
                            DatabaseReference adRef = FirebaseDatabase.getInstance().getReference("Ads");
                            adRef.child(adId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                            try {
                                                //prepare modelAd with all data from firebase DB
                                                ModelAd modelAd = snapshot.getValue(ModelAd.class);
                                                //add prepared model to adArrayList
                                                adArrayList.add(modelAd);
                                            } catch (Exception e) {
                                                Log.e(TAG, "onDataChange: ", e);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }

                        //sometimes fav ads are not loading due to nested db listeners because
                        //we are getting data using two paths so added some delay e.g. half-second
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                adapterAd = new AdapterAd(mContext, adArrayList);
                                binding.adsRv.setAdapter(adapterAd);
                            }
                        }, 500);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }
}