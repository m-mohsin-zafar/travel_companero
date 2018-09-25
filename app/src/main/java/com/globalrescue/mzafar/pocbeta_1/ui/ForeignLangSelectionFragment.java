package com.globalrescue.mzafar.pocbeta_1.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.globalrescue.mzafar.pocbeta_1.R;
import com.globalrescue.mzafar.pocbeta_1.adapters.LangListAdapter;
import com.globalrescue.mzafar.pocbeta_1.models.CountryModel;
import com.globalrescue.mzafar.pocbeta_1.utilities.DataUtil;

import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInDownAnimator;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ForeignLangSelectionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ForeignLangSelectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ForeignLangSelectionFragment extends Fragment implements DataUtil.FirebaseDataListner, LangListAdapter.CountrySelectionListener, View.OnClickListener {

    private static final String TAG = "FLFragment";

    private List<CountryModel> countryList;
    private CountryModel selectedForeignCountry;

    private RecyclerView recyclerView;
    private Toast mToast;
    private FloatingActionButton mGotoHomeActivityBtn;
    private ProgressBar mLoading;

    private Context mContext;

    private CountryModel nativeCountryModel;

    private OnFragmentInteractionListener mListener;

    public ForeignLangSelectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment ForeignLangSelectionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ForeignLangSelectionFragment newInstance(CountryModel model) {
        ForeignLangSelectionFragment fragment = new ForeignLangSelectionFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_foreign_lang_selection, container, false);

        mLoading = view.findViewById(R.id.pg_foreign_country_list);
        mGotoHomeActivityBtn = view.findViewById(R.id.btn_goto_home_activity);
        recyclerView = view.findViewById(R.id.rv_foreign_country);

        mLoading.setVisibility(View.VISIBLE);
        mGotoHomeActivityBtn.setOnClickListener(this);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        DataUtil dataUtil = new DataUtil(this);
//        dataUtil.getListOfCountries(dataUtil.getFirebaseDBRefernce("countries"));
        dataUtil.getListOfCountriesFirestore();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void initRecyclerView() {
        Log.i(TAG, "init RecyclerView. ");
        LangListAdapter langListAdapter = new LangListAdapter(countryList, mContext, this);
        langListAdapter.setOnCountrySelection(this);
        recyclerView.setAdapter(langListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setItemAnimator(new SlideInDownAnimator());
    }

    //DataUtil Interface Implementation
    @Override
    public void onResultNotification(Object tClass) {

    }

    @Override
    public void onResultListNotification(List<?> classList) {
        Log.i(TAG, "onResultListNotification: Getting Results from Firebase");
        countryList = (List<CountryModel>) classList;
        mLoading.setVisibility(View.INVISIBLE);
        initRecyclerView();
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCountrySelected(CountryModel countryModel) {

        nativeCountryModel = ((MainActivity)getActivity()).getNativeCountry();

        if (mToast != null) {
            mToast.cancel();
        }
        if (!nativeCountryModel.getCountry().equals(countryModel.getCountry())){
            selectedForeignCountry = countryModel;
            Log.i(TAG, "onClick -> Clicked on: " + selectedForeignCountry.getCountry());
            mToast = Toast.makeText(mContext, selectedForeignCountry.getCountry() + " Selected", Toast.LENGTH_SHORT);
            mToast.show();
        } else {
            selectedForeignCountry = null;
            mToast = Toast.makeText(mContext, "Choose a different country", Toast.LENGTH_SHORT);
            mToast.show();
        }

    }

    @Override
    public void onClick(View v) {
        if (v == mGotoHomeActivityBtn) {
            if (selectedForeignCountry != null) {

                if (mListener != null) {
                    mListener.onFLFragmentInteraction(selectedForeignCountry);
                }

                ((MainActivity) getActivity()).startHomeActivity();

            } else {
                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(mContext, "Please Select any Country and Press Again!", Toast.LENGTH_SHORT);
                mToast.show();
            }
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFLFragmentInteraction(Object model);
    }
}
