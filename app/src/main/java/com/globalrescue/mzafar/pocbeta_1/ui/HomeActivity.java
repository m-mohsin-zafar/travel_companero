package com.globalrescue.mzafar.pocbeta_1.ui;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.globalrescue.mzafar.pocbeta_1.R;
import com.globalrescue.mzafar.pocbeta_1.models.CountryModel;
import com.globalrescue.mzafar.pocbeta_1.root.TravelCompanero;
import com.globalrescue.mzafar.pocbeta_1.utilities.ConnectivityReceiver;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener,
        ConnectivityReceiver.ConnectivityReceiverListener{

    private static final String TAG = "HomeActivity";

    private TextView mDestCountryTextView;
    private ImageView mCountryFlag;
    private Button mTranslatorBtn;
    private Button mImageTranslationBtn;

    private CountryModel mForeignCountryModel;
    private CountryModel mNativeCountryModel;

    private AlertDialog connectionAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mDestCountryTextView = findViewById(R.id.tv_dest_country);
        mTranslatorBtn = findViewById(R.id.btn_translator);
        mCountryFlag = findViewById(R.id.iv_flag_selected);
        mImageTranslationBtn = findViewById(R.id.btn_image_translator);

        mTranslatorBtn.setOnClickListener(this);
        mImageTranslationBtn.setOnClickListener(this);

        Bundle extraBundle = getIntent().getExtras();
        mForeignCountryModel = (CountryModel) extraBundle.getSerializable("FOREIGN_COUNTRY_MODEL");
        mNativeCountryModel = (CountryModel) extraBundle.getSerializable("NATIVE_COUNTRY_MODEL");

        int flagId = this.getResources().getIdentifier("com.globalrescue.mzafar.pocbeta_1:drawable/"+mForeignCountryModel.getFlagURL(),null,null);
        mDestCountryTextView.setText(mForeignCountryModel.getCountry());
        mCountryFlag.setImageResource(flagId);

        //Manually Checking Internet Connection
        checkConnection();

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

    @Override
    public void onClick(View v) {
        if(v == mTranslatorBtn){
            Context context = HomeActivity.this;
            Class destinationActivity = VoiceTextTranslatorActivity.class;
            Intent intent = new Intent(context, destinationActivity);
            intent.putExtra("FOREIGN_COUNTRY_MODEL",mForeignCountryModel);
            intent.putExtra("NATIVE_COUNTRY_MODEL",mNativeCountryModel);
            startActivity(intent);
        }
        else if (v == mImageTranslationBtn){
            Context context = HomeActivity.this;
            Class destinationActivity = ImageTranslationActivity.class;
            Intent intent = new Intent(context, destinationActivity);
            intent.putExtra("FOREIGN_COUNTRY_MODEL",mForeignCountryModel);
            intent.putExtra("NATIVE_COUNTRY_MODEL",mNativeCountryModel);
            startActivity(intent);
//            Toast.makeText(this, "This feature will be available in next release", Toast.LENGTH_SHORT).show();
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
