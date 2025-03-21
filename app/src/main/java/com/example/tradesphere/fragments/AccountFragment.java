package com.example.tradesphere.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.tradesphere.R;
import com.example.tradesphere.Utils;
import com.example.tradesphere.activities.ChangePasswordActivity;
import com.example.tradesphere.activities.DeleteAccountActivity;
import com.example.tradesphere.activities.MainActivity;
import com.example.tradesphere.activities.ProfileEditActivity;
import com.example.tradesphere.databinding.FragmentAccountBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private static final String TAG = "ACCOUNT_TAG";

    //context for this fragment class
    private Context mContext;
    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    @Override
    public void onAttach(@NonNull Context context) {

        //get and innit the context for this fragment class
        mContext = context;
        super.onAttach(context);
    }

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAccountBinding.inflate(LayoutInflater.from(mContext), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        loadMyInfo();

        binding.logoutCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseAuth.signOut();

                startActivity(new Intent(mContext, MainActivity.class));
                getActivity().finishAffinity();
            }
        });

        binding.editProfileCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, ProfileEditActivity.class));
            }
        });

        //handle changePasswordCv click, start ForgotPasswordActivity
        binding.changePasswordCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, ChangePasswordActivity.class));
            }
        });

        //handle verify account click, start verification of user
        binding.verifyAccountCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                verifyAccount();
            }
        });

        //handle deleteAccountCv click, start DeleteAccountActivity
        binding.deleteAccountCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start delete account activity
                startActivity(new Intent(mContext, DeleteAccountActivity.class));
                getActivity().finishAffinity();
                //remove all activities from backstack because we're deleting user and it's data,
                //so it may produce null exceptions if we don't
            }
        });
    }

    private void loadMyInfo(){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //get user info
                        String dob = ""+snapshot.child("dob").getValue();
                        String email = ""+snapshot.child("email").getValue();
                        String name = ""+snapshot.child("name").getValue();
                        String phoneCode = ""+snapshot.child("phoneCode").getValue();
                        String phoneNumber = ""+snapshot.child("phoneNumber").getValue();
                        String profileImageUrl = ""+snapshot.child("profileImageUrl").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String userType = ""+snapshot.child("userType").getValue();

                        //concatenate phone code and phone no to get full number
                        String phone = phoneCode + phoneNumber;

                        ////to avoid null or format exceptions
                        if (timestamp.equals("null")){
                            timestamp = "0";
                        }

                        //format timestamp to dd/MM/yyyy
                        String formattedDate = Utils.formatTimeStampDate(Long.parseLong(timestamp));

                        //set data to UI
                        binding.emailTv.setText(email);
                        binding.nameTv.setText(name);
                        binding.dobTv.setText(dob);
                        binding.phoneTv.setText(phone);
                        binding.memberSinceTv.setText(formattedDate);

                        //check userType: that is: phone, email google etc.
                        //in case of phone/google, account is already verified
                        //but in case of email, user has to verify
                        if (userType.equals("Email")) {
                            //userType is email, have to check if verified or not
                            boolean isVerified = firebaseAuth.getCurrentUser().isEmailVerified();
                            if (isVerified) {
                                //verified, hide the verified account option
                                binding.verifyAccountCv.setVisibility(View.GONE);
                                binding.verificationTv.setText("Verified");
                            } else {
                                //not verified
                                binding.verifyAccountCv.setVisibility(View.VISIBLE);
                                binding.verificationTv.setText("Not Verified");
                            }
                        } else {
                            //userType is google/phone, no need to check if verified or not
                            //since they're already verified
                            binding.verifyAccountCv.setVisibility(View.GONE);
                            binding.verificationTv.setText("Verified");
                        }

                        try {

                            //set profile image to profileTv
                            Glide.with(mContext)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.person)
                                    .into(binding.profileTv);

                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void verifyAccount(){

        Log.d(TAG, "verifyAccount: ");

        progressDialog.setMessage("Sending account verification instructions to your email");
        progressDialog.show();

        //send account/email verification instructions to the registered email
        firebaseAuth.getCurrentUser().sendEmailVerification()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        //instructions sent, check email, sometimes it appears in the spam folder too
                        Log.d(TAG, "onSuccess: Sent");
                        progressDialog.dismiss();
                        Utils.toast(mContext, "Account verification instructions sent to your email");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Utils.toast(mContext, "Failed due to "+e.getMessage());
                    }
                });

    }
}