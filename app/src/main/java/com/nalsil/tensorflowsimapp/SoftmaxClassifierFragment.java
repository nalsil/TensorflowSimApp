package com.nalsil.tensorflowsimapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 * A simple {@link Fragment} subclass.
 */
public class SoftmaxClassifierFragment extends Fragment {

    private final static String TAG = SoftmaxClassifierFragment.class.getSimpleName();
    private FirebaseAnalytics mFirebaseAnalytics;

    private Unbinder unbinder;

    @BindView(R.id.etInput1)EditText etInput1;
    @BindView(R.id.etInput2)EditText etInput2;
    @BindView(R.id.etInput3)EditText etInput3;
    @BindView(R.id.etInput4)EditText etInput4;
    @BindView(R.id.tvOutput11)TextView tvOutput11;
    @BindView(R.id.tvOutput12)TextView tvOutput12;
    @BindView(R.id.tvOutput21)TextView tvOutput21;
    @BindView(R.id.tvOutput22)TextView tvOutput22;
    @BindView(R.id.tvOutput31)TextView tvOutput31;
    @BindView(R.id.tvOutput32)TextView tvOutput32;
    @BindView(R.id.tvOutput41)TextView tvOutput41;
    @BindView(R.id.tvOutput42)TextView tvOutput42;

    @BindView(R.id.tvInfo)TextView tvInfo;
    @BindView(R.id.adView)AdView mAdView;


    private static final String MODEL_FILE = "file:///android_asset/optimized_lab_06_1_softmax_classifier.pb";
    private static final String INPUT_NODE_X = "X";
    private static final String INPUT_NODE_Y = "Y";
    private static final String OUTPUT_NODE_HYPO = "hypothesis";
    private static final String OUTPUT_NODE_ARGMAX = "ud_argmax";
    private static final String[] OUTPUT_NODES_HYPO = new String[]{OUTPUT_NODE_HYPO};
    private static final String[] OUTPUT_NODES_ARGMAX = new String[]{OUTPUT_NODE_ARGMAX};

    private boolean logStats = false;
    private TensorFlowInferenceInterface inferenceInterface;

    public SoftmaxClassifierFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_softmax_classifier, container, false);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        unbinder = ButterKnife.bind(this, view);
        initInfo();
        initAds();
        initTensorFlow();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.btnRandom)
    public void btnRandom_OnClick() {
        Random generator = new Random();
        etInput1.setText(generator.nextInt(10) + "," + generator.nextInt(10) + "," + generator.nextInt(10)  + "," + generator.nextInt(10));
        etInput2.setText(generator.nextInt(10) + "," + generator.nextInt(10) + "," + generator.nextInt(10)  + "," + generator.nextInt(10));
        etInput3.setText(generator.nextInt(10) + "," + generator.nextInt(10) + "," + generator.nextInt(10)  + "," + generator.nextInt(10));
        etInput4.setText( etInput1.getText().toString() + "," + etInput2.getText().toString() + "," + etInput3.getText().toString() );

        tvOutput11.setText("");
        tvOutput12.setText("");
        tvOutput21.setText("");
        tvOutput22.setText("");
        tvOutput31.setText("");
        tvOutput32.setText("");
        tvOutput41.setText("");
        tvOutput42.setText("");
    }

    @OnClick(R.id.btnReset)
    public void btnReset_OnClick() {
        etInput1.setText("1, 11, 7, 9");
        etInput2.setText("1, 3, 4, 3");
        etInput3.setText("1, 1, 0, 1");
        etInput4.setText("1, 11, 7, 9, 1, 3, 4, 3, 1, 1, 0, 1");

        tvOutput11.setText("");
        tvOutput12.setText("");
        tvOutput21.setText("");
        tvOutput22.setText("");
        tvOutput31.setText("");
        tvOutput32.setText("");
        tvOutput41.setText("");
        tvOutput42.setText("");
    }

    @OnClick(R.id.btnRun)
    public void btnRun_OnClick() {

        String strInput = etInput1.getText().toString();
        calcHypothesisArgMax(strInput, 4, 3, tvOutput11, tvOutput12);

        strInput = etInput2.getText().toString();
        calcHypothesisArgMax(strInput, 4, 3, tvOutput21, tvOutput22);

        strInput = etInput3.getText().toString();
        calcHypothesisArgMax(strInput, 4, 3, tvOutput31, tvOutput32);

        strInput = etInput4.getText().toString();
        calcHypothesisArgMax(strInput, 4, 3, tvOutput41, tvOutput42);
    }

    private void initTensorFlow() {
        inferenceInterface = new TensorFlowInferenceInterface(this.getActivity().getAssets(), MODEL_FILE);
    }

    private float[] hypothesis(float[] inputFloats, int nFeatures, int nClasses) {

        int nInstance = inputFloats.length / nFeatures;
        FloatBuffer.wrap(inputFloats);

        inferenceInterface.feed(INPUT_NODE_X, inputFloats, nInstance, nFeatures);
        inferenceInterface.run(OUTPUT_NODES_HYPO, logStats);

        float[] result = new float[nInstance * nClasses];
        inferenceInterface.fetch(OUTPUT_NODE_HYPO, result);
        return result;
    }

    private long[] argMax(float[] inputFloats, int nClasses) {

        int nInstance = inputFloats.length / nClasses;
        FloatBuffer.wrap(inputFloats);

        inferenceInterface.feed(INPUT_NODE_Y, inputFloats, nInstance, nClasses);
        inferenceInterface.run(OUTPUT_NODES_ARGMAX, logStats);

        long[] nResult = new long[nInstance * 1];
        inferenceInterface.fetch(OUTPUT_NODE_ARGMAX, nResult);
        return nResult;
    }

    private void calcHypothesisArgMax(String strInput, int nFeatures, int nClasses, TextView tvOutputHypo, TextView tvOutputArgMax ) {

        String[] strInputs = strInput.split(",");
        if (strInputs.length == 0 || TextUtils.isEmpty(strInput)) {
            Toast.makeText(getActivity(), "Floats separated by \",\" required", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1 - instance
        int idx = 0;
        float[] inputFloatsX = new float[strInputs.length];
        try {
            for (String strItem : strInputs) {
                inputFloatsX[idx++] = Float.parseFloat(strItem);
            }
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        float[] results = {};
        try {
            results = hypothesis(inputFloatsX, nFeatures, nClasses);
            tvOutputHypo.setText(Arrays.toString(results));
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        long[] nResultOfArgMax = {};
        try {
            nResultOfArgMax = argMax(results, nClasses);
            tvOutputArgMax.setText(Arrays.toString(nResultOfArgMax));
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void initInfo() {
        String strInfo = "The optimized model source: <br/>";
        strInfo = strInfo + "<a href='https://github.com/nalsil/DeepLearningZeroToAll/blob/master/lab-06-1-softmax_classifier_model.py'>* lab-06-1-softmax_classifier</a>";
        tvInfo.setText(Html.fromHtml(strInfo));
        tvInfo.setMovementMethod(LinkMovementMethod.getInstance());
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