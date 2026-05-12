package com.senai.get_in.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.senai.get_in.R;

public class ConfiguracoesFragment extends Fragment {

    private static final String PREFS_NAME = "GetInSettings";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final String KEY_SOUND = "notification_sound";

    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configuracoes, container, false);

        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        SwitchMaterial switchDarkMode = view.findViewById(R.id.switchDarkMode);
        SwitchMaterial switchNotificacoes = view.findViewById(R.id.switchNotificacoes);
        SwitchMaterial switchSomNotificacao = view.findViewById(R.id.switchSomNotificacao);

        // Carregar estados salvos
        switchDarkMode.setChecked(sharedPreferences.getBoolean(KEY_DARK_MODE, false));
        switchNotificacoes.setChecked(sharedPreferences.getBoolean(KEY_NOTIFICATIONS, true));
        switchSomNotificacao.setChecked(sharedPreferences.getBoolean(KEY_SOUND, true));

        // Listeners
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        switchNotificacoes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS, isChecked).apply();
            Toast.makeText(getContext(), isChecked ? "Notificações ativadas" : "Notificações desativadas", Toast.LENGTH_SHORT).show();
        });

        switchSomNotificacao.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_SOUND, isChecked).apply();
        });



        view.findViewById(R.id.btnAlterarSenha).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Funcionalidade de alterar senha em breve", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}
