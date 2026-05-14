package com.senai.get_in.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    private String token;
    private boolean sucesso;
    private String mensagem;
    private UserData data;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public boolean isSucesso() { return sucesso; }
    public void setSucesso(boolean sucesso) { this.sucesso = sucesso; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public UserData getData() { return data; }
    public void setData(UserData data) { this.data = data; }

    public static class UserData {
        @SerializedName(value = "id", alternate = {"usuario_id", "userId"})
        private int id;
        
        @SerializedName(value = "nome", alternate = {"usuario_nome", "userName"})
        private String nome;

        @SerializedName("cpf")
        private String cpf;

        @SerializedName("celular")
        private String celular;

        @SerializedName("email")
        private String email;

        @SerializedName("cargo")
        private String cargo;

        @SerializedName(value = "data_criacao", alternate = {"dataDeCriacao", "createdAt"})
        private String dataDeCriacao;

        public int getId() { return id; }
        public String getNome() { return nome; }
        public String getCpf() { return cpf; }
        public String getCelular() { return celular; }
        public String getEmail() { return email; }
        public String getCargo() { return cargo; }
        public String getDataDeCriacao() { return dataDeCriacao; }
    }
}
