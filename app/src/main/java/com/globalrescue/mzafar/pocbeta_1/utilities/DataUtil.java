package com.globalrescue.mzafar.pocbeta_1.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;

import com.globalrescue.mzafar.pocbeta_1.models.CountryModel;
import com.globalrescue.mzafar.pocbeta_1.models.LanguageListModel;
import com.globalrescue.mzafar.pocbeta_1.R;
import com.globalrescue.mzafar.pocbeta_1.models.LanguageModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.gson.Gson;

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

    //Access FireBase DB
    private DatabaseReference mDB;
    private static final String fRootPrefix = "PoC_travel_db/";

    private FirebaseDataListner dataListner;

    public DataUtil(Context mContext, FirebaseDataListner dataListner) {
        this.jsonLangList = JSONResourceReader(mContext.getResources(), LANGLISTRESOURCEID);
        this.dataListner = dataListner;
    }

//    public DataUtil(FirebaseDataListner dataListner){
//        this.dataListner = dataListner;
//    }

    public DataUtil() {
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

    public <T> Object parseJSON(Class<T> tClass, String jsonString) {
        Gson gson = new Gson();
        T clazz = gson.fromJson(jsonString, tClass);
        return clazz;
    }

    public DatabaseReference getFirebaseDBRefernce(String refernceFor) {
        if (mDB == null) {
            mDB = FirebaseDatabase.getInstance().getReference(fRootPrefix+refernceFor);
        } else {
            mDB = FirebaseDatabase.getInstance().getReference(fRootPrefix+refernceFor);
        }
        return mDB;
    }

    public void getListOfCountries(final DatabaseReference databaseReference) {

        mDB.orderByChild("country").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                CountryModel countryModel = null;
                List<CountryModel> countryModels = new ArrayList<>();
                Log.d(TAG, "onDataChange: Getting value: " + dataSnapshot.getValue());
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    countryModel = snapshot.getValue(CountryModel.class);
                    countryModels.add(countryModel);
                }
                dataListner.onResultListNotification(countryModels);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Not Getting Anything...");
            }
        });

    }

    public void getLanguagenCode(final DatabaseReference databaseReference, String lang_country, final FirebaseDataListner dataListner) {
        mDB.orderByChild("languageCountry").equalTo(lang_country)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.i(TAG, "onDataChange: Getting value" + dataSnapshot.getValue());
                        LanguageModel languageModel = null;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            languageModel = snapshot.getValue(LanguageModel.class);
                        }
                        dataListner.onResultNotification(languageModel);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.i(TAG, "onDataCancelled: Got an error!!!");
                    }
                });
    }

    public String getReverseCode(String input){
        String[] parts = input.split("-");
        return parts[1] + "-" + parts[0];
    }

    public interface FirebaseDataListner {
        void onResultNotification(Object tClass);
        void onResultListNotification(List<?> classList);
    }
}
