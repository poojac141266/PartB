package com.example.partb;

import com.google.gson.Gson;

public class JsonHelper {

    public static <T> T fromJson(String json, Class<T> clazz) {
        Gson gson = new Gson();
        return gson.fromJson(json, clazz);
    }
}
