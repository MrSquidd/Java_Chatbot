package com.example.sondeneme;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.example.GeminiApiClient;
import org.example.GeminiCallback;

public class MainActivity extends AppCompatActivity {

    private LinearLayout chatLayout;
    private EditText etUserInput;
    private ImageButton btnSend;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GeminiApiClient client;
        client = new GeminiApiClient(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatLayout = findViewById(R.id.chatLayout);
        etUserInput = findViewById(R.id.etUserInput);
        btnSend = findViewById(R.id.btnSend);
        scrollView = findViewById(R.id.scrollView);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userMessage = etUserInput.getText().toString().trim();
                if (!userMessage.isEmpty()) {
                    addUserMessage(userMessage);
                    etUserInput.setText("");
                    client.ask(userMessage, new GeminiCallback() {
                        @Override
                        public void onResponse(String reply) {
                            runOnUiThread(() -> addBotMessage(reply));
                        }
                    });
                }
            }
        });
    } // ← ← ← ← BU PARANTEZ EKLENMİŞTİ

    private void addUserMessage(String message) {
        View userMessageView = getLayoutInflater().inflate(R.layout.message_item_user, null);
        TextView tvMessage = userMessageView.findViewById(R.id.tvMessage);
        tvMessage.setText(message);
        chatLayout.addView(userMessageView);
        scrollToBottom();
    }

    private void addBotMessage(String message) {
        View botMessageView = getLayoutInflater().inflate(R.layout.message_item_bot, null);
        TextView tvMessage = botMessageView.findViewById(R.id.tvMessage);
        tvMessage.setText(message);
        chatLayout.addView(botMessageView);
        scrollToBottom();
    }

    private void scrollToBottom() {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
}
