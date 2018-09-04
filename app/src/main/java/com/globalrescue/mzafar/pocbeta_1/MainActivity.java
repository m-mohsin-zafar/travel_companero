package com.globalrescue.mzafar.pocbeta_1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.globalrescue.mzafar.pocbeta_1.Models.LanguageListModel;
import com.globalrescue.mzafar.pocbeta_1.Utilities.DataUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements LangListAdapter.LanguageSelection {

    private static final String TAG = "MainActivity";

    private List<LanguageListModel> langList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: started.");

        DataUtil dataUtil = new DataUtil(this);
        langList = (List<LanguageListModel>) dataUtil.construcListFromJson(dataUtil.getJsonLangList());
        initRecyclerView();
    }

    private void initRecyclerView() {
        Log.d(TAG,"init RecyclerView. ");
        RecyclerView recyclerView = findViewById(R.id.rv_native_langs);
        LangListAdapter langListAdapter = new LangListAdapter(langList, this);
        langListAdapter.setOnLanguageSelection(this);
        recyclerView.setAdapter(langListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onLanguageSelected(LanguageListModel language) {
        Intent intent = new Intent(this, MainActivity.class);
        // intent.putExtra();
        // startActivity(new Intent(this, ...));
    }
}