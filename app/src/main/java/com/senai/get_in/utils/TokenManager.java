package com.senai.get_in.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.gson.Gson;
import com.senai.get_in.model.UsuarioDetalhado;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class TokenManager {
    private static final String TAG = "TokenManager";
    private static final String PREF_NAME = "GetInSecurePrefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_DATA = "user_data";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_SHOW_LABELS = "show_nav_labels";
    
    private SharedPreferences prefs;
    private Gson gson;

    public TokenManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            prefs = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Erro ao inicializar EncryptedSharedPreferences, caindo para SharedPreferences comum", e);
            // Fallback para SharedPreferences comum se houver falha na criptografia (dispositivos muito antigos ou erro de hardware)
            prefs = context.getSharedPreferences(PREF_NAME + "_unsecure", Context.MODE_PRIVATE);
        }
        gson = new Gson();
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void saveUserData(UsuarioDetalhado userData) {
        String json = gson.toJson(userData);
        prefs.edit().putString(KEY_USER_DATA, json).apply();
    }

    public UsuarioDetalhado getUserData() {
        String json = prefs.getString(KEY_USER_DATA, null);
        if (json != null) {
            return gson.fromJson(json, UsuarioDetalhado.class);
        }
        return null;
    }

    public void setDarkMode(boolean isEnabled) {
        prefs.edit().putBoolean(KEY_DARK_MODE, isEnabled).apply();
    }

    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public void setShowLabels(boolean show) {
        prefs.edit().putBoolean(KEY_SHOW_LABELS, show).apply();
    }

    public boolean shouldShowLabels() {
        return prefs.getBoolean(KEY_SHOW_LABELS, false);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
