package com.senai.get_in.model;

import java.util.List;

public class VisitanteLocalResponse {
    private boolean sucesso;
    private List<VisitanteLocal> dados;

    public boolean isSucesso() { return sucesso; }
    public List<VisitanteLocal> getDados() { return dados; }
}
