package com.example.tradesphere;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

//a class that will contain static functions, constants, variables that
// will be used in the whole application
public class Utils {

    public static final String MESSAGE_TYPE_TEXT = "TEXT";
    public static final String MESSAGE_TYPE_IMAGE = "IMAGE";
    public static final String AD_STATUS_AVAILABLE = "AVAILABLE";
    public static final String AD_STATUS_SOLD = "SOLD";
    public static final String NOTIFICATION_TYPE_NEW_MESSAGE = "NEW_MESSAGE";
    public static final String FCM_SERVER_KEY = "AAAAV1qTFt0:APA91bGBRSaSmOSpkIEetnp0KS9XtoQPU71N5mmiOfpFJxTF_6daFJJ-qE0MULPInWAF9bFejUT0SBxim6y2uDP3u18tCaxtV59ysaZXScK3zkNiSnig7zDKEcmqHiMFjx2hVaj9RW_W";

    //categories array of the ad
    public static final String[] categories = {
            "Mobiles",
            "Laptops",
            "Electronics",
            "Vehicles",
            "Furniture & Home Decor",
            "Fashion",
            "Books",
            "Sports",
            "Pets",
            "Property For Rent",
            "Beauty"
    };

    //category icon array of ads
    public static final int[] categoryIcons = {
      R.drawable.ic_category_mobiles,
      R.drawable.ic_category_computer,
      R.drawable.ic_category_electronics,
      R.drawable.ic_category_vehicles,
      R.drawable.ic_category_furniture,
      R.drawable.ic_category_fashion,
      R.drawable.ic_category_books,
      R.drawable.ic_category_sports,
      R.drawable.ic_category_animals,
      R.drawable.ic_category_business,
      R.drawable.ic_category_lipstick
    };


    //conditions array of the ad
    public static final String[] conditions = {"New", "Used", "Refurbished"};

    //age array of the ad
    public static final String[] age = {"Brand New", "0-1 month", "1-6 months",
    "6-12 months", "1-2 years", "2-5 years", "5-10 years", "10+ years"};


    //a function to show toast
//@param context - the context of activity/fragment from where this function will be called
//@param message - the message to be shown in the toast
    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

//function to return current timestamp
//@return the current timestamp as long datatype
    public static long getTimestamp() {

        return System.currentTimeMillis();
    }



//a function to show toast
//timestamp - the timestamp of type long that we need to format to dd/MM/yyyy
//@return timestamp formatted to date dd/MM/yyyy

    public static String formatTimeStampDate(Long timestamp){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy", calendar).toString();

        return date;
    }

    //a function to show toast
//timestamp - the timestamp of type long that we need to format to dd/MM/yyyy hh:mm:a
//@return timestamp formatted to date dd/MM/yyyy hh:mm:a

    public static String formatTimeStampDateTime(Long timestamp){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy hh:mm:a", calendar).toString();

        return date;
    }


    public static void addToFavorite(Context context, String adId) {

        //add only if user is logged in
        //check if user is logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){
            //not logged in, can't add to favorite
            Utils.toast(context, "You're not logged in!");
        } else {
            //logged in, can add to favorite
            //get timestamp
            long timestamp = Utils.getTimestamp();

            //setup data to add in firebase database
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("adId", adId);
            hashMap.put("timestamp", timestamp);

            //add data to db. users > uid > favorites > adId > favorite
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(adId)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Utils.toast(context, "Added to favorites...!");
                        }
                    });
        }
    }

    public static void removeFromFavorite(Context context, String adId){

        //we can only add if user is logged in
        //1) check if user is logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){
            //not logged in, can't remove from favorite
            Utils.toast(context, "You're not logged in!");
        } else {

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(adId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //success
                            Utils.toast(context, "Removed from favorite");
                        }
                    });
        }
    }


    //generate chat path
    //this will generate chat path by sorting these uids and concatenate sorted arrays of uids having _ in between
    //all messages of this user will be saved in this path
    public static String chatPath(String receiptUid, String yourUid) {
       //array of uids
        String [] arrayUids = new String[]{receiptUid, yourUid};
        //sort array
        Arrays.sort(arrayUids);
        //concatenate path uids (after sorting) having _ between
        String chatPath = arrayUids[0] + "_" + arrayUids[1];
        //return chat path e.g. if receipt uid = aftDJsdk49kksjk and yourUid = dojodjoajdoaawie
        //then chat path = dojodjoajdoaawie_aftDJsdk49kksjk
        return chatPath;
    }


    //launch call intent with phone number
    public static void callIntent(Context context, String phone){

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:"+Uri.encode(phone)));
        context.startActivity(intent);
    }


    //launch sms intent with phone number
    public static void smsIntent(Context context, String phone){

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"+Uri.encode(phone)));
        context.startActivity(intent);
    }

    //launch google maps with input location
    public static void mapIntent(Context context, double latitude, double longitude){

        Uri gmmIntentUri = Uri.parse("http://maps.google.com/maps?daddr=" + latitude +","+longitude);

        //create an intent from gmmIntentUri. set the action to actionView
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        //make the intent explicit by setting the google maps package
        if (mapIntent.resolveActivity(context.getPackageManager()) != null){
            //google maps installed, start
            context.startActivity(mapIntent);
        } else {
            //google map not installed, can't start
            Utils.toast(context, "Google MAP not installed");
        }
    }

}
