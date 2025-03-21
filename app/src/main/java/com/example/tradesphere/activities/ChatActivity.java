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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.tradesphere.R;
import com.example.tradesphere.Utils;
import com.example.tradesphere.adapters.AdapterChat;
import com.example.tradesphere.databinding.ActivityChatBinding;
import com.example.tradesphere.models.ModelChat;
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
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {


    private ActivityChatBinding binding;

    private static final String TAG = "CHAT_TAG";
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    //uid of receipt, will get from intent
    private String receiptUid = "";
    private String receiptFcmToke = "";

    //uid of current user
    private String myUid = "";
    private String myName = "";

    //will generate using uids of current user and reciept
    private String chatPath = "";
    //uri of image picked from camera/gallery
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //get uid of the receipt (as passed in ChatActivity class while starting this activity)
        receiptUid = getIntent().getStringExtra("receiptUid");
        //get uid of current signed-in user
        myUid = firebaseAuth.getUid();
        //chat path
        chatPath = Utils.chatPath(receiptUid, myUid);

        loadMyInfo();
        loadReceiptDetails();
        loadMessages();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.attachFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePickDialog();
            }
        });

        //handle sendBtn click, validate data before sending text message
        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private void loadMyInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(""+firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        myName = ""+ snapshot.child("name").getValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadReceiptDetails(){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(receiptUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        try {
                            //get user data
                            String name = ""+snapshot.child("name").getValue();
                            String profileImageUrl = ""+snapshot.child("profileImageUrl").getValue();
                            receiptFcmToke = ""+snapshot.child("fcmToken").getValue();

                            binding.toolbarTitleTv.setText(name);

                            //set user profile image
                            try {

                                Glide.with(ChatActivity.this)
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.person)
                                        .error(R.drawable.broken_image)
                                        .into(binding.toolbarProfileIv);
                            } catch (Exception e) {

                            }

                        } catch (Exception e) {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadMessages() {

        ArrayList<ModelChat> chatArrayList = new ArrayList<>();

        //db reference to load chat messages
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.child(chatPath)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear chatArrayList each time add data into it
                        chatArrayList.clear();
                        //load messages list
                        for (DataSnapshot ds: snapshot.getChildren()){
                            try {

                                //prepare modelChat with all data from firebase db
                                ModelChat modelChat = ds.getValue(ModelChat.class);
                                //add prepared model to adArrayList
                                chatArrayList.add(modelChat);

                            } catch (Exception e) {

                                Log.e(TAG, "loadMessages: onDataChangeL ", e);
                            }
                        }
                        //init/setup adapterChat class and set it to recyclerView
                        AdapterChat adapterChat = new AdapterChat(ChatActivity.this, chatArrayList);
                        binding.chatRv.setAdapter(adapterChat);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void imagePickDialog(){

        PopupMenu popupMenu = new PopupMenu(this, binding.attachFab);

        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //get the uid of the menu item clicked
                int itemId = item.getItemId();
                if (itemId == 1) {
                    //camera clicked
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        //device is above tiramisu, we only need camera permissions
                        requestCameraPermissions.launch(new String[]{Manifest.permission.CAMERA});

                    } else {
                        //device is below tiramisu, we need camera and storage permissions
                        requestCameraPermissions.launch(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
                    }
                } else if (itemId == 2) {
                    //gallery is clicked, we need to check if we have permission of storage before
                    //launching Gallery to pick image
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pickImageGallery();
                    } else {
                        //device is below tiramisu, we need storage permission to launch Gallery
                        requestStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                }
                return true;
            }
        });
    }

    private ActivityResultLauncher<String[]> requestCameraPermissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    //check if permissions are granted or not
                    boolean areAllGranted = true;
                    for (Boolean isGranted : result.values()){

                        areAllGranted = areAllGranted && isGranted;
                    }

                    if (areAllGranted) {
                        //all permissions, camera/storage are granted, we launch camera to
                        //capture image
                        pickImageCamera();

                    } else {
                        //camera/storage permissions denied, can't launch camera to capture image
                        Utils.toast(ChatActivity.this, "Camera or Storage or both permisions denied...!");
                    }
                }
            }
    );

    private ActivityResultLauncher<String> requestStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {

                    if (isGranted) {

                        pickImageGallery();

                    } else {

                        Utils.toast(ChatActivity.this, "Permission denied...!");
                    }
                }
            }
    );

    private void pickImageCamera() {

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "CHAT_IMAGE_TEMP");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "CHAT_IMAGE_TEMP_DESCRIPTION");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //image captured, we have image in imageUri as assigned in imagePickCamera
                        uploadToFirebaseStorage();
                    } else {
                        //cancelled
                        Utils.toast(ChatActivity.this, "Cancelled...!");
                    }
                }
            }
    );

    private void pickImageGallery(){

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK) {

                        Intent data = result.getData();

                        imageUri = data.getData();

                        //image picked, upload/send
                        uploadToFirebaseStorage();

                    } else {

                        Utils.toast(ChatActivity.this, "Cancelled...!");
                    }
                }
            }
    );

    private void uploadToFirebaseStorage() {

        progressDialog.setMessage("Uploading image...!");
        progressDialog.show();

        long timestamp = Utils.getTimestamp();
        String filePathAndName = "ChatImages/"+timestamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(imageUri)
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                        double progress = (100.0 * snapshot.getBytesTransferred()) /snapshot.getTotalByteCount();

                        progressDialog.setMessage("Uploading image. Progress: " + (int) progress + "%");
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        String imageUrl = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()){
                            sendMessage(Utils.MESSAGE_TYPE_IMAGE, imageUrl, timestamp);
                        }
                    }
                });
    }

    private void validateData() {

        String message = binding.messageEt.getText().toString().trim();
        long timestamp = Utils.getTimestamp();

        if (message.isEmpty()) {

            Utils.toast(this, "Enter message to send...");

        } else {

            sendMessage(Utils.MESSAGE_TYPE_TEXT, message, timestamp);
        }
    }
    private void sendMessage(String messageType, String message, long timestamp) {

        progressDialog.setMessage("Sending message...!");
        progressDialog.show();

        //database reference of chats
        DatabaseReference refChat = FirebaseDatabase.getInstance().getReference("Chats");

        //key id to be used as messageId
        String keyId = "" + refChat.push().getKey();

        //setup chat data in hashMap to add in firebase db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("messageId", ""+keyId);
        hashMap.put("messageType", ""+messageType);
        hashMap.put("message", ""+message);
        hashMap.put("fromUid", ""+myUid);
        hashMap.put("toUid", ""+receiptUid);
        hashMap.put("timestamp", timestamp);

        //add chat data to firebase db
        refChat.child(chatPath)
                .child(keyId)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //message successfully sent/added
                        binding.messageEt.setText("");
                        progressDialog.dismiss();

                        //if message type is text, pass the actual message to show as
                        //notification description/body. if message type is image, then
                        //pass "send an attachment"
                        if (messageType.equals(Utils.MESSAGE_TYPE_TEXT)) {
                            prepareNotification(message);
                        } else {
                            prepareNotification("Sent an attachment");
                        }
                    }
                });

    }

    private void prepareNotification(String message) {

        //prepare JSON what to send, where to send
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationDataJo = new JSONObject();
        JSONObject notificationNotificationJo = new JSONObject();

        try {
            //extra/custom data
            notificationDataJo.put("notificationType", "" + Utils.NOTIFICATION_TYPE_NEW_MESSAGE);
            notificationDataJo.put("senderUid", "" + firebaseAuth.getUid());
            //title/description/sound
            notificationNotificationJo.put("title", ""+myName);
            notificationNotificationJo.put("body", ""+message);
            notificationNotificationJo.put("sound", "default");
            //combine all data in single JSON object
            notificationJo.put("to", ""+receiptFcmToke);
            notificationJo.put("notification", notificationNotificationJo);
            notificationJo.put("data", notificationDataJo);

        } catch (Exception e) {

        }
        sendFcmNotification(notificationJo);
    }

    private void sendFcmNotification(JSONObject notificationJo) {
        //prepare JSON Object Request to enqueue
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                "https://fcm.googleapis.com/fcm/send",
                notificationJo,
                response -> {},
                error -> {}
        ) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                //put required headers
                Map<String, String> headers = new HashMap<>();
                //Content-Type is reserved name in Volley Networking API/Library
                headers.put("Content-Type", "application/json");
                //Authorization is reserved name in Volley Networking API/Library
                //value against it must be like "key=fcm_server_key_here"
                headers.put("Authorization", "key=" + Utils.FCM_SERVER_KEY);

                return headers;
            }
        };

        //enqueue the JSON Object Request
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}