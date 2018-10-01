package com.globalrescue.mzafar.pocbeta_1.utilities;

import android.util.Log;

import com.globalrescue.mzafar.pocbeta_1.models.GoogleTranslatePOSTRequestModel;
import com.globalrescue.mzafar.pocbeta_1.models.TranslatedTextModel;
//import com.google.cloud.translate.Translate;
//import com.google.cloud.translate.TranslateOptions;
//import com.google.cloud.translate.Translation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

//import static com.google.cloud.translate.Translate.*;

public class NetworkUtils {

    private static final String TAG = "NetworkUtils";

    //Google Translate API Key Credentials
    private static final String GOOGLE_TRANSLATE_API_KEY = "AIzaSyCvIQEnhzPQ30Y55dHSYWn0HhrR45EDCQ4";
    private static final String GOOGLE_TRANSLATE_API_URL = "https://translation.googleapis.com/language/translate/v2";

    // YANDEX Translate API
    private static final String BASE_URL = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=";
    private static final String API_KEY = "trnsl.1.1.20180912T110932Z.cccdb2ebe122d199.678ba6c16c149c1d8ef9868b155e893f9d229b7e";

    //OkHttp
    OkHttpClient client;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public NetworkUtils() {
        client = new OkHttpClient();
    }

    public String TranslateTextFromAudio(String textToBeTranslated, String languagePair) {

        String jsonString;

        BufferedReader bufferedReader = null;
        InputStream inputStream = null;
        HttpURLConnection httpJsonConnection = null;
        try {
            //Set up the translation call URL
            String yandexUrl = BASE_URL + API_KEY
                    + "&text=" + textToBeTranslated + "&lang=" + languagePair;
            yandexUrl = yandexUrl.replaceAll("\\s", "%20");
            URL yandexTranslateURL = new URL(yandexUrl);

            //Set Http Conncection, Input Stream, and Buffered Reader
            httpJsonConnection = (HttpURLConnection) yandexTranslateURL.openConnection();
            inputStream = httpJsonConnection.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            //Set string builder and insert retrieved JSON result into it
            StringBuilder jsonStringBuilder = new StringBuilder();
            while ((jsonString = bufferedReader.readLine()) != null) {
                jsonStringBuilder.append(jsonString + "\n");
            }

            //Making result human readable
            String resultString = jsonStringBuilder.toString().trim();

            DataUtil dataUtil = new DataUtil();
            TranslatedTextModel translatedTextModel = (TranslatedTextModel) dataUtil.parseJSON(TranslatedTextModel.class, resultString);
            if (translatedTextModel.getResultCode() == 200) {
                String outputText = "";
                String temp[] = translatedTextModel.getTranslatedText();
                for (int i = 0; i < temp.length; i++) {
                    outputText = temp[i];
                }
                return outputText;
            }else {
                return null;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //Close and disconnect
            if (bufferedReader != null && inputStream != null && httpJsonConnection != null){
                try {
                    bufferedReader.close();
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                httpJsonConnection.disconnect();
            }
        }
        return null;
    }

    public String GoogleTextTranslationREST(String sourceText, String sourceLang, String targetLang){

        String translatedText = "";

        GoogleTranslatePOSTRequestModel postModel = new GoogleTranslatePOSTRequestModel();
        postModel.setSourceText(sourceText);
        postModel.setSourceLang(sourceLang);
        postModel.setTargetLang(targetLang);

        DataUtil dataUtil = new DataUtil();
        String json = dataUtil.getJSON(postModel);

        HttpUrl.Builder urlBuilder = HttpUrl.parse(GOOGLE_TRANSLATE_API_URL).newBuilder();
        urlBuilder.addQueryParameter("key",GOOGLE_TRANSLATE_API_KEY);
        String url = urlBuilder.build().toString();

        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                JSONObject jsonObject = new JSONObject(response.body().string());
                translatedText = dataUtil.getTranslationResponseAsString(jsonObject);
                return translatedText;
            } else {
                return translatedText;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return translatedText;

//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.i(TAG,"Something Went Wrong, Network Call Failed!");
//                call.cancel();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                Log.i(TAG,"Got Response: "+response.body().string());
//            }
//        });
//        return translatedText;
//
//        Request request = new Request().Builder().
    }

}
