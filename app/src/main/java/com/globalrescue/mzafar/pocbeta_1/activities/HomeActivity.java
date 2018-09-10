package com.globalrescue.mzafar.pocbeta_1.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.globalrescue.mzafar.pocbeta_1.R;
import com.globalrescue.mzafar.pocbeta_1.models.LanguageListModel;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mDestCountryTextView;
    private TextView mDestLanguageTextView;
    private LanguageListModel mLanguageModel;
    private String mSelectedLanguage;
    private Button mTranslatorBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mDestCountryTextView = findViewById(R.id.tv_dest_country);
        mDestLanguageTextView = findViewById(R.id.tv_dest_lang);
        mTranslatorBtn = findViewById(R.id.btn_translator);

        mTranslatorBtn.setOnClickListener(this);

        Bundle extraBundle = getIntent().getExtras();
        mLanguageModel = (LanguageListModel) extraBundle.getSerializable("LANGUAGE_MODEL");
        mSelectedLanguage = mLanguageModel.getmLangName();

        String formattedLanguageText = "(" + mSelectedLanguage + ")";
        mDestCountryTextView.setText(mLanguageModel.getmLangCountry());
        mDestLanguageTextView.setText(formattedLanguageText);

    }

    @Override
    public void onClick(View v) {
        if(v == mTranslatorBtn){
            Context context = HomeActivity.this;
            Class destinationActivity = VoiceTextTranslatorActivity.class;
            Intent intent = new Intent(context, destinationActivity);
            intent.putExtra("LANGUAGE_MODEL",mLanguageModel);
            startActivity(intent);
        }
    }
}
