package com.example.tradesphere.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tradesphere.adapters.AdapterAd;
import com.example.tradesphere.databinding.FragmentMyAdsAdsBinding;
import com.example.tradesphere.models.ModelAd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyAdsAdsFragment extends Fragment {

    private FragmentMyAdsAdsBinding binding;
    private static final String TAG = "MY_ADS_TAG";
    private Context mContext;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelAd> adArrayList;
    private AdapterAd adapterAd;

    public MyAdsAdsFragment() {
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
        binding = FragmentMyAdsAdsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        loadAds();

        //add textChangedListener to searchEt to search ads using filter applied in adapter class
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try {
                    String query = s.toString();
                    adapterAd.getFilter().filter(query);
                } catch (Exception e){
                    Log.e(TAG, "onTextChanged: ", e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void loadAds() {

        //init adArrayList before starting adding data into it
        adArrayList = new ArrayList<>();

        //firebase db listener to load ads by currently logged in user .i.e show only
        //those ads whose key uid is equal to the uid of the currently logged in user
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear arraylist each time starting adding data into it
                        adArrayList.clear();
                        //load ads list
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            try {
                                //prepare modelAd with all data from firebase db
                                ModelAd modelAd = ds.getValue(ModelAd.class);
                                //add prepared model to arrayList
                                adArrayList.add(modelAd);

                            } catch (Exception e) {
                                Log.e(TAG, "onDataChange: ", e);
                            }
                        }
                        //init/setup AdapterAd class and set to recyclerView
                        adapterAd = new AdapterAd(mContext, adArrayList);
                        binding.adsRv.setAdapter(adapterAd);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}