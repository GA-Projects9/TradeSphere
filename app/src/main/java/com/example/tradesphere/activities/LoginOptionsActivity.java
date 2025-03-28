package com.example.tradesphere.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.tradesphere.LoginEmailActivity;
import com.example.tradesphere.R;
import com.example.tradesphere.Utils;
import com.example.tradesphere.databinding.ActivityLoginOptionsBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginOptionsActivity extends AppCompatActivity {

    private ActivityLoginOptionsBinding binding;
    private static final String TAG = "LOGIN_OPTIONS_TAG";
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginOptionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //handle close button click, go back
        binding.closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();

            }
        });

        binding.loginEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginOptionsActivity.this, LoginEmailActivity.class);
                startActivity(intent);
            }
        });

        binding.loginPhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginOptionsActivity.this, LoginPhoneActivity.class));
            }
        });

        binding.loginGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                beginGoogleLogin();

            }
        });
    }

    private void beginGoogleLogin() {

        Log.d(TAG, "beginGoogleLogin: ");

        Intent googleSignInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInnARL.launch(googleSignInIntent);

    }

    private ActivityResultLauncher<Intent> googleSignInnARL = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    Log.d(TAG, "onActivityResult: ");

                    if (result.getResultCode() == Activity.RESULT_OK) {

                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

                        try {
                            //google sign in was successful, then authenticate with firebase
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            Log.d(TAG, "onActivityResult: Account ID: " + account.getId());
                            firebaseAuthWithGoogleAccount(account.getIdToken());

                        } catch (ApiException e) { //google sign in failed, update ui accordingly
                            Log.e(TAG, "onActivityResult: ", e);
                        }
                    } else { //cancelled from google signIn options/configuration dialog
                        Log.d(TAG, "onActivityResult: Cancelled");
                        Utils.toast(LoginOptionsActivity.this, "Cancelled...");
                    }

                }
            });

    private void firebaseAuthWithGoogleAccount(String idToken) {

        Log.d(TAG, "firebaseAuthWithGoogleAccount: idToken: " + idToken);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        //sign in to firebase auth using Google credentials
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
//signin success, we check if the user is new (new account register) or existing (existing login)
                        if (authResult.getAdditionalUserInfo().isNewUser()) {

                            Log.d(TAG, "onSuccess: New User, Account created...");
                            //new user, account created. saving user info to firebase realtime db
                            updateUserInfoDb();

                        } else {

                            Log.d(TAG, "onSuccess: Existing User, Logged In...");
                            //new user, account created. no need to save user info to firebase db, starting MainActivity
                            startActivity(new Intent(LoginOptionsActivity.this, MainActivity.class));
                            finishAffinity();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.e(TAG, "onFailure: ", e);

                    }
                });
    }

    private void updateUserInfoDb() {

        Log.d(TAG, "updateUserInfoDb: ");

        progressDialog.setMessage("Saving User Info");
        progressDialog.show();

        //get current timestamp to show user registration date/time
        long timestamp = Utils.getTimestamp();
        //get email of registered user
        String registerUserEmail = firebaseAuth.getCurrentUser().getEmail();
        //get uid of registered user
        String registerUserUid = firebaseAuth.getUid();
        //since each user has name, we can get it to save in firebase db
        String name = firebaseAuth.getCurrentUser().getDisplayName();


        //setup data to save in firebase realtime db. most of the data will be empty and set in edit profile
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", "" + name);
        hashMap.put("phoneCode", "");
        hashMap.put("phoneNumber", "");
        hashMap.put("profileImageUrl", "");
        hashMap.put("dob", "");
        hashMap.put("userType", "Google"); //possible values: email, phone, google
        hashMap.put("typingTo", "");
        hashMap.put("timestamp", timestamp);
        hashMap.put("onlineStatus", true);
        hashMap.put("email", registerUserEmail);
        hashMap.put("uid", registerUserUid);


        //set data to firebase db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(registerUserUid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Log.d(TAG, "onSuccess: User info saved...");
                        progressDialog.dismiss();

                        startActivity(new Intent(LoginOptionsActivity.this, MainActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Utils.toast(LoginOptionsActivity.this, "Failed to save user info due to " + e.getMessage());
                    }
                });

    }

}