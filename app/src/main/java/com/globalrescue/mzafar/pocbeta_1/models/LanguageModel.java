package com.globalrescue.mzafar.pocbeta_1.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class LanguageModel implements Serializable {

    private String languageName;
    private String nuanceCode;
    private String yandexCode;
    private String languageCountry;

    public LanguageModel(String languageName, String nuanceCode, String yandexCode, String languageCountry) {
        this.languageName = languageName;
        this.nuanceCode = nuanceCode;
        this.yandexCode = yandexCode;
        this.languageCountry = languageCountry;
    }

    public LanguageModel() {
    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public String getNuanceCode() {
        return nuanceCode;
    }

    public void setNuanceCode(String nuanceCode) {
        this.nuanceCode = nuanceCode;
    }

    public String getYandexCode() {
        return yandexCode;
    }

    public void setYandexCode(String yandexCode) {
        this.yandexCode = yandexCode;
    }

    public String getLanguageCountry() {
        return languageCountry;
    }

    public void setLanguageCountry(String languageCountry) {
        this.languageCountry = languageCountry;
    }
}
