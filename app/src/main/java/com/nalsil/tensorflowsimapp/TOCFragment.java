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
        strInfo1 += "<li><a href=\"fragment://" + LinearRegressionFragment.class.getSimpleName() + "\"> Linear Regression </a>, using *.pb created by <a href=\"https://github.com/nalsil/DeepLearningZeroToAll/blob/master/lab-02-3-linear_regression_tensorflow.org_model.py\">this source</a> </li>" ;
        strInfo1 += "<li><a href=\"fragment://" + MinimizingCostGradientUpdateFragment.class.getSimpleName() + "\"> Minimizing Cost Gradient Update </a>, using *.pb created by <a href=\"https://github.com/nalsil/DeepLearningZeroToAll/blob/master/lab-03-2-minimizing_cost_gradient_update_model.py\">this source</a> </li> ";
        strInfo1 += "<li><a href=\"fragment://" + MultiVariableMatmulLinearRegressionFragment.class.getSimpleName() + "\"> Multi-Variable Matmul Linear Regression </a>, using *.pb created by <a href=\"https://github.com/nalsil/DeepLearningZeroToAll/blob/master/lab-04-2-multi_variable_matmul_linear_regression_model.py\">this source</a> </li> ";
        strInfo1 += "<li><a href=\"fragment://" + LogisticRegressionFragment.class.getSimpleName() + "\"> Logistic Regression </a>, using *.pb created by <a href=\"https://github.com/nalsil/DeepLearningZeroToAll/blob/master/lab-05-1-logistic_regression_model.py\">this source</a> </li> ";
        strInfo1 += "<li><a href=\"fragment://" + SoftmaxClassifierFragment.class.getSimpleName() + "\"> Softmax Classifier </a>, using *.pb created by <a href=\"https://github.com/nalsil/DeepLearningZeroToAll/blob/master/lab-06-1-softmax_classifier_model.py\">this source</a> </li> ";
        strInfo1 += "<li><a href=\"fragment://" + LearningRateAndEvaluationFragment.class.getSimpleName() + "\"> Learning Rate and Evaluation</a>, using *.pb created by <a href=\"https://github.com/nalsil/DeepLearningZeroToAll/blob/master/lab-07-1-learning_rate_and_evaluation_model.py\">this source</a> </li> ";
        strInfo1 += "<li><a href=\"fragment://" + "nav_MNISTIntroduction" + "\"> MNIST Introduction</a>, using *.pb created by <a href=\"https://github.com/nalsil/DeepLearningZeroToAll/blob/master/lab-07-4-mnist_introduction_model.py\">this source</a> </li> ";
        strInfo1 += "<li><a href=\"fragment://" + XorNNWideDeepFragment.class.getSimpleName() + "\"> XOR-NN-Wide-Deep</a>, using *.pb created by <a href=\"https://github.com/nalsil/DeepLearningZeroToAll/blob/master/lab-09-3-xor-nn-wide-deep_model.py\">this source</a> </li> ";
        strInfo1 += "<li><a href=\"fragment://" + "nav_MNISTSoftmax" + "\"> MNIST Softmax</a>, using *.pb created by <a href=\"https://github.com/nalsil/DeepLearningZeroToAll/blob/master/lab-10-1-mnist_softmax_model.py\">this source</a> </li> ";

        strInfo1 += "<li><a href=\"fragment://" + "nav_MNISTNN" + "\"> MNIST NN</a>, using *.pb created by <a href=\"https://github.com/nalsil/DeepLearningZeroToAll/blob/master/lab-10-2-mnist_nn_model.py\">this source</a> </li> ";
        strInfo1 += "<li><a href=\"fragment://" + "nav_MNISTNNXavier" + "\"> MNIST NN Xavier</a>, using *.pb created by <a href=\"https://github.com/nalsil/DeepLearningZeroToAll/blob/master/lab-10-3-mnist_nn_xavier_model.py\">this source</a> </li> ";
        strInfo1 += "<li><a href=\"fragment://" + "nav_MNISTNNDeep" + "\"> MNIST NN Deep</a>, using *.pb created by <a href=\"https://github.com/nalsil/DeepLearningZeroToAll/blob/master/lab-10-4-mnist_nn_deep_model.py\">this source</a> </li> ";
        strInfo1 += "<li><a href=\"fragment://" + "nav_MNISTNNDropout" + "\"> MNIST NN Dropout</a>, using *.pb created by <a href=\"https://github.com/nalsil/DeepLearningZeroToAll/blob/master/lab-10-5-mnist_nn_dropout_model.py\">this source</a> </li> ";
        strInfo1 += "<li><a href=\"fragment://" + "nav_MNISTCNN" + "\"> MNIST CNN</a>, using *.pb created by <a href=\"https://github.com/nalsil/DeepLearningZeroToAll/blob/master/lab-11-1-mnist_cnn_model.py\">this source</a> </li> ";
        strInfo1 += "<li><a href=\"fragment://" + "nav_MNISTDEEPCNN" + "\"> MNIST Deep CNN</a>, using *.pb created by <a href=\"https://github.com/nalsil/DeepLearningZeroToAll/blob/master/lab-11-2-mnist_deep_cnn_model.py\">this source</a> </li> ";

        strInfo1 += "</ul>";
        strInfo1 += "<br/><br/>";

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
