package com.drinkkwater.introtofirebase;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder> {

    private List<MessageModel> messages;
    private OnMessageClicklistner onMessageClicklistner;
    private Context context;

    public MessagesAdapter(List<MessageModel> messages, OnMessageClicklistner onMessageClicklistner) {
        this.messages = messages;
        this.onMessageClicklistner = onMessageClicklistner;
    }

    @NonNull
    @Override
    public MessagesAdapter.MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.messages_list_item,parent,false);
        context = view.getContext();
        return new MessagesViewHolder(view,onMessageClicklistner);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesViewHolder holder, int position) {
        holder.authorTextView.setText(messages.get(position).getAuthor());
        holder.messagesTextView.setText(messages.get(position).getMessage());
        if(messages.get(position).getUrl() == null){
            holder.urlTextView.setText("No attatchment available");
            holder.imageViewPicture.setVisibility(View.GONE);
        }
        else {
            holder.imageViewPicture.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(messages.get(position).getUrl())
                    .into(holder.imageViewPicture);

            holder.urlTextView.setText("Attatchment available click to download");
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }


    public class MessagesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView messagesTextView;
        TextView authorTextView;
        TextView urlTextView;
        ImageView imageViewPicture;

        OnMessageClicklistner onMessageClicklistner;

        public MessagesViewHolder(@NonNull View itemView,OnMessageClicklistner onMessageClicklistner) {
            super(itemView);
            messagesTextView = itemView.findViewById(R.id.messages);
            authorTextView = itemView.findViewById(R.id.author);
            urlTextView = itemView.findViewById(R.id.url);
            imageViewPicture = itemView.findViewById(R.id.ivPicture);
            this.onMessageClicklistner = onMessageClicklistner;
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            onMessageClicklistner.OnMessageItemClicked(getAdapterPosition(), messages);
        }
    }
    public interface OnMessageClicklistner{
         void OnMessageItemClicked(int position, List<MessageModel> messageModelList) ;
    }
}
