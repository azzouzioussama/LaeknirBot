package com.finsday.testfinalproject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import pl.droidsonroids.gif.GifImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.myViewHolder> {
    private ArrayList<Message>messages;
    private String senderImg;
    private Context mContext;

    public MessageAdapter(ArrayList<Message> messages, String senderImg, Context mContext) {
        this.messages = messages;
        this.senderImg = senderImg;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.message_handler,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.tvMessage.setText(message.getContent());
        ConstraintLayout constraintLayout = holder.CL;
        if (message.getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
            Glide.with(mContext).asBitmap().load(senderImg).error(R.drawable.ic_account).placeholder(R.drawable.ic_account).into(holder.imgMessage);
            holder.tvMessage.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            holder.tvMessage.setBackgroundResource(R.drawable.message_bot);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.clear(R.id.cv1,ConstraintSet.LEFT);
            constraintSet.clear(R.id.tvMessage,ConstraintSet.LEFT);
            constraintSet.connect(R.id.cv1,ConstraintSet.RIGHT,R.id.CL,ConstraintSet.RIGHT,0);
            constraintSet.connect(R.id.tvMessage,ConstraintSet.RIGHT,R.id.cv1,ConstraintSet.LEFT,0);
            constraintSet.applyTo(constraintLayout);
        }else if (message.getSender().equals("laeknir")){
            Glide.with(mContext).asBitmap().load(R.drawable.bot).error(R.drawable.bot).placeholder(R.drawable.bot).into(holder.imgMessage);
            holder.tvMessage.setBackgroundResource(R.drawable.message_shape);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.clear(R.id.cv1,ConstraintSet.RIGHT);
            constraintSet.clear(R.id.tvMessage,ConstraintSet.RIGHT);
            constraintSet.connect(R.id.cv1,ConstraintSet.LEFT,R.id.CL,ConstraintSet.LEFT,0);
            constraintSet.connect(R.id.tvMessage,ConstraintSet.LEFT,R.id.cv1,ConstraintSet.RIGHT,0);
            constraintSet.applyTo(constraintLayout);
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class myViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout CL;
        private TextView tvMessage;
        private GifImageView imgMessage;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            CL = itemView.findViewById(R.id.CL);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            imgMessage = itemView.findViewById(R.id.imgMessage);

        }
    }
}
