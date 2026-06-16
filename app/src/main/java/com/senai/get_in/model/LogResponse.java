package com.senai.get_in.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class LogResponse {
    @SerializedName(value = "sucesso", alternate = {"success", "ok"})
    private boolean sucesso;
    
    @SerializedName("mensagem")
    private String mensagem;
    
    @SerializedName(value = "data", alternate = {"logs", "dados", "historico"})
    private List<LogAcesso> data;

    public boolean isSucesso() { return sucesso; }
    public List<LogAcesso> getData() { return data; }
    public String getMensagem() { return mensagem; }
}
