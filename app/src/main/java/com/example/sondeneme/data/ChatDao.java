package com.example.sondeneme.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChatDao {
    @Insert
    long insertSession(ChatSession session);

    @Insert
    void insertMessage(Message message);

    @Query("SELECT * FROM ChatSession ORDER BY timestamp DESC")
    List<ChatSession> getAllSessions();

    @Query("SELECT * FROM Message WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    List<Message> getMessagesForSession(int sessionId);
}
