package org.example;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.sondeneme.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

public class GeminiApiClient {

    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;
    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    private final RequestQueue requestQueue;
    private final Context context;

    public GeminiApiClient(Context context) {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
    }

    public void ask(String userInput, GeminiCallback callback) {
        try {
            JSONArray parts = new JSONArray();
            parts.put(new JSONObject().put("text", userInput));
            sendRequestWithParts(parts, callback);
        } catch (JSONException e) {
            callback.onResponse("Metin JSON'a dönüştürülerken hata: " + e.getMessage());
        }
    }

    public void askWithImage(String userInput, Uri imageUri, GeminiCallback callback) {
        try {
            JSONArray parts = new JSONArray();

            // Metin kısmı
            if (userInput != null && !userInput.isEmpty()) {
                parts.put(new JSONObject().put("text", userInput));
            }

            // Görsel kısmı
            if (imageUri != null) {
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                byte[] imageBytes = new byte[inputStream.available()];
                inputStream.read(imageBytes);
                inputStream.close();

                String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
                JSONObject inlineData = new JSONObject();
                inlineData.put("mimeType", "image/jpeg");
                inlineData.put("data", base64Image);

                JSONObject imagePart = new JSONObject();
                imagePart.put("inlineData", inlineData);

                parts.put(imagePart);
            }

            sendRequestWithParts(parts, callback);

        } catch (Exception e) {
            callback.onResponse("Resim/metin JSON isleme hatası: " + e.getMessage());
        }
    }

    private void sendRequestWithParts(JSONArray parts, GeminiCallback callback) {
        try {
            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject contentObject = new JSONObject();
            contentObject.put("parts", parts);
            contents.put(contentObject);
            requestBody.put("contents", contents);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ENDPOINT,
                    requestBody,
                    response -> {
                        try {
                            String reply = response
                                    .getJSONArray("candidates")
                                    .getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0)
                                    .getString("text");

                            callback.onResponse(reply);
                        } catch (Exception e) {
                            callback.onResponse("Yanıt ayrıştırılamadı: " + e.getMessage());
                        }
                    },
                    error -> callback.onResponse("Hata oluştu: " + error.toString())
            );

            requestQueue.add(request);

        } catch (JSONException e) {
            callback.onResponse("JSON isteği hazırlanırken hata: " + e.getMessage());
        }
    }
}
