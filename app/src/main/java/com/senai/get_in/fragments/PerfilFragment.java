package com.senai.get_in.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.senai.get_in.R;
import com.senai.get_in.api.RetrofitClient;
import com.senai.get_in.model.UsuarioDetalhado;
import com.senai.get_in.model.UsuarioResponse;
import com.senai.get_in.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {

    private TextView tvNome, tvCargo, tvEmail, tvCpf, tvCelular;
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        tvNome = view.findViewById(R.id.tvPerfilNome);
        tvCargo = view.findViewById(R.id.tvPerfilCargo);
        tvEmail = view.findViewById(R.id.tvPerfilEmail);
        tvCpf = view.findViewById(R.id.tvPerfilCpf);
        tvCelular = view.findViewById(R.id.tvPerfilCelular);

        tokenManager = new TokenManager(requireContext());
        
        carregarDadosLocais();
        carregarDadosRemotos();

        return view;
    }

    private void carregarDadosLocais() {
        UsuarioDetalhado user = tokenManager.getUserData();
        if (user != null) {
            exibirUsuario(user);
        }
    }

    private void carregarDadosRemotos() {
        UsuarioDetalhado user = tokenManager.getUserData();
        if (user == null) return;

        String token = "Bearer " + tokenManager.getToken();
        RetrofitClient.getApiService().getUsuarioPorId(token, user.getId()).enqueue(new Callback<UsuarioResponse>() {
            @Override
            public void onResponse(Call<UsuarioResponse> call, Response<UsuarioResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    // O backend retorna uma lista ou objeto único dependendo da implementação, 
                    // mas o UsuarioResponse do app mapeia como objeto único via getUsuarioPorId
                    // Se o backend retornar lista, precisaríamos ajustar o UsuarioResponse
                }
            }

            @Override
            public void onFailure(Call<UsuarioResponse> call, Throwable t) {
                if (getContext() != null) {
                    Log.e("PerfilFragment", "Erro ao carregar perfil remoto", t);
                }
            }
        });
    }

    private void exibirUsuario(UsuarioDetalhado user) {
        tvNome.setText(user.getNome());
        tvCargo.setText(user.getCargo() != null ? user.getCargo() : "Usuário");
        tvEmail.setText(user.getEmail());
        tvCpf.setText(user.getCpf());
        tvCelular.setText(user.getCelular());
    }
}
