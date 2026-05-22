package com.senai.get_in.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.senai.get_in.databinding.FragmentConfiguracoesBinding;
import com.senai.get_in.utils.ToastUtils;
import com.senai.get_in.utils.TokenManager;

public class ConfiguracoesFragment extends Fragment {

    private FragmentConfiguracoesBinding binding;
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentConfiguracoesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        tokenManager = new TokenManager(requireContext());

        // Carregar estados salvos
        binding.switchDarkMode.setChecked(tokenManager.isDarkMode());

        // Listeners
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tokenManager.setDarkMode(isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        binding.switchNotificacoes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ToastUtils.showInfo(getContext(), isChecked ? "Notificações ativadas" : "Notificações desativadas");
        });

        binding.btnAlterarSenha.setOnClickListener(v -> {
            ToastUtils.showInfo(getContext(), "Funcionalidade de alterar senha em breve");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}