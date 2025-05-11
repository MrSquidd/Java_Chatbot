package org.example;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.sondeneme.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import com.example.sondeneme.BuildConfig;


public class GeminiApiClient {

    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;
    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    private final RequestQueue requestQueue;

    public GeminiApiClient(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    public void ask(String userInput, GeminiCallback callback) {
        try {
            // JSON Yapısı
            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject contentObject = new JSONObject();
            JSONArray parts = new JSONArray();
            parts.put(new JSONObject().put("text", userInput));
            contentObject.put("parts", parts);
            contents.put(contentObject);
            requestBody.put("contents", contents);

            // Volley isteği
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

        } catch (Exception e) {
            callback.onResponse("İstek hazırlanırken hata: " + e.getMessage());
        }
    }
}
