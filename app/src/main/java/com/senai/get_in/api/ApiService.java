package com.senai.get_in.api;

import com.senai.get_in.model.*;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.DELETE;

public interface ApiService {

    // --- Autenticação ---
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("auth/")
    Call<LoginResponse> register(@Body UsuarioDetalhado usuario);

    // --- Views Consolidadas (Leitura) ---
    @GET("views/usuarios")
    Call<UsuarioResponse> getUsuarios(@Header("Authorization") String token);

    @GET("views/requisicoes")
    Call<RequisicaoResponse> getRequisicoes(@Header("Authorization") String token);

    @GET("views/logs")
    Call<LogResponse> getLogs(@Header("Authorization") String token);

    @GET("views/tags")
    Call<TagResponse> getTags(@Header("Authorization") String token);

    // --- Requisições ---
    @PUT("requisicao/{id}")
    Call<Requisicao> atualizarStatus(@Header("Authorization") String token, @Path("id") int id, @Body Requisicao requisicao);

    @POST("requisicao/")
    Call<Requisicao> criarRequisicao(@Header("Authorization") String token, @Body Requisicao requisicao);

    @POST("requisicao-visitante/")
    Call<Requisicao> criarRequisicaoVisitante(@Header("Authorization") String token, @Body Requisicao requisicao);

    // --- Portaria ---
    @GET("portaria/vlocal")
    Call<VisitanteLocalResponse> getVisitantesLocal(@Header("Authorization") String token);

    // --- Usuários ---
    @GET("user/{id}")
    Call<UsuarioResponse> getUsuarioPorId(@Header("Authorization") String token, @Path("id") int id);

    @DELETE("user/{id}")
    Call<Void> deletarUsuario(@Header("Authorization") String token, @Path("id") int id);
}
