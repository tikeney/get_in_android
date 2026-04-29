package com.senai.get_in.api;

import com.senai.get_in.model.LoginRequest;
import com.senai.get_in.model.LoginResponse;
import com.senai.get_in.model.Requisicao;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @GET("requisicoes")
    Call<List<Requisicao>> getRequisicoes(@Header("Authorization") String token);

    @PATCH("requisicoes/{id}")
    Call<Requisicao> atualizarStatus(@Header("Authorization") String token, @Path("id") int id, @Body Requisicao requisicao);
}
