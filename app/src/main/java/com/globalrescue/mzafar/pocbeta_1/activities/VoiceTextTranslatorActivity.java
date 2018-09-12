package com.globalrescue.mzafar.pocbeta_1.activities;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.globalrescue.mzafar.pocbeta_1.R;
import com.globalrescue.mzafar.pocbeta_1.models.LanguageListModel;
import com.globalrescue.mzafar.pocbeta_1.nuance.Configuration;
import com.globalrescue.mzafar.pocbeta_1.utilities.NetworkUtils;
import com.nuance.speechkit.Audio;
import com.nuance.speechkit.DetectionType;
import com.nuance.speechkit.Language;
import com.nuance.speechkit.Recognition;
import com.nuance.speechkit.RecognitionType;
import com.nuance.speechkit.Session;
import com.nuance.speechkit.Transaction;
import com.nuance.speechkit.TransactionException;

import java.util.concurrent.ExecutionException;

public class VoiceTextTranslatorActivity extends AppCompatActivity implements View.OnClickListener, TextInputDialog.onInputTextListener, AudioInputDialog.onInputAudioListener {

    private static final String TAG = "VTTranslator";

    private LanguageListModel mLanguageModel;
    private Button btnNativeAudio;
    private Button btnForeignAudio;
    private Button btnNativeText;
    private Button btnForeignText;

    NetworkUtils networkUtils;
    String recognizedResult;

    /*
    ASR Related Attributes - Start
     */
    private Audio startEarcon;
    private Audio stopEarcon;
    private Audio errorEarcon;

    private TextView logs;
    private ProgressBar volumeBar;

    private Session speechSession;
    private Transaction recoTransaction;
    private State state = State.IDLE;
    /*
    ASR Related Attributes - End
     */

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

        /*
        ASR Related Methods/Properties - Start
         */
        logs = findViewById(R.id.logs);
        volumeBar = findViewById(R.id.volume_bar);
        //Create a session
        speechSession = Session.Factory.session(this, Configuration.SERVER_URI, Configuration.APP_KEY);
        loadEarcons();
        setState(State.IDLE);
        /*
        ASR Related Methods/Properties - End
         */

    }

    /*
        ASR Related Methods - Start
     */
    @Override
    protected void onPause() {
        switch (state) {
            case IDLE:
                // Nothing to do since there is no ongoing recognition
                break;
            case LISTENING:
                // End the ongoing recording
                stopRecording();
                break;
            case PROCESSING:
                // End the ongoing recording and cancel the server recognition
                // This cancel request will generate an internal onError callback even if the server
                // returns a successful recognition.
                cancel();
                break;
        }
        super.onPause();
    }

    /*
    Reco transactions
    */
    private void toggleReco() {
        switch (state) {
            case IDLE:
                recognize();
                break;
            case LISTENING:
                stopRecording();
                break;
            case PROCESSING:
                cancel();
                break;
        }
    }

    /**
     * Start listening to the user and streaming their voice to the server.
     */
    private void recognize() {
        //Setup our Reco transaction options.
        Transaction.Options options = new Transaction.Options();
        options.setRecognitionType(RecognitionType.DICTATION);
        options.setDetection(DetectionType.Long);
        options.setLanguage(new Language("eng-USA"));
        options.setEarcons(startEarcon, stopEarcon, errorEarcon, null);

        //Start listening
        recoTransaction = speechSession.recognize(options, recoListener);
    }

    private Transaction.Listener recoListener = new Transaction.Listener() {
        @Override
        public void onStartedRecording(Transaction transaction) {
            logs.append("\nonStartedRecording");

            //We have started recording the users voice.
            //We should update our state and start polling their volume.
            setState(State.LISTENING);
            startAudioLevelPoll();
        }

        @Override
        public void onFinishedRecording(Transaction transaction) {
            logs.append("\nonFinishedRecording");

            //We have finished recording the users voice.
            //We should update our state and stop polling their volume.
            setState(State.PROCESSING);
            stopAudioLevelPoll();
        }

        @Override
        public void onRecognition(Transaction transaction, Recognition recognition) {
            logs.append("\nonRecognition: " + recognition.getText());
            recognizedResult = recognition.getText();
            //We have received a transcription of the users voice from the server.
        }

        @Override
        public void onSuccess(Transaction transaction, String s) {
            logs.append("\nonSuccess");

            //Notification of a successful transaction.
            setState(State.IDLE);
            //Default variables for translation
            String textToBeTranslated = recognizedResult;
            String languagePair = "en-hi"; //English to French ("<source_language>-<target_language>")
            //Executing the translation function
            Translate(textToBeTranslated, languagePair);
        }

        @Override
        public void onError(Transaction transaction, String s, TransactionException e) {
            logs.append("\nonError: " + e.getMessage() + ". " + s);

            //Something went wrong. Check Configuration.java to ensure that your settings are correct.
            //The user could also be offline, so be sure to handle this case appropriately.
            //We will simply reset to the idle state.
            setState(State.IDLE);
        }
    };

    /**
     * Stop recording the user
     */
    private void stopRecording() {
        recoTransaction.stopRecording();
    }

    /**
     * Cancel the Reco transaction.
     * This will only cancel if we have not received a response from the server yet.
     */
    private void cancel() {
        recoTransaction.cancel();
        setState(State.IDLE);
    }

    /* Audio Level Polling */

    private Handler handler = new Handler();

    /**
     * Every 50 milliseconds we should update the volume meter in our UI.
     */
    private Runnable audioPoller = new Runnable() {
        @Override
        public void run() {
            float level = recoTransaction.getAudioLevel();
            volumeBar.setProgress((int) level);
            handler.postDelayed(audioPoller, 50);
        }
    };

    /**
     * Start polling the users audio level.
     */
    private void startAudioLevelPoll() {
        audioPoller.run();
    }

    /**
     * Stop polling the users audio level.
     */
    private void stopAudioLevelPoll() {
        handler.removeCallbacks(audioPoller);
        volumeBar.setProgress(0);
    }

    /* State Logic: IDLE -> LISTENING -> PROCESSING -> repeat */

    private enum State {
        IDLE,
        LISTENING,
        PROCESSING
    }

    /**
     * Set the state and update the button text.
     */
    private void setState(State newState) {
        state = newState;
        switch (newState) {
            case IDLE:
                btnNativeAudio.setText(R.string.recognize);
                break;
            case LISTENING:
                btnNativeAudio.setText(R.string.listening);
                break;
            case PROCESSING:
                btnNativeAudio.setText(R.string.processing);
                break;
        }
    }

    /* Earcons */

    private void loadEarcons() {
        //Load all the earcons from disk
        startEarcon = new Audio(this, R.raw.sk_start, Configuration.PCM_FORMAT);
        stopEarcon = new Audio(this, R.raw.sk_stop, Configuration.PCM_FORMAT);
        errorEarcon = new Audio(this, R.raw.sk_error, Configuration.PCM_FORMAT);
    }

    /*
        ASR Related Methods - End
     */

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_native_audio:
                Log.d(TAG, "onClick -> NativeAudioButton");
//                AudioInputDialog naAudioInputDialog = new AudioInputDialog();
//                FragmentManager naFragmentManager = this.getSupportFragmentManager();
//                naAudioInputDialog.show(naFragmentManager, "NativeAudioInputDialog");

                /* Calling an ASR Method */
                toggleReco();
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
            default:
                break;
        }


    }

    @Override
    public void sendInputText(String input) {
        Log.d(TAG, "sendInput(): input = " + input);
    }

    @Override
    public void sendTextFromInputAudio(String input) {
        Log.d(TAG, "sendInputTextFromAudio(): input = " + input);
        logs.append(input);
        //Default variables for translation
        String textToBeTranslated = input;
        String languagePair = "en-hi"; //English to French ("<source_language>-<target_language>")
        //Executing the translation function
        Translate(textToBeTranslated, languagePair);
    }

    //Function for calling executing the Translator Background Task
    void Translate(String textToBeTranslated, String languagePair) {
//        TranslatorBackgroundTask translatorBackgroundTask = new TranslatorBackgroundTask(this);
        String translationResult = null; // Returns the translated text as a String
        try {
//            translationResult = translatorBackgroundTask.execute(textToBeTranslated, languagePair).get();
            translationResult = new TextTranslationTask().execute(textToBeTranslated,languagePair).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d("Translation Result", translationResult);
//        logs.append(translationResult);// Logs the result in Android Monitor
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
            logs.append(s);
        }
    }
}
