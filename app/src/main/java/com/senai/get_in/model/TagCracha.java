package com.senai.get_in.model;

import com.google.gson.annotations.SerializedName;

public class TagCracha {
    private String codigoTag;
    @SerializedName("usuario_id")
    private int usuarioId;
    @SerializedName("usuario_nome")
    private String usuarioNome;
    @SerializedName("status_cracha")
    private String statusCracha;
    private boolean temporario;
    @SerializedName("validade_tag")
    private String validadeTag;
    @SerializedName("departamento_vinculado")
    private String departamentoVinculado;

    // Getters
    public String getCodigoTag() { return codigoTag; }
    public int getUsuarioId() { return usuarioId; }
    public String getUsuarioNome() { return usuarioNome; }
    public String getStatusCracha() { return statusCracha; }
    public boolean isTemporario() { return temporario; }
    public String getValidadeTag() { return validadeTag; }
    public String getDepartamentoVinculado() { return departamentoVinculado; }
}
