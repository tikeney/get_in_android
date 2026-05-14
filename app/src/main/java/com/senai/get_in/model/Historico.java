package com.senai.get_in.model;

public class Historico {
    private String nome;
    private String empresa;
    private String setor;
    private String status;
    private String hora;
    private String duracao;
    private int fotoResId;

    public Historico(String nome, String empresa, String setor, String status, String hora, String duracao, int fotoResId) {
        this.nome = nome;
        this.empresa = empresa;
        this.setor = setor;
        this.status = status;
        this.hora = hora;
        this.duracao = duracao;
        this.fotoResId = fotoResId;
    }

    public String getNome() { return nome; }
    public String getEmpresa() { return empresa; }
    public String getSetor() { return setor; }
    public String getStatus() { return status; }
    public String getHora() { return hora; }
    public String getDuracao() { return duracao; }
    public int getFotoResId() { return fotoResId; }
}
