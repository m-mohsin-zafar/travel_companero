package com.globalrescue.mzafar.pocbeta_1.activities;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.globalrescue.mzafar.pocbeta_1.R;
import com.globalrescue.mzafar.pocbeta_1.models.LanguageListModel;
import com.globalrescue.mzafar.pocbeta_1.nuance.Configuration;
import com.globalrescue.mzafar.pocbeta_1.utilities.NetworkUtils;
import com.nuance.speechkit.Audio;
import com.nuance.speechkit.AudioPlayer;
import com.nuance.speechkit.DetectionType;
import com.nuance.speechkit.Language;
import com.nuance.speechkit.Recognition;
import com.nuance.speechkit.RecognitionType;
import com.nuance.speechkit.Session;
import com.nuance.speechkit.Transaction;
import com.nuance.speechkit.TransactionException;

import java.util.concurrent.ExecutionException;

public class VoiceTextTranslatorActivity extends AppCompatActivity implements View.OnClickListener, TextInputDialog.onInputTextListener, AudioInputDialog.onInputAudioListener, AudioPlayer.Listener {

    private static final String TAG = "VTTranslator";

    private LanguageListModel mLanguageModel;
    private Button btnNativeAudio;
    private Button btnForeignAudio;
    private Button btnNativeText;
    private Button btnForeignText;

    private TextView logs;

    private ImageButton btnPlayAudio;

    NetworkUtils networkUtils;
    String recognizedResult;
    private String translatedText;

    /*
    TTS Related Attributes - Start
     */
    private Session speechSession;
    private Transaction ttsTransaction;
    private State state = State.IDLE;
    /*
    TTS Related Attributes - End
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_text_translator);

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
        mLanguageModel = (LanguageListModel) extraBundle.getSerializable("LANGUAGE_MODEL");
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
        options.setLanguage(new Language("hin-IND"));
        //options.setVoice(new Voice(Voice.SAMANTHA)); //optionally change the Voice of the speaker, but will use the default if omitted.

        //Start a TTS transaction
        ttsTransaction = speechSession.speakString(getTranslatedText(), options, new Transaction.Listener() {
            @Override
            public void onAudio(Transaction transaction, Audio audio) {
                Log.d(TAG,"\nonAudio");

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
                Log.d(TAG,"\nonError: " + e.getMessage() + ". " + s);

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
        Log.d(TAG,"\nonBeginPlaying");

        //The TTS Audio will begin playing.

        setState(State.PLAYING);
    }

    @Override
    public void onFinishedPlaying(AudioPlayer audioPlayer, Audio audio) {
        Log.d(TAG,"\nonFinishedPlaying");

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
                AudioInputDialog naAudioInputDialog = new AudioInputDialog();
                FragmentManager naFragmentManager = this.getSupportFragmentManager();
                naAudioInputDialog.show(naFragmentManager, "NativeAudioInputDialog");
                break;
            case R.id.btn_foreign_audio:
                Log.d(TAG, "onClick -> ForeignAudioButton");
                AudioInputDialog nfAudioInputDialog = new AudioInputDialog();
                FragmentManager nfFragmentManager = this.getSupportFragmentManager();
                nfAudioInputDialog.show(nfFragmentManager, "ForeignAudioInputDialog");
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
            case R.id.btn_play_audio:
                toggleTTS();
                break;
            default:
                break;
        }


    }

    @Override
    public void sendInputText(String input) {
        Log.d(TAG, "sendInput(): input = " + input);
        logs.append(input + " = \n");
        //Default variables for translation
        String textToBeTranslated = input;
        String languagePair = "en-hi"; //English to Hindi ("<source_language>-<target_language>")
        //Executing the translation function
        Translate(textToBeTranslated, languagePair);
    }

    @Override
    public void sendTextFromInputAudio(String input) {
        Log.d(TAG, "sendInputTextFromAudio(): input = " + input);
        logs.append(input + " = \n");
        //Default variables for translation
        recognizedResult = input;
        String languagePair = "en-hi"; //English to Hindi ("<source_language>-<target_language>")
        //Executing the translation function
        Translate(recognizedResult, languagePair);
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
            if(!s.equals("Got Nothing!")){
                btnPlayAudio.setVisibility(View.VISIBLE);
            }
            logs.append(s + "\n");
        }
    }
    /*
     * Text to Text Translation - End
     * */

}
