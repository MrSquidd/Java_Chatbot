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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatSessionAdapter extends RecyclerView.Adapter<ChatSessionAdapter.SessionViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ChatSession session);
    }

    private List<ChatSession> sessions = new ArrayList<>();
    private final OnItemClickListener listener;

    public ChatSessionAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setSessions(List<ChatSession> newSessions) {
        this.sessions = newSessions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        ChatSession session = sessions.get(position);
        holder.bind(session, listener);
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {
        private final TextView title, date;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.chat_title);
            date = itemView.findViewById(R.id.chat_date);
        }

        public void bind(ChatSession session, OnItemClickListener listener) {
            title.setText(session.title);

            String formattedDate = new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
                    .format(new Date(session.timestamp));
            date.setText(formattedDate);

            itemView.setOnClickListener(v -> listener.onItemClick(session));
        }
    }
}
