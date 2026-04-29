package com.senai.get_in.model;

import com.google.gson.annotations.SerializedName;

public class Requisicao {
    private int id;
    private int idUsuario;
    private int idDepartamento;
    private String status;
    private String motivo;
    private String validade;
    
    @SerializedName("dataDaRequisicao")
    private String dataRequisicao;
    
    private String descricao;
    private String empresa;

    // Campos extras que geralmente vêm no JOIN da API para facilitar o Adapter
    private String nomeUsuario;
    private String cpfUsuario;
    private String nomeDepartamento;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public int getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(int idDepartamento) { this.idDepartamento = idDepartamento; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getValidade() { return validade; }
    public void setValidade(String validade) { this.validade = validade; }

    public String getDataRequisicao() { return dataRequisicao; }
    public void setDataRequisicao(String dataRequisicao) { this.dataRequisicao = dataRequisicao; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public String getNomeUsuario() { return nomeUsuario; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }

    public String getCpfUsuario() { return cpfUsuario; }
    public void setCpfUsuario(String cpfUsuario) { this.cpfUsuario = cpfUsuario; }

    public String getNomeDepartamento() { return nomeDepartamento; }
    public void setNomeDepartamento(String nomeDepartamento) { this.nomeDepartamento = nomeDepartamento; }
}
