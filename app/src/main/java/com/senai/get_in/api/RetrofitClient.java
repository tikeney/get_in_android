package com.senai.get_in.api;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.senai.get_in.LoginActivity;
import com.senai.get_in.utils.TokenManager;
import com.senai.get_in.utils.ToastUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://api.getin.dev.br/";
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    public static synchronized ApiService getApiService(Context context) {
        if (retrofit == null) {
            Context appContext = context.getApplicationContext();
            TokenManager tokenManager = new TokenManager(appContext);

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        String token = tokenManager.getToken();

                        Request.Builder requestBuilder = original.newBuilder()
                                .header("Accept", "application/json")
                                .header("Content-Type", "application/json");

                        if (token != null && !token.isEmpty()) {
                            requestBuilder.header("Authorization", "Bearer " + token);
                        }

                        Response response = chain.proceed(requestBuilder.build());

                        // Tratamento global de erro 401 (Não autorizado/Token expirado)
                        if (response.code() == 401) {
                            handleUnauthorized(appContext, tokenManager);
                        }

                        return response;
                    })
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }

    private static void handleUnauthorized(Context context, TokenManager tokenManager) {
        tokenManager.clear();
        new Handler(Looper.getMainLooper()).post(() -> {
            ToastUtils.showError(context, "Sessão expirada. Por favor, faça login novamente.");
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        });
    }
}
