package com.globalrescue.mzafar.pocbeta_1.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.globalrescue.mzafar.pocbeta_1.R;
import com.globalrescue.mzafar.pocbeta_1.models.LanguageListModel;

public class HomeActivity extends AppCompatActivity {

    private TextView mDestCountryTextView;
    private TextView mDestLanguageTextView;
    private LanguageListModel mLanguageModel;
    private String mSelectedLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mDestCountryTextView = findViewById(R.id.tv_dest_country);
        mDestLanguageTextView = findViewById(R.id.tv_dest_lang);

        Bundle extraBundle = getIntent().getExtras();
        mLanguageModel = (LanguageListModel) extraBundle.getSerializable("LANGUAGE_MODEL");
        mSelectedLanguage = mLanguageModel.getmLangName();

        String formattedLanguageText = "(" + mSelectedLanguage + ")";
        mDestCountryTextView.setText(mLanguageModel.getmLangCountry());
        mDestLanguageTextView.setText(formattedLanguageText);

    }
}
