package com.globalrescue.mzafar.pocbeta_1.activities;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.globalrescue.mzafar.pocbeta_1.R;
import com.globalrescue.mzafar.pocbeta_1.models.LanguageListModel;

public class VoiceTextTranslatorActivity extends AppCompatActivity implements View.OnClickListener, TextInputDialog.onInputTextListener {

    private static final String TAG = "VTTranslator";

    private LanguageListModel mLanguageModel;
    private Button btnNativeAudio;
    private Button btnForeignAudio;
    private Button btnNativeText;
    private Button btnForeignText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_text_translator);

        btnNativeAudio = findViewById(R.id.btn_native_audio);
        btnForeignAudio = findViewById(R.id.btn_foreign_audio);
        btnNativeText = findViewById(R.id.btn_native_keyboard);
        btnForeignText = findViewById(R.id.btn_foreign_keyboard);

        btnNativeAudio.setOnClickListener(this);
        btnForeignAudio.setOnClickListener(this);
        btnNativeText.setOnClickListener(this);
        btnForeignText.setOnClickListener(this);

        Bundle extraBundle = getIntent().getExtras();
        mLanguageModel = (LanguageListModel) extraBundle.getSerializable("LANGUAGE_MODEL");

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_native_audio:

                break;
            case R.id.btn_foreign_audio:

                break;
            case R.id.btn_native_keyboard:
                Log.d(TAG, "onClick -> NativeKeyboardButton");
                TextInputDialog nTextInputDialog = new TextInputDialog();
                FragmentManager nFragmentManager = this.getSupportFragmentManager();
                nTextInputDialog.show(nFragmentManager, "NativeTextInputDialog");
                break;
            case R.id.btn_foreign_keyboard:
                Log.d(TAG, "onClick -> ForeignKeyboardButton");
                TextInputDialog fTextInputDialog = new TextInputDialog();
                FragmentManager fFragmentManager = this.getSupportFragmentManager();
                fTextInputDialog.show(fFragmentManager, "ForeignTextInputDialog");
                break;
            default:
                break;
        }


    }

    @Override
    public void sendInputText(String input) {
        Log.d(TAG, "sendInput(): input = " +input);
    }
}
