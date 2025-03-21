package com.example.tradesphere.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tradesphere.FilterAd;
import com.example.tradesphere.R;
import com.example.tradesphere.Utils;
import com.example.tradesphere.activities.AdDetailsActivity;
import com.example.tradesphere.databinding.RowAdBinding;
import com.example.tradesphere.models.ModelAd;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

//custom adapter class to show ad list in recyclerview
public class AdapterAd extends RecyclerView.Adapter<AdapterAd.HolderAd> implements Filterable {

    private RowAdBinding binding;
    private static final String TAG = "ADAPTER_AD_TAG";

    private FirebaseAuth firebaseAuth;
    private Context context;
    public ArrayList<ModelAd> adArrayList;
    private ArrayList<ModelAd> filterList;
    private FilterAd filter;

    public AdapterAd(Context context, ArrayList<ModelAd> adArrayList) {
        this.context = context;
        this.adArrayList = adArrayList;
        this.filterList = adArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderAd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = RowAdBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderAd(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderAd holder, int position) {
        //get data from particular position of list and set UI views of row_ad.xml and handle clicks
        ModelAd modelAd = adArrayList.get(position);

        String title = modelAd.getTitle();
        String description = modelAd.getDescription();
        String condition = modelAd.getCondition();
        String price = modelAd.getPrice();
        long timestamp = modelAd.getTimestamp();
        String formattedDate = Utils.formatTimeStampDate(timestamp);

        //function call: load first image from available images of ads
        //e.g. if there are 5 images of ad, load first one
        loadAdFirstImage(modelAd, holder);

        //if user is logged in, then check if the ad is in favorite of the current user
        if (firebaseAuth.getCurrentUser() != null){
            checkIsFavorite(modelAd, holder);
        }

        //set data to UI views of row_ad.xml
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.conditionTv.setText(condition);
        holder.priceTv.setText(price);
        holder.dateTv.setText(formattedDate);

        //handle itemView (i.e. Ad) click, open the AdDetailsActivity
        //also pass the id of the Ad to intent to load details
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AdDetailsActivity.class);
                intent.putExtra("adId", modelAd.getId());
                context.startActivity(intent);
            }
        });

        //handle favBtn click, add/remove the ad to/from favorite of current user
        holder.favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if ad is in favorite of current user or not - true/false
                boolean favorite = modelAd.isFavorite();
                if (favorite) {
                    //this ad is in favorite of current user, remove from favorite
                    Utils.removeFromFavorite(context, modelAd.getId());
                } else {
                    //this add is not in favorite of current user, add to favorite
                    Utils.addToFavorite(context, modelAd.getId());
                }
            }
        });
    }

    private void checkIsFavorite(ModelAd modelAd, HolderAd holder) {

        //db path to check if add is in fav of current user. users > uid > favorites > adId
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(modelAd.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //if snapshot exists (value is true) means ad is in favorite of
                        //current user otherwise no
                        boolean favorite = snapshot.exists();
                        modelAd.setFavorite(favorite);
                        //check if fav or not to set image of favBtn accordingly
                        if (favorite) {
                            //favorite, set image to ic_fav_yes to button favBtn
                            holder.favBtn.setImageResource(R.drawable.ic_fav_yes);
                        } else {
                            //not favorite, set image to ic_fav_no to button favBtn
                            holder.favBtn.setImageResource(R.drawable.ic_fav_no);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadAdFirstImage(ModelAd modelAd, HolderAd holder) {
        Log.d(TAG, "loadAdFirstImage: ");
        //load first image from available images of ad. e.g if there are 5 images, load first one

        //adId to get the image of it
        String adId = modelAd.getId();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Ads");
        reference.child(adId).child("Images").limitToFirst(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //this will return only 1 image as we have used query .limitToFirst(1)
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            //get url of the image
                            String imageUrl = ""+ ds.child("imageUrl").getValue();
                            Log.d(TAG, "onDataChange: imageUrl: "+imageUrl);
                            //set image to imageView i.e. imageIv
                            try {
                                Glide.with(context)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.image)
                                        .into(holder.imageIv);
                            } catch (Exception e) {
                                Log.e(TAG, "onDataChange: ", e);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    @Override
    public int getItemCount() {
        //return the size of the list
        return adArrayList.size();
    }

    @Override
    public Filter getFilter() {
        //init the filter object only if it is null
        if (filter == null){
            filter = new FilterAd(this, filterList);
        }
        return filter;
    }

    class HolderAd extends RecyclerView.ViewHolder {

        ShapeableImageView imageIv;
        TextView titleTv, descriptionTv, conditionTv, priceTv, dateTv;
        ImageButton favBtn;
        public HolderAd(@NonNull View itemView) {
            super(itemView);

            imageIv = binding.imageIv;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            conditionTv = binding.conditionTv;
            priceTv = binding.priceTv;
            dateTv = binding.dateTv;
            favBtn = binding.favBtn;
        }
    }
}
