package com.senai.get_in.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LoginResponse {
    @SerializedName("token")
    private String token;
    
    @SerializedName("sucesso")
    private boolean sucesso;
    
    @SerializedName("mensagem")
    private String mensagem;
    
    @SerializedName("data")
    private List<UsuarioDetalhado> data;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public boolean isSucesso() { return sucesso; }
    public void setSucesso(boolean sucesso) { this.sucesso = sucesso; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public List<UsuarioDetalhado> getData() { return data; }
    public void setData(List<UsuarioDetalhado> data) { this.data = data; }
}
