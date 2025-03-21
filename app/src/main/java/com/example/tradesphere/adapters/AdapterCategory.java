package com.example.tradesphere.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradesphere.RvListenerCategory;
import com.example.tradesphere.databinding.RowCategoryBinding;
import com.example.tradesphere.models.ModelCategory;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.Random;

//custom adapter class for Ad Categories list to show in recycler view (horizontal)
public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.HolderCategory>{

    private RowCategoryBinding binding;

    //context of activity/fragment from where instance of AdapterCategory class is created
    private Context context;

    //categoryArrayList - the list of the categories
    private ArrayList<ModelCategory> categoryArrayList;

    //rvListenerCategory interface to handle the category click event in it's calling
    //instead of this class
    private RvListenerCategory rvListenerCategory;

    public AdapterCategory(Context context, ArrayList<ModelCategory> categoryArrayList, RvListenerCategory rvListenerCategory) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.rvListenerCategory = rvListenerCategory;
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate/bind the row_category.xml
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCategory holder, int position) {

        //get data from particular position of list and set to the UI views of
        //row_category.xml, handle clicks
        ModelCategory modelCategory = categoryArrayList.get(position);

        //get data from modelCategory
        String category = modelCategory.getCategory();
        int icon = modelCategory.getIcon();

        //get random color to set as background of the categoryIconTv
        Random random = new Random();
        int color = Color.argb(255, random.nextInt(255), random.nextInt(255), random.nextInt(255));

        //set data to UI views of row_category.xml
        holder.categoryIconIv.setImageResource(icon);
        holder.categoryTitleTv.setText(category);
        holder.categoryIconIv.setBackgroundColor(color);

        //handle item click, call interface (RvListenerCategory) method to perform
        //click in calling activity/fragment class instead of this class
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                rvListenerCategory.onCategoryClick(modelCategory);
            }
        });

    }

    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    class HolderCategory extends RecyclerView.ViewHolder{

        //UI views of the row_category.xml
        ShapeableImageView categoryIconIv;
        TextView categoryTitleTv;
        public HolderCategory(@NonNull View itemView) {
            super(itemView);

            //init UI views of the row_category.xml
            categoryIconIv = binding.categoryIconIv;
            categoryTitleTv = binding.categoryTitleTv;

        }
    }
}
