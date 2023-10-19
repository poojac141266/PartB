package com.example.partb;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpHelper {

    private static OkHttpHelper instance;

    private OkHttpClient client;

    private OkHttpHelper() {
        client = new OkHttpClient();
    }

    public static synchronized OkHttpHelper getInstance() {
        if (instance == null) {
            instance = new OkHttpHelper();
        }

        return instance;
    }

    public void get(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(callback);
    }
}