package com.senai.get_in.model;

import java.util.List;

public class TagResponse {
    private boolean sucesso;
    private String mensagem;
    private List<TagCracha> data;

    public boolean isSucesso() { return sucesso; }
    public List<TagCracha> getData() { return data; }
    public String getMensagem() { return mensagem; }
}
