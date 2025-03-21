package com.example.tradesphere;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import com.example.tradesphere.activities.ForgotPasswordActivity;
import com.example.tradesphere.activities.MainActivity;
import com.example.tradesphere.activities.RegisterEmailActivity;
import com.example.tradesphere.databinding.ActivityLoginEmailBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginEmailActivity extends AppCompatActivity {

    private ActivityLoginEmailBinding binding;
    private static final String TAG = "LOGIN_TAG";
    private ProgressDialog progressDialog;

    //firebase auth for auth related tasks
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get instance for firebase auth for auth related tasks
        firebaseAuth = FirebaseAuth.getInstance();

        //innit setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle toolbarBackBtn click, go back
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginEmailActivity.this, RegisterEmailActivity.class);
                startActivity(intent);
            }
        });

        //handle forgotpasswordTv click, open ForgotPasswordActivity to send password recovery
        //instructions to registered email
        binding.forgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginEmailActivity.this, ForgotPasswordActivity.class));
            }
        });

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private String email, password;


    private void validateData() {

        //input data
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();

        Log.d(TAG, "validateData: email: "+email);
        Log.d(TAG, "validateData: password: "+password);

        //validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            //email pattern is invalid, show error
            binding.emailEt.setError("Invalid Email");
            binding.emailEt.requestFocus();
        }
        else if (password.isEmpty()) {

            //password is not entered, show error
            binding.passwordEt.setError("Enter Password");
            binding.passwordEt.requestFocus();
        }
        else {

            //email pattern is entered and password is valid, start login
            loginUser();
        }
    }
    private void loginUser() {

        //show progress
        progressDialog.setMessage("Logging In");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        //user login success
                        Log.d(TAG, "onSuccess: Logged In...");
                        progressDialog.dismiss();

                        //start main activity
                        Intent intent = new Intent(LoginEmailActivity.this, MainActivity.class);
                        startActivity(intent);
                        finishAffinity(); //finish current and all activities from back stack
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        //user login logged
                        Utils.toast(LoginEmailActivity.this, "Account does not exist. Please try again.");
                        progressDialog.dismiss();

                    }
                });
    }
}