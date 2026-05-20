package com.senai.get_in.model;

import com.google.gson.annotations.SerializedName;

public class Requisicao {
    private int id;
    private Integer idUsuario;
    private Integer idSetor;
    private String status;
    private String motivo;
    private String validade;
    
    @SerializedName("dataDaRequisicao")
    private String dataRequisicao;
    
    private String descricao;
    private String empresa;

    @SerializedName("tipo_requisicao")
    private String tipoRequisicao;

    @SerializedName("empresa_visitante")
    private String empresaVisitante;

    @SerializedName("validade_visita")
    private String validadeVisita;

    // Campos extras de visualização (da view)
    @SerializedName(value = "usuario_nome", alternate = {"nome", "visitante"})
    private String usuarioNome;
    
    @SerializedName(value = "usuario_cpf", alternate = {"cpf"})
    private String usuarioCpf;
    
    @SerializedName(value = "departamento_nome", alternate = {"setor"})
    private String departamentoNome;

    @SerializedName("codigo_tag")
    private String codigoTag;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public Integer getIdSetor() { return idSetor; }
    public void setIdSetor(Integer idSetor) { this.idSetor = idSetor; }

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

    public String getTipoRequisicao() { return tipoRequisicao; }
    public void setTipoRequisicao(String tipoRequisicao) { this.tipoRequisicao = tipoRequisicao; }

    public String getEmpresaVisitante() { return empresaVisitante; }
    public void setEmpresaVisitante(String empresaVisitante) { this.empresaVisitante = empresaVisitante; }

    public String getValidadeVisita() { return validadeVisita; }
    public void setValidadeVisita(String validadeVisita) { this.validadeVisita = validadeVisita; }

    public String getUsuarioNome() { return usuarioNome; }
    public void setUsuarioNome(String usuarioNome) { this.usuarioNome = usuarioNome; }

    public String getUsuarioCpf() { return usuarioCpf; }
    public void setUsuarioCpf(String usuarioCpf) { this.usuarioCpf = usuarioCpf; }

    public String getDepartamentoNome() { return departamentoNome; }
    public void setDepartamentoNome(String departamentoNome) { this.departamentoNome = departamentoNome; }

    public String getCodigoTag() { return codigoTag; }
    public void setCodigoTag(String codigoTag) { this.codigoTag = codigoTag; }
}
