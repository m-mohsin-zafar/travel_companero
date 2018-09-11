package com.globalrescue.mzafar.pocbeta_1.nuance;

import android.net.Uri;

import com.nuance.speechkit.PcmFormat;

public class Configuration {

    //Credentials to access API services
    public static final String APP_KEY = "3e1f8b3cd62ee6acfc2bb7fca0441fbfcdce4061c30886e3249ff9a0fd8e473badd27784005d30fc89e8f8292c18e8f9e6285aa931e08c557f85f57c143cb7c1";
    public static final String APP_ID = "NMDPTRIAL_mmohsin970_gmail_com20180903012323";
    public static final String SERVER_HOST = "sslsandbox-nmdp.nuancemobility.net";
    public static final String SERVER_PORT = "443";

    public static final String LANGUAGE = "!LANGUAGE!";

    public static final Uri SERVER_URI = Uri.parse("nmsps://" + APP_ID + "@" + SERVER_HOST + ":" + SERVER_PORT);

    //Only needed if using NLU
    public static final String CONTEXT_TAG = "!NLU_CONTEXT_TAG!";

    public static final PcmFormat PCM_FORMAT = new PcmFormat(PcmFormat.SampleFormat.SignedLinear16, 16000, 1);
    public static final String LANGUAGE_CODE = (Configuration.LANGUAGE.contains("!") ? "eng-USA" : Configuration.LANGUAGE);
}
