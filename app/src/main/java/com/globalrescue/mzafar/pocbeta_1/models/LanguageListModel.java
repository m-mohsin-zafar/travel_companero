package com.globalrescue.mzafar.pocbeta_1.models;

import java.io.Serializable;

public class LanguageListModel implements Serializable {

    private String mLangName;
    private String mLangCountry;

    public LanguageListModel(String mLangName, String mLangCountry) {
        this.mLangName = mLangName;
        this.mLangCountry = mLangCountry;
    }

    public LanguageListModel() {
    }

    public String getmLangName() {
        return mLangName;
    }

    public void setmLangName(String mLangName) {
        this.mLangName = mLangName;
    }

    public String getmLangCountry() {
        return mLangCountry;
    }

    public void setmLangCountry(String mLangCountry) {
        this.mLangCountry = mLangCountry;
    }
}
