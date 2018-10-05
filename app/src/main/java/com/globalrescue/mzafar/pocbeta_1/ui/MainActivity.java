package com.globalrescue.mzafar.pocbeta_1.ui;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.globalrescue.mzafar.pocbeta_1.R;
import com.globalrescue.mzafar.pocbeta_1.adapters.LangsSelectionStatePagerAdapter;
import com.globalrescue.mzafar.pocbeta_1.models.CountryModel;
import com.globalrescue.mzafar.pocbeta_1.root.TravelCompanero;
import com.globalrescue.mzafar.pocbeta_1.utilities.ConnectivityReceiver;


public class MainActivity extends AppCompatActivity implements
        NativeLangSelectionFragment.OnFragmentInteractionListener,
        ForeignLangSelectionFragment.OnFragmentInteractionListener,
        ConnectivityReceiver.ConnectivityReceiverListener{

    private static final String TAG = "MainActivity";
    private ViewPager mViewPager;

    private CountryModel nativeCountry;
    private CountryModel foreignCountry;

    private AlertDialog connectionAlert;

    private ConnectivityReceiver connectivityReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = findViewById(R.id.main_activity_fragments_container);

        // Manually checking internet connection
        checkConnection();

        setupViewPagerAdapter(mViewPager);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Code for Listening to Connection Status Broadcast on Android N and Above

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        connectivityReceiver = new ConnectivityReceiver();
        registerReceiver(connectivityReceiver, intentFilter);

        /*register connection status listener*/
        TravelCompanero.getInstance().setConnectivityListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(connectivityReceiver);
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

    private void setupViewPagerAdapter(ViewPager viewPager) {
        LangsSelectionStatePagerAdapter adapter = new LangsSelectionStatePagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new NativeLangSelectionFragment(), "NLSelectionFragment");
        adapter.addFragment(new ForeignLangSelectionFragment(), "FLSelectionFragment");
        viewPager.setAdapter(adapter);
    }

    public void setFragmentOnViewPager(int fragmentNumber){
        mViewPager.setCurrentItem(fragmentNumber);
    }

    public void startHomeActivity(){
        if (foreignCountry != null && nativeCountry != null){
            Context context = MainActivity.this;
            Class destinationActivity = HomeActivity.class;
            Intent intent = new Intent(context, destinationActivity);
            intent.putExtra("FOREIGN_COUNTRY_MODEL", foreignCountry);
            intent.putExtra("NATIVE_COUNTRY_MODEL", nativeCountry);
            startActivity(intent);
        } else{
            Toast.makeText(this, "Something is wrong..", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFragmentTag(int viewPagerId, int fragmentPosition)
    {
        return "android:switcher:" + viewPagerId + ":" + fragmentPosition;
    }

    public CountryModel getNativeCountry() {
        return nativeCountry;
    }

    @Override
    public void onNLFragmentInteraction(Object model) {
        nativeCountry = (CountryModel) model;
        Log.i(TAG, "onNLFragmentInteraction: "+nativeCountry.getCountry());
    }

    @Override
    public void onFLFragmentInteraction(Object model) {
        foreignCountry = (CountryModel) model;
        Log.i(TAG, "onNLFragmentInteraction: "+foreignCountry.getCountry());
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
