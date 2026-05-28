package com.senai.get_in.api;

import android.content.Context;
import com.senai.get_in.model.VisitanteLocalResponse;
import retrofit2.Call;
import retrofit2.Callback;

public class VisitanteRepository {
    private final ApiService apiService;

    public VisitanteRepository(Context context) {
        this.apiService = RetrofitClient.getApiService(context);
    }

    public void getVisitantesNoLocal(Callback<VisitanteLocalResponse> callback) {
        apiService.getVisitantesLocal().enqueue(callback);
    }
}
