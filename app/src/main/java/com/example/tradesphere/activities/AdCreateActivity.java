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
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.tradesphere.R;
import com.example.tradesphere.Utils;
import com.example.tradesphere.adapters.AdapterImagesPicked;
import com.example.tradesphere.databinding.ActivityAdCreateBinding;
import com.example.tradesphere.fragments.HomeFragment;
import com.example.tradesphere.models.ModelImagePicked;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdCreateActivity extends AppCompatActivity {

    private ActivityAdCreateBinding binding;
    private static final String TAG = "AD_CREATE_TAG";
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    //image uri to hold uri of image (picked/captured using gallery/camera) to add in Ad Images List
    private Uri imageUri = null;

    //list of images picked/captured using gallery/camera from internet
    private ArrayList<ModelImagePicked> imagePickedArrayList;
    private AdapterImagesPicked adapterImagesPicked;
    private boolean isEditMode = false;
    private String adIdForEditing = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        //setup and set the categories adapter to the category input field .i.e. categoryAct
        ArrayAdapter<String> adapterCategories = new ArrayAdapter<>(this, R.layout.row_category_act, Utils.categories);
        binding.categoryAct.setAdapter(adapterCategories);

        //setup and set the conditions adapter to the category input field .i.e. condtionAct
        ArrayAdapter<String> adapterConditions = new ArrayAdapter<>(this, R.layout.row_condition_act, Utils.conditions);
        binding.conditionAct.setAdapter(adapterConditions);

        ArrayAdapter<String> adapterAge = new ArrayAdapter<>(this, R.layout.row_age_act, Utils.age);
        binding.ageAct.setAdapter(adapterAge);

        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("isEditMode", false);

        if (isEditMode) {
            //edit Ad Model: get the ad id for editing the ad
            adIdForEditing = intent.getStringExtra("adId");
            //function call to load ad details by using Ad Id
            loadAdDetails();

            //change toolbar title and submit button text
            binding.toolbarTitleTv.setText("Update Ad");
            binding.postAdBtn.setText("Update Ad");

        } else {

            binding.toolbarTitleTv.setText("Create Ad");
            binding.postAdBtn.setText("Post Ad");
        }

        //init imagePickedArrayList
        imagePickedArrayList = new ArrayList<>();
        loadImages();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle toolbarAdImageBtn click, show image add options (gallery/camera)
        binding.toolbarAdImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickOptions();
            }
        });

        binding.postAdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private void loadImages() {
        Log.d(TAG, "loadImages: ");
        //init setup adapterImagesPicked to set it RecyclerView i.e. imageRv
        //param 1 is context, param 2 is Images List to show in RecyclerView
        adapterImagesPicked = new AdapterImagesPicked(this, imagePickedArrayList, adIdForEditing);
        //set the adapter to RecyclerView i.e. imagesRv
        binding.imagesRv.setAdapter(adapterImagesPicked);

    }

    private void showImagePickOptions() {
        Log.d(TAG, "showImagePickOptions");

        //init the popup menu. param1 is context, param2 is anchor view for this popup
        //the popup will appear below the anchor if there is room, or above it if there is not
        PopupMenu popupMenu = new PopupMenu(this, binding.toolbarAdImageBtn);

        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //get the id of the item clicked in popup menu
                int itemId = item.getItemId();
                //check which item id is clicked from the popup menu
                //1 - camera, 2 - gallery
                if (itemId == 1) {
                    //camera is selected

                    //camera is clicked, we need to check if we have permission of camera,
                    //storage, before launching camera to capture image
                    Log.d(TAG, "onMenuItemClick: Camera clicked, check if camera permission is granted or not");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        //device permission is tiramisu or above, we only need camera permission
                        String[] cameraPermissions = new String[]{android.Manifest.permission.CAMERA};
                        requestCameraPermissions.launch(cameraPermissions);

                    } else {
                        //device permission is below tiramisu, we need camera and storage permissions
                        String[] cameraPermissions = new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestCameraPermissions.launch(cameraPermissions);
                    }


                } else if (itemId == 2) {
                    //gallery is clicked

                    Log.d(TAG, "onMenuItemClick: Check if storage permission is granted or not");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                        pickImageGallery();

                    } else {
                        //device version is below tiramisu, we need storage permission to launch gallery
                        String storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                        requestStoragePermission.launch(storagePermission);

                    }
                }
                return true;
            }
        });

    }

    private ActivityResultLauncher<String> requestStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG, "onActivityResult: isGranted: " + isGranted);
                    //let's check if permission is granted or not
                    if (isGranted) {
                        //storage permission granted, we can now launch gallery to pick image
                        pickImageGallery();
                    } else {
                        //storage permission denied, we cannot launch gallery to pick image
                        Toast.makeText(AdCreateActivity.this, "Storage Permission Denied...", Toast.LENGTH_LONG).show();
                        //OR:  Utils.toast(AdCreateActivity.this, "Storage Permission Denied..."
                    }
                }
            }
    );

    private ActivityResultLauncher<String[]> requestCameraPermissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {

                    Log.d(TAG, "onActivityResult: ");
                    Log.d(TAG, "onActivityResult: " + result.toString());

                    //checking if permissions are granted or not
                    boolean areAllGranted = true;
                    for (Boolean isGranted : result.values()) {

                        areAllGranted = areAllGranted && isGranted;

                    }
                    if (areAllGranted) {
                        //if all permissions camera, we can now launch camera to capture image
                        Log.d(TAG, "onActivityResult: All Granted e.g. Camera, Storage");
                        pickImageCamera();

                    } else {
                        //camera or storage or both permissions are denied, can't launch camera
                        //to capture image
                        Log.d(TAG, "onActivityResult: All or either one is denied");
                        Utils.toast(AdCreateActivity.this, "Camera or Storage or both permissions denied...");

                    }

                }
            }
    );

    private void pickImageGallery() {
        Log.d(TAG, "pickImageGallery: ");

        //intent to launch Image Picker e.g. Gallery
        Intent intent = new Intent(Intent.ACTION_PICK);

        //we only want to pick images
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private void pickImageCamera() {
        Log.d(TAG, "pickImageCamera: ");

        //setup content values, mediastore to capture high quality image using camera intent
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMP_TITLE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMP_DESCRIPTION");

        //uri of image to be captured from camera
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        //intent to launch camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult: ");
                    //check if image is picked or not
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //get data
                        Intent data = result.getData();
                        //get uri of image picked
                        imageUri = data.getData();

                        Log.d(TAG, "onActivityResult: Image Picked From Gallery: " + imageUri);

                        //timestamp will be used as the id of the image clicked
                        String timestamp = "" + System.currentTimeMillis();

                        //setup model for image. param1 is id, 2 is imageUri, 3 is imageUrl, fromInternet
                        //add model to the imagePickedArraryList
                        ModelImagePicked modelImagePicked = new ModelImagePicked(timestamp, imageUri, null, false);
                        imagePickedArrayList.add(modelImagePicked);
                        //reload the images
                        loadImages();

                    } else {
                        //cancelled
                        Utils.toast(AdCreateActivity.this, "Cancelled...");
                    }
                }
            }
    );

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult: ");

                    //check if image is captured or not
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //no need to get image uri here we will have it in pickImageCamera function
                        Log.d(TAG, "onActivityResult: Image Picked From Gallery: " + imageUri);

                        //timestamp will be used as the id of the image clicked
                        String timestamp = "" + Utils.getTimestamp();

                        //setup model for image. param1 is id, 2 is imageUri, 3 is imageUrl, fromInternet
                        //add model to the imagePickedArraryList
                        ModelImagePicked modelImagePicked = new ModelImagePicked(timestamp, imageUri, null, false);
                        imagePickedArrayList.add(modelImagePicked);
                        //reload the images
                        loadImages();

                    } else {
                        //cancelled
                        Utils.toast(AdCreateActivity.this, "Cancelled...");
                    }
                }
            }
    );

    private String brand = "";
    private String category = "";
    private String condition = "";
    private String address = "";
    private String price = "";
    private String title = "";
    private String age = "";
    private String description = "";
    private double latitude = 0;
    private double longitude = 0;

    private void validateData() {
        Log.d(TAG, "validateData: ");

        brand = binding.brandEt.getText().toString().trim();
        category = binding.categoryAct.getText().toString().trim();
        condition = binding.conditionAct.getText().toString().trim();
        address = binding.locationEt.getText().toString().trim();
        price = binding.priceEt.getText().toString().trim();
        title = binding.titleEt.getText().toString().trim();
        age = binding.ageAct.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();

        if (brand.isEmpty()) {
            binding.brandEt.setError("Enter Brand");
            binding.brandEt.requestFocus();
        } else if (category.isEmpty()) {
            binding.categoryAct.setError("Choose Category");
            binding.categoryAct.requestFocus();
        } else if (condition.isEmpty()) {
            binding.conditionAct.setError("Choose Condition");
            binding.conditionAct.requestFocus();
        } else if (age.isEmpty()) {
            binding.ageAct.setError("Choose Age");
            binding.ageAct.requestFocus();
        } else if (address.isEmpty()) {
            binding.locationEt.setError("Choose Location");
            binding.locationEt.requestFocus();
        } else if (price.isEmpty()) {
            binding.priceEt.setError("Enter Price");
            binding.priceEt.requestFocus();
        } else if (title.isEmpty()) {
            binding.titleEt.setError("Enter Title");
            binding.titleEt.requestFocus();
        } else if (description.isEmpty()) {
            binding.descriptionEt.setError("Enter Description");
            binding.descriptionEt.requestFocus();
        } else if (age.isEmpty()) {
            binding.ageAct.setError("Enter Description");
            binding.ageAct.requestFocus();
        } else if (imagePickedArrayList.isEmpty()) {
            Toast.makeText(AdCreateActivity.this, "Please select at least one image", Toast.LENGTH_LONG).show();
        } else {
            if (isEditMode) {
                updateAd();
            } else {
                //all data is validated, proceed
                postAd();
            }

        }
    }

    private void postAd(){
        Log.d(TAG, "postAd: ");

        progressDialog.setMessage("Publishing Ad...");
        progressDialog.show();

        //get current timestamp
        long timestamp = Utils.getTimestamp();
        //firebase db reference to store new ads
        DatabaseReference refAds = FirebaseDatabase.getInstance().getReference("Ads");
        //key id is from the reference to use as ad id
        String keyId = refAds.push().getKey();

        //set up data to add in firebase database
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", ""+ keyId);
        hashMap.put("uid", ""+ firebaseAuth.getUid());
        hashMap.put("brand", ""+ brand);
        hashMap.put("category", ""+ category);
        hashMap.put("condition", ""+ condition);
        hashMap.put("address", ""+ address);
        hashMap.put("price", ""+ price);
        hashMap.put("title", ""+ title);
        hashMap.put("age", ""+age);
        hashMap.put("description", ""+ description);
        hashMap.put("status", ""+ Utils.AD_STATUS_AVAILABLE);
        hashMap.put("timestamp", timestamp);
//        hashMap.put("latitude", latitude);
//        hashMap.put("longitude", longitude);

        //set data to firebase database. Ads > AdId > AdDataJson
        refAds.child(keyId)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Ad Published");

                        uploadImagesStorage(keyId);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);

                        progressDialog.dismiss();
                        Toast.makeText(AdCreateActivity.this, "Failed to publish the Ad", Toast.LENGTH_LONG).show();

                    }
                });
    }

    private void updateAd(){

        progressDialog.setMessage("Updating Ad...");
        progressDialog.show();

        //set up data to add in firebase database
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("brand", ""+ brand);
        hashMap.put("category", ""+ category);
        hashMap.put("condition", ""+ condition);
        hashMap.put("address", ""+ address);
        hashMap.put("price", ""+ price);
        hashMap.put("title", ""+ title);
        hashMap.put("age", ""+age);
        hashMap.put("description", ""+ description);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adIdForEditing)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        progressDialog.dismiss();
                        uploadImagesStorage(adIdForEditing);

                    }
                });

    }

    private void uploadImagesStorage(String adId){
        Log.d(TAG, "uploadImagesStorage: ");

        //there are multiple images in imagePickedArrayList, loop to upload all of them
        for (int i=0; i<imagePickedArrayList.size(); i++) {
            //get model from the current position of the imagePickedArrayList
            ModelImagePicked modelImagePicked = imagePickedArrayList.get(i);

            //upload image only if picked from gallery/camera
            if (!modelImagePicked.getFromInternet()){
                //for name of the image in firebase storage
                String imageName = modelImagePicked.getId();
                //path and name of the image in firebase storage
                String filePathAndName = "Ads/"+imageName;

                int imageIndexForProgress = i+1;

                //storage reference with filepath and name
                StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);

                storageReference.putFile(modelImagePicked.getImageUri())
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                                //calculate the current progress of the image being uploaded
                                double progress = (100.0 * snapshot.getBytesTransferred())/ snapshot.getTotalByteCount();
                                //setup progress dialog message on basis of current progress .e.g.
                                //uploading 1 out of 10 images...Progress 95%
                                String message = "Uploading " + imageIndexForProgress + " of " + imagePickedArrayList.size() + " images...\nProgress " + (int) progress + "%";
                                //show progress
                                progressDialog.setMessage(message);
                                progressDialog.show();
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Log.d(TAG, "onSuccess: ");
                                //image uploaded, get Url of uploaded image
                                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                while(!uriTask.isSuccessful());
                                Uri uploadedImageUrl = uriTask.getResult();

                                if (uriTask.isSuccessful()) {

                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("id", ""+modelImagePicked.getId());
                                    hashMap.put("imageUrl", ""+uploadedImageUrl);

                                    //add in firebase db. Ads > AdId > Images > ImageId > ImageData
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
                                    ref.child(adId).child("Images")
                                            .child(imageName)
                                            .updateChildren(hashMap);
                                }
                                progressDialog.dismiss();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Log.e(TAG, "onFailure: ", e);
                                progressDialog.dismiss();
                            }
                        });
            }
        }

    }

    private void loadAdDetails(){
        //ad's db path to get the ad details. ad > adId
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adIdForEditing)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get the ad details from firebase db, spellings should be same as in firebase db
                        String brand = ""+ snapshot.child("brand").getValue();
                        String category = ""+ snapshot.child("category").getValue();
                        String condition = ""+ snapshot.child("condition").getValue();
                        String address = ""+ snapshot.child("address").getValue();
                        String price = ""+ snapshot.child("price").getValue();
                        String title = ""+ snapshot.child("title").getValue();
                        String age = ""+ snapshot.child("age").getValue();
                        String description = ""+ snapshot.child("description").getValue();

                        //set data to ui views (form)
                        binding.brandEt.setText(brand);
                        binding.conditionAct.setText(condition);
                        binding.categoryAct.setText(category);
                        binding.ageAct.setText(age);
                        binding.locationEt.setText(address);
                        binding.priceEt.setText(price);
                        binding.titleEt.setText(title);
                        binding.descriptionEt.setText(description);

                        //load the ad images. ads > adId > images
                        DatabaseReference refImages = snapshot.child("Images").getRef();
                        refImages.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                //might be multiple images, so loop to get it all
                                for (DataSnapshot ds: snapshot.getChildren()){
                                    String id = ""+ ds.child("id").getValue();
                                    String imageUrl = ""+ ds.child("imageUrl").getValue();

                                    ModelImagePicked modelImagePicked = new ModelImagePicked(id, null, imageUrl, true);
                                    imagePickedArrayList.add(modelImagePicked);

                                }

                                loadImages();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}