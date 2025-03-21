package com.example.tradesphere.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tradesphere.R;
import com.example.tradesphere.Utils;
import com.example.tradesphere.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.HolderChat>{

    private static final String TAG = "ADAPTER_CHAT_TAG";
    private Context context;
    private ArrayList<ModelChat> chatArrayList;

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private FirebaseUser firebaseUser;

    public AdapterChat(Context context, ArrayList<ModelChat> chatArrayList) {
        this.context = context;
        this.chatArrayList = chatArrayList;

        //get currently signed in user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public HolderChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate the layouts: row_chat_left.xml and row_chat_right.xml
        if (viewType == MSG_TYPE_RIGHT) {

            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new HolderChat(view);

        } else {

            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new HolderChat(view);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderChat holder, int position) {
        //get data from particular position of the list and set to the UI views
        //of row_chat_left.xml and row_chat_right.xml and Handle clicks

        ModelChat modelChat = chatArrayList.get(position);

        String message = modelChat.getMessage(); //contains text message in case of msg_type_text, contains image Url in case of msg_type_image
        String messageType = modelChat.getMessageType();
        //format date time .e.g. dd/MM/yyyy hh:mm:a (03/08/2023 08:30 AM)
        long timestamp = modelChat.getTimestamp();

        String formattedDate = Utils.formatTimeStampDateTime(timestamp);

        if (messageType.equals(Utils.MESSAGE_TYPE_TEXT)) {
            //message type is text, show messageTv and hide imageIv
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.imageIv.setVisibility(View.GONE);
            //set text message to textView, that is messageTv
            holder.messageTv.setText(message);
        } else {
            //message type is image, hide messageTv and show imageIv
            holder.messageTv.setVisibility(View.GONE);
            holder.imageIv.setVisibility(View.VISIBLE);

            try {
                //set image to imageView, i.e. imageIv
                Glide.with(context)
                        .load(message)
                        .placeholder(R.drawable.image)
                        .error(R.drawable.broken_image)
                        .into(holder.imageIv);

            } catch (Exception e) {


            }
        }

        holder.timeTv.setText(formattedDate);
    }

    @Override
    public int getItemCount() {
        //return the size of the list/ number of items in the list
        return chatArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //if the fromId = current_user_uid then message is by currently logged in user
        //otherwise message is from receipt
        if (chatArrayList.get(position).getFromUid().equals(firebaseUser.getUid())) {
            //from uid == current_user_uid, message is by currently logged in user
            //will show row_chat_right.xml
            return MSG_TYPE_RIGHT;
        } else {
            //from uid != current_user_uid, message is by receipt user
            //will show row_chat_left.xml
            return MSG_TYPE_LEFT;
        }
    }

    class HolderChat extends RecyclerView.ViewHolder{

        //UI views of row_chat_left.xml and row_chat_right.xml
        TextView messageTv, timeTv;
        ImageView imageIv;
        public HolderChat(@NonNull View itemView) {
            super(itemView);

            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            imageIv = itemView.findViewById(R.id.imageIv);
        }
    }
}
