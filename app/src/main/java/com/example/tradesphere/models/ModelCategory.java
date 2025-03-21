package com.example.tradesphere.models;

//model class for Ad Categories list to show in recycler view (horizontal)
public class ModelCategory {

    //variables
    String category;
    int icon;

    //constructor will all params
    public ModelCategory(String category, int icon) {
        this.category = category;
        this.icon = icon;
    }

    //getter and setter to get and set items to/from model of list
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
