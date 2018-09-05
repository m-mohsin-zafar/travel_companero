package com.globalrescue.mzafar.pocbeta_1.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.globalrescue.mzafar.pocbeta_1.models.LanguageListModel;
import com.globalrescue.mzafar.pocbeta_1.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DataUtil {

    private static final String TAG = "DataUtil";
    //    raw/langlist.json
    private static final int LANGLISTRESOURCEID = R.raw.langlist;

    private String jsonLangList;

    public DataUtil(Context mContext) {
        this.jsonLangList = JSONResourceReader(mContext.getResources(), LANGLISTRESOURCEID);
    }

    public String JSONResourceReader(Resources resources, int resourceId) {

        String json;
        InputStream resourceReader = resources.openRawResource(resourceId);
        try {
            int size = resourceReader.available();
            byte[] buffer = new byte[size];
            resourceReader.read(buffer);
            json = new String(buffer, "UTF-8");
        } catch (Exception e) {
            Log.e(TAG, "Unhandled exception while using JSONResourceReader", e);
            return null;
        } finally {
            try {
                resourceReader.close();
            } catch (Exception e) {
                Log.e(TAG, "Unhandled exception while using JSONResourceReader", e);
            }
        }
        return json;
    }
    public List<?> construcListFromJson(String jsonString) {
        Log.d(TAG, "In Construct List Method");
        try {
            JSONObject obj = new JSONObject(jsonString);
            JSONArray m_jArry = obj.getJSONArray("languages");
            List<LanguageListModel> langList = new ArrayList<>();
            LanguageListModel languageListModel;

            for (int i = 0; i < m_jArry.length(); i++) {
                JSONObject jo_inside = m_jArry.getJSONObject(i);
                Log.d("Details-->", jo_inside.getString("country"));
                String country_value = jo_inside.getString("country");
                String language_value = jo_inside.getString("language");

                //Add your values in your `ArrayList` as below:
                languageListModel = new LanguageListModel(language_value, country_value);

                langList.add(languageListModel);
            }

            return langList;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getJsonLangList() {
        return jsonLangList;
    }
}
