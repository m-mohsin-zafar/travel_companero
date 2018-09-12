package com.globalrescue.mzafar.pocbeta_1.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.globalrescue.mzafar.pocbeta_1.R;
import com.globalrescue.mzafar.pocbeta_1.nuance.ASR;
import com.globalrescue.mzafar.pocbeta_1.nuance.Configuration;
import com.nuance.speechkit.Audio;
import com.nuance.speechkit.DetectionType;
import com.nuance.speechkit.Language;
import com.nuance.speechkit.Recognition;
import com.nuance.speechkit.ResultDeliveryType;
import com.nuance.speechkit.RecognitionType;
import com.nuance.speechkit.Session;
import com.nuance.speechkit.Transaction;
import com.nuance.speechkit.TransactionException;

public class AudioInputDialog extends DialogFragment implements View.OnClickListener, ASR.ChangeListener {

    private static final String TAG = "AudioInputDialog";

    private Button mCancelButton;
    private ImageButton mOkayButton;
    private Button mTryAgainButton;
    private ProgressBar mAudioLevelBar;
    private TextView mStatusTextView;
    private TextView mlogs;

    private Context mContext;

//    private ASR nuanceASR;

    /*
        Nuance ASR API Methods/Properties - Start
    */
    private Audio startEarcon;
    private Audio stopEarcon;
    private Audio errorEarcon;

    private Session speechSession;
    private Transaction recoTransaction;
    private State state = State.IDLE;
    /*
        Nuance ASR API Methods/Properties - End
    */

    private String resultantText;

    public onInputAudioListener mOnTextFromAudioistener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

//        nuanceASR = new ASR(mContext, this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setCancelable(false);
        View view = inflater.inflate(R.layout.dialog_audio_input, container, false);

        mCancelButton = view.findViewById(R.id.btn_audio_cancel);
        mOkayButton = view.findViewById(R.id.btn_audio_ok);
        mTryAgainButton = view.findViewById(R.id.btn_audio_retry);
        mAudioLevelBar = view.findViewById(R.id.audio_level_bar);
        mStatusTextView = view.findViewById(R.id.tv_audio_dialog_message);
        mlogs = view.findViewById(R.id.tv_audio_dialog_results);

        mCancelButton.setOnClickListener(this);
        mOkayButton.setOnClickListener(this);
        mTryAgainButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onStart() {
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        /*
        Nuance ASR API Methods/Properties - Start
         */
        speechSession = Session.Factory.session(mContext, Configuration.SERVER_URI, Configuration.APP_KEY);
        loadEarcons();
        setState(State.IDLE);
        /*
        Nuance ASR API Methods/Properties - End
         */

        super.onStart();
    }

    // Another activity comes into the foreground. Let's release the server resources if in used.
    @Override
    public void onPause() {
//        nuanceASR.actionOnPause(nuanceASR.getState());
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
        Nuance ASR API Methods/Properties - Start
    */

    /* Reco transactions */
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

//        if(progressiveResuts.isChecked()) {
//            options.setResultDeliveryType(ResultDeliveryType.PROGRESSIVE);
//        }

        //Start listening
        recoTransaction = speechSession.recognize(options, recoListener);
    }

    private Transaction.Listener recoListener = new Transaction.Listener() {
        @Override
        public void onStartedRecording(Transaction transaction) {
            Log.d(TAG, "onStartedRecording");
            mlogs.append("\nonStartedRecording");
            //We have started recording the users voice.
            //We should update our state and start polling their volume.
            setState(State.LISTENING);
            startAudioLevelPoll();
        }

        @Override
        public void onFinishedRecording(Transaction transaction) {
            Log.d(TAG, "onFinishedRecording");
            mlogs.append("\nonFinishedRecording");
            //We have finished recording the users voice.
            //We should update our state and stop polling their volume.
            setState(State.PROCESSING);
            stopAudioLevelPoll();
        }

        @Override
        public void onRecognition(Transaction transaction, Recognition recognition) {
            resultantText = recognition.getText();
            Log.d(TAG, "onRecognition: " + resultantText);
            mlogs.append("\nonRecognition: " + resultantText);
            //We have received a transcription of the users voice from the server.
        }

        @Override
        public void onSuccess(Transaction transaction, String s) {
            Log.d(TAG, "onSuccess");
            mlogs.append("\nonSuccess");
            //Notification of a successful transaction.
            setState(State.IDLE);
            mOnTextFromAudioistener.sendTextFromInputAudio(resultantText);
            getDialog().dismiss();
        }

        @Override
        public void onError(Transaction transaction, String s, TransactionException e) {
            Log.d(TAG, "onError: " + e.getMessage() + ". " + s);
            mlogs.append("\nonError: " + e.getMessage() + ". " + s);
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
            mAudioLevelBar.setProgress((int) level);
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
        mAudioLevelBar.setProgress(0);
    }


    /* State Logic: IDLE -> LISTENING -> PROCESSING -> repeat */
    private enum State {
        IDLE,
        LISTENING,
        PROCESSING
    }

    /**
     * Set the state and update the Status text.
     */
    private void setState(State newState) {
        this.state = newState;
        switch (newState) {
            case IDLE:
                mStatusTextView.setText(R.string.audio_dialog_message);
                break;
            case LISTENING:
                mStatusTextView.setText(R.string.listening);
                break;
            case PROCESSING:
                mStatusTextView.setText(R.string.processing);
                break;
        }
    }

    /* Earcons */
    private void loadEarcons() {
        //Load all the earcons from disk
        startEarcon = new Audio(mContext, R.raw.sk_start, Configuration.PCM_FORMAT);
        stopEarcon = new Audio(mContext, R.raw.sk_stop, Configuration.PCM_FORMAT);
        errorEarcon = new Audio(mContext, R.raw.sk_error, Configuration.PCM_FORMAT);
    }

    /*
        Nuance ASR API Methods/Properties - End
    */

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_audio_cancel:
                Log.d(TAG, "onClick -> Cancel ");
                getDialog().dismiss();
                break;

            case R.id.btn_audio_ok:
                Log.d(TAG, "onClick -> Okay ");
//                if (nuanceASR == null) {
//                    nuanceASR = new ASR(mContext, this);
//                }
//                nuanceASR.registerChangeListener(this);
                toggleReco();
//                mOnTextFromAudioistener.sendTextFromInputAudio(resultantText);
//                getDialog().dismiss();
                break;
            case R.id.btn_audio_retry:
                Log.d(TAG, "onClick -> Retry ");
                mlogs.setText(R.string.recognition_results);
                toggleReco();
                break;
            default:
                getDialog().dismiss();
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnTextFromAudioistener = (onInputAudioListener) getActivity();
        } catch (ClassCastException e) {
            Log.d(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
    }

    @Override
    public void notifyChanges(String changes) {
        Log.d(TAG, "notifyOnChanges");
    }

    @Override
    public void notifyRecognizedText(String text) {
        resultantText = text;
    }

    public interface onInputAudioListener {
        void sendTextFromInputAudio(String input);
    }

}