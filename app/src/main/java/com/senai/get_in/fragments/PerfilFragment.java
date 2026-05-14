package com.senai.get_in.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.senai.get_in.R;
import com.senai.get_in.model.UsuarioDetalhado;
import com.senai.get_in.utils.TokenManager;

public class PerfilFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        TextView tvNome = view.findViewById(R.id.tvPerfilNome);
        TextView tvCargo = view.findViewById(R.id.tvPerfilCargo);
        TextView tvEmail = view.findViewById(R.id.tvPerfilEmail);
        TextView tvCpf = view.findViewById(R.id.tvPerfilCpf);
        TextView tvCelular = view.findViewById(R.id.tvPerfilCelular);

        TokenManager tokenManager = new TokenManager(requireContext());
        UsuarioDetalhado user = tokenManager.getUserData();

        if (user != null) {
            tvNome.setText(user.getNome());
            tvCargo.setText(user.getCargo() != null ? user.getCargo() : "Usuário");
            // Removendo prefixos redundantes para um design mais limpo (ícones já explicam o campo)
            tvEmail.setText(user.getEmail());
            tvCpf.setText(user.getCpf());
            tvCelular.setText(user.getCelular());
        }

        return view;
    }
}
