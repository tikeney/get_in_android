package com.senai.get_in.model;

import com.google.gson.annotations.SerializedName;

public class UsuarioDetalhadoResponse {
    @SerializedName(value = "sucesso", alternate = {"success", "ok"})
    private boolean sucesso;
    
    @SerializedName("mensagem")
    private String mensagem;
    
    @SerializedName(value = "data", alternate = {"usuario", "user", "dados"})
    private UsuarioDetalhado data;

    public boolean isSucesso() { return sucesso; }
    public String getMensagem() { return mensagem; }
    public UsuarioDetalhado getData() { return data; }
}
