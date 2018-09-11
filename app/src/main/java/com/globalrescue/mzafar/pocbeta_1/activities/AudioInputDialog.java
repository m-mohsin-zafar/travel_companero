package com.globalrescue.mzafar.pocbeta_1.activities;

import android.content.Context;
import android.os.Bundle;
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

import com.globalrescue.mzafar.pocbeta_1.R;
import com.globalrescue.mzafar.pocbeta_1.nuance.ASR;
import com.nuance.speechkit.Audio;
import com.nuance.speechkit.Session;
import com.nuance.speechkit.Transaction;

public class AudioInputDialog extends DialogFragment implements View.OnClickListener, ASR.ChangeListener {

    private static final String TAG = "AudioInputDialog";

    private Button mCancelButton;
    private ImageButton mOkayButton;

    private Audio startEarcon;
    private Audio stopEarcon;
    private Audio errorEarcon;

    private Context mContext;

    private ASR nuanceASR;

    private String resultantText;

    public onInputAudioListener mOnTextFromAudioistener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        nuanceASR = new ASR(mContext, this);

        //nuanceASR.registerChangeListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setCancelable(false);
        View view = inflater.inflate(R.layout.dialog_audio_input, container, false);

        mCancelButton = view.findViewById(R.id.btn_audio_cancel);
        mOkayButton = view.findViewById(R.id.btn_audio_ok);

        mCancelButton.setOnClickListener(this);
        mOkayButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onStart() {
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        super.onStart();
    }

    // Another activity comes into the foreground. Let's release the server resources if in used.
    @Override
    public void onPause() {
        nuanceASR.actionOnPause(nuanceASR.getState());
        super.onPause();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_audio_cancel:
                Log.d(TAG, "onClick -> Cancel ");
                getDialog().dismiss();
                break;

            case R.id.btn_audio_ok:
                Log.d(TAG, "onClick -> Okay ");
                if (nuanceASR == null) {
                    nuanceASR = new ASR(mContext, this);
                }
//                nuanceASR.registerChangeListener(this);
                nuanceASR.toggleReco();
                mOnTextFromAudioistener.sendTextFromInputAudio(resultantText);
                getDialog().dismiss();
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