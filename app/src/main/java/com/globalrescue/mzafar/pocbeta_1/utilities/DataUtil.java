package com.globalrescue.mzafar.pocbeta_1.utilities;

import android.support.annotation.NonNull;
import android.util.Log;

import com.globalrescue.mzafar.pocbeta_1.models.CountryModel;
import com.globalrescue.mzafar.pocbeta_1.models.LanguageModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class DataUtil {

    private static final String TAG = "DataUtil";

    //Access FireStore
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private static final String COUNTRIES_COLLECTION_PATH = "countries";
    private static final String LANGUAGES_COLLECTION_PATH = "languages";

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
            mDB = FirebaseDatabase.getInstance().getReference(fRootPrefix + refernceFor);
        } else {
            mDB = FirebaseDatabase.getInstance().getReference(fRootPrefix + refernceFor);
        }
        return mDB;
    }

    //Access Via Firebase Realtime Database
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

    public void getListOfCountriesFirestore() {

        mFirestore.collection(COUNTRIES_COLLECTION_PATH)
                .orderBy("country")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        CountryModel countryModel = null;
                        List<CountryModel> countryModels = new ArrayList<>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.i(TAG, "onComplete: " + document.getData());
                                countryModel = document.toObject(CountryModel.class);
                                countryModels.add(countryModel);
                            }
                            dataListner.onResultListNotification(countryModels);
                        } else {
                            Log.i(TAG, "Error getting documents.", task.getException());
                        }

                    }
                });
    }

    public void getLanguagenCodeFirestore(String lang_country, final FirebaseDataListner dataListner) {

        //Reference to the Collection
        CollectionReference languagesRef = mFirestore.collection(LANGUAGES_COLLECTION_PATH);

        //Query for collection
        Query query = languagesRef.whereEqualTo("languageCountry", lang_country);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.i(TAG, "onComplete: Getting Language Model");
                LanguageModel languageModel = null;
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        languageModel = document.toObject(LanguageModel.class);
                    }
                    dataListner.onResultNotification(languageModel);
                } else {
                    Log.i(TAG, "Error getting documents.", task.getException());
                }
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

    public String getReverseCode(String input) {
        String[] parts = input.split("-");
        return parts[1] + "-" + parts[0];
    }

    public interface FirebaseDataListner {
        void onResultNotification(Object tClass);

        void onResultListNotification(List<?> classList);
    }
}
