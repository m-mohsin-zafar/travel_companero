package com.globalrescue.mzafar.pocbeta_1.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class GoogleTranslatePOSTRequestModel implements Serializable {

    @SerializedName("q")
    private String sourceText;

    @SerializedName("source")
    private String sourceLang;

    @SerializedName("target")
    private String targetLang;

    public GoogleTranslatePOSTRequestModel(String sourceText, String sourceLang, String targetLang) {
        this.sourceText = sourceText;
        this.sourceLang = sourceLang;
        this.targetLang = targetLang;
    }

    public GoogleTranslatePOSTRequestModel() {
    }

    public String getSourceText() {
        return sourceText;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    public String getSourceLang() {
        return sourceLang;
    }

    public void setSourceLang(String sourceLang) {
        this.sourceLang = sourceLang;
    }

    public String getTargetLang() {
        return targetLang;
    }

    public void setTargetLang(String targetLang) {
        this.targetLang = targetLang;
    }
}
