package com.senai.get_in.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "requisicoes")
public class Requisicao {
    @PrimaryKey
    @SerializedName(value = "id", alternate = {"requisicao_id", "id_requisicao"})
    private int id;

    @SerializedName(value = "idUsuario", alternate = {"usuario_id", "user_id", "id_usuario"})
    private Integer idUsuario;

    @SerializedName(value = "idSetor", alternate = {"setor_id", "id_setor", "idDepartamento", "id_departamento"})
    private Integer idSetor;

    @SerializedName(value = "status", alternate = {"situacao", "requisicao_status", "status_nome"})
    private String status;

    @SerializedName("motivo")
    private String motivo;

    @SerializedName("validade")
    private String validade;
    
    @SerializedName(value = "dataDaRequisicao", alternate = {"data_requisicao", "created_at", "data"})
    private String dataRequisicao;
    
    @SerializedName("descricao")
    private String descricao;

    @SerializedName(value = "empresa", alternate = {"empresa_visitante", "visitante_empresa", "empresaVisitante"})
    private String empresa;

    @SerializedName(value = "tipo_requisicao", alternate = {"tipoRequisicao", "tipo"})
    private String tipoRequisicao;

    @SerializedName(value = "validade_visita", alternate = {"validadeVisita"})
    private String validadeVisita;

    @SerializedName(value = "usuario_nome", alternate = {"nome", "visitante", "user_nome", "nome_usuario"})
    private String usuarioNome;
    
    @SerializedName(value = "usuario_cpf", alternate = {"cpf", "user_cpf", "cpf_usuario"})
    private String usuarioCpf;
    
    @SerializedName(value = "departamento_nome", alternate = {"setor_nome", "dep_nome", "departamento"})
    private String departamentoNome;

    @SerializedName(value = "codigo_tag", alternate = {"tag_id", "rfid", "tag"})
    private String codigoTag;

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public Integer getIdSetor() { return idSetor; }
    public void setIdSetor(Integer idSetor) { this.idSetor = idSetor; }

    public String getStatus() { return status != null ? status.trim() : null; }
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

    // Compatibilidade com código antigo que usava getEmpresaVisitante
    public String getEmpresaVisitante() { return empresa; }
    public void setEmpresaVisitante(String empresaVisitante) { this.empresa = empresaVisitante; }
}
