package com.senai.get_in.api;

import android.content.Context;
import com.senai.get_in.model.LogResponse;
import retrofit2.Callback;

public class LogRepository {
    private final ApiService apiService;

    public LogRepository(Context context) {
        this.apiService = RetrofitClient.getApiService(context);
    }

    public void getLogs(Callback<LogResponse> callback) {
        apiService.getLogs().enqueue(callback);
    }
}
