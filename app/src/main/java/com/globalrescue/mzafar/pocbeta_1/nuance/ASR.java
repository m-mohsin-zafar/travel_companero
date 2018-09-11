package com.globalrescue.mzafar.pocbeta_1.nuance;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.globalrescue.mzafar.pocbeta_1.R;
import com.globalrescue.mzafar.pocbeta_1.utilities.NotificationConstants;
import com.nuance.speechkit.Audio;
import com.nuance.speechkit.DetectionType;
import com.nuance.speechkit.Language;
import com.nuance.speechkit.Recognition;
import com.nuance.speechkit.RecognitionType;
import com.nuance.speechkit.Session;
import com.nuance.speechkit.Transaction;
import com.nuance.speechkit.TransactionException;

public class ASR {

    private static final String TAG = "ASR";

    private Audio startEarcon;
    private Audio stopEarcon;
    private Audio errorEarcon;

    private Session speechSession;
    private Transaction recoTransaction;
    private State state = State.IDLE;

    private Context mContext;

    public ChangeListener mChangeListener;

    public ASR(Context mContext, ChangeListener changeListner){

        this.mContext = mContext;
        //Create a session
        speechSession = Session.Factory.session(mContext, Configuration.SERVER_URI, Configuration.APP_KEY);

        loadEarcons();

        this.mChangeListener = changeListner;

        setState(State.IDLE);
        //toggle Recognition
//        toggleReco();
    }

    public void actionOnPause(State state){
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
    }

    /* Reco transactions */
//    private void toggleReco()
    public void toggleReco(){
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

//        if(progressiveResuts.isChecked()) {
//            options.setResultDeliveryType(ResultDeliveryType.PROGRESSIVE);
//        }

        //Start listening
        recoTransaction = speechSession.recognize(options, recoListener);
    }

    private Transaction.Listener recoListener = new Transaction.Listener() {
        @Override
        public void onStartedRecording(Transaction transaction) {
            Log.d(TAG,"onStartedRecording");
            //We have started recording the users voice.
            //We should update our state and start polling their volume.
            setState(State.LISTENING);
            startAudioLevelPoll();
            mChangeListener.notifyChanges(NotificationConstants.START_AUDIO_POLL_ACTION);
        }

        @Override
        public void onFinishedRecording(Transaction transaction) {
            Log.d(TAG,"onFinishedRecording");

            //We have finished recording the users voice.
            //We should update our state and stop polling their volume.
            setState(State.PROCESSING);
            stopAudioLevelPoll();
            mChangeListener.notifyChanges(NotificationConstants.STOP_AUDIO_POLL_ACTION);
        }

        @Override
        public void onRecognition(Transaction transaction, Recognition recognition) {
            Log.d(TAG, "onRecognition: " + recognition.getText());

            //We have received a transcription of the users voice from the server.
            mChangeListener.notifyRecognizedText(recognition.getText());
        }

        @Override
        public void onSuccess(Transaction transaction, String s) {
            Log.d(TAG, "onSuccess-Recoginition");

            //Notification of a successful transaction.
            mChangeListener.notifyChanges(NotificationConstants.SUCCESS_NOTIFICATION);
            setState(State.IDLE);
        }

        @Override
        public void onError(Transaction transaction, String s, TransactionException e) {
            Log.d(TAG, "onError: " + e.getMessage() + ". " + s);

            //Something went wrong. Check Configuration.java to ensure that your settings are correct.
            //The user could also be offline, so be sure to handle this case appropriately.
            //We will simply reset to the idle state.
            mChangeListener.notifyChanges(NotificationConstants.ERROR_NOTIFICATION);
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
//            volumeBar.setProgress((int)level);
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
//        volumeBar.setProgress(0);
    }
    public State getState() {
        return state;
    }

    //Method sets new State and notifies the update
    public void setState(State state) {
        this.state = state;
        notifyStateChange(state);
    }

    public void notifyStateChange(State state) {
        switch (state) {
            case IDLE:
//                toggleReco.setText(getResources().getString(R.string.recognize));
                mChangeListener.notifyChanges(NotificationConstants.STATE_IDLE);
                break;
            case LISTENING:
//                toggleReco.setText(getResources().getString(R.string.listening));
                mChangeListener.notifyChanges(NotificationConstants.STATE_LISTENING);
                break;
            case PROCESSING:
//                toggleReco.setText(getResources().getString(R.string.processing));
                mChangeListener.notifyChanges(NotificationConstants.STATE_PROCESSING);
                break;
        }
    }

    /* State Logic: IDLE -> LISTENING -> PROCESSING -> repeat */
    private enum State {
        IDLE,
        LISTENING,
        PROCESSING
    }

    /* Earcons */
    private void loadEarcons() {
        //Load all the earcons from disk
        startEarcon = new Audio(mContext, R.raw.sk_start, Configuration.PCM_FORMAT);
        stopEarcon = new Audio(mContext, R.raw.sk_stop, Configuration.PCM_FORMAT);
        errorEarcon = new Audio(mContext, R.raw.sk_error, Configuration.PCM_FORMAT);
    }

    public interface ChangeListener{
        void notifyChanges(String changes);
        void notifyRecognizedText(String text);
    }

    public void registerChangeListener(ChangeListener mChangeListener) {
        this.mChangeListener = mChangeListener;
    }
}
