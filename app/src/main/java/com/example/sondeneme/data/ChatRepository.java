package com.example.sondeneme.data;

import android.content.Context;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatRepository {
    private final ChatDao chatDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ChatRepository(Context context) {
        ChatDatabase db = ChatDatabase.getInstance(context);
        this.chatDao = db.chatDao();
    }

    public void insertSession(ChatSession session, Callback<Long> callback) {
        executor.execute(() -> {
            long id = chatDao.insertSession(session);
            if (callback != null) callback.onResult(id);
        });
    }

    public void insertMessage(Message message) {
        executor.execute(() -> chatDao.insertMessage(message));
    }

    public void getAllSessions(Callback<List<ChatSession>> callback) {
        executor.execute(() -> callback.onResult(chatDao.getAllSessions()));
    }

    public void getMessagesForSession(int sessionId, Callback<List<Message>> callback) {
        executor.execute(() -> callback.onResult(chatDao.getMessagesForSession(sessionId)));
    }

    public interface Callback<T> {
        void onResult(T result);
    }
}
