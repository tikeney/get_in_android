package com.senai.get_in.model;

import java.util.List;

public class UsuarioResponse {
    private boolean sucesso;
    private String mensagem;
    private List<UsuarioDetalhado> data;

    public boolean isSucesso() { return sucesso; }
    public List<UsuarioDetalhado> getData() { return data; }
    public String getMensagem() { return mensagem; }
}
