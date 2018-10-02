package com.globalrescue.mzafar.pocbeta_1.nuance;

import android.content.Context;
import android.util.Log;

import com.globalrescue.mzafar.pocbeta_1.models.LanguageModel;
import com.nuance.speechkit.Audio;
import com.nuance.speechkit.AudioPlayer;
import com.nuance.speechkit.Language;
import com.nuance.speechkit.Session;
import com.nuance.speechkit.Transaction;
import com.nuance.speechkit.TransactionException;

public class TTS {

    private static final String TAG = "TTS";

    /*
    TTS Related Attributes - Start
     */
    private Session speechSession;
    private Transaction ttsTransaction;
    private State state = State.IDLE;
    /*
    TTS Related Attributes - End
     */

    private Context mContext;
    private AudioPlayer.Listener audioListener;
    private boolean isNativeToForeignConversion;
    private LanguageModel mNativeLanguageModel;
    private LanguageModel mForeignLanguageModel;
    private String translatedText;

    public TTS(Context mContext, AudioPlayer.Listener audioListener,
               boolean isNativeToForeignConversion, LanguageModel mNativeLanguageModel,
               LanguageModel mForeignLanguageModel, String translatedText) {

        this.mContext = mContext;
        this.audioListener = audioListener;
        this.isNativeToForeignConversion = isNativeToForeignConversion;
        this.mNativeLanguageModel = mNativeLanguageModel;
        this.mForeignLanguageModel = mForeignLanguageModel;
        this.translatedText = translatedText;

        /*
        TTS Related Methods/Properties - Start
         */
        //Create a session
        speechSession = Session.Factory.session(mContext, Configuration.SERVER_URI, Configuration.APP_KEY);
        speechSession.getAudioPlayer().setListener(audioListener);
        setState(State.IDLE);
        /*
        TTS Related Methods/Properties - End
         */
    }

    /*
        TTS Related Methods - Start
     */

    /* TTS transactions */

    public void toggleTTS() {
        switch (state) {
            case IDLE:
                //If we are not loading TTS from the server, then we should do so.
                if (ttsTransaction == null) {
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

    private void synthesize() {
        //Setup our TTS transaction options.
        Transaction.Options options = new Transaction.Options();
        if (isNativeToForeignConversion) {
            //If TTranslation was done from Native to Foreign then we must synthesize via Foreign Code and vice versa
            options.setLanguage(new Language(mForeignLanguageModel.getNuanceCode()));
        } else {
            options.setLanguage(new Language(mNativeLanguageModel.getNuanceCode()));
        }

        Log.i(TAG, options.getLanguage().toString());

        //options.setVoice(new Voice(Voice.SAMANTHA)); //optionally change the Voice of the speaker, but will use the default if omitted.

        //Start a TTS transaction
        ttsTransaction = speechSession.speakString(getTranslatedText(), options, new Transaction.Listener() {
            @Override
            public void onAudio(Transaction transaction, Audio audio) {
                Log.i(TAG, "\nonAudio");

//                The TTS audio has returned from the server, and has begun auto-playing.
                ttsTransaction = null;
            }

            @Override
            public void onSuccess(Transaction transaction, String s) {
                Log.i(TAG, "\nonSuccess");

                //Notification of a successful transaction. Nothing to do here.
            }

            @Override
            public void onError(Transaction transaction, String s, TransactionException e) {
                Log.i(TAG, "\nonError: " + e.getMessage() + ". " + s);

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

    /* State Logic: IDLE <-> PLAYING <-> PAUSED */
    public enum State {
        IDLE,
        PLAYING,
        PAUSED
    }

    /**
     * Set the state and update the button text.
     */
    public void setState(State newState) {
        this.state = newState;
    }

    public void playExistingAudio() {
        Log.i(TAG, "playExistingAudio: -> Here!");
        speechSession.getAudioPlayer().play();
    }

    /*
        TTS Related Methods - End
     */

    public String getTranslatedText() {
        return translatedText;
    }

    public Transaction getTtsTransaction() {
        return ttsTransaction;
    }

    public void setTtsTransaction(Transaction ttsTransaction) {
        this.ttsTransaction = ttsTransaction;
    }

    public State getState() {
        return state;
    }

}
