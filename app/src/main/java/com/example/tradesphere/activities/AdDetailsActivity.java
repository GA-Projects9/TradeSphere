package com.example.tradesphere.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.example.tradesphere.R;
import com.example.tradesphere.Utils;
import com.example.tradesphere.adapters.AdapterImageSlider;
import com.example.tradesphere.databinding.ActivityAdDetailsBinding;
import com.example.tradesphere.models.ModelAd;
import com.example.tradesphere.models.ModelImageSlider;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class AdDetailsActivity extends AppCompatActivity {

    private ActivityAdDetailsBinding binding;

    private static final String TAG = "AD_DETAILS_TAG";
    private FirebaseAuth firebaseAuth;

    //adId will get from Intent
    private String adId = "";


    //latitude and longitude of the ad to view it on map
    private double adLatitude = 0;
    private double adLongitude = 0;


    //to load seller info, chat with seller and call/sms
    private String sellerUid = null;
    private String sellerPhone = "";


    //hold the ad's favorite state by current user
    private boolean favorite = false;


    //list of the ad's images to show in slider
    private ArrayList<ModelImageSlider> imageSliderArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //hide same ui views in start. display the edit/delete option if the
        //user is ad owner, and we will show call/chat/sms option if the user isn't ad owner
        binding.toolBarEditBtn.setVisibility(View.GONE);
        binding.toolBarDeleteBtn.setVisibility(View.GONE);
        binding.chatBtn.setVisibility(View.GONE);
        binding.callBtn.setVisibility(View.GONE);
        binding.smsBtn.setVisibility(View.GONE);

        //get the id of the ad, as have passed in the AdapterAd class before
        //starting this activity
        adId = getIntent().getStringExtra("adId");

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            checkIsFavorite();
        }

        loadAdDetails();
        loadAdImages();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.toolBarDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //alert dialog to confirm if the user really wants to delete the ad
                MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(AdDetailsActivity.this);
                materialAlertDialogBuilder.setTitle("Delete Ad")
                        .setMessage("Are you sure you want to delete this Ad?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                deleteAd();
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        //handle toolbarEditbtn click, start AdCreateActivity to edit this ad
        binding.toolBarEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editOptions();
            }
        });

        binding.toolBarFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (favorite) {
                    //this ad is in fav of current user, remove from fav
                    Utils.removeFromFavorite(AdDetailsActivity.this, adId);
                } else {
                    //this ad is not in fav of current user, add to fav
                    Utils.addToFavorite(AdDetailsActivity.this, adId);
                }
            }
        });

        //handle seller profile click, start sellerProfileActivity
        binding.sellerProfileCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AdDetailsActivity.this, AdSellerProfileActivity.class);
                intent.putExtra("sellerUid", sellerUid);
                startActivity(intent);

            }
        });

        binding.chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AdDetailsActivity.this, ChatActivity.class);
                intent.putExtra("receiptUid", sellerUid);
                startActivity(intent);
            }
        });

        binding.callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.callIntent(AdDetailsActivity.this, sellerPhone);
            }
        });

        binding.smsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.smsIntent(AdDetailsActivity.this, sellerPhone);
            }
        });

    }

    private void editOptions(){

        PopupMenu popupMenu = new PopupMenu(this, binding.toolBarEditBtn);

        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Edit");
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Mark as Sold");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //get id of the item clicked
                int itemId = item.getItemId();

                if (itemId == 0){
                    //edit clicked, start the AdCreateActivity with adId and isEditMode as true
                    Intent intent = new Intent(AdDetailsActivity.this, AdCreateActivity.class);
                    intent.putExtra("isEditMode", true);
                    intent.putExtra("adId", adId);
                    startActivity(intent);
                } else if (itemId == 1) {
                    showMarkAsSoldDialog();
                }

                return false;
            }
        });
    }

    private void showMarkAsSoldDialog(){

        //material alert dialog - setup and show
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        alertDialogBuilder.setTitle("Mark as Sold")
                .setMessage("Are you sure you want to mark this Ad as sold?")
                .setPositiveButton("SOLD", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //setup info to update in the existing ad i.e. mark as sold by setting the value of status to SOLD
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("status", ""+ Utils.AD_STATUS_SOLD);

                        //ads db path to update its available/sold status. ads > adid
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
                        ref.child(adId)
                                .updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                        Log.d(TAG, "onSuccess: Marked as Sold: ");
                                        Utils.toast(AdDetailsActivity.this, "Marked as Sold");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Utils.toast(AdDetailsActivity.this, "Failed to mark as sold. Please try again.");
                                    }
                                });
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void loadAdDetails(){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        try {
                            //setup data from firebase datasnapshot
                            ModelAd modelAd = snapshot.getValue(ModelAd.class);

                            //get data from model
                            sellerUid = modelAd.getUid();
                            String title = modelAd.getTitle();
                            String description = modelAd.getDescription();
                            String address = modelAd.getAddress();
                            String condition = modelAd.getCondition();
                            String category = modelAd.getCategory();
                            String price = modelAd.getPrice();
                            long timestamp = modelAd.getTimestamp();

                            //format date time e.g. timestamp to dd/MM/yyyy
                            String formattedDate = Utils.formatTimeStampDate(timestamp);

                            //check if the ad is by currently signed in user
                            if (sellerUid.equals(firebaseAuth.getUid())){
                                //ad is created by currently signed in user so
                                //1) should be able to edit and delete ad
                                binding.toolBarEditBtn.setVisibility(View.VISIBLE);
                                binding.toolBarDeleteBtn.setVisibility(View.VISIBLE);
                                //2) shouldn't be able to chat/call/sms to himself
                                binding.chatBtn.setVisibility(View.GONE);
                                binding.callBtn.setVisibility(View.GONE);
                                binding.smsBtn.setVisibility(View.GONE);
                                binding.sellerProfileLabelTv.setVisibility(View.GONE);
                                binding.sellerProfileCv.setVisibility(View.GONE);
                            } else {
                                //ad is not created by currently signed in user so
                                //1) shouldn't be able to edit and delete ad
                                binding.toolBarEditBtn.setVisibility(View.GONE);
                                binding.toolBarDeleteBtn.setVisibility(View.GONE);
                                //2) should be able to chat/call/sms to the ad creator
                                binding.chatBtn.setVisibility(View.VISIBLE);
                                binding.callBtn.setVisibility(View.VISIBLE);
                                binding.smsBtn.setVisibility(View.VISIBLE);
                                binding.sellerProfileLabelTv.setVisibility(View.VISIBLE);
                                binding.sellerProfileCv.setVisibility(View.VISIBLE);
                            }

                            //set data to ui views
                            binding.titleTv.setText(title);
                            binding.descriptionTv.setText(description);
                            binding.addressTv.setText(address);
                            binding.conditionTv.setText(condition);
                            binding.categoryTv.setText(category);
                            binding.priceTv.setText(price);
                            binding.dateTv.setText(formattedDate);

                            //function call, load seller info, e.g. profile image/name/member since
                            loadSellerDetails();


                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadSellerDetails(){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(sellerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String phoneCode = ""+ snapshot.child("phoneCode").getValue();
                        String phoneNumber = ""+ snapshot.child("phoneNumber").getValue();
                        String name = ""+ snapshot.child("name").getValue();
                        String profileImageUrl = ""+ snapshot.child("profileImageUrl").getValue();
                        long timestamp = (Long) snapshot.child("timestamp").getValue();

                        String formattedDate = Utils.formatTimeStampDate(timestamp);

                        sellerPhone = phoneCode +""+phoneNumber;

                        binding.sellerNameTv.setText(name);
                        binding.memberSinceTv.setText(formattedDate);

                        try {
                            Glide.with(AdDetailsActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.person)
                                    .into(binding.sellerProfileIv);

                        } catch (Exception e){
                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsFavorite(){

        //db path to check if ad is in favorite of current user
        //users > uid > favorites > adId
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(adId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //if snapshot exists (value is true) means the ad is in
                        //favorite of current user otherwise no
                        favorite = snapshot.exists();
                        //check if favorite or not to set the image of favBtn accordingly
                        if (favorite) {
                            binding.toolBarFavBtn.setImageResource(R.drawable.ic_fav_yes);
                        } else {
                            binding.toolBarFavBtn.setImageResource(R.drawable.ic_fav_no);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAdImages(){
        imageSliderArrayList = new ArrayList<>();

        //db path to load the ad images. ads > adId > images
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adId).child("Images")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear the list before starting add data into it
                        imageSliderArrayList.clear();
                        //there might be multiple images, loop it to load it all
                        for (DataSnapshot ds: snapshot.getChildren()){
                            //prepare model
                            ModelImageSlider modelImageSlider = ds.getValue(ModelImageSlider.class);
                            //add the prepared model to list
                            imageSliderArrayList.add(modelImageSlider);
                        }
                        //setup adapter and set to viewPager i.e. imageSliderVp
                        AdapterImageSlider adapterImageSlider = new AdapterImageSlider(AdDetailsActivity.this, imageSliderArrayList);
                        binding.imageSliderVp.setAdapter(adapterImageSlider);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void deleteAd(){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Utils.toast(AdDetailsActivity.this, "Deleted");
                        finish();
                    }
                });
    }
}