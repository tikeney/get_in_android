package com.senai.get_in.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("token")
    public String token;
    
    @SerializedName("sucesso")
    public boolean sucesso;
    
    @SerializedName("mensagem")
    public String mensagem;
    
    @SerializedName("data")
    public LoginData data;

    public String getToken() { return token; }
    public boolean isSucesso() { return sucesso; }
    public String getMensagem() { return mensagem; }
    public LoginData getData() { return data; }

    public static class LoginData {
        @SerializedName("usuario")
        public UsuarioInterno usuario;
        
        @SerializedName("funcionario")
        public FuncionarioInterno funcionario;
    }

    public static class UsuarioInterno {
        @SerializedName(value = "id", alternate = {"usuario_id", "userId"})
        public int id;
        
        @SerializedName(value = "nome", alternate = {"usuario_nome", "name"})
        public String nome;
        
        @SerializedName("email")
        public String email;
        
        @SerializedName("cpf")
        public String cpf;
        
        @SerializedName("celular")
        public String celular;

        @SerializedName(value = "dataDeCriacao", alternate = {"data_criacao", "createdAt"})
        public String dataDeCriacao;
    }

    public static class FuncionarioInterno {
        @SerializedName(value = "tipo", alternate = {"cargo", "role"})
        public String tipo;
        
        @SerializedName("id")
        public int id;

        @SerializedName(value = "idSetor", alternate = {"setor_id", "id_setor"})
        public Integer idSetor;
    }
}
