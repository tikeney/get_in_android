package com.senai.get_in.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.senai.get_in.model.LoginResponse;

public class TokenManager {
    private static final String PREF_NAME = "GetInPrefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_DATA = "user_data";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Gson gson;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        gson = new Gson();
    }

    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void saveUserData(LoginResponse.UserData userData) {
        String json = gson.toJson(userData);
        editor.putString(KEY_USER_DATA, json);
        editor.apply();
    }

    public LoginResponse.UserData getUserData() {
        String json = prefs.getString(KEY_USER_DATA, null);
        if (json != null) {
            return gson.fromJson(json, LoginResponse.UserData.class);
        }
        return null;
    }

    public void clear() {
        editor.clear();
        editor.apply();
    }
}
