package com.globalrescue.mzafar.pocbeta_1.root;

import android.app.Application;

import com.globalrescue.mzafar.pocbeta_1.utilities.ConnectivityReceiver;

public class TravelCompanero extends Application {

    private static TravelCompanero mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
    }

    public static synchronized TravelCompanero getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }
}
