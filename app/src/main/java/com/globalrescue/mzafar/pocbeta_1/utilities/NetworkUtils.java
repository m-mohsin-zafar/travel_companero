package com.globalrescue.mzafar.pocbeta_1.utilities;

import android.util.Log;

import com.globalrescue.mzafar.pocbeta_1.models.TranslatedTextModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class NetworkUtils {

    private static final String BASE_URL = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=";
    private static final String API_KEY = "trnsl.1.1.20180912T110932Z.cccdb2ebe122d199.678ba6c16c149c1d8ef9868b155e893f9d229b7e";

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
}
