package com.example.tradesphere.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tradesphere.R;
import com.example.tradesphere.databinding.RowImageSliderBinding;
import com.example.tradesphere.models.ModelImageSlider;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class AdapterImageSlider extends RecyclerView.Adapter<AdapterImageSlider.HolderImageSlider>{

    private RowImageSliderBinding binding;
    private static final String TAG = "IMAGE_SLIDER_TAG";
    private Context context;
    private ArrayList<ModelImageSlider> imageSliderArrayList;

    public AdapterImageSlider(Context context, ArrayList<ModelImageSlider> imageSliderArrayList) {
        this.context = context;
        this.imageSliderArrayList = imageSliderArrayList;
    }

    @NonNull
    @Override
    public HolderImageSlider onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = RowImageSliderBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderImageSlider(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderImageSlider holder, int position) {
        //get data from particular position of list and set to the ui views of row_ad.xml
        //and handle clicks
        ModelImageSlider modelImageSlider = imageSliderArrayList.get(position);

        //get url of the image
        String imageUrl = modelImageSlider.getImageUrl();
        //show current image/total images e.g. 1/3 here is 1 is current image and 3 is total images
        String imageCount = (position + 1) +"/" +imageSliderArrayList.size();

        //set image count
        holder.imageCountTv.setText(imageCount);
        //set image
        try {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.image)
                    .into(holder.imageIv);
        } catch (Exception e){
            Log.e(TAG, "onBindViewHolder: ", e);
        }

        //handle image click, open in fullscreen e.g. ImageViewActivity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return imageSliderArrayList.size();
    }

    class HolderImageSlider extends RecyclerView.ViewHolder{

        //ui views of the row_ad.xml
        ShapeableImageView imageIv;
        TextView imageCountTv;
        public HolderImageSlider(@NonNull View itemView) {
            super(itemView);
            //init views of row_ad.xml
            imageIv = binding.imageIv;
            imageCountTv = binding.imageCountTv;
        }
    }
}
