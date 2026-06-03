package com.senai.get_in.model;

import com.google.gson.annotations.SerializedName;

public class Requisicao {
    @SerializedName(value = "id", alternate = {"requisicao_id"})
    private int id;

    @SerializedName(value = "idUsuario", alternate = {"usuario_id", "user_id"})
    private Integer idUsuario;

    @SerializedName(value = "idSetor", alternate = {"setor_id", "id_setor", "idDepartamento"})
    private Integer idSetor;

    @SerializedName("status")
    private String status;

    @SerializedName("motivo")
    private String motivo;

    @SerializedName("validade")
    private String validade;
    
    @SerializedName(value = "dataDaRequisicao", alternate = {"data_requisicao", "created_at"})
    private String dataRequisicao;
    
    @SerializedName("descricao")
    private String descricao;

    @SerializedName("empresa")
    private String empresa;

    @SerializedName(value = "tipo_requisicao", alternate = {"tipoRequisicao", "tipo"})
    private String tipoRequisicao;

    @SerializedName(value = "empresa_visitante", alternate = {"empresaVisitante"})
    private String empresaVisitante;

    @SerializedName(value = "validade_visita", alternate = {"validadeVisita"})
    private String validadeVisita;

    // Campos extras de visualização (da view consolidada)
    @SerializedName(value = "usuario_nome", alternate = {"nome", "visitante", "user_nome"})
    private String usuarioNome;
    
    @SerializedName(value = "usuario_cpf", alternate = {"cpf", "user_cpf"})
    private String usuarioCpf;
    
    @SerializedName(value = "departamento_nome", alternate = {"setor", "dep_nome", "departamento"})
    private String departamentoNome;

    public Integer getIdDepartamento() { return idSetor; }
    public void setIdDepartamento(Integer idDepartamento) { this.idSetor = idDepartamento; }

    @SerializedName(value = "codigo_tag", alternate = {"tag_id", "rfid"})
    private String codigoTag;

    // Getters e Setters
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
