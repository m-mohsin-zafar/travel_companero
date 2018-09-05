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
import android.widget.Toast;

import com.globalrescue.mzafar.pocbeta_1.adapters.LangListAdapter;
import com.globalrescue.mzafar.pocbeta_1.models.LanguageListModel;
import com.globalrescue.mzafar.pocbeta_1.R;
import com.globalrescue.mzafar.pocbeta_1.utilities.DataUtil;

import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInDownAnimator;

public class DestinationLanguageSelectActivity extends AppCompatActivity implements LangListAdapter.LanguageSelectionListener {

    private static final String TAG = "LanguageSelectActivity";

    private List<LanguageListModel> langList;

    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_language_select);
        Log.d(TAG, "onCreate: started.");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

        } else {

        }

        DataUtil dataUtil = new DataUtil(this);
        langList = (List<LanguageListModel>) dataUtil.construcListFromJson(dataUtil.getJsonLangList());
        initRecyclerView();
    }

    private void initRecyclerView() {
        Log.d(TAG, "init RecyclerView. ");
        RecyclerView recyclerView = findViewById(R.id.rv_native_langs);
        LangListAdapter langListAdapter = new LangListAdapter(langList, this, this);
        langListAdapter.setOnLanguageSelection(this);
        recyclerView.setAdapter(langListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setItemAnimator(new SlideInDownAnimator());
    }

    @Override
    public void onLanguageSelected(LanguageListModel language) {

        if(mToast != null){
            mToast.cancel();
        }

        Log.d(TAG, "onClick -> Clicked on: " + language.getmLangName());
        mToast =  Toast.makeText(this, language.getmLangName() + " Selected", Toast.LENGTH_SHORT);
        mToast.show();

        Context context = DestinationLanguageSelectActivity.this;
        Class destinationActivity = HomeActivity.class;
        Intent intent = new Intent(context, destinationActivity);
        intent.putExtra("LANGUAGE_MODEL",language);
//        try {
//            this.wait(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        startActivity(intent);
    }
}