package com.senai.get_in.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.senai.get_in.R;
import com.senai.get_in.api.RetrofitClient;
import com.senai.get_in.model.UsuarioDetalhado;
import com.senai.get_in.model.UsuarioDetalhadoResponse;
import com.senai.get_in.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {

    private static final String TAG = "PerfilFragment";
    private TextView tvNome, tvCargo, tvEmail, tvCpf, tvCelular, tvDepartamento;
    private ImageView ivPerfilFoto;
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
        tvDepartamento = view.findViewById(R.id.tvPerfilDepartamento);
        ivPerfilFoto = view.findViewById(R.id.ivPerfilFoto);

        tokenManager = new TokenManager(requireContext());
        
        carregarDadosLocais();
        carregarDadosRemotos();

        return view;
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
                if (response.isSuccessful() && response.body() != null) {
                    UsuarioDetalhado userRemoto = response.body().getData();
                    if (userRemoto != null) {
                        Log.d(TAG, "Dados remotos recebidos com sucesso: " + userRemoto.getNome());
                        exibirUsuario(userRemoto);
                        tokenManager.saveUserData(userRemoto);
                    } else {
                        Log.e(TAG, "Resposta sucesso, mas 'data' veio nulo");
                    }
                } else {
                    Log.e(TAG, "Erro na resposta API: " + response.code() + " - " + response.message());
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "Corpo do erro: " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao ler errorBody", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UsuarioDetalhadoResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Falha crítica na requisição: " + t.getMessage(), t);
                if (isAdded()) {
                    Toast.makeText(getContext(), "Erro de conexão com o servidor", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void exibirUsuario(UsuarioDetalhado user) {
        if (user == null) return;

        tvNome.setText(user.getNome() != null ? user.getNome() : "Sem nome");
        
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
        tvCargo.setText(cargoDisplay);
        
        tvEmail.setText(user.getEmail() != null ? user.getEmail() : "---");
        tvCpf.setText(user.getCpf() != null ? user.getCpf() : "---");
        tvDepartamento.setText(user.getDepartamentoNome() != null ? user.getDepartamentoNome() : "Geral");
        tvCelular.setText(user.getCelular() != null ? user.getCelular() : "---");
        
        if (user.getFotoPerfil() != null && !user.getFotoPerfil().isEmpty()) {
            carregarFoto(user.getFotoPerfil());
        } else {
            ivPerfilFoto.setImageResource(R.drawable.outline_person_24);
        }
    }

    private void carregarFoto(String url) {
        if (isAdded() && getContext() != null) {
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.outline_person_24)
                    .error(R.drawable.outline_person_24)
                    .circleCrop()
                    .into(ivPerfilFoto);
        }
    }
}
