package com.senai.get_in.model;

import com.google.gson.annotations.SerializedName;

public class Gestor {
    @SerializedName("id")
    private int id;
    
    @SerializedName("idUsuario")
    private int idUsuario;
    
    @SerializedName("gestor")
    private String nomeGestor;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public String getNomeGestor() { return nomeGestor; }
    public void setNomeGestor(String nomeGestor) { this.nomeGestor = nomeGestor; }
}
