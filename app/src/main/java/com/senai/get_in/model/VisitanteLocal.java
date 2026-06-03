package com.senai.get_in.model;

import com.google.gson.annotations.SerializedName;

public class VisitanteLocal {
    @SerializedName("id")
    private int id;
    
    @SerializedName("nome")
    private String nome;
    
    @SerializedName("cpf")
    private String cpf;
    
    @SerializedName("empresa")
    private String empresa;
    
    @SerializedName("setor")
    private String setor;
    
    @SerializedName("idRequisicao")
    private Integer idRequisicao;
    
    @SerializedName("motivo")
    private String motivo;
    
    @SerializedName("descricao")
    private String descricao;
    
    @SerializedName("idLog")
    private Integer idLog;
    
    @SerializedName("entrada")
    private String entrada;
    
    @SerializedName("dataEntrada")
    private String dataEntrada;
    
    @SerializedName("dataSaida")
    private String dataSaida;
    
    @SerializedName("celular")
    private String celular;
    
    @SerializedName("telefone")
    private String telefone;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("status")
    private String status;

    // Getters
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getEmpresa() { return empresa; }
    public String getSetor() { return setor; }
    public Integer getIdRequisicao() { return idRequisicao; }
    public String getMotivo() { return motivo; }
    public String getDescricao() { return descricao; }
    public Integer getIdLog() { return idLog; }
    public String getEntrada() { return entrada; }
    public String getDataEntrada() { return dataEntrada; }
    public String getDataSaida() { return dataSaida; }
    public String getCelular() { return celular; }
    public String getTelefone() { return telefone; }
    public String getEmail() { return email; }
    public String getStatus() { return status; }
}
