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
    public String imageUri; // Görsel desteği için ekledik
    public long timestamp;

    public Message(int sessionId, String content, boolean isUser, String imageUri, long timestamp) {
        this.sessionId = sessionId;
        this.content = content;
        this.isUser = isUser;
        this.imageUri = imageUri;
        this.timestamp = timestamp;
    }

    public Message() {} // Room için boş constructor
}
