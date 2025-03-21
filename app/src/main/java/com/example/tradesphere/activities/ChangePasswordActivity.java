package com.example.tradesphere.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.tradesphere.Utils;
import com.example.tradesphere.databinding.ActivityChangePasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding binding;

    //TAG for logs in logcat
    private static final String TAG = "CHANGE_PASS_TAG";
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

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

    private String currentPassword = "";
    private String newPassword = "";
    private String confirmNewPassword = "";
    private void validateData() {

        Log.d(TAG, "validateData: ");

        currentPassword = binding.currentPasswordEt.getText().toString();
        newPassword = binding.newPasswordEt.getText().toString();
        confirmNewPassword = binding.confirmNewPasswordEt.getText().toString();

        Log.d(TAG,"validateData: currentPassword: "+currentPassword);
        Log.d(TAG,"validateData: newPassword: "+newPassword);
        Log.d(TAG,"validateData: confirmNewPassword: "+confirmNewPassword);


        //validate data
        if(currentPassword.isEmpty()){
            //current password field is empty, show error in current password et
            binding.currentPasswordEt.setError("Enter current password!");
            binding.currentPasswordEt.requestFocus();
        } else if (newPassword.isEmpty()){
            //new password field is empty, show error in new password et
            binding.newPasswordEt.setError("Enter cew password!");
            binding.newPasswordEt.requestFocus();
        } else if (confirmNewPassword.isEmpty()){
            //confirm new password field is empty, show error in confirm new password et
            binding.confirmNewPasswordEt.setError("Enter Confirm password!");
            binding.confirmNewPasswordEt.requestFocus();
        } else if (!newPassword.equals(confirmNewPassword)){
            //passwords in newPassword and confirmNewPassword are not the same,
            //then show error
            binding.confirmNewPasswordEt.setError("Password doesn't match!");
            binding.confirmNewPasswordEt.requestFocus();
        } else {
            //all data is validated, verify current password is
            //correct first before updating password
            authenticateUserForUpdatePassword();
        }

    }
    private void authenticateUserForUpdatePassword(){
        Log.d(TAG,"authenticateUserForUpdatePassword: ");

        progressDialog.setMessage("Authenticating User");
        progressDialog.show();

        //before changing password, reauthenticate the user to check if the user has entered
        //correct current password
        AuthCredential authCredential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), currentPassword);
        firebaseUser.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //successfully authenticated, begin update
                        updatePassword();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Toast.makeText(ChangePasswordActivity.this, "Failed to authenticate. Enter the correct current password", Toast.LENGTH_SHORT).show();
//                        Utils.toast(ChangePasswordActivity.this,"Failed to authenticate due to "+e.getMessage());

                    }
                });
    }
    private void updatePassword(){
        Log.d(TAG, "updatePassword: ");

        //show progress
        progressDialog.setMessage("Updating Password");
        progressDialog.show();

        //begin password update, add new password as parameter
        firebaseUser.updatePassword(newPassword)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        //password update success, you could do logout and move to login activity
                        //if you want
                        progressDialog.dismiss();
                        Utils.toast(ChangePasswordActivity.this,"Password Updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    //password update failure, show error message
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Utils.toast(ChangePasswordActivity.this,"Failed to update password due to "+e.getMessage());
                    }
                });

    }
}