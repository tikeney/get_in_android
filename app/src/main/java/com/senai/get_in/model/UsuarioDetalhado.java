package com.senai.get_in.model;

import com.google.gson.annotations.SerializedName;

public class UsuarioDetalhado {
    @SerializedName("usuario_id")
    private int id;
    
    @SerializedName("usuario_nome")
    private String nome;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("cpf")
    private String cpf;
    
    @SerializedName("celular")
    private String celular;
    
    @SerializedName("cargo")
    private String cargo;
    
    @SerializedName("dataDeNascimento")
    private String dataDeNascimento;
    
    @SerializedName("foto_perfil")
    private String fotoPerfil;
    
    @SerializedName("departamento_nome")
    private String departamentoNome;
    
    @SerializedName("dataDeCriacao")
    private String dataDeCriacao;

    public UsuarioDetalhado() {}

    // Getters
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getCpf() { return cpf; }
    public String getCelular() { return celular; }
    public String getCargo() { return cargo; }
    public String getDataDeNascimento() { return dataDeNascimento; }
    public String getFotoPerfil() { return fotoPerfil; }
    public String getDepartamentoNome() { return departamentoNome; }
    public String getDataDeCriacao() { return dataDeCriacao; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setEmail(String email) { this.email = email; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public void setCelular(String celular) { this.celular = celular; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public void setDataDeNascimento(String dataDeNascimento) { this.dataDeNascimento = dataDeNascimento; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }
    public void setDepartamentoNome(String departamentoNome) { this.departamentoNome = departamentoNome; }
    public void setDataDeCriacao(String dataDeCriacao) { this.dataDeCriacao = dataDeCriacao; }
}
