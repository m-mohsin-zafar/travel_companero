package com.globalrescue.mzafar.pocbeta_1.ui;

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
import android.widget.EditText;

import com.globalrescue.mzafar.pocbeta_1.R;

import java.io.Serializable;

public class TextInputDialog extends DialogFragment implements View.OnClickListener {

    private static final String TAG = "TextInputDialog";

    private EditText mInput;
    private Button mCancelButton;
    private Button mOkayButton;

    public onInputTextListener mOnInputTextListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mOnInputTextListener =(onInputTextListener) getArguments().getSerializable("TEXT_LISTENER");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setCancelable(false);
        View view = inflater.inflate(R.layout.dialog_keyboard_input, container, false);

        mInput = view.findViewById(R.id.ed_keyboard_input);
        mCancelButton = view.findViewById(R.id.btn_keyboard_cancel);
        mOkayButton = view.findViewById(R.id.btn_keyboard_ok);

        mCancelButton.setOnClickListener(this);
        mOkayButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onStart() {
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        super.onStart();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_keyboard_cancel:
                Log.d(TAG, "onClick -> Cancel ");
                getDialog().dismiss();
                break;

            case R.id.btn_keyboard_ok:
                Log.d(TAG, "onClick -> Okay ");
                String inputText = mInput.getText().toString();
                mOnInputTextListener.sendInputText(inputText);
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
    }

    public interface onInputTextListener extends Serializable{
        void sendInputText(String input);
    }
}
