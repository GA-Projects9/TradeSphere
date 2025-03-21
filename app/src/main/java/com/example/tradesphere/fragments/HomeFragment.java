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

import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.tradesphere.R;
import com.example.tradesphere.RvListenerCategory;
import com.example.tradesphere.Utils;
import com.example.tradesphere.adapters.AdapterAd;
import com.example.tradesphere.adapters.AdapterCategory;
import com.example.tradesphere.databinding.FragmentHomeBinding;
import com.example.tradesphere.models.ModelAd;
import com.example.tradesphere.models.ModelCategory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private static final String TAG = "HOME_TAG";
    private Context mContext;
    private ArrayList<ModelAd> adArrayList;
    private AdapterAd adapterAd;

    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(LayoutInflater.from(mContext), container, false);


        List<SlideModel> slideModel = new ArrayList<>();

        slideModel.add(new SlideModel(R.drawable.one, ScaleTypes.FIT));
        slideModel.add(new SlideModel(R.drawable.two, ScaleTypes.FIT));
        slideModel.add(new SlideModel(R.drawable.three, ScaleTypes.FIT));

        binding.imageSlider.setImageList(slideModel, ScaleTypes.FIT);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadCategories();
        loadAds("All");

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged: Query: " +s);

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

    private void loadCategories() {

        //init category arraylist
        ArrayList<ModelCategory> categoryArrayList = new ArrayList<>();

        //model category instance to show all products
        ModelCategory modelCategoryAll = new ModelCategory("All", R.drawable.ic_category_all);
        categoryArrayList.add(modelCategoryAll);

        //get categories from utils class and add in categoryArrayList
        for (int i = 0; i< Utils.categories.length; i++) {
            //model instance to get/hold category from current index
            ModelCategory modelCategory = new ModelCategory(Utils.categories[i], Utils.categoryIcons[i]);
            //add model category to categoryArrayList
            categoryArrayList.add(modelCategory);
        }

        //init setup AdapterCategory
        AdapterCategory adapterCategory = new AdapterCategory(mContext, categoryArrayList, new RvListenerCategory() {
            @Override
            public void onCategoryClick(ModelCategory modelCategory) {

                loadAds(modelCategory.getCategory());
            }
        });

        //set adapter to the RecyclerView i.e. categoriesRv
        binding.categoriesRv.setAdapter(adapterCategory);
    }

    private void loadAds(String category) {
        Log.d(TAG, "loadAds: Category" +category);

        adArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear adArrayList each time starting adding data into it
                adArrayList.clear();
                //load ads list
                for (DataSnapshot ds: snapshot.getChildren()){
                    //prepare modelAd with all data from Firebase DB
                    ModelAd modelAd = ds.getValue(ModelAd.class);

                    if (category.equals("All")){
                        //category all is selected
                        //add to list
                        adArrayList.add(modelAd);
                    } else {
                        //some category is selected e.g. furniture
                        if (modelAd.getCategory().equals(category)){
                            adArrayList.add(modelAd);
                        }
                    }
                }

                adapterAd = new AdapterAd(mContext,  adArrayList);
                binding.adsRv.setAdapter(adapterAd);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}