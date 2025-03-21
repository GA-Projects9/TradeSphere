package com.example.tradesphere.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.tradesphere.R;
import com.example.tradesphere.Utils;
import com.example.tradesphere.databinding.ActivityRegisterEmailBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterEmailActivity extends AppCompatActivity {

    private ActivityRegisterEmailBinding binding;
    private static final String TAG = "REGISTER_TAG";
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private String email, password, cPassword;

    private CardView frameOne, frameTwo, frameThree, frameFour;
    private boolean isAtLeast8 = false, hasUppercase = false, hasNumber = false, hasSymbol = false, isRegistrationClickable = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        frameOne = binding.frameOne;
        frameTwo = binding.frameTwo;
        frameThree = binding.frameThree;
        frameFour = binding.frameFour;

        //handle toolbarBackBtn click, go back
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });

        //handle haveAccountTv click, go back to LoginEmailActivity
        binding.haveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });

        //handle registerBtn click, start user registration
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email = binding.emailEt.getText().toString().trim();
                password = binding.passwordEt.getText().toString().trim();
                cPassword = binding.cPasswordEt.getText().toString().trim();

                if (email.length() > 0 && password.length() > 0 && cPassword.length() > 0) {
                    if (isRegistrationClickable) {
                        // Validate data only if registration is clickable
                        validateData();
                    } else {
                        Toast.makeText(RegisterEmailActivity.this, "Registration not clickable", Toast.LENGTH_LONG).show();
                    }
                } else {
                    if (email.length() == 0) {
                        binding.emailEt.setError("Please enter Email");
                    }
                    if (password.length() == 0) {
                        binding.passwordEt.setError("Please enter Password");
                    }
                    if (cPassword.length() == 0) {
                        binding.cPasswordEt.setError("Confirm Password");
                    }
                }
            }
        });

        inputChange();
    }

    @SuppressLint("ResourceType")
    private void checkAllData(String email) {
        if (isAtLeast8 && hasUppercase && hasNumber && hasSymbol && email.length() > 0) {
            isRegistrationClickable = true;
            binding.registerBtn.setTextColor(Color.WHITE);
            binding.registerBtn.setBackgroundColor(Color.parseColor(getString(R.color.colorAccent)));
        } else {
            isRegistrationClickable = false;
            binding.registerBtn.setBackgroundColor(Color.parseColor(getString(R.color.colorDefault)));
        }
    }

    @SuppressLint("ResourceType")
    private void registrationDataCheck() {
        String password = binding.passwordEt.getText().toString(), email = binding.emailEt.getText().toString(), cPassword = binding.cPasswordEt.getText().toString();

        if (password.length() >= 8) {
            isAtLeast8 = true;
            frameOne.setCardBackgroundColor(Color.parseColor(getString(R.color.colorAccent)));
        } else {
            isAtLeast8 = false;
            frameOne.setCardBackgroundColor(Color.parseColor(getString(R.color.colorDefault)));
        }
        if (password.matches("(.*[A-Z].*)")) {
            hasUppercase = true;
            frameTwo.setCardBackgroundColor(Color.parseColor(getString(R.color.colorAccent)));
        } else {
            hasUppercase = false;
            frameTwo.setCardBackgroundColor(Color.parseColor(getString(R.color.colorDefault)));
        }
        if (password.matches("(.*[0-9].*)")) {
            hasNumber = true;
            frameThree.setCardBackgroundColor(Color.parseColor(getString(R.color.colorAccent)));
        } else {
            hasNumber = false;
            frameThree.setCardBackgroundColor(Color.parseColor(getString(R.color.colorDefault)));
        }
        if (password.matches("^(?=.*[_.()$*=?^!#@&+-]).*$")) {
            hasSymbol = true;
            frameFour.setCardBackgroundColor(Color.parseColor(getString(R.color.colorAccent)));
        } else {
            hasSymbol = false;
            frameFour.setCardBackgroundColor(Color.parseColor(getString(R.color.colorDefault)));
        }

        checkAllData(email);
    }

    private void inputChange() {
        binding.emailEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                registrationDataCheck();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.passwordEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                registrationDataCheck();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void validateData(){

        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();
        cPassword = binding.cPasswordEt.getText().toString().trim();

        Log.d(TAG, "validateData: email: "+email);
        Log.d(TAG, "validateData: password: "+password);
        Log.d(TAG, "validateData: cPassword: "+cPassword);


        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            binding.emailEt.setError("Invalid Email Pattern");
            binding.emailEt.requestFocus();

        }
        else if (password.isEmpty()) {

            binding.passwordEt.setError("Enter Password");
            binding.passwordEt.requestFocus();

        }
        else if (!password.equals(cPassword)) {

            binding.cPasswordEt.setError("Password doesn't match");
            binding.cPasswordEt.requestFocus();

        }
        else {

            registerUser();

        }
    }

    private void registerUser() {

        progressDialog.setMessage("Creating Account");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        //user info success, we also need to save user info to firebase db
                        Log.d(TAG, "onSuccess: RegisterSuccess");
                        updateUserInfo();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        //user register failed
                        Log.e(TAG, "onFailure: ", e);
                        Utils.toast(RegisterEmailActivity.this, "Failed due to "+e.getMessage());
                        progressDialog.dismiss();

                    }
                });
    }

    private void updateUserInfo() {

        progressDialog.setMessage("Saving User Info");

        //get current timestamp, eg, to show user registration date/time
        long timestamp = Utils.getTimestamp();
        String registerUserEmail = firebaseAuth.getCurrentUser().getEmail();
        String registerUserUid = firebaseAuth.getUid();

        //setup data to save in firebase realtime db. most of the data will be \
        // empty and set in edit profile
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", "");
        hashMap.put("phoneCode", "");
        hashMap.put("phoneNumber", "");
        hashMap.put("profileImageUrl", "");
        hashMap.put("dob", "");
        hashMap.put("userType", "Email"); //possible values: email, phone, google
        hashMap.put("typingTo", "");
        hashMap.put("timestamp", timestamp);
        hashMap.put("onlineStatus", true);
        hashMap.put("email", registerUserEmail);
        hashMap.put("uid", registerUserUid);

        //set data in firebase db
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(registerUserUid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        //firebase db save success
                        Log.d(TAG, "onSuccess: Info saved...");

                        startActivity(new Intent(RegisterEmailActivity.this, MainActivity.class));
                        finishAffinity();


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        //firebase db save failure
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Utils.toast(RegisterEmailActivity.this, "Failed to save info due to "+e.getMessage());

                    }
                });


    }

}