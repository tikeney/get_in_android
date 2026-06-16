package com.senai.get_in.api;

import com.senai.get_in.model.*;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.DELETE;

public interface ApiService {

    // --- Autenticação ---
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("auth/login-tag")
    Call<LoginResponse> loginByTag(@Body TagLoginRequest tagLoginRequest);

    @POST("auth/")
    Call<LoginResponse> register(@Body UsuarioDetalhado usuario);

    // --- Views Consolidadas (Leitura Principal) ---
    @GET("views/usuarios")
    Call<UsuarioResponse> getUsuarios();

    @GET("views/usuarios/{id}")
    Call<UsuarioDetalhadoResponse> getUsuarioDetalhadoPorId(@Path("id") int id);

    @GET("views/requisicoes")
    Call<RequisicaoResponse> getRequisicoes();

    @GET("views/logs")
    Call<LogResponse> getLogs();

    @GET("views/tags")
    Call<TagResponse> getTags();

    @GET("views/gestores")
    Call<List<Gestor>> getGestores();

    // --- Operações de Escrita (Usuários) ---
    @PUT("user/{id}")
    Call<UsuarioResponse> atualizarUsuario(@Path("id") int id, @Body UsuarioDetalhado usuario);

    @DELETE("user/{id}")
    Call<Void> deletarUsuario(@Path("id") int id);

    // --- Requisições ---
    @PUT("requisicao/{id}")
    Call<Requisicao> atualizarStatus(@Path("id") int id, @Body Requisicao requisicao);

    @GET("requisicao/setor/{id}")
    Call<RequisicaoResponse> getRequisicoesPorSetor(@Path("id") int id);

    @POST("requisicao/")
    Call<Requisicao> criarRequisicao(@Body Requisicao requisicao);

    @POST("requisicao-visitante/")
    Call<Requisicao> criarRequisicaoVisitante(@Body Requisicao requisicao);

    // --- Portaria ---
    @GET("portaria/vlocal")
    Call<VisitanteLocalResponse> getVisitantesLocal();

    @POST("portaria/checkout")
    Call<LogResponse> checkout(@Body CheckoutRequest checkoutRequest);

    @GET("portaria/historico")
    Call<LogResponse> getHistoricoPortaria();

    // --- Tabelas de Apoio ---
    @GET("dep/")
    Call<List<Departamento>> getDepartamentos();

    @GET("setores/")
    Call<SetorResponse> getSetores(); // Agora espera um objeto SetorResponse

    @GET("views/empresas")
    Call<EmpresaResponse> getEmpresas();

    // --- Filtros Específicos ---
    @GET("requisicao/func/{id}")
    Call<RequisicaoResponse> getRequisicoesPorFuncionario(@Path("id") int id);

    @GET("requisicao-visitante/user/{id}")
    Call<RequisicaoResponse> getRequisicoesVisitantePorUsuario(@Path("id") int id);
}