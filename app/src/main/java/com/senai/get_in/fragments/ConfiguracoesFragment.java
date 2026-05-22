package com.senai.get_in.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.senai.get_in.R;
import com.senai.get_in.utils.ToastUtils;
import com.senai.get_in.utils.TokenManager;

public class ConfiguracoesFragment extends Fragment {

    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configuracoes, container, false);

        tokenManager = new TokenManager(requireContext());

        SwitchMaterial switchDarkMode = view.findViewById(R.id.switchDarkMode);
        SwitchMaterial switchNotificacoes = view.findViewById(R.id.switchNotificacoes);
        SwitchMaterial switchSomNotificacao = view.findViewById(R.id.switchSomNotificacao);

        // Carregar estados salvos
        switchDarkMode.setChecked(tokenManager.isDarkMode());
        // Aqui você pode adicionar outros campos no TokenManager se quiser persistir notificações também
        // Por enquanto mantendo a lógica local se necessário, ou expandindo o TokenManager

        // Listeners
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tokenManager.setDarkMode(isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        switchNotificacoes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ToastUtils.showInfo(getContext(), isChecked ? "Notificações ativadas" : "Notificações desativadas");
        });

        view.findViewById(R.id.btnAlterarSenha).setOnClickListener(v -> {
            ToastUtils.showInfo(getContext(), "Funcionalidade de alterar senha em breve");
        });

        return view;
    }
}
