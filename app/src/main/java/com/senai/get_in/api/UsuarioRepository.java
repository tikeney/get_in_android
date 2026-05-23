package com.senai.get_in.api;

import android.content.Context;
import com.senai.get_in.model.UsuarioResponse;
import retrofit2.Callback;

public class UsuarioRepository {
    private ApiService apiService;

    public UsuarioRepository(Context context) {
        this.apiService = RetrofitClient.getApiService(context);
    }

    public void getUsuarios(Callback<UsuarioResponse> callback) {
        apiService.getUsuarios().enqueue(callback);
    }

    public void getUsuarioPorId(int id, Callback<com.senai.get_in.model.UsuarioDetalhadoResponse> callback) {
        apiService.getUsuarioDetalhadoPorId(id).enqueue(callback);
    }
}
