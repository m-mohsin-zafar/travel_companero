package com.globalrescue.mzafar.pocbeta_1.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
 * {@link NativeLangSelectionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NativeLangSelectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NativeLangSelectionFragment extends Fragment implements DataUtil.FirebaseDataListner, LangListAdapter.CountrySelectionListener, View.OnClickListener {
//Can add this if needed

    private static final String TAG = "NLFragment";

    private List<CountryModel> countryList;
    private CountryModel selectedNativeCountry;

    private RecyclerView recyclerView;
    private Toast mToast;
    private FloatingActionButton mNextBtn;
    private ProgressBar mLoading;

    private Context mContext;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public NativeLangSelectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NativeLangSelectionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NativeLangSelectionFragment newInstance(String param1, String param2) {
        NativeLangSelectionFragment fragment = new NativeLangSelectionFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_native_lang_selection, container, false);

        mLoading = view.findViewById(R.id.pg_native_country_list);
        mNextBtn = view.findViewById(R.id.btn_native_to_foreign_fragment);
        recyclerView = view.findViewById(R.id.rv_native_country);

        mLoading.setVisibility(View.VISIBLE);
        mNextBtn.setOnClickListener(this);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        DataUtil dataUtil = new DataUtil(this);
        dataUtil.getListOfCountries(dataUtil.getFirebaseDBRefernce("countries"));
    }

    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

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
        Log.d(TAG, "init RecyclerView. ");
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
        Log.d(TAG, "onResultListNotification: Getting Results from Firebase");
        countryList = (List<CountryModel>) classList;
        mLoading.setVisibility(View.INVISIBLE);
        initRecyclerView();
        recyclerView.setVisibility(View.VISIBLE);
    }


    @Override
    public void onCountrySelected(CountryModel countryModel) {

        selectedNativeCountry = countryModel;

        if (mToast != null) {
            mToast.cancel();
        }

        Log.d(TAG, "onClick -> Clicked on: " + selectedNativeCountry.getCountry());
        mToast = Toast.makeText(mContext, selectedNativeCountry.getCountry() + " Selected", Toast.LENGTH_SHORT);
        mToast.show();


    }

    @Override
    public void onClick(View v) {
        if (v == mNextBtn) {
            if (selectedNativeCountry != null) {

//                Context context = DestinationLanguageSelectActivity.this;
//                Class destinationActivity = HomeActivity.class;
//                Intent intent = new Intent(context, destinationActivity);
//                intent.putExtra("COUNTRY_MODEL", country);
//                startActivity(intent);
                if (mListener != null) {
                    mListener.onNLFragmentInteraction(selectedNativeCountry);
                }

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
        void onNLFragmentInteraction(Object model);
    }
}
