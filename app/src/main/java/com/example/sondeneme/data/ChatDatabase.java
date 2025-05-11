package com.example.sondeneme.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ChatSession.class, Message.class}, version = 1)
public abstract class ChatDatabase extends RoomDatabase {
    private static volatile ChatDatabase INSTANCE;

    public abstract ChatDao chatDao();

    public static ChatDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ChatDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            ChatDatabase.class,
                            "chat_db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
