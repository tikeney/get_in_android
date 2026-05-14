package com.senai.get_in.model;

import java.util.List;

public class LogResponse {
    private boolean sucesso;
    private String mensagem;
    private List<LogAcesso> data;

    public boolean isSucesso() { return sucesso; }
    public List<LogAcesso> getData() { return data; }
    public String getMensagem() { return mensagem; }
}
