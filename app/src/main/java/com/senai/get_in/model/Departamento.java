package com.senai.get_in.model;

import com.google.gson.annotations.SerializedName;

public class Departamento {
    private int id;
    private String nome;
    
    @SerializedName("id_ger")
    private int idGer;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getIdGer() { return idGer; }
    public void setIdGer(int idGer) { this.idGer = idGer; }
}
