package com.example.sondeneme.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sondeneme.R;
import com.example.sondeneme.data.ChatSession;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ChatSession session);
    }

    private List<ChatSession> sessions;
    private final OnItemClickListener listener;

    public ChatListAdapter(List<ChatSession> sessions, OnItemClickListener listener) {
        this.sessions = sessions;
        this.listener = listener;
    }

    public void updateSessions(List<ChatSession> newSessions) {
        this.sessions = newSessions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_session, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatSession session = sessions.get(position);
        holder.bind(session, listener);
    }

    @Override
    public int getItemCount() {
        return sessions != null ? sessions.size() : 0;
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView titleView, dateView;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.chat_title);
            dateView = itemView.findViewById(R.id.chat_date);
        }

        void bind(ChatSession session, OnItemClickListener listener) {
            titleView.setText(session.title);
            dateView.setText(new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(session.timestamp));
            itemView.setOnClickListener(v -> listener.onItemClick(session));
        }
    }
}
