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

public class AudioInputDialog extends DialogFragment implements View.OnClickListener {

    private static final String TAG = "AudioInputDialog";

    private Button mCancelButton;
    private ImageButton mOkayButton;

    public onInputAudioListener mOnAudioTextListener;

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

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_audio_cancel:
                Log.d(TAG, "onClick -> Cancel ");
                getDialog().dismiss();
                break;

            case R.id.btn_audio_ok:
                Log.d(TAG, "onClick -> Okay ");
//                String inputText = mInput.getText().toString();
//                mOnInputTextListener.sendInputText(inputText);
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
//        try {
//            mOnInputTextListener = (onInputTextListener) getActivity();
//        } catch (ClassCastException e) {
//            Log.d(TAG, "onAttach: ClassCastException: " + e.getMessage());
//        }
    }

    public interface onInputAudioListener {
        void sendInputAudio(String input);
    }
}