package com.senai.get_in.api;

import com.senai.get_in.model.LogAcesso;
import com.senai.get_in.model.LoginRequest;
import com.senai.get_in.model.LoginResponse;
import com.senai.get_in.model.Requisicao;
import com.senai.get_in.model.RequisicaoResponse;
import com.senai.get_in.model.TagCracha;
import com.senai.get_in.model.UsuarioDetalhado;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @GET("views/requisicoes")
    Call<RequisicaoResponse> getRequisicoes(@Header("Authorization") String token);

    @GET("views/logs")
    Call<List<LogAcesso>> getLogs(@Header("Authorization") String token);

    @GET("views/usuarios")
    Call<List<UsuarioDetalhado>> getUsuarios(@Header("Authorization") String token);

    @GET("views/tags")
    Call<List<TagCracha>> getTags(@Header("Authorization") String token);

    @PUT("requisicao/{id}")
    Call<Requisicao> atualizarStatus(@Header("Authorization") String token, @Path("id") int id, @Body Requisicao requisicao);
}
