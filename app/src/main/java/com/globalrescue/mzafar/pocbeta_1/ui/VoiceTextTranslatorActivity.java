package com.globalrescue.mzafar.pocbeta_1.ui;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.globalrescue.mzafar.pocbeta_1.R;
import com.globalrescue.mzafar.pocbeta_1.models.CountryModel;
import com.globalrescue.mzafar.pocbeta_1.models.LanguageModel;
import com.globalrescue.mzafar.pocbeta_1.nuance.Configuration;
import com.globalrescue.mzafar.pocbeta_1.nuance.TTS;
import com.globalrescue.mzafar.pocbeta_1.root.TravelCompanero;
import com.globalrescue.mzafar.pocbeta_1.utilities.ConnectivityReceiver;
import com.globalrescue.mzafar.pocbeta_1.utilities.DataUtil;
import com.globalrescue.mzafar.pocbeta_1.utilities.NetworkUtils;
import com.globalrescue.mzafar.pocbeta_1.utilities.PermissionUtil;
import com.google.gson.annotations.SerializedName;
import com.nuance.speechkit.Audio;
import com.nuance.speechkit.AudioPlayer;
import com.nuance.speechkit.Language;
import com.nuance.speechkit.Session;
import com.nuance.speechkit.Transaction;
import com.nuance.speechkit.TransactionException;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class VoiceTextTranslatorActivity extends AppCompatActivity implements View.OnClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback, ConnectivityReceiver.ConnectivityReceiverListener {

    private static final String TAG = "VTTranslator";

    private static final int REQUEST_AUDIO_N_STORAGE = 1;

    private static String[] AUDIO_N_STORAGE_PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private CountryModel mNativeCountry;
    private CountryModel mForeignCountry;
    private LanguageModel mNativeLanguageModel;
    private LanguageModel mForeignLanguageModel;

    //Needed for Yandex API
//    private String NativeToForeignYandexCode;
//    private String ForeignToNativeYandexCode;

    private Button btnNativeAudio;
    private Button btnForeignAudio;
    private Button btnNativeText;
    private Button btnForeignText;
    private ImageButton btnPlayAudio;
    private ProgressBar pgPlayingAudio;

    private TextView logs;

    private View mLayout;

    NetworkUtils networkUtils;
    String recognizedResult;
    private String translatedText;
    private String previousText;
    //A Check flag for Audio Player API to know which Conversion Code to use;
    private boolean isNativeToForeignConversion;

    private TTS ttsService;

    private AlertDialog connectionAlert;

    //Default Constructor Instance for DataUtil Class
    private DataUtil dataUtilDefault;

    /*
    Callback Listeners/handlers
     */

    DataUtil.FirebaseDataListner NativeLangModelListner = new DataUtil.FirebaseDataListner() {
        @Override
        public void onResultNotification(Object tClass) {
            mNativeLanguageModel = (LanguageModel) tClass;

        }

        @Override
        public void onResultListNotification(List<?> classList) {

        }

    };

    DataUtil.FirebaseDataListner ForeignLangModelListner = new DataUtil.FirebaseDataListner() {
        @Override
        public void onResultNotification(Object tClass) {
            mForeignLanguageModel = (LanguageModel) tClass;

            //Setting up Yandex Codes for Text to Text Translation
//            dataUtilDefault = new DataUtil();
//            NativeToForeignYandexCode = mNativeLanguageModel.getYandexCode() + "-" + mForeignLanguageModel.getYandexCode();
//            ForeignToNativeYandexCode = dataUtilDefault.getReverseCode(NativeToForeignYandexCode);
        }

        @Override
        public void onResultListNotification(List<?> classList) {

        }
    };

    AudioInputDialog.onInputAudioListener NativeAudioInputListner = new AudioInputDialog.onInputAudioListener() {
        @Override
        public void sendTextFromInputAudio(String input) {
            Log.i(TAG, "sendInputTextFromAudio(): input = " + input);
            logs.append(input + " = \n");

            if (!isNativeToForeignConversion) {
                isNativeToForeignConversion = true;
            }
            //Default variables for translation
            recognizedResult = input;
            //Executing the translation function
            GoogleTranslation(recognizedResult, mNativeLanguageModel.getYandexCode(), mForeignLanguageModel.getYandexCode());
        }
    };

    AudioInputDialog.onInputAudioListener ForeignAudioInputListner = new AudioInputDialog.onInputAudioListener() {
        @Override
        public void sendTextFromInputAudio(String input) {
            Log.i(TAG, "sendInputTextFromAudio(): input = " + input);
            logs.append(input + " = \n");

            if (isNativeToForeignConversion) {
                isNativeToForeignConversion = false;
            }

            //Default variables for translation
            recognizedResult = input;
            //Executing the translation function
            GoogleTranslation(recognizedResult, mForeignLanguageModel.getYandexCode(), mNativeLanguageModel.getYandexCode());
        }
    };

    TextInputDialog.onInputTextListener NativeTextInputListner = new TextInputDialog.onInputTextListener() {
        @Override
        public void sendInputText(String input) {
            Log.i(TAG, "sendInput(): input = " + input);
            logs.append(input + " = \n");
            //Default variables for translation
            String textToBeTranslated = input;

            if (!isNativeToForeignConversion) {
                isNativeToForeignConversion = true;
            }

            //Executing the translation function
            GoogleTranslation(textToBeTranslated, mNativeLanguageModel.getYandexCode(), mForeignLanguageModel.getYandexCode());
        }
    };

    TextInputDialog.onInputTextListener ForeignTextInputListner = new TextInputDialog.onInputTextListener() {
        @Override
        public void sendInputText(String input) {
            Log.i(TAG, "sendInput(): input = " + input);
            logs.append(input + " = \n");
            //Default variables for translation
            String textToBeTranslated = input;

            if (isNativeToForeignConversion) {
                isNativeToForeignConversion = false;
            }

            //Executing the translation function
            GoogleTranslation(textToBeTranslated, mForeignLanguageModel.getYandexCode(), mNativeLanguageModel.getYandexCode());
        }
    };

    AudioPlayer.Listener audioPlayerListener = new AudioPlayer.Listener() {
        @Override
        public void onBeginPlaying(AudioPlayer audioPlayer, Audio audio) {
            Log.i(TAG, "\nonBeginPlaying");

            ttsService.setTtsTransaction(null);

            //The TTS Audio will begin playing.

            ttsService.setState(TTS.State.PLAYING);
        }

        @Override
        public void onFinishedPlaying(AudioPlayer audioPlayer, Audio audio) {
            Log.i(TAG, "\nonFinishedPlaying");

            //The TTS Audio has finished playing
            ttsService.setState(TTS.State.IDLE);
            pgPlayingAudio.setVisibility(View.INVISIBLE);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_text_translator);

        mLayout = findViewById(R.id.vtt_main_layout);

        btnNativeAudio = findViewById(R.id.btn_native_audio);
        btnForeignAudio = findViewById(R.id.btn_foreign_audio);
        btnNativeText = findViewById(R.id.btn_native_keyboard);
        btnForeignText = findViewById(R.id.btn_foreign_keyboard);
        btnPlayAudio = findViewById(R.id.btn_play_audio);
        pgPlayingAudio = findViewById(R.id.pg_play_audio);

        btnNativeAudio.setOnClickListener(this);
        btnForeignAudio.setOnClickListener(this);
        btnNativeText.setOnClickListener(this);
        btnForeignText.setOnClickListener(this);
        btnPlayAudio.setOnClickListener(this);

        Bundle extraBundle = getIntent().getExtras();
        mNativeCountry = (CountryModel) extraBundle.getSerializable("NATIVE_COUNTRY_MODEL");
        mForeignCountry = (CountryModel) extraBundle.getSerializable("FOREIGN_COUNTRY_MODEL");

        DataUtil dataUtil = new DataUtil();

//        dataUtil.getLanguagenCode(dataUtil.getFirebaseDBRefernce("languages"), mNativeCountry.getCountry(), NativeLangModelListner);
//        dataUtil.getLanguagenCode(dataUtil.getFirebaseDBRefernce("languages"), mForeignCountry.getCountry(), ForeignLangModelListner);
        dataUtil.getLanguagenCodeFirestore(mNativeCountry.getCountry(), NativeLangModelListner);
        dataUtil.getLanguagenCodeFirestore(mForeignCountry.getCountry(), ForeignLangModelListner);

        logs = findViewById(R.id.logs);

        checkConnection();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Code for Listening to Connection Status Broadcast on Android N and Above

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        ConnectivityReceiver connectivityReceiver = new ConnectivityReceiver();
        registerReceiver(connectivityReceiver, intentFilter);

        /*register connection status listener*/
        TravelCompanero.getInstance().setConnectivityListener(this);
    }

    // Method to manually check connection status
    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        if(!isConnected){
            Log.i(TAG, "checkConnection: Not Connected with Internet");
            showConnectionAlert();
        }
    }

    // Show an Alert in case Internet Connection is not Present
    private void showConnectionAlert() {

        if(connectionAlert != null){
            connectionAlert.dismiss();
        }
        // Create an Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the Alert Dialog Message
        builder.setMessage("Internet Connection Required")
                .setCancelable(false)
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Retry",
                        (dialog, id) -> {
                            // Restart the Activity
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        });

        connectionAlert = builder.create();
        connectionAlert.show();

    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_native_audio:
                Log.i(TAG, "onClick -> NativeAudioButton");

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, "onClick: Permissions not granted...");
                    requestAudioAndStoragePermission();

                } else {

                    displayAudioInputDialog(mNativeLanguageModel.getNuanceCode(), NativeAudioInputListner);

                }
                break;
            case R.id.btn_foreign_audio:
                Log.i(TAG, "onClick -> ForeignAudioButton");

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, "onClick: Permissions not granted...");
                    requestAudioAndStoragePermission();

                } else {

                    displayAudioInputDialog(mForeignLanguageModel.getNuanceCode(), ForeignAudioInputListner);

                }
                break;
            case R.id.btn_native_keyboard:
                Log.i(TAG, "onClick -> NativeKeyboardButton");

                displayTextInputDialog(NativeTextInputListner);

                break;
            case R.id.btn_foreign_keyboard:
                Log.i(TAG, "onClick -> ForeignKeyboardButton");

                displayTextInputDialog(ForeignTextInputListner);

                break;
            case R.id.btn_play_audio:

                if(!getTranslatedText().equals(getPreviousText())){

                    Log.i(TAG, "onClick: Play Audio -> if Condition = true");

                    if(ttsService != null){
                        ttsService = null;
                    }
                    setPreviousText(getTranslatedText());
                    ttsService = new TTS(this, audioPlayerListener, isNativeToForeignConversion,
                            mNativeLanguageModel, mForeignLanguageModel, getTranslatedText());
                    pgPlayingAudio.setVisibility(View.VISIBLE);
                    ttsService.toggleTTS();
                }else{
                    pgPlayingAudio.setVisibility(View.VISIBLE);
                    ttsService.toggleTTS();
                }
                break;
            default:
                break;
        }


    }

    public String getPreviousText() {
        return previousText;
    }

    public void setPreviousText(String previousText) {
        this.previousText = previousText;
    }

    void GoogleTranslation(String sourceText, String sourceLanguage, String targetLanguage) {
        String translationResult = null; // Returns the translated text as a String
        try {
            translationResult = new GoogleTextTranslationTask().execute(sourceText, sourceLanguage, targetLanguage).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i("G-Translation Result", translationResult);
    }

    class GoogleTextTranslationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            networkUtils = new NetworkUtils();
            String Result = networkUtils.GoogleTextTranslationREST(strings[0], strings[1], strings[2]);
            if (Result != null && !Result.equals("")) {
                return Result;
            }
            return "Got Nothing!";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            setTranslatedText(s);
            if (!s.equals("Got Nothing!")) {
                btnPlayAudio.setVisibility(View.VISIBLE);
            }
            logs.append(s + "\n");
        }
    }

    /*
     * Yandex Text to Text Translation - Start
     * */

    //Function for calling & executing the Translator Background Task
    void Translate(String textToBeTranslated, String languagePair) {
        String translationResult = null; // Returns the translated text as a String
        try {
            translationResult = new TextTranslationTask().execute(textToBeTranslated, languagePair).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i("Translation Result", translationResult);
    }

    //Task Translation Async Task Handler
    class TextTranslationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            networkUtils = new NetworkUtils();
            String Result = networkUtils.TranslateTextFromAudio(strings[0], strings[1]);
            if (Result != null && !Result.equals("")) {
                return Result;
            }
            return "Got Nothing!";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            setTranslatedText(s);
            if (!s.equals("Got Nothing!")) {
                btnPlayAudio.setVisibility(View.VISIBLE);
            }
            logs.append(s + "\n");
        }
    }
    /*
     * Text to Text Translation - End
     * */


    /**
     * Method for Displaying Text Input Dialog Fragment
     */
    private void displayTextInputDialog(TextInputDialog.onInputTextListener listener) {

        TextInputDialog mTextInputDialog = new TextInputDialog();
        FragmentManager mFragmentManager = this.getSupportFragmentManager();
        Bundle textArgs = new Bundle();
        textArgs.putSerializable("TEXT_LISTENER", listener);
        mTextInputDialog.setArguments(textArgs);

        mTextInputDialog.show(mFragmentManager, "TextInputDialog");
    }

    /**
     * Method for Displaying Audio Input Dialog Fragment
     */
    private void displayAudioInputDialog(String nuanceCode, AudioInputDialog.onInputAudioListener listener) {
        AudioInputDialog mAudioInputDialog = new AudioInputDialog();
        FragmentManager mFragmentManager = this.getSupportFragmentManager();
        Bundle args = new Bundle();
        args.putString("NUANCE_CODE", nuanceCode);
        args.putSerializable("AUDIO_LISTENER", listener);

        mAudioInputDialog.setArguments(args);

        mAudioInputDialog.show(mFragmentManager, "AudioInputDialog");
    }


    /**
     * Methods for Handling Audio and Storage Permissions
     */
    private void requestAudioAndStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(VoiceTextTranslatorActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale(VoiceTextTranslatorActivity.this, Manifest.permission.RECORD_AUDIO)) {

            Log.i(TAG, "requestAudioAndStoragePermission: Displaying Permissions Rationale with Additional Information...");

            Snackbar.make(mLayout, R.string.permissions_audio_storage_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat
                                    .requestPermissions(VoiceTextTranslatorActivity.this, AUDIO_N_STORAGE_PERMISSIONS, REQUEST_AUDIO_N_STORAGE);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(VoiceTextTranslatorActivity.this, AUDIO_N_STORAGE_PERMISSIONS, REQUEST_AUDIO_N_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_AUDIO_N_STORAGE) {
            Log.i(TAG, "onRequestPermissionsResult: Received Response for Audio and Storage Request");

            if (PermissionUtil.verifyPermissions(grantResults)) {
                Snackbar.make(mLayout, R.string.permissions_audio_storage_granted, Snackbar.LENGTH_SHORT).show();
            } else {
                Log.i(TAG, "onRequestPermissionsResult: Permissions NOT granted");
                Snackbar.make(mLayout, R.string.permissions_audio_storage_not_granted, Snackbar.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Log.i(TAG, "onNetworkConnectionChanged: -> Network Status has changed -> Status: "+isConnected);
        if(!isConnected) {
            showConnectionAlert();
        }
        if(isConnected && (connectionAlert != null) ){
            connectionAlert.dismiss();
        }
    }
}
