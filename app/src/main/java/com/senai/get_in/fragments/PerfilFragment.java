package com.senai.get_in.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.senai.get_in.R;
import com.senai.get_in.api.RetrofitClient;
import com.senai.get_in.model.AvatarResponse;
import com.senai.get_in.model.UsuarioDetalhado;
import com.senai.get_in.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {

    private static final String TAG = "PerfilFragment";
    private TextView tvNome, tvCargo, tvEmail, tvCpf, tvCelular;
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
        ivPerfilFoto = view.findViewById(R.id.ivPerfilFoto);

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

        // Chamada para a rota de avatar (agora usando o interceptor automático)
        RetrofitClient.getApiService(requireContext()).getAvatar(user.getId()).enqueue(new Callback<AvatarResponse>() {
            @Override
            public void onResponse(@NonNull Call<AvatarResponse> call, @NonNull Response<AvatarResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    String url = response.body().getData().getUrl();
                    if (url != null && !url.isEmpty()) {
                        carregarFoto(url);
                        
                        // Atualiza o cache local do usuário com a nova URL da foto
                        user.setFotoPerfil(url);
                        tokenManager.saveUserData(user);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<AvatarResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Erro ao carregar avatar remoto", t);
            }
        });
    }

    private void exibirUsuario(UsuarioDetalhado user) {
        tvNome.setText(user.getNome());
        tvCargo.setText(user.getCargo() != null ? user.getCargo() : "Usuário");
        tvEmail.setText(user.getEmail());
        tvCpf.setText(user.getCpf());
        tvCelular.setText(user.getCelular());
        
        if (user.getFotoPerfil() != null && !user.getFotoPerfil().isEmpty()) {
            carregarFoto(user.getFotoPerfil());
        }
    }

    private void carregarFoto(String url) {
        if (isAdded() && getContext() != null) {
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.outline_person_24)
                    .circleCrop()
                    .into(ivPerfilFoto);
        }
    }
}
