package com.globalrescue.mzafar.pocbeta_1.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.globalrescue.mzafar.pocbeta_1.R;
import com.globalrescue.mzafar.pocbeta_1.models.CountryModel;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mDestCountryTextView;
    private ImageView mCountryFlag;
    private Button mTranslatorBtn;
    private Button mImageTranslationBtn;

    private CountryModel mForeignCountryModel;
    private CountryModel mNativeCountryModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mDestCountryTextView = findViewById(R.id.tv_dest_country);
        mTranslatorBtn = findViewById(R.id.btn_translator);
        mCountryFlag = findViewById(R.id.iv_flag_selected);
        mImageTranslationBtn = findViewById(R.id.btn_image_translator);

        mTranslatorBtn.setOnClickListener(this);
        mImageTranslationBtn.setOnClickListener(this);

        Bundle extraBundle = getIntent().getExtras();
        mForeignCountryModel = (CountryModel) extraBundle.getSerializable("FOREIGN_COUNTRY_MODEL");
        mNativeCountryModel = (CountryModel) extraBundle.getSerializable("NATIVE_COUNTRY_MODEL");

        int flagId = this.getResources().getIdentifier("com.globalrescue.mzafar.pocbeta_1:drawable/"+mForeignCountryModel.getFlagURL(),null,null);
        mDestCountryTextView.setText(mForeignCountryModel.getCountry());
        mCountryFlag.setImageResource(flagId);

    }

    @Override
    public void onClick(View v) {
        if(v == mTranslatorBtn){
            Context context = HomeActivity.this;
            Class destinationActivity = VoiceTextTranslatorActivity.class;
            Intent intent = new Intent(context, destinationActivity);
            intent.putExtra("FOREIGN_COUNTRY_MODEL",mForeignCountryModel);
            intent.putExtra("NATIVE_COUNTRY_MODEL",mNativeCountryModel);
            startActivity(intent);
        }
        else if (v == mImageTranslationBtn){
            Toast.makeText(this, "This feature will be available in next release", Toast.LENGTH_SHORT).show();
        }
    }
}
