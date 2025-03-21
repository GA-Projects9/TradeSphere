package com.example.tradesphere;

import android.widget.Filter;

import com.example.tradesphere.adapters.AdapterAd;
import com.example.tradesphere.models.ModelAd;

import java.util.ArrayList;

public class FilterAd extends Filter {

    //declaring AdapterAd and ArrayList<ModelAd> instance that will be
    //initialized in the constructor of this class
    private AdapterAd adapter;
    private ArrayList<ModelAd> filterList;

    public FilterAd(AdapterAd adapter, ArrayList<ModelAd> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        //perform filter based on what the user type

        FilterResults results = new FilterResults();
        //the search query is not null and not empty, we can perform filter
        if (constraint != null && constraint.length() > 0){
            //convert the typed query to upper case to make search not case sensitive
            //e.g. Samsung S25 Ultra -> SAMSUNG S25 ULTRA
            constraint = constraint.toString().toUpperCase();
            //hold the filtered list of Ads based on user search query
            ArrayList<ModelAd> filteredModels = new ArrayList<>();
            for (int i=0; i<filterList.size(); i++) {
                //ad filter based on brand, category, condition, title,
                //if any of these matches, add to the filteredModels list
                if (filterList.get(i).getBrand().toUpperCase().contains(constraint) ||
                        filterList.get(i).getCategory().toUpperCase().contains(constraint) ||
                        filterList.get(i).getCondition().toUpperCase().contains(constraint) ||
                        filterList.get(i).getTitle().toUpperCase().contains(constraint)) {
                    //filter matched ads to filtered models list
                    filteredModels.add(filterList.get(i));
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;

        } else {
            //the search query is either null or empty, we can't perform filter
            //return full/original list
            results.count = filterList.size();
            results.values = filterList;

        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        //publish the filtered result

        adapter.adArrayList = (ArrayList<ModelAd>) results.values;

        adapter.notifyDataSetChanged();
    }
}
