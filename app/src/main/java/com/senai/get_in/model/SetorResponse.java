package com.senai.get_in.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SetorResponse {
    @SerializedName("sucesso")
    private boolean sucesso;

    @SerializedName("data")
    private List<Setor> data;

    public boolean isSucesso() { return sucesso; }
    public List<Setor> getData() { return data; }
}
