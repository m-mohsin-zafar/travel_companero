package com.globalrescue.mzafar.pocbeta_1.utilities;

import android.support.annotation.NonNull;
import android.util.Log;

import com.globalrescue.mzafar.pocbeta_1.models.CountryModel;
import com.globalrescue.mzafar.pocbeta_1.models.LanguageModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class DataUtil {

    private static final String TAG = "DataUtil";

    //Access FireBase DB
    private DatabaseReference mDB;
    private static final String fRootPrefix = "PoC_travel_db/";

    private FirebaseDataListner dataListner;

    public DataUtil(FirebaseDataListner dataListner) {
        this.dataListner = dataListner;
    }


    public DataUtil() {
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
