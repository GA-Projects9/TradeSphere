package com.example.tradesphere.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.tradesphere.R;
import com.example.tradesphere.Utils;
import com.example.tradesphere.adapters.AdapterAd;
import com.example.tradesphere.databinding.ActivityAdSellerProfileBinding;
import com.example.tradesphere.models.ModelAd;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdSellerProfileActivity extends AppCompatActivity {

    private ActivityAdSellerProfileBinding binding;
    private static final String TAG = "PROFILE_AD_SELLER";

    private String sellerUid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdSellerProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sellerUid = getIntent().getStringExtra("sellerUid");

        loadSellerDetails();
        loadAds();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadSellerDetails(){

        //the path to load seller info, users > sellerUid
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(sellerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String name = ""+ snapshot.child("name").getValue();
                        String profileImageUrl = ""+ snapshot.child("profileImageUrl").getValue();
                        long timestamp = (Long) snapshot.child("timestamp").getValue();
                        //format date time. e.g. timestamp to dd/MM/yyyy
                        String formattedDate = Utils.formatTimeStampDate(timestamp);

                        binding.sellerNameTv.setText(name);
                        binding.sellerMemberSinceTv.setText(formattedDate);
                        try {
                            Glide.with(AdSellerProfileActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.person)
                                    .into(binding.sellerProfileIv);
                        } catch (Exception e) {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAds(){

        //init adArrayList before starting add data into it
        ArrayList<ModelAd> adArrayList = new ArrayList<>();

        //firebase db to load ads of seller using orderByChild query
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.orderByChild("uid").equalTo(sellerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {


                        adArrayList.clear();

                        for (DataSnapshot ds: snapshot.getChildren()) {
                            try {
                                //prepare modelAd with all data from firebase db
                                ModelAd modelAd = ds.getValue(ModelAd.class);
                                //add the prepared modelAd to list
                                adArrayList.add(modelAd);
                            } catch (Exception e) {

                            }
                        }
                        //init/setup adapterAd and set to RecyclerView i.e. adsRv
                        AdapterAd adapterAd = new AdapterAd(AdSellerProfileActivity.this, adArrayList);
                        binding.adsRv.setAdapter(adapterAd);

                        //set ads count
                        String adsCount = ""+ adArrayList.size();
                        binding.publishedAdsCountTv.setText(adsCount);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}