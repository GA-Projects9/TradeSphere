package com.example.tradesphere.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import com.example.tradesphere.Utils;
import com.example.tradesphere.databinding.ActivityForgotPasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;

    //TAG for logs in logcat
    private static final String TAG = "FORGOT_PASS_TAG";
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private String email = "";
    private void validateData() {
        Log.d(TAG, "validateData: ");
        //input data
        email = binding.emailEt.getText().toString().trim();

        Log.d(TAG,"validateData: email: "+email);

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            //invalid email pattern, show error in emailEt
            binding.emailEt.setError("Invalid Email Pattern!");
            binding.emailEt.requestFocus();
        } else {
            //email pattern is valid, send password recovery instructions
            sendPasswordRecoveryInstructions();
        }

    }
    private void sendPasswordRecoveryInstructions(){

        progressDialog.setMessage("Sending password recovery instructions to "+email);
        progressDialog.show();

        //send password recovery instructions, pass the input email as param
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //instructions sent, check email, sometimes it appears in spam folder too
                        progressDialog.dismiss();
                        Utils.toast(ForgotPasswordActivity.this,"Instructions to reset password is sent to "+email);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to send instructions
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Utils.toast(ForgotPasswordActivity.this,"Failed to send due to "+e.getMessage());
                    }
                });

    }
}