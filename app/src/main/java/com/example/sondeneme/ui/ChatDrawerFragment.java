package com.example.sondeneme.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.sondeneme.R;
import com.example.sondeneme.data.ChatRepository;
import com.example.sondeneme.data.ChatSession;

import java.util.ArrayList;
import java.util.List;

public class ChatDrawerFragment extends Fragment {

    private ChatRepository repository;
    private ChatListAdapter adapter;
    private OnChatSelectedListener listener;

    public interface OnChatSelectedListener {
        void onNewChat();
        void onChatSelected(ChatSession session);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnChatSelectedListener) {
            listener = (OnChatSelectedListener) context;
        }
        repository = new ChatRepository(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_drawer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button newChatBtn = view.findViewById(R.id.btn_new_chat);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_chat_sessions);

        adapter = new ChatListAdapter(new ArrayList<>(), session -> {
            if (listener != null) listener.onChatSelected(session);
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        newChatBtn.setOnClickListener(v -> {
            if (listener != null) listener.onNewChat();
        });

        loadSessions();
    }

    private void loadSessions() {
        repository.getAllSessions(sessions -> getActivity().runOnUiThread(() -> {
            adapter.updateSessions(sessions);
        }));
    }
}
