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

import com.example.sondeneme.data.ChatRepository;
import com.example.sondeneme.data.ChatSession;
import com.example.sondeneme.ui.ChatDrawerFragment;
import com.google.android.material.navigation.NavigationView;

import org.example.GeminiApiClient;
import org.example.GeminiCallback;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ChatDrawerFragment.OnChatSelectedListener {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private static final int IMAGE_PICK_CODE = 101;

    private DrawerLayout drawerLayout;
    private NavigationView navView;
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
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new GeminiApiClient(this);
        repository = new ChatRepository(this);

        // Drawer + Toolbar
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation menu
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            }
            drawerLayout.closeDrawers();
            return true;
        });

        // Chat layout
        chatLayout = findViewById(R.id.chatLayout);
        etUserInput = findViewById(R.id.etUserInput);
        btnSend = findViewById(R.id.btnSend);
        btnMic = findViewById(R.id.btnMic);
        btnAdd = findViewById(R.id.btnAdd);
        scrollView = findViewById(R.id.scrollView);

        checkImagePermission();

        // Voice input
        btnMic.setOnClickListener(v -> startVoiceInput());

        // Image pick
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_PICK_CODE);
        });

        // Send
        btnSend.setOnClickListener(v -> {
            String userMessage = etUserInput.getText().toString().trim();
            if (userMessage.isEmpty() && selectedImageUri == null) {
                Toast.makeText(this, "Metin ya da görsel giriniz", Toast.LENGTH_SHORT).show();
                return;
            }

            addUserCombinedMessage(userMessage, selectedImageUri);

            client.askWithImage(userMessage, selectedImageUri, new GeminiCallback() {
                @Override
                public void onResponse(String reply) {
                    runOnUiThread(() -> addBotCombinedMessage(reply, null));
                }
            });

            etUserInput.setText("");
            selectedImageUri = null;
        });

        // Sohbet menüsünü başlat
        if (savedInstanceState == null) {
            ChatDrawerFragment fragment = new ChatDrawerFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.chat_drawer_container, fragment)
                    .commit();
        }
    }

    // Voice input
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

    // Permission
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

    // Mesaj ekleme
    private void addUserCombinedMessage(String message, Uri imageUri) {
        View userMessageView = getLayoutInflater().inflate(R.layout.message_item_user, null);
        TextView tvMessage = userMessageView.findViewById(R.id.tvMessage);
        ImageView imgMessage = userMessageView.findViewById(R.id.imgUserMessage);

        if (message != null && !message.isEmpty()) {
            tvMessage.setText(message);
            tvMessage.setVisibility(View.VISIBLE);
        } else {
            tvMessage.setVisibility(View.GONE);
        }

        if (imageUri != null) {
            imgMessage.setImageURI(imageUri);
            imgMessage.setVisibility(View.VISIBLE);
        } else {
            imgMessage.setVisibility(View.GONE);
        }

        chatLayout.addView(userMessageView);
        scrollToBottom();
    }

    private void addBotCombinedMessage(String message, Uri imageUri) {
        View botMessageView = getLayoutInflater().inflate(R.layout.message_item_bot, null);
        TextView tvMessage = botMessageView.findViewById(R.id.tvMessage);
        ImageView imgMessage = botMessageView.findViewById(R.id.imgBotMessage);

        if (message != null && !message.isEmpty()) {
            tvMessage.setText(message);
            tvMessage.setVisibility(View.VISIBLE);
        } else {
            tvMessage.setVisibility(View.GONE);
        }

        if (imageUri != null) {
            imgMessage.setImageURI(imageUri);
            imgMessage.setVisibility(View.VISIBLE);
        } else {
            imgMessage.setVisibility(View.GONE);
        }

        chatLayout.addView(botMessageView);
        scrollToBottom();
    }

    private void scrollToBottom() {
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    // Yeni sohbet başlatıldığında
    @Override
    public void onNewChat() {
        ChatSession newSession = new ChatSession();
        newSession.title = "Sohbet - " + System.currentTimeMillis();
        newSession.timestamp = System.currentTimeMillis();

        repository.insertSession(newSession, sessionId -> {
            currentSessionId = sessionId.intValue();
            runOnUiThread(() -> {
                Toast.makeText(this, "Yeni sohbet başlatıldı", Toast.LENGTH_SHORT).show();
            });
        });
    }

    // Mevcut sohbete tıklanırsa
    @Override
    public void onChatSelected(ChatSession session) {
        currentSessionId = session.id;
        runOnUiThread(() -> {
            Toast.makeText(this, "Sohbet seçildi: " + session.title, Toast.LENGTH_SHORT).show();
            // TODO: Eski mesajları yükle
        });
    }
}
