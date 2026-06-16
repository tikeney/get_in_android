package com.senai.get_in.model;

import com.google.gson.annotations.SerializedName;

public class LogAcesso {
    @SerializedName("log_id")
    private int id;
    @SerializedName(value = "usuario_nome", alternate = {"nome", "usuarioNome"})
    private String usuarioNome;
    @SerializedName(value = "usuario_cpf", alternate = {"cpf", "usuarioCpf"})
    private String usuarioCpf;
    @SerializedName(value = "local_dispositivo", alternate = {"local", "localDispositivo", "empresa"})
    private String localDispositivo;
    @SerializedName(value = "dataDeEntrada", alternate = {"data_entrada", "dataEntrada", "entrada"})
    private String dataEntrada;
    @SerializedName(value = "dataDeSaida", alternate = {"data_saida", "dataSaida", "saida"})
    private String dataSaida;
    @SerializedName(value = "departamento_usuario", alternate = {"departamento", "setor", "departamentoUsuario"})
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
