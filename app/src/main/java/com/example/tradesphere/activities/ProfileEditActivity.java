package com.example.tradesphere.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.example.tradesphere.R;
import com.example.tradesphere.Utils;
import com.example.tradesphere.databinding.ActivityProfileEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class ProfileEditActivity extends AppCompatActivity {

    private ActivityProfileEditBinding binding;
    private static final String TAG = "PROFILE_EDIT_TAG";
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private Uri imageUri = null;
    private String myUserType = "";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        loadMyInfo();

        //handle toolbar click, go back
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.profileImagePickFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imagePickDialog();
            }
        });

        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private String name = "";
    private String email = "";
    private String dob = "";
    private String phoneCode = "";
    private String phoneNumber = "";
    private void validateData() {

        //input data
        name = binding.nameEt.getText().toString().trim();
        dob = binding.dobEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        phoneCode = binding.countryCodePicker.getSelectedCountryCodeWithPlus();
        phoneNumber = binding.phoneNumberEt.getText().toString().trim();

        //validate data
        if (imageUri == null) { //no image to upload to storage, just update db

            updateProfileDb(null);
        }
        else { //image need to upload to storage, first upload storage, then update db
            uploadProfileImageStorage();
        }
    }

    private void uploadProfileImageStorage(){

        Log.d(TAG, "uploadProfileImageStorage: ");

        //show progress
        progressDialog.setMessage("Uploading user profile image...");
        progressDialog.show();

        //set up image name and path e.g. UserImages/profile_useruid
        String filePathAndName = "User Images/"+"profile "+firebaseAuth.getUid();

        //storage reference to upload image
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putFile(imageUri)
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        Log.d(TAG, "onProgress: Progress: "+progress);

                        progressDialog.setMessage("Uploading profile image. Progress: " + '%');
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image uploaded successfully, get url of uploaded image
                        Log.d(TAG, "onSuccess: Uploaded");

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                        while (!uriTask.isSuccessful()) ;
                        String uploadedImageUrl = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()) {
                            updateProfileDb(uploadedImageUrl);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Utils.toast(ProfileEditActivity.this, "Failed to upload image due to "+e.getMessage());
                    }
                });
    }

    private void updateProfileDb(String imageUrl){

        progressDialog.setMessage("Updating user info...");
        progressDialog.show();


        //set up data in hashmap to update to firebase db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", "" + name);
        hashMap.put("dob", "" + dob);
        if (imageUrl != null) { //update profileimageUrl in db only if uploaded image url is not null
            hashMap.put("profileImageUrl", "" + imageUrl);
        }
        //is user type is phone, then allow to update email
        //otherwise, (in case of google or email), allow to update phone
        if (myUserType.equalsIgnoreCase("Phone")){
            hashMap.put("email", ""+email);
        }
        else if (myUserType.equalsIgnoreCase("Email") || myUserType.equalsIgnoreCase("Google")){
            hashMap.put("phoneCode", phoneCode);
            hashMap.put("phoneNumber", phoneNumber);

        }

        //database reference of user to update info
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //updated successfully

                        Log.d(TAG, "onSuccess: Info Updated: ");
                        progressDialog.dismiss();
                        Utils.toast(ProfileEditActivity.this, "Profile Updated...");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        //failed to update
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Utils.toast(ProfileEditActivity.this, "Failed to update due to "+e.getMessage());
                    }
                });
    }

    private void loadMyInfo(){
        Log.d(TAG, "loadMyInfo: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String dob = ""+ snapshot.child("dob").getValue();
                        String email = ""+snapshot.child("email").getValue();
                        String name = ""+snapshot.child("name").getValue();
                        String phoneCode = ""+snapshot.child("phoneCode").getValue();
                        String phoneNumber = ""+snapshot.child("phoneNumber").getValue();
                        String profileImageUrl = ""+snapshot.child("profileImageUrl").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        myUserType = ""+ snapshot.child("userType").getValue();

                        String phone = phoneCode + phoneNumber;

                        //check userType: if email/google, then don't allow user to edit/update email
                        if(myUserType.equalsIgnoreCase("Email") || myUserType.equalsIgnoreCase("Google")){

                            binding.emailTil.setEnabled(false);
                            binding.emailEt.setEnabled(false);
                        }

                        else {

                            //userType is phone, don't allow to edit phone
                            binding.phoneNumberTil.setEnabled(false);
                            binding.phoneNumberEt.setEnabled(false);
                            binding.countryCodePicker.setEnabled(false);
                        }

                        //set data to ui
                        binding.emailEt.setText(email);
                        binding.dobEt.setText(dob);
                        binding.nameEt.setText(name);
                        binding.phoneNumberEt.setText(phoneNumber);
                        try {
                            int phoneCodeInt = Integer.parseInt(phoneCode.replace("+", "")); //eg: +92 ---> 92
                            binding.countryCodePicker.setCountryForPhoneCode(phoneCodeInt);
                        }
                        catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }
                        try {

                            Glide.with(ProfileEditActivity.this)
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

    private void imagePickDialog() {
        PopupMenu popupMenu = new PopupMenu(this, binding.profileImagePickFab);

        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int itemId = item.getItemId();
                if (itemId == 1) {

                    //camera is clicked, we need to check if we have permission of camera,
                    //storage, before launching camera to capture image
                    Log.d(TAG, "onMenuItemClick: Camera clicked, check if camera permission is granted or not");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        //device permission is tiramisu or above, we only need camera permission
                        requestCameraPermissions.launch(new String[]{Manifest.permission.CAMERA});

                    }
                    else {
                        //device permission is below tiramisu, we need camera and storage permissions
                        requestCameraPermissions.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});


                    }
                }
                else if (itemId == 2) {

                    Log.d(TAG, "onMenuItemClick: Check if storage permission is granted or not");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                        pickImageGallery();

                    } else {
                        requestStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    }
                }
                return false;
            }
        });
    }

    private ActivityResultLauncher<String[]> requestCameraPermissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Log.d(TAG, "onActivityResult: " + result.toString());

                    //checking if permissions are granted or not
                    boolean areAllGranted = true;
                    for (Boolean isGranted: result.values()) {

                        areAllGranted = areAllGranted && isGranted;

                    }
                    if (areAllGranted) {
                    //if all are granted, we can now launch camera to capture image
                        Log.d(TAG, "onActivityResult: All Granted e.g. Camera, Storage");
                        pickImageCamera();

                    } else {
                        Log.d(TAG, "onActivityResult: All or either one is denied");
                        Utils.toast(ProfileEditActivity.this, "Camera or Storage or both permissions denied...");

                    }
                }
            }
    );

    private ActivityResultLauncher<String> requestStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {

                    //checking if permission is granted or not
                    Log.d(TAG, "onActivityResult: isGranted: "+isGranted);

                    if (isGranted){
                        //storage permission granted, we can launch gallery to pick image
                        pickImageGallery();

                    } else {
                        //storage permission denied, cannot launch gallery to pick image
                        Utils.toast(ProfileEditActivity.this, "Storage permission denied...!");
                    }

                }
            }
    );

    private void pickImageCamera(){
        Log.d(TAG, "pickImageCamera: ");

        //setup content values, mediastore to capture high quality image using camera intent
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMP_TITLE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMP_DESCRIPTION");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        //intent to launch camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    //check if image is captured or not
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //image captured, we have image in imageUri as assigned in pickImageCamera()
                        Log.d(TAG, "onActivityResult: Image Captured: " + imageUri);

                        //set to profiletv
                        try {
                            Glide.with(ProfileEditActivity.this)
                                    .load(imageUri)
                                    .placeholder(R.drawable.person)
                                    .into(binding.profileTv);
                        } catch (Exception e) {
                            Log.e(TAG, "onActivityResult: ", e);
                        }
                    }
                        else {
                            Utils.toast(ProfileEditActivity.this, "Cancelled...");
                        }
                    }
            }
    );

    private void pickImageGallery(){
        Log.d(TAG, "pickImageGallery: ");

        //intent to launch Image Picker e.g. Gallery
        Intent intent = new Intent(Intent.ACTION_PICK);

        //we only want to pick images
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);

    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //check if image is picked or not
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //get data
                        Intent data = result.getData();
                        //get uri of image picked
                        imageUri = data.getData();

                        Log.d(TAG, "onActivityResult: Image Picked From Gallery: "+imageUri);
                        //set to profileTv
                        try {

                            Glide.with(ProfileEditActivity.this)
                            .load(imageUri)
                                    .placeholder(R.drawable.person)
                                    .into(binding.profileTv);

                        }
                        catch (Exception e){
                            Log.e(TAG, "onActivityResult: ", e);
                        }
                    }
                    else {
                        Utils.toast(ProfileEditActivity.this, "Cancelled...");
                    }
                }
            }
    );
}