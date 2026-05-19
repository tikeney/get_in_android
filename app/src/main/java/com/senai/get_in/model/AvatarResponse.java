package com.senai.get_in.model;

import com.google.gson.annotations.SerializedName;

public class AvatarResponse {
    private boolean sucesso;
    private String mensagem;
    private AvatarData data;

    public boolean isSucesso() { return sucesso; }
    public AvatarData getData() { return data; }
    public String getMensagem() { return mensagem; }

    public static class AvatarData {
        private int id;
        private String nome;
        @SerializedName("imagem")
        private String url;

        public int getId() { return id; }
        public String getNome() { return nome; }
        public String getUrl() { return url; }
    }
}
