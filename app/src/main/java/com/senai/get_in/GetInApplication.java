package com.senai.get_in;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import com.senai.get_in.utils.TokenManager;

public class GetInApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        TokenManager tokenManager = new TokenManager(this);
        if (tokenManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
