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

import java.util.List;

public class ChatDrawerFragment extends Fragment {

    private ChatRepository repository;
    private ChatSessionAdapter adapter;
    private OnChatSelectedListener listener;

    public interface OnChatSelectedListener {
        void onChatSelected(ChatSession session);
        void onNewChat();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnChatSelectedListener) {
            listener = (OnChatSelectedListener) context;
        } else {
            throw new RuntimeException("Activity must implement OnChatSelectedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_drawer, container, false);

        repository = new ChatRepository(requireContext());

        RecyclerView recyclerView = view.findViewById(R.id.recycler_chat_sessions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatSessionAdapter(session -> {
            if (listener != null) {
                listener.onChatSelected(session);
            }
        });
        recyclerView.setAdapter(adapter);

        Button btnNewChat = view.findViewById(R.id.btn_new_chat);
        btnNewChat.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNewChat();
            }
        });

        refreshSessionList();

        return view;
    }

    public void refreshSessionList() {
        repository.getAllSessions(sessions -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> adapter.setSessions(sessions));
            }
        });
    }
}
