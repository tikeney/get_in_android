package com.senai.get_in.model;

import com.google.gson.annotations.SerializedName;

public class CheckoutRequest {
    @SerializedName("idLog")
    private Integer idLog;
    
    @SerializedName("idUsuario")
    private Integer idUsuario;
    
    @SerializedName("dataSaida")
    private String dataSaida;

    public CheckoutRequest(Integer idLog, Integer idUsuario, String dataSaida) {
        this.idLog = idLog;
        this.idUsuario = idUsuario;
        this.dataSaida = dataSaida;
    }

    // Getters and Setters
    public Integer getIdLog() { return idLog; }
    public void setIdLog(Integer idLog) { this.idLog = idLog; }
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public String getDataSaida() { return dataSaida; }
    public void setDataSaida(String dataSaida) { this.dataSaida = dataSaida; }
}
