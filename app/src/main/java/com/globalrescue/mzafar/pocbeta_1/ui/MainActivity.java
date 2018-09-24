package com.globalrescue.mzafar.pocbeta_1.ui;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.globalrescue.mzafar.pocbeta_1.R;
import com.globalrescue.mzafar.pocbeta_1.adapters.LangsSelectionStatePagerAdapter;
import com.globalrescue.mzafar.pocbeta_1.models.CountryModel;
import com.globalrescue.mzafar.pocbeta_1.models.LanguageModel;


public class MainActivity extends AppCompatActivity implements NativeLangSelectionFragment.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";
    private LangsSelectionStatePagerAdapter mLangsSelectionStatePagerAdapter;
    private ViewPager mViewPager;

    private CountryModel nativeCountry;
    private CountryModel foreignCountry;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLangsSelectionStatePagerAdapter = new LangsSelectionStatePagerAdapter(getSupportFragmentManager());
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

    @Override
    public void onNLFragmentInteraction(Object model) {
        nativeCountry = (CountryModel) model;
        Log.i(TAG, "onNLFragmentInteraction: "+nativeCountry.getCountry());
    }
}
