package com.example.tradesphere.activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.tradesphere.databinding.ActivityMainBinding;
import com.example.tradesphere.fragments.AccountFragment;
import com.example.tradesphere.fragments.ChatsFragment;
import com.example.tradesphere.fragments.HomeFragment;
import com.example.tradesphere.fragments.MyAdsFragment;
import com.example.tradesphere.R;
import com.example.tradesphere.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get instance of firebase auth for auth related tasks
        firebaseAuth = FirebaseAuth.getInstance();

        //checks if the user is logged in or not
        if(firebaseAuth.getCurrentUser() == null) {
            //user is not logged in, move to LoginOptionsActivity
            startLoginOptions();
        } else {

            //user logged in, ask notification permission and update fcm token
            updateFCMToken();
            askNotificationPermission();
        }

        //by default (when app opens), show HomeFragment
        showHomeFragment();

        binding.bottomNv.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            int itemId = item.getItemId();
            if (itemId == R.id.menu_home){
                //home item clicked. show HomeFragment

                showHomeFragment();
                return true;
            }
            else if (itemId == R.id.menu_chats){
                //home item clicked. show ChatsFragment

                if (firebaseAuth.getCurrentUser() == null){
                    Utils.toast(MainActivity.this, "Login Required");
                    startLoginOptions();

                    return false;
                }
                else {

                    showChatsFragment();
                    return true;

                }
            }
            else if (itemId == R.id.menu_my_ads){
                //home item clicked. show MyAdsFragment

                if (firebaseAuth.getCurrentUser() == null) {
                    Utils.toast(MainActivity.this, "Login Required");
                    startLoginOptions();

                    return false;
                } else {

                    showMyAdsFragment();
                    return true;

                }

            }
            else if (itemId == R.id.menu_account){
                //home item clicked. show AccountFragment

                if (firebaseAuth.getCurrentUser() == null) {
                    Utils.toast(MainActivity.this, "Login Required");
                    startLoginOptions();

                    return false;
                } else {

                    showAccountFragment();
                    return true;

                }

            }
            else {
                return false;
            }
            }
        });

        binding.sellFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, AdCreateActivity.class);
                intent.putExtra("isEditMode", false);
                startActivity(intent);
            }
        });
    }


    private void showHomeFragment() {

        //changing toolbar textview title/text to "Home" and likewise for others
        binding.toolbarTitleTv.setText("Home");

        //show HomeFragment
        HomeFragment fragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "HomeFragment");
        fragmentTransaction.commit();

    }

    private void showChatsFragment() {

        binding.toolbarTitleTv.setText("Chats");

        ChatsFragment fragment = new ChatsFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "ChatsFragment");
        fragmentTransaction.commit();

    }

    private void showMyAdsFragment() {

        binding.toolbarTitleTv.setText("My Ads");

        MyAdsFragment fragment = new MyAdsFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "MyAdsFragment");
        fragmentTransaction.commit();

    }

    private void showAccountFragment() {

        binding.toolbarTitleTv.setText("Account");

        AccountFragment fragment = new AccountFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "AccountFragment");
        fragmentTransaction.commit();

    }

    private void startLoginOptions() {
        Intent intent = new Intent(MainActivity.this, LoginOptionsActivity.class);
        startActivity(intent);
    }

    //FCM = firebase cloud messaging
    private void updateFCMToken() {

        String myUid = ""+firebaseAuth.getUid();

        //1.) get fcm token
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {

                        //setup data (fcmToken) to update to currently logged-in users db
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("fcmToken", token);

                        //update fcm token to firebase db
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(myUid)
                                .updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }
                                });
                    }
                });
    }

    private void askNotificationPermission() {

        //android 13/api 34/ tiramisu and above requires POST_NOTIFICATIONS permissions e.g. to show push notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //check if permission is granted or not
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {

                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

    }

    private ActivityResultLauncher<String> requestNotificationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {



                }
            }
    );
}