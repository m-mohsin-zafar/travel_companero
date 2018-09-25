package com.globalrescue.mzafar.pocbeta_1.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.globalrescue.mzafar.pocbeta_1.R;
import com.globalrescue.mzafar.pocbeta_1.adapters.LangsSelectionStatePagerAdapter;
import com.globalrescue.mzafar.pocbeta_1.models.CountryModel;


public class MainActivity extends AppCompatActivity implements
        NativeLangSelectionFragment.OnFragmentInteractionListener,
        ForeignLangSelectionFragment.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";
    private ViewPager mViewPager;

    private CountryModel nativeCountry;
    private CountryModel foreignCountry;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = findViewById(R.id.main_activity_fragments_container);

        setupViewPagerAdapter(mViewPager);
    }

    private void setupViewPagerAdapter(ViewPager viewPager) {
        LangsSelectionStatePagerAdapter adapter = new LangsSelectionStatePagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new NativeLangSelectionFragment(), "NLSelectionFragment");
        adapter.addFragment(new ForeignLangSelectionFragment(), "FLSelectionFragment");
        viewPager.setAdapter(adapter);
    }

    public void setFragmentOnViewPager(int fragmentNumber){
        mViewPager.setCurrentItem(fragmentNumber);
    }

    public void startHomeActivity(){
        Context context = MainActivity.this;
        Class destinationActivity = HomeActivity.class;
        Intent intent = new Intent(context, destinationActivity);
        intent.putExtra("FOREIGN_COUNTRY_MODEL", foreignCountry);
        intent.putExtra("NATIVE_COUNTRY_MODEL", nativeCountry);
        startActivity(intent);
    }

    private String getFragmentTag(int viewPagerId, int fragmentPosition)
    {
        return "android:switcher:" + viewPagerId + ":" + fragmentPosition;
    }

    public CountryModel getNativeCountry() {
        return nativeCountry;
    }

    @Override
    public void onNLFragmentInteraction(Object model) {
        nativeCountry = (CountryModel) model;
        Log.i(TAG, "onNLFragmentInteraction: "+nativeCountry.getCountry());
    }

    @Override
    public void onFLFragmentInteraction(Object model) {
        foreignCountry = (CountryModel) model;
        Log.i(TAG, "onNLFragmentInteraction: "+foreignCountry.getCountry());
    }
}
