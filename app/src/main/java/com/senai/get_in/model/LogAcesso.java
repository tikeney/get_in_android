package com.senai.get_in.model;

import com.google.gson.annotations.SerializedName;

public class LogAcesso {
    @SerializedName("log_id")
    private int id;
    @SerializedName("usuario_nome")
    private String usuarioNome;
    @SerializedName("usuario_cpf")
    private String usuarioCpf;
    @SerializedName("local_dispositivo")
    private String localDispositivo;
    @SerializedName("dataDeEntrada")
    private String dataEntrada;
    @SerializedName("dataDeSaida")
    private String dataSaida;
    @SerializedName("departamento_usuario")
    private String departamentoUsuario;

    // Getters e Setters
    public int getId() { return id; }
    public String getUsuarioNome() { return usuarioNome; }
    public String getUsuarioCpf() { return usuarioCpf; }
    public String getLocalDispositivo() { return localDispositivo; }
    public String getDataEntrada() { return dataEntrada; }
    public String getDataSaida() { return dataSaida; }
    public String getDepartamentoUsuario() { return departamentoUsuario; }
}
