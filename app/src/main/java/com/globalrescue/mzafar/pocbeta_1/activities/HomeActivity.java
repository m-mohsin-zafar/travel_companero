package com.globalrescue.mzafar.pocbeta_1.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.globalrescue.mzafar.pocbeta_1.R;
import com.globalrescue.mzafar.pocbeta_1.models.CountryModel;
import com.globalrescue.mzafar.pocbeta_1.models.LanguageListModel;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mDestCountryTextView;
    private ImageView mCountryFlag;
    private Button mTranslatorBtn;

    private CountryModel mCountryModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mDestCountryTextView = findViewById(R.id.tv_dest_country);
        mTranslatorBtn = findViewById(R.id.btn_translator);
        mCountryFlag = findViewById(R.id.iv_flag_selected);

        mTranslatorBtn.setOnClickListener(this);

        Bundle extraBundle = getIntent().getExtras();
        mCountryModel = (CountryModel) extraBundle.getSerializable("COUNTRY_MODEL");

        int flagId = this.getResources().getIdentifier("com.globalrescue.mzafar.pocbeta_1:drawable/"+mCountryModel.getFlagURL(),null,null);
        mDestCountryTextView.setText(mCountryModel.getCountry());
        mCountryFlag.setImageResource(flagId);

    }

    @Override
    public void onClick(View v) {
        if(v == mTranslatorBtn){
            Context context = HomeActivity.this;
            Class destinationActivity = VoiceTextTranslatorActivity.class;
            Intent intent = new Intent(context, destinationActivity);
            intent.putExtra("COUNTRY_MODEL",mCountryModel);
            startActivity(intent);
        }
    }
}
