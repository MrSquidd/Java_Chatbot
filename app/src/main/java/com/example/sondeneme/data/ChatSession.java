package com.example.sondeneme.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ChatSession {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;

    public long timestamp;
}
