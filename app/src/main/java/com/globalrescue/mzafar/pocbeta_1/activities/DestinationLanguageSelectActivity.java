package com.globalrescue.mzafar.pocbeta_1.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.globalrescue.mzafar.pocbeta_1.adapters.LangListAdapter;
import com.globalrescue.mzafar.pocbeta_1.models.CountryModel;
import com.globalrescue.mzafar.pocbeta_1.models.LanguageListModel;
import com.globalrescue.mzafar.pocbeta_1.R;
import com.globalrescue.mzafar.pocbeta_1.utilities.DataUtil;

import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInDownAnimator;

public class DestinationLanguageSelectActivity extends AppCompatActivity implements LangListAdapter.CountrySelectionListener, DataUtil.FirebaseDataListner {

    private static final String TAG = "LanguageSelectActivity";

    ProgressBar mLoading;

    private List<LanguageListModel> langList;
    private List<CountryModel> countryList;
    private RecyclerView recyclerView;

    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_language_select);

        mLoading = findViewById(R.id.pg_main_activity_loading);
        mLoading.setVisibility(View.VISIBLE);

        //Setting Firebase Persistence Storage Enabled to store data locally and available for offline use
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Log.d(TAG, "onCreate: started.");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

        } else {

        }

        DataUtil dataUtil = new DataUtil(this, this);
//        dataUtil.getLanguagenCode("Saudi Arabia");
        dataUtil.getListOfCountries(dataUtil.getFirebaseDBRefernce("countries"));
        langList = (List<LanguageListModel>) dataUtil.construcListFromJson(dataUtil.getJsonLangList());
    }

    private void initRecyclerView() {
        Log.d(TAG, "init RecyclerView. ");
        recyclerView = findViewById(R.id.rv_native_langs);
        LangListAdapter langListAdapter = new LangListAdapter(countryList, this, this);
        langListAdapter.setOnCountrySelection(this);
        recyclerView.setAdapter(langListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setItemAnimator(new SlideInDownAnimator());
    }

    @Override
    public void onCountrySelected(CountryModel country) {

        if(mToast != null){
            mToast.cancel();
        }

        Log.d(TAG, "onClick -> Clicked on: " + country.getCountry());
        mToast =  Toast.makeText(this, country.getCountry() + " Selected", Toast.LENGTH_SHORT);
        mToast.show();

        Context context = DestinationLanguageSelectActivity.this;
        Class destinationActivity = HomeActivity.class;
        Intent intent = new Intent(context, destinationActivity);
        intent.putExtra("COUNTRY_MODEL",country);
//        try {
//            this.wait(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        mToast.cancel();
        startActivity(intent);
    }

    @Override
    public void onResultNotification(Object tClass) {

    }

    @Override
    public void onResultListNotification(List<?> classList) {
        Log.d(TAG, "onResultListNotification: Getting Results from Firebase");
        countryList = (List<CountryModel>) classList;
        mLoading.setVisibility(View.INVISIBLE);
        initRecyclerView();
        recyclerView.setVisibility(View.VISIBLE);

    }

}