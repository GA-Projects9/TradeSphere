package com.example.tradesphere.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.tradesphere.Utils;
import com.example.tradesphere.databinding.ActivityLoginPhoneBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class LoginPhoneActivity extends AppCompatActivity {

    private ActivityLoginPhoneBinding binding;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private PhoneAuthProvider.ForceResendingToken forceResendingToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private static final String TAG = "LOGIN_PHONE_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginPhoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //for the start phone show input UI and hide OTP UI
        binding.phoneInputRl.setVisibility(View.VISIBLE);
        binding.otpInputRl.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        phoneLoginCallBack();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //hande sendotpbtn to send otp to input phone number
        binding.sendOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        binding.resendOtpTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                resendVerificationCode(forceResendingToken);

            }
        });

        binding.verifyOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String otp = binding.pinFromUser.getText().toString().trim();

                Log.d(TAG, "onClick: OTP: "+otp);

                if (otp.isEmpty()){

                    binding.pinFromUser.setError("Enter OTP");
                    binding.pinFromUser.requestFocus();
                }
                else if (otp.length() <6) {

                    binding.pinFromUser.setError("OTP length must be 6 Characters");
                    binding.pinFromUser.requestFocus();
                }
                else {
                    verifyPhoneNumberWithCode(mVerificationId, otp);
                }
            }
        });
    }

    private String phoneCode = "", phoneNumber = "", phoneNumberWithCode = "";
    private void validateData() {

        //input data
        phoneCode = binding.phoneCodeTil.getSelectedCountryCodeWithPlus();
        phoneNumber = binding.phoneNumberEt.getText().toString().trim();
        phoneNumberWithCode = phoneCode+phoneNumber;

        Log.d(TAG, "validateData: phoneCode: "+phoneCode);
        Log.d(TAG, "validateData: phoneNumber: "+phoneNumber);
        Log.d(TAG, "validateData: phoneNumberWithCode: "+phoneNumberWithCode);

        //validate data
        if (phoneNumber.isEmpty()) { //if phone number is not shown, show error
            Utils.toast(this, "Please enter phone number");
        } else {
            startPhoneNumberVerification();
        }
    }

    private void startPhoneNumberVerification() {

        Log.d(TAG, "startPhoneNumberVerification: ");

        //show progress
        progressDialog.setMessage("Sending OTP to: "+phoneNumberWithCode);
        progressDialog.show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumberWithCode)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this) //activity for callback binding
                        .setCallbacks(mCallbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);


    }

    private void phoneLoginCallBack() {
        Log.d(TAG, "phoneLoginCallBack: ");

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted: ");
                //this callback will be invoked in two situations
                //1. instant verification - in some cases the phone number can be instantly verified
                //without needing to send or enter a verification code
                //2. auto retrieval - one some devices, google play services can automatically detect
                //the incoming verification SMS and perform verification without user action

                signinWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.e(TAG, "onVerificationFailed: ", e);
                //this callback is invoked in an invalid request for verification is made,
                //for instance if the phone number format is not valid
                progressDialog.dismiss();

                Utils.toast(LoginPhoneActivity.this, ""+e.getMessage());

            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, token);
                //the SMS verification code has been sent to the provided phone number, we
                //now need to ask the user to enter the code and then construct a credential
                //by combining the code with a verification ID

                //save verification Id and resending token so we can use them later
                mVerificationId = verificationId;
                forceResendingToken = token;

                //otp is sent to hide progress for now
                progressDialog.dismiss();

                //otp is sent so hide phone UI and show otp UI
                binding.phoneInputRl.setVisibility(View.INVISIBLE);
                binding.otpInputRl.setVisibility(View.VISIBLE);

                //show toast for successful sending of otp
                Utils.toast(LoginPhoneActivity.this, "OTP Sent to "+phoneNumberWithCode);

                //show user a message that the verification code has been sent to the no that the
                //user has input
                binding.loginLabelTv.setText("Please type the verification code sent to: "+phoneNumberWithCode);
            }
        };
    }

    private void verifyPhoneNumberWithCode(String verificationId, String otp) {
        Log.d(TAG, "verifyPhoneNumberWithCode: verificationId: "+verificationId);
        Log.d(TAG, "verifyPhoneNumberWithCode: otp: "+otp);

        progressDialog.setMessage("Verifying OTP");
        progressDialog.show();

        //phoneauthcredential with verification id and OTP to signin user with signinWithphoneauthcredential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);

        signinWithPhoneAuthCredential(credential);

    }

    private void resendVerificationCode(PhoneAuthProvider.ForceResendingToken token){

        Log.d(TAG, "resendVerificationCode: ForceResendingToken: "+ token);
        progressDialog.setMessage("Resending OTP to "+phoneNumberWithCode);
        progressDialog.show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumberWithCode)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this) //activity for callback binding
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(token)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);

    }
    private void signinWithPhoneAuthCredential(PhoneAuthCredential credential){

        Log.d(TAG, "signinWithPhoneAuthCredential: ");
        progressDialog.setMessage("Logging In");

        //sign in to firebase using phone credentials
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        Log.d(TAG, "onSuccess: ");

                        //sign in success, let's check if the user is new (new account register)
                        //or existing (existing login)
                        if (authResult.getAdditionalUserInfo().isNewUser()) {
                            //new user, acc created - let's save this info in firebase realtime db
                            Log.d(TAG, "onSuccess: New User, Account Created...");

                            updateUserInfoDb();

                        } else {

                            Log.d(TAG, "onSuccess: Existing User, Logged In");
                            //new user, account created - no need to save user info to firebase realtime db, start MainActivity
                            startActivity(new Intent(LoginPhoneActivity.this, MainActivity.class));
                            finishAffinity();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Utils.toast(LoginPhoneActivity.this, "Failed to login due to: "+e.getMessage());
                    }
                });
    }

    private void updateUserInfoDb(){
        Log.d(TAG, "updateUserInfoDb: ");
        progressDialog.setMessage("Saving User Info");
        progressDialog.dismiss();

        //saving user info in firebase database. key name is given the same
        // as register via email/google

        long timestamp = Utils.getTimestamp();
        String registerUserUid = firebaseAuth.getUid();

        //setup data to save in firebase realtime db. most of the data will be \
        // empty and set in edit profile
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", "");
        hashMap.put("phoneCode", ""+phoneCode);
        hashMap.put("phoneNumber", ""+phoneNumber);
        hashMap.put("profileImageUrl", "");
        hashMap.put("dob", "");
        hashMap.put("userType", "Phone"); //possible values: email, phone, google
        hashMap.put("typingTo", "");
        hashMap.put("timestamp", timestamp);
        hashMap.put("onlineStatus", true);
        hashMap.put("email", "");
        hashMap.put("uid", registerUserUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(registerUserUid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Log.d(TAG, "onSuccess: User info saved");
                        progressDialog.dismiss();

                        startActivity(new Intent(LoginPhoneActivity.this, MainActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Utils.toast(LoginPhoneActivity.this, "Failed to save user info due to "+e.getMessage());
                    }
                });


    }
}