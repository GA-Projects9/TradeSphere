package com.example.tradesphere;

import com.example.tradesphere.models.ModelCategory;

//interface to handle ad category click event in fragment (homeFragment) instead of
//adapter (AdapterCategory) class
public interface RvListenerCategory {

    void onCategoryClick(ModelCategory modelCategory);
}
