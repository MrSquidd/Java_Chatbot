package com.example.sondeneme.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = @ForeignKey(
        entity = ChatSession.class,
        parentColumns = "id",
        childColumns = "sessionId",
        onDelete = ForeignKey.CASCADE
))
public class Message {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int sessionId;

    public boolean isUser;

    public String content;

    public long timestamp;
}
