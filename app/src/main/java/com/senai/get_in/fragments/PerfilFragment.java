package com.senai.get_in.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.senai.get_in.R;
import com.senai.get_in.api.RetrofitClient;
import com.senai.get_in.databinding.FragmentPerfilBinding;
import com.senai.get_in.model.UsuarioDetalhado;
import com.senai.get_in.model.UsuarioDetalhadoResponse;
import com.senai.get_in.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {

    private static final String TAG = "PerfilFragment";
    private FragmentPerfilBinding binding;
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        tokenManager = new TokenManager(requireContext());
        
        carregarDadosLocais();
        carregarDadosRemotos();
    }

    private void carregarDadosLocais() {
        UsuarioDetalhado user = tokenManager.getUserData();
        if (user != null) {
            Log.d(TAG, "Carregando dados locais para: " + user.getNome());
            exibirUsuario(user);
        } else {
            Log.w(TAG, "Nenhum dado de usuário encontrado no TokenManager");
        }
    }

    private void carregarDadosRemotos() {
        UsuarioDetalhado userLocal = tokenManager.getUserData();
        if (userLocal == null) {
            Log.e(TAG, "Impossível carregar remoto: ID do usuário local é nulo");
            return;
        }

        Log.d(TAG, "Buscando dados remotos para ID: " + userLocal.getId());

        RetrofitClient.getApiService(requireContext()).getUsuarioDetalhadoPorId(userLocal.getId()).enqueue(new Callback<UsuarioDetalhadoResponse>() {
            @Override
            public void onResponse(@NonNull Call<UsuarioDetalhadoResponse> call, @NonNull Response<UsuarioDetalhadoResponse> response) {
                if (!isAdded() || binding == null) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    UsuarioDetalhado userRemoto = response.body().getData();
                    if (userRemoto != null) {
                        Log.d(TAG, "Dados remotos recebidos com sucesso: " + userRemoto.getNome());
                        exibirUsuario(userRemoto);
                        tokenManager.saveUserData(userRemoto);
                    }
                } else {
                    Log.e(TAG, "Erro na resposta API: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<UsuarioDetalhadoResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "Falha crítica na requisição: " + t.getMessage());
                Toast.makeText(getContext(), "Erro de conexão com o servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void exibirUsuario(UsuarioDetalhado user) {
        if (user == null || binding == null) return;

        binding.tvPerfilNome.setText(user.getNome() != null ? user.getNome() : "Sem nome");
        
        String cargoDisplay = "Usuário";
        if (user.getCargo() != null) {
            switch (user.getCargo().toLowerCase()) {
                case "sup": cargoDisplay = "Supervisor"; break;
                case "port": cargoDisplay = "Portaria"; break;
                case "func": cargoDisplay = "Funcionário"; break;
                case "ger": cargoDisplay = "Gerente"; break;
                case "adm": cargoDisplay = "Administrador"; break;
                default: cargoDisplay = user.getCargo(); break;
            }
        }
        binding.tvPerfilCargo.setText(cargoDisplay);
        
        binding.tvPerfilEmail.setText(user.getEmail() != null ? user.getEmail() : "---");
        binding.tvPerfilCpf.setText(user.getCpf() != null ? user.getCpf() : "---");
        binding.tvPerfilDepartamento.setText(user.getDepartamentoNome() != null ? user.getDepartamentoNome() : "Geral");
        binding.tvPerfilCelular.setText(user.getCelular() != null ? user.getCelular() : "---");
        
        if (user.getFotoPerfil() != null && !user.getFotoPerfil().isEmpty()) {
            carregarFoto(user.getFotoPerfil());
        } else {
            binding.ivPerfilFoto.setImageResource(R.drawable.outline_person_24);
        }
    }

    private void carregarFoto(String url) {
        if (isAdded() && getContext() != null && binding != null) {
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.outline_person_24)
                    .error(R.drawable.outline_person_24)
                    .circleCrop()
                    .into(binding.ivPerfilFoto);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}