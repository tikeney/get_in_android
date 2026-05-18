package com.senai.get_in.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VisitanteLocalResponse {
    @SerializedName("sucesso")
    private boolean sucesso;

    @SerializedName("mensagem")
    private String mensagem;

    @SerializedName("data")
    private List<VisitanteLocal> data;

    public boolean isSucesso() {
        return sucesso;
    }

    public void setSucesso(boolean sucesso) {
        this.sucesso = sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public List<VisitanteLocal> getData() {
        return data;
    }

    public void setData(List<VisitanteLocal> data) {
        this.data = data;
    }

    // Mantendo compatibilidade com o código antigo que chamava getDados()
    public List<VisitanteLocal> getDados() {
        return data;
    }
}
