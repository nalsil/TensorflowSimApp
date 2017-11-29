package com.nalsil.tensorflowsimapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * A simple {@link Fragment} subclass.
 */
public class TOCFragment extends Fragment {

    private FirebaseAnalytics mFirebaseAnalytics;
    private Unbinder unbinder;

    @BindView(R.id.tvDesc)TextView tvDesc;
    @BindView(R.id.tvInfo1)TextView tvInfo1;
    @BindView(R.id.adView) AdView mAdView;

    public TOCFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_toc, container, false);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        unbinder = ButterKnife.bind(this, view);
        initInfo();
        initAds();

        return view;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void initInfo() {

        String strDesc = getString(R.string.about_description_text);
        tvDesc.setText(Html.fromHtml(strDesc));
        tvDesc.setMovementMethod(LinkMovementMethod.getInstance());


        String strInfo1 = "";
        strInfo1 += "<h3>Deep Learning Zero to All</h3>";
        strInfo1 += "<ul>";
        strInfo1 += "<li><a href=\"fragment://" + LinearRegressionFragment.class.getSimpleName() + "\"> Linear Regression </a>, using *.pb created by <a href=\"https://github.com/nalsil/DeepLearningZeroToAll/blob/master/lab-02-3-linear_regression_tensorflow.org_model.py\">this source</a> </li>";
        strInfo1 += "<li><a href=\"fragment://" + MinimizingCostGradientUpdateFragment.class.getSimpleName() + "\"> Minimizing Cost Gradient Update </a>, using *.pb created by <a href=\"https://github.com/nalsil/DeepLearningZeroToAll/blob/master/lab-03-2-minimizing_cost_gradient_update_model.py\">this source</a> </li> ";
        strInfo1 += "<li><a href=\"fragment://" + MultiVariableMatmulLinearRegressionFragment.class.getSimpleName() + "\"> Multi-Variable Matmul Linear Regression </a>, using *.pb created by <a href=\"https://github.com/nalsil/DeepLearningZeroToAll/blob/master/lab-04-2-multi_variable_matmul_linear_regression_model.py\">this source</a> </li> ";

        strInfo1 += "</ul>";

        tvInfo1.setText(Html.fromHtml(strInfo1));
        tvInfo1.setMovementMethod(CustomLinkMovementMethod.getInstance(getContext()));

    }

    private void initAds() {

        AdRequest adRequest;
        if (BuildConfig.DEBUG) {
            adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice(MainActivity.getDeviceId())
                    .build();
        } else {
            adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();
        }
        mAdView.loadAd(adRequest);
    }



}
