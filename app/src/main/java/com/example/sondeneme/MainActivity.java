package com.example.sondeneme;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.sondeneme.data.ChatRepository;
import com.example.sondeneme.data.ChatSession;
import com.example.sondeneme.data.Message;
import com.example.sondeneme.ui.ChatDrawerFragment;

import org.example.GeminiApiClient;
import org.example.GeminiCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ChatDrawerFragment.OnChatSelectedListener {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private static final int IMAGE_PICK_CODE = 101;

    private DrawerLayout drawerLayout;
    private LinearLayout chatLayout;
    private EditText etUserInput;
    private ImageButton btnSend, btnMic, btnAdd;
    private ScrollView scrollView;
    private Uri selectedImageUri = null;
    private GeminiApiClient client;

    private ChatRepository repository;
    private int currentSessionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new GeminiApiClient(this);
        repository = new ChatRepository(this);

        drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ImageButton btnSettings = findViewById(R.id.btn_settings);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        chatLayout = findViewById(R.id.chatLayout);
        etUserInput = findViewById(R.id.etUserInput);
        btnSend = findViewById(R.id.btnSend);
        btnMic = findViewById(R.id.btnMic);
        btnAdd = findViewById(R.id.btnAdd);
        scrollView = findViewById(R.id.scrollView);

        checkImagePermission();

        btnMic.setOnClickListener(v -> startVoiceInput());

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_PICK_CODE);
        });

        btnSend.setOnClickListener(v -> {
            String userMessage = etUserInput.getText().toString().trim();

            if (userMessage.isEmpty() && selectedImageUri == null) {
                Toast.makeText(this, "Metin ya da görsel giriniz", Toast.LENGTH_SHORT).show();
                return;
            }

            addUserCombinedMessage(userMessage, selectedImageUri);

            if (currentSessionId != -1) {
                Message message = new Message(currentSessionId, userMessage, true,
                        selectedImageUri != null ? selectedImageUri.toString() : null,
                        System.currentTimeMillis());
                repository.insertMessage(message);
            }

            client.askWithImage(userMessage, selectedImageUri, new GeminiCallback() {
                @Override
                public void onResponse(String reply) {
                    runOnUiThread(() -> {
                        addBotCombinedMessage(reply, null);
                        if (currentSessionId != -1) {
                            Message botMessage = new Message(currentSessionId, reply, false, null, System.currentTimeMillis());
                            repository.insertMessage(botMessage);
                        }
                    });
                }
            });

            etUserInput.setText("");
            selectedImageUri = null;
        });

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.chat_drawer_container, new ChatDrawerFragment());
            transaction.commit();
        }
    }

    @Override
    public void onNewChat() {
        ChatSession newSession = new ChatSession();
        newSession.title = generateRandomTitle();
        newSession.timestamp = System.currentTimeMillis();

        repository.insertSession(newSession, sessionId -> {
            currentSessionId = sessionId.intValue();
            runOnUiThread(() -> {
                chatLayout.removeAllViews();
                etUserInput.setText("");
                selectedImageUri = null;
                Toast.makeText(this, "Yeni sohbet: " + newSession.title, Toast.LENGTH_SHORT).show();

                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.chat_drawer_container);
                if (fragment instanceof ChatDrawerFragment) {
                    ((ChatDrawerFragment) fragment).refreshSessionList();
                }
            });
        });
    }

    @Override
    public void onChatSelected(ChatSession session) {
        currentSessionId = session.id;
        chatLayout.removeAllViews();

        repository.getMessagesForSession(session.id, messages -> {
            runOnUiThread(() -> {
                if (messages == null || messages.isEmpty()) {
                    Toast.makeText(this, "Bu sohbette mesaj yok", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (Message msg : messages) {
                    Uri uri = msg.imageUri != null ? Uri.parse(msg.imageUri) : null;
                    if (msg.isUser) {
                        addUserCombinedMessage(msg.content, uri);
                    } else {
                        addBotCombinedMessage(msg.content, uri);
                    }
                }
            });
        });
    }

    private String generateRandomTitle() {
        String[] titles = {
                "Project Alpha", "Daily Thoughts", "Midnight Notes", "Code Review",
                "Debug Zone", "Idea Burst", "Mind Notes", "Quick Log", "Free Flow", "Echo Stream"
        };
        int i = (int) (Math.random() * titles.length);
        return titles[i];
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Lütfen konuşun...");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Cihaz konuşma tanımayı desteklemiyor.", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkImagePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    private void addUserCombinedMessage(String message, Uri imageUri) {
        View view = getLayoutInflater().inflate(R.layout.message_item_user, null);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        ImageView imgMessage = view.findViewById(R.id.imgUserMessage);

        tvMessage.setText(message);
        tvMessage.setVisibility(message.isEmpty() ? View.GONE : View.VISIBLE);

        if (imageUri != null) {
            imgMessage.setImageURI(imageUri);
            imgMessage.setVisibility(View.VISIBLE);
        } else {
            imgMessage.setVisibility(View.GONE);
        }

        chatLayout.addView(view);
        scrollToBottom();
    }

    private void addBotCombinedMessage(String message, Uri imageUri) {
        View view = getLayoutInflater().inflate(R.layout.message_item_bot, null);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        ImageView imgMessage = view.findViewById(R.id.imgBotMessage);

        tvMessage.setText(message);
        tvMessage.setVisibility(message.isEmpty() ? View.GONE : View.VISIBLE);

        if (imageUri != null) {
            imgMessage.setImageURI(imageUri);
            imgMessage.setVisibility(View.VISIBLE);
        } else {
            imgMessage.setVisibility(View.GONE);
        }

        chatLayout.addView(view);
        scrollToBottom();
    }

    private void scrollToBottom() {
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                etUserInput.setText(results.get(0));
            }
        } else if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            Toast.makeText(this, "Görsel seçildi", Toast.LENGTH_SHORT).show();
        }
    }
}
