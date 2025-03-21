package com.example.tradesphere.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.tradesphere.Utils;
import com.example.tradesphere.databinding.ActivityDeleteAccountBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DeleteAccountActivity extends AppCompatActivity {

    private ActivityDeleteAccountBinding binding;
    private static final String TAG = "DELETE_ACCOUNT_TAG";
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDeleteAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        //get instance of FirebaseUser to get current user and delete
        firebaseUser = firebaseAuth.getCurrentUser();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle submit button click, start account deletion
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount();
            }
        });
    }

    private void deleteAccount() {
        Log.d(TAG, "deleteAccount: ");

        String myUid = firebaseAuth.getUid();

        progressDialog.setMessage("Deleting User Account");
        progressDialog.show();

        firebaseUser.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //user account deleted
                        Log.d(TAG, "onSuccess: User Account Deleted");

                        progressDialog.setMessage("Deleting User Ads");

                        //step 2: remove user ads, ads will be saved in db > ads > adID
                        //each ad will contain the uid of the owner
                        DatabaseReference refUserAds = FirebaseDatabase.getInstance().getReference("Ads");
                        refUserAds.orderByChild("uid").equalTo(myUid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //there may be multiple ads that user needs to loop
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            //delete ad
                                            ds.getRef().removeValue();
                                        }

                                        progressDialog.setMessage("Deleting User Data");
                                        //step 2: removing user data. DB > Users > UserId
                                        DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("Users");
                                        refUsers.child(myUid)
                                                .removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        //account data deleted
                                                        Log.d(TAG, "onSuccess: User data deleted...");
                                                        startMainActivity();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                        //failed to delete data, maybe due to firebase rules and we have to make it
                                                        //public since we delete data after account
                                                        Log.e(TAG, "onFailure: ", e);
                                                        progressDialog.dismiss();
                                                        Utils.toast(DeleteAccountActivity.this, "Failed to delete user data. Please re-login and try again.");
                                                        startMainActivity();
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        //failed to delete user account, maybe user need to re-login for
                        //authentication purpose before deleting
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Utils.toast(DeleteAccountActivity.this, "Failed to delete account. Please re-login and try again.");
                    }
                });
    }

    private void startMainActivity() {
        Log.d(TAG, "startMainActivity: ");

        startActivity(new Intent(this, MainActivity.class));
        finishAffinity(); //clear backstack of activities
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
    }
}