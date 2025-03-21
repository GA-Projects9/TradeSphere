package com.example.tradesphere.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tradesphere.R;
import com.example.tradesphere.Utils;
import com.example.tradesphere.activities.AdDetailsActivity;
import com.example.tradesphere.databinding.RowImagesPickedBinding;
import com.example.tradesphere.models.ModelImagePicked;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterImagesPicked extends RecyclerView.Adapter<AdapterImagesPicked.HolderImagesPicked>{

    private RowImagesPickedBinding binding;
    private static final String TAG = "IMAGES_TAG"; //to show logs in logcat


    //context of activity/fragment where instance of AdapterImagesPicked class is created
    private Context context;

    //imagePickedArrayList - the list of images picked/captured from Gallery/Camera
    // or from the Internet
    private ArrayList<ModelImagePicked> imagePickedArrayList;

    private String adId;

    //constructor
    public AdapterImagesPicked(Context context, ArrayList<ModelImagePicked> imagePickedArrayList, String adId) {
        this.context = context;
        this.imagePickedArrayList = imagePickedArrayList;
        this.adId = adId;
    }

    @NonNull
    @Override
    public HolderImagesPicked onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate/bind the row_images_picked.xml
        binding = RowImagesPickedBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderImagesPicked(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderImagesPicked holder, int position) {
        //get data from particular position of list and set to the UI views of row_images_picked.xml
        //and handle clicks
        ModelImagePicked model = imagePickedArrayList.get(position);

        if (model.getFromInternet()) {

            //image is from internet/firebase db. get image url of the image to set in imageTv
            String imageUrl = model.getImageUrl();

            Log.d(TAG, "onBindViewHolder: imageUri: " + imageUrl);

            //set the image in imageTv
            try {

                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.image)
                        .into(holder.imageTv);

            } catch (Exception e) {

                Log.e(TAG, "onBindViewHolder: ", e);
            }

            } else {

            //image is picked from gallery/camera. get image uri of the image to set in imageTv
            Uri imageUri = model.getImageUri();

            try {
                Glide.with(context)
                        .load(imageUri)
                        .placeholder(R.drawable.image)
                        .into(holder.imageTv);

            } catch (Exception e){

            }

        }

        //handle closeBtn click, remove image from imagePickedArrayList
        holder.closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (model.getFromInternet()) {

                    deleteImageFirebase(model, holder, position);

                } else {
                    imagePickedArrayList.remove(model);
                    notifyItemRemoved(position);
                }
            }
        });

    }

    private void deleteImageFirebase(ModelImagePicked model, HolderImagesPicked holder, int position) {

        //get id of the image to delete image
        String imageId = model.getId();

        //ads > adId > images > imageId
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adId).child("Images").child(imageId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //deleting success
                        Utils.toast(context, "Image successfully deleted");

                        try {
                            //remove from imagePickedArrayList
                            imagePickedArrayList.remove(model);
                            notifyItemRemoved(position);

                        } catch (Exception e){


                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return imagePickedArrayList.size(); //return the size of list
    }

    //viewholder class to hold/init UI views of the row_images_picked.xml
    class HolderImagesPicked extends RecyclerView.ViewHolder{

        //UI views of the row_images_picked.xml
        ImageView imageTv;
        ImageButton closeBtn;

        public HolderImagesPicked(@NonNull View itemView) {
            super(itemView);

            //init UI views of the row_images_picked.xml
            imageTv = binding.imageTv;
            closeBtn = binding.closeBtn;
        }
    }
}
