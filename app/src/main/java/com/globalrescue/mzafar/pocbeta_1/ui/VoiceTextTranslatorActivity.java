package com.globalrescue.mzafar.pocbeta_1.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.globalrescue.mzafar.pocbeta_1.R;
import com.globalrescue.mzafar.pocbeta_1.models.CountryModel;
import com.globalrescue.mzafar.pocbeta_1.models.LanguageModel;
import com.globalrescue.mzafar.pocbeta_1.nuance.Configuration;
import com.globalrescue.mzafar.pocbeta_1.utilities.DataUtil;
import com.globalrescue.mzafar.pocbeta_1.utilities.NetworkUtils;
import com.globalrescue.mzafar.pocbeta_1.utilities.PermissionUtil;
import com.nuance.speechkit.Audio;
import com.nuance.speechkit.AudioPlayer;
import com.nuance.speechkit.Language;
import com.nuance.speechkit.Session;
import com.nuance.speechkit.Transaction;
import com.nuance.speechkit.TransactionException;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class VoiceTextTranslatorActivity extends AppCompatActivity implements View.OnClickListener, AudioPlayer.Listener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "VTTranslator";

    private static final int REQUEST_AUDIO_N_STORAGE = 1;

    private static String[] AUDIO_N_STORAGE_PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private CountryModel mNativeCountry;
    private CountryModel mForeignCountry;
    private LanguageModel mNativeLanguageModel;
    private LanguageModel mForeignLanguageModel;
    private String NativeToForeignYandexCode;
    private String ForeignToNativeYandexCode;

    private Button btnNativeAudio;
    private Button btnForeignAudio;
    private Button btnNativeText;
    private Button btnForeignText;
    private ImageButton btnPlayAudio;

    private TextView logs;

    private View mLayout;

    NetworkUtils networkUtils;
    String recognizedResult;
    private String translatedText;
    //A Check flag for Audio Player API to know which Conversion Code to use;
    private boolean isNativeToForeignConversion;

    /*
    TTS Related Attributes - Start
     */
    private Session speechSession;
    private Transaction ttsTransaction;
    private State state = State.IDLE;
    /*
    TTS Related Attributes - End
     */

    //Default Constructor Instance for DataUtil Class
    private DataUtil dataUtilDefault;

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
            dataUtilDefault = new DataUtil();
            NativeToForeignYandexCode = mNativeLanguageModel.getYandexCode() + "-" + mForeignLanguageModel.getYandexCode();
            ForeignToNativeYandexCode = dataUtilDefault.getReverseCode(NativeToForeignYandexCode);
        }

        @Override
        public void onResultListNotification(List<?> classList) {

        }
    };

    AudioInputDialog.onInputAudioListener NativeAudioInputListner = new AudioInputDialog.onInputAudioListener() {
        @Override
        public void sendTextFromInputAudio(String input) {
            Log.d(TAG, "sendInputTextFromAudio(): input = " + input);
            logs.append(input + " = \n");

            if(!isNativeToForeignConversion){
                isNativeToForeignConversion = true;
            }
            //Default variables for translation
            recognizedResult = input;
            String languagePair = NativeToForeignYandexCode; //("<source_language>-<target_language>")
            //Executing the translation function
            Translate(recognizedResult, languagePair);
        }
    };

    AudioInputDialog.onInputAudioListener ForeignAudioInputListner = new AudioInputDialog.onInputAudioListener() {
        @Override
        public void sendTextFromInputAudio(String input) {
            Log.d(TAG, "sendInputTextFromAudio(): input = " + input);
            logs.append(input + " = \n");

            if(isNativeToForeignConversion){
                isNativeToForeignConversion = false;
            }

            //Default variables for translation
            recognizedResult = input;
            String languagePair = ForeignToNativeYandexCode; //("<source_language>-<target_language>")
            //Executing the translation function
            Translate(recognizedResult, languagePair);
        }
    };

    TextInputDialog.onInputTextListener NativeTextInputListner = new TextInputDialog.onInputTextListener() {
        @Override
        public void sendInputText(String input) {
            Log.d(TAG, "sendInput(): input = " + input);
            logs.append(input + " = \n");
            //Default variables for translation
            String textToBeTranslated = input;
            String languagePair = NativeToForeignYandexCode; // ("<source_language>-<target_language>")
            //Executing the translation function
            Translate(textToBeTranslated, languagePair);
        }
    };

    TextInputDialog.onInputTextListener ForeignTextInputListner = new TextInputDialog.onInputTextListener() {
        @Override
        public void sendInputText(String input) {
            Log.d(TAG, "sendInput(): input = " + input);
            logs.append(input + " = \n");
            //Default variables for translation
            String textToBeTranslated = input;
            String languagePair = ForeignToNativeYandexCode; // ("<source_language>-<target_language>")
            //Executing the translation function
            Translate(textToBeTranslated, languagePair);
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

        btnNativeAudio.setOnClickListener(this);
        btnForeignAudio.setOnClickListener(this);
        btnNativeText.setOnClickListener(this);
        btnForeignText.setOnClickListener(this);
        btnPlayAudio.setOnClickListener(this);

        Bundle extraBundle = getIntent().getExtras();
        mNativeCountry = (CountryModel) extraBundle.getSerializable("NATIVE_COUNTRY_MODEL");
        mForeignCountry = (CountryModel) extraBundle.getSerializable("FOREIGN_COUNTRY_MODEL");

        DataUtil dataUtil = new DataUtil();

        // TODO: 9/17/2018 Make Hardcoded Native Language Input to generic...
//        dataUtil.getLanguagenCode(dataUtil.getFirebaseDBRefernce("languages"), mNativeCountry.getCountry(), NativeLangModelListner);
//        dataUtil.getLanguagenCode(dataUtil.getFirebaseDBRefernce("languages"), mForeignCountry.getCountry(), ForeignLangModelListner);
        dataUtil.getLanguagenCodeFirestore(mNativeCountry.getCountry(),NativeLangModelListner);
        dataUtil.getLanguagenCodeFirestore(mForeignCountry.getCountry(),ForeignLangModelListner);

        logs = findViewById(R.id.logs);
        /*
        TTS Related Methods/Properties - Start
         */
        //Create a session
        speechSession = Session.Factory.session(this, Configuration.SERVER_URI, Configuration.APP_KEY);
        speechSession.getAudioPlayer().setListener(this);

        setState(State.IDLE);
        /*
        TTS Related Methods/Properties - End
         */

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /*
        TTS Related Methods - Start
     */

    /* TTS transactions */

    private void toggleTTS() {
        switch (state) {
            case IDLE:
                //If we are not loading TTS from the server, then we should do so.
                if (ttsTransaction == null) {
                    //toggleTTS.setText(getResources().getString(R.string.cancel));
                    synthesize();
                }
                //Otherwise lets attempt to cancel that transaction
                else {
                    cancel();
                }
                break;
            case PLAYING:
                speechSession.getAudioPlayer().pause();
                setState(State.PAUSED);
                break;
            case PAUSED:
                speechSession.getAudioPlayer().play();
                setState(State.PLAYING);
                break;
        }
    }

    /**
     * Speak the text that is in the ttsText EditText, using the language in the language EditText.
     */
    private void synthesize() {
        //Setup our TTS transaction options.
        Transaction.Options options = new Transaction.Options();
        if(isNativeToForeignConversion){
            options.setLanguage(new Language(mForeignLanguageModel.getNuanceCode()));
        }else {
            options.setLanguage(new Language(mNativeLanguageModel.getNuanceCode()));
        }

        //options.setVoice(new Voice(Voice.SAMANTHA)); //optionally change the Voice of the speaker, but will use the default if omitted.

        //Start a TTS transaction
        ttsTransaction = speechSession.speakString(getTranslatedText(), options, new Transaction.Listener() {
            @Override
            public void onAudio(Transaction transaction, Audio audio) {
                Log.d(TAG, "\nonAudio");

                //The TTS audio has returned from the server, and has begun auto-playing.
                ttsTransaction = null;
//                toggleTTS.setText(getResources().getString(R.string.speak_string));
            }

            @Override
            public void onSuccess(Transaction transaction, String s) {
                Log.d(TAG, "\nonSuccess");

                //Notification of a successful transaction. Nothing to do here.
            }

            @Override
            public void onError(Transaction transaction, String s, TransactionException e) {
                Log.d(TAG, "\nonError: " + e.getMessage() + ". " + s);

                //Something went wrong. Check Configuration.java to ensure that your settings are correct.
                //The user could also be offline, so be sure to handle this case appropriately.

                ttsTransaction = null;
            }
        });
    }

    /**
     * Cancel the TTS transaction.
     * This will only cancel if we have not received the audio from the server yet.
     */
    private void cancel() {
        ttsTransaction.cancel();
    }

    @Override
    public void onBeginPlaying(AudioPlayer audioPlayer, Audio audio) {
        Log.d(TAG, "\nonBeginPlaying");

        //The TTS Audio will begin playing.

        setState(State.PLAYING);
    }

    @Override
    public void onFinishedPlaying(AudioPlayer audioPlayer, Audio audio) {
        Log.d(TAG, "\nonFinishedPlaying");

        //The TTS Audio has finished playing

        setState(State.IDLE);
    }

    /* State Logic: IDLE <-> PLAYING <-> PAUSED */

    private enum State {
        IDLE,
        PLAYING,
        PAUSED
    }

    /**
     * Set the state and update the button text.
     */
    private void setState(State newState) {
        state = newState;
        switch (newState) {
            case IDLE:
                // Next possible action is speaking
//                toggleTTS.setText(getResources().getString(R.string.speak_string));
                break;
            case PLAYING:
                // Next possible action is pausing
//                toggleTTS.setText(getResources().getString(R.string.pause));
                break;
            case PAUSED:
                // Next possible action is resuming the speech
//                toggleTTS.setText(getResources().getString(R.string.speak_string));
                break;
        }
    }

    /*
        TTS Related Methods - End
     */

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
                Log.d(TAG, "onClick -> NativeAudioButton");

                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED ||  ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){

                    Log.i(TAG, "onClick: Permissions not granted...");
                    requestAudioAndStoragePermission();

                } else {

                    displayAudioInputDialog(mNativeLanguageModel.getNuanceCode(), NativeAudioInputListner);

                }
                break;
            case R.id.btn_foreign_audio:
                Log.d(TAG, "onClick -> ForeignAudioButton");

                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED ||  ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){

                    Log.i(TAG, "onClick: Permissions not granted...");
                    requestAudioAndStoragePermission();

                } else {

                    displayAudioInputDialog(mForeignLanguageModel.getNuanceCode(), ForeignAudioInputListner);

                }
                break;
            case R.id.btn_native_keyboard:
                Log.d(TAG, "onClick -> NativeKeyboardButton");

                displayTextInputDialog(NativeTextInputListner);

                break;
            case R.id.btn_foreign_keyboard:
                Log.d(TAG, "onClick -> ForeignKeyboardButton");

                displayTextInputDialog(ForeignTextInputListner);

                break;
            case R.id.btn_play_audio:
                toggleTTS();
                break;
            default:
                break;
        }


    }

    /*
     * Text to Text Translation - Start
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
        Log.d("Translation Result", translationResult);
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
    private void displayTextInputDialog(TextInputDialog.onInputTextListener listener){

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
    private void displayAudioInputDialog(String nuanceCode, AudioInputDialog.onInputAudioListener listener){
        AudioInputDialog mAudioInputDialog = new AudioInputDialog();
        FragmentManager mFragmentManager = this.getSupportFragmentManager();
        Bundle args = new Bundle();
        args.putString("NUANCE_CODE", nuanceCode);
        args.putSerializable("AUDIO_LISTENER", listener);
        // TODO: 9/17/2018 Make this native country as a generic input

        mAudioInputDialog.setArguments(args);

        mAudioInputDialog.show(mFragmentManager, "AudioInputDialog");
    }


    /**
     * Method for Handling Audio and Storage Permissions
     */
    private void requestAudioAndStoragePermission(){

        if(ActivityCompat.shouldShowRequestPermissionRationale(VoiceTextTranslatorActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale(VoiceTextTranslatorActivity.this, Manifest.permission.RECORD_AUDIO)){

            Log.i(TAG, "requestAudioAndStoragePermission: Displaying Permissions Rationale with Additional Information...");

            Snackbar.make(mLayout,R.string.permissions_audio_storage_rationale, Snackbar.LENGTH_INDEFINITE)
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
        if (requestCode == REQUEST_AUDIO_N_STORAGE){
            Log.d(TAG, "onRequestPermissionsResult: Received Response for Audio and Storage Request");

            if (PermissionUtil.verifyPermissions(grantResults)){
                Snackbar.make(mLayout,R.string.permissions_audio_storage_granted, Snackbar.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "onRequestPermissionsResult: Permissions NOT granted");
                Snackbar.make(mLayout,R.string.permissions_audio_storage_not_granted, Snackbar.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }
}
