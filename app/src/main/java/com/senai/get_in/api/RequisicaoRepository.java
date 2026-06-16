package com.senai.get_in.api;

import android.content.Context;
import android.os.AsyncTask;

import com.senai.get_in.database.AppDatabase;
import com.senai.get_in.database.RequisicaoDao;
import com.senai.get_in.model.Requisicao;
import com.senai.get_in.model.RequisicaoResponse;

import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequisicaoRepository {
    private final ApiService apiService;
    private final RequisicaoDao requisicaoDao;

    public RequisicaoRepository(Context context) {
        this.apiService = RetrofitClient.getApiService(context);
        this.requisicaoDao = AppDatabase.getInstance(context).requisicaoDao();
    }

    public interface LocalCallback<T> {
        void onLoaded(T data);
    }

    public void getRequisicoes(Callback<RequisicaoResponse> callback) {
        apiService.getRequisicoes().enqueue(new Callback<RequisicaoResponse>() {
            @Override
            public void onResponse(Call<RequisicaoResponse> call, Response<RequisicaoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveToLocal(response.body().getData());
                }
                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<RequisicaoResponse> call, Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }

    public void getRequisicoesPorSetor(int idSetor, Callback<RequisicaoResponse> callback) {
        apiService.getRequisicoesPorSetor(idSetor).enqueue(new Callback<RequisicaoResponse>() {
            @Override
            public void onResponse(Call<RequisicaoResponse> call, Response<RequisicaoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveToLocal(response.body().getData());
                }
                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<RequisicaoResponse> call, Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }

    public void getHistoricoLocal(LocalCallback<List<Requisicao>> callback) {
        AsyncTask.execute(() -> {
            List<Requisicao> localData = requisicaoDao.getHistorico();
            callback.onLoaded(localData);
        });
    }

    public void getHistoricoLocalPorSetor(int idSetor, LocalCallback<List<Requisicao>> callback) {
        AsyncTask.execute(() -> {
            List<Requisicao> localData = requisicaoDao.getHistoricoPorSetor(idSetor);
            callback.onLoaded(localData);
        });
    }

    private void saveToLocal(List<Requisicao> data) {
        if (data == null) return;
        AsyncTask.execute(() -> {
            requisicaoDao.insertAll(data);
        });
    }

    public void atualizarStatus(int id, Requisicao requisicao, Callback<Requisicao> callback) {
        apiService.atualizarStatus(id, requisicao).enqueue(callback);
    }
    
    public void criarRequisicaoVisitante(Requisicao requisicao, Callback<Requisicao> callback) {
        apiService.criarRequisicaoVisitante(requisicao).enqueue(callback);
    }
}
