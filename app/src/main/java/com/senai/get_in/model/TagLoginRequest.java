package com.senai.get_in.model;

import com.google.gson.annotations.SerializedName;

public class TagLoginRequest {
    @SerializedName("codigo_tag")
    private String codigoTag;

    public TagLoginRequest(String codigoTag) {
        this.codigoTag = codigoTag;
    }

    public String getCodigoTag() {
        return codigoTag;
    }

    public void setCodigoTag(String codigoTag) {
        this.codigoTag = codigoTag;
    }
}
