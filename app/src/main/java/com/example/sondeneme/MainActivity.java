package com.example.sondeneme;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
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
import androidx.appcompat.app.AppCompatDelegate;
import android.content.SharedPreferences;


import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.example.GeminiApiClient;
import org.example.GeminiCallback;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

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




    @Override

    protected void onCreate(Bundle savedInstanceState) {
        // Tema ayarı uygulama başlarken yapılmalı
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

        // Drawer ve Toolbar
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Bu satır özelleştirilmiş başlığın ortalanması için gereklidir
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Varsayılan başlığı kapat


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));

            }
            drawerLayout.closeDrawers();
            return true;
        });

        chatLayout = findViewById(R.id.chatLayout);
        etUserInput = findViewById(R.id.etUserInput);
        btnSend = findViewById(R.id.btnSend);
        btnMic = findViewById(R.id.btnMic);
        btnAdd = findViewById(R.id.btnAdd);
        scrollView = findViewById(R.id.scrollView);

        checkImagePermission();

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

        btnMic.setOnClickListener(v -> startVoiceInput());

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_PICK_CODE);
        });
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
}
