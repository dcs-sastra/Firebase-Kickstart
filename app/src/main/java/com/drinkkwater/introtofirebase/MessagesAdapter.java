package com.drinkkwater.introtofirebase;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder> {
    private List<Messages> messages;
    OnMessageClicklistner onMessageClicklistner;

    public MessagesAdapter(List<Messages> messages,OnMessageClicklistner onMessageClicklistner) {
        this.messages = messages;
        this.onMessageClicklistner = onMessageClicklistner;
    }

    @NonNull
    @Override
    public MessagesAdapter.MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.messages_list_item,parent,false);
        return new MessagesViewHolder(view,onMessageClicklistner);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesViewHolder holder, int position) {
        holder.authorTextView.setText(messages.get(position).getAuthor());
        holder.messagesTextView.setText(messages.get(position).getMessage());
        if(messages.get(position).getUrl() == null){
            holder.urlTextView.setText("No attatchment available");
        }
        else {
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
        OnMessageClicklistner onMessageClicklistner;

        public MessagesViewHolder(@NonNull View itemView,OnMessageClicklistner onMessageClicklistner) {
            super(itemView);
            messagesTextView = itemView.findViewById(R.id.messages);
            authorTextView = itemView.findViewById(R.id.author);
            urlTextView = itemView.findViewById(R.id.url);
            this.onMessageClicklistner = onMessageClicklistner;
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            onMessageClicklistner.OnClick(getAdapterPosition());
        }
    }
    public interface OnMessageClicklistner{
         void OnClick(int position) ;
    }
}
