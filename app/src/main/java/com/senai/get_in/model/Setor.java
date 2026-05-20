package com.senai.get_in.model;

import com.google.gson.annotations.SerializedName;

public class Setor {
    private int id;
    private String nome;
    private String acesso;
    private String status;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getAcesso() { return acesso; }
    public void setAcesso(String acesso) { this.acesso = acesso; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
