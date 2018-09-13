package com.globalrescue.mzafar.pocbeta_1.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TranslatedTextModel implements Serializable {

    @SerializedName("code")
    private int resultCode;

    @SerializedName("lang")
    private String languagePair;

    @SerializedName("text")
    private String[] translatedText;

    public TranslatedTextModel(int resultCode, String languagePair, String[] translatedText) {
        this.resultCode = resultCode;
        this.languagePair = languagePair;
        this.translatedText = translatedText;
    }

    public TranslatedTextModel() {
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getLanguagePair() {
        return languagePair;
    }

    public void setLanguagePair(String languagePair) {
        this.languagePair = languagePair;
    }

    public String[] getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String[] translatedText) {
        this.translatedText = translatedText;
    }
}
