package com.globalrescue.mzafar.pocbeta_1.models;


import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class CountryModel implements Serializable {

    private String country;
    private String flagURL;

    public CountryModel() {
    }

    public CountryModel(String country, String flagURL) {
        this.country = country;
        this.flagURL = flagURL;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getFlagURL() {
        return flagURL;
    }

    public void setFlagURL(String flagURL) {
        this.flagURL = flagURL;
    }
}
