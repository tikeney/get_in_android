package com.senai.get_in.api;

import android.content.Context;
import com.senai.get_in.model.Requisicao;
import com.senai.get_in.model.RequisicaoResponse;
import retrofit2.Callback;

public class RequisicaoRepository {
    private final ApiService apiService;

    public RequisicaoRepository(Context context) {
        this.apiService = RetrofitClient.getApiService(context);
    }

    public void getRequisicoes(Callback<RequisicaoResponse> callback) {
        apiService.getRequisicoes().enqueue(callback);
    }

    public void getRequisicoesPorSetor(int idSetor, Callback<RequisicaoResponse> callback) {
        apiService.getRequisicoesPorSetor(idSetor).enqueue(callback);
    }

    public void atualizarStatus(int id, Requisicao requisicao, Callback<Requisicao> callback) {
        apiService.atualizarStatus(id, requisicao).enqueue(callback);
    }
    
    public void criarRequisicaoVisitante(Requisicao requisicao, Callback<Requisicao> callback) {
        apiService.criarRequisicaoVisitante(requisicao).enqueue(callback);
    }
}
