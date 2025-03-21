package com.example.tradesphere;

import android.widget.Filter;

import com.example.tradesphere.adapters.AdapterChats;
import com.example.tradesphere.models.ModelChats;

import java.util.ArrayList;

public class FilterChats extends Filter {

    private AdapterChats adapter;
    private ArrayList<ModelChats> filterList;

    public FilterChats(AdapterChats adapter, ArrayList<ModelChats> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {

        //perform filter based on what user types
        FilterResults results = new FilterResults();

        if (constraint != null && constraint.length() > 0) {
            //the search query is not null and not empty, we can perform filter, convert the typed query to upper case
            //to make search not case sensitive e.g. gayathri > GAYATHRI
            constraint = constraint.toString().toUpperCase();

            //hold the filtered list of ads based on user searched query
            ArrayList<ModelChats> filteredModels = new ArrayList<>();
            for (int i=0; i<filterList.size(); i++) {
                //ad filter based on receipt user name. if matches, add to the
                //filtered models list
                if (filterList.get(i).getName().toUpperCase().contains(constraint)) {
                    //filter matched add to filtered models list
                    filteredModels.add(filterList.get(i));
                }
            }
            //the search query has matched items, we can perform filter, return filteredModels list
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
        adapter.chatsArrayList = (ArrayList<ModelChats>)  results.values;

        adapter.notifyDataSetChanged();
    }
}
