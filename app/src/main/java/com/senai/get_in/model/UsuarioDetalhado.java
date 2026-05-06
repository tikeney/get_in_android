package com.senai.get_in.model;

import com.google.gson.annotations.SerializedName;

public class UsuarioDetalhado {
    @SerializedName("usuario_id")
    private int id;
    @SerializedName("usuario_nome")
    private String nome;
    private String email;
    private String cpf;
    private String celular;
    private String cargo;
    private String dataDeNascimento;
    @SerializedName("foto_perfil")
    private String fotoPerfil;
    @SerializedName("departamento_nome")
    private String departamentoNome;
    private String dataDeCriacao;

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
}
