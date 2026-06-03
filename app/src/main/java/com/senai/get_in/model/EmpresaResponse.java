package com.senai.get_in.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EmpresaResponse {
    @SerializedName("sucesso")
    private boolean sucesso;

    @SerializedName("dados")
    private List<Empresa> dados;

    public boolean isSucesso() { return sucesso; }
    public void setSucesso(boolean sucesso) { this.sucesso = sucesso; }

    public List<Empresa> getDados() { return dados; }
    public void setDados(List<Empresa> dados) { this.dados = dados; }
}