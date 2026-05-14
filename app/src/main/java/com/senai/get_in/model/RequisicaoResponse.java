package com.senai.get_in.model;

import java.util.List;

public class RequisicaoResponse {
    private boolean sucesso;
    private String mensagem;
    private List<Requisicao> data;

    public boolean isSucesso() { return sucesso; }
    public List<Requisicao> getData() { return data; }
    public String getMensagem() { return mensagem; }
}
