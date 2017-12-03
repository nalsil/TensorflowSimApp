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
public class LearningRateAndEvaluationFragment extends Fragment {


    private final static String TAG = LearningRateAndEvaluationFragment.class.getSimpleName();
    private FirebaseAnalytics mFirebaseAnalytics;

    private Unbinder unbinder;

    @BindView(R.id.etInput1)EditText etInput1;
    @BindView(R.id.tvOutput1)TextView tvOutput1;
    @BindView(R.id.tvOutput2)TextView tvOutput2;
    @BindView(R.id.tvOutput3)TextView tvOutput3;

    @BindView(R.id.tvInfo)TextView tvInfo;
    @BindView(R.id.adView)AdView mAdView;


    private static final String MODEL_FILE = "file:///android_asset/optimized_lab_07_1_learning_rate_and_evaluation.pb";
    private static final String INPUT_NODE_X = "X";
    private static final String INPUT_NODE_Y = "Y";
    private static final String OUTPUT_NODE_HYPO = "hypothesis";
    private static final String OUTPUT_NODE_PRED = "prediction";
    private static final String OUTPUT_NODE_ACCU = "accuracy";
    private static final String[] OUTPUT_NODES_HYPO = new String[]{OUTPUT_NODE_HYPO};
    private static final String[] OUTPUT_NODES_PRED = new String[]{OUTPUT_NODE_PRED};
    private static final String[] OUTPUT_NODES_ACCU = new String[]{OUTPUT_NODE_ACCU};

    private boolean logStats = false;
    private TensorFlowInferenceInterface inferenceInterface;

    public LearningRateAndEvaluationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_learning_rate_and_evaluation, container, false);
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

        if (inferenceInterface != null) {
            inferenceInterface.close();
        }

    }

    @OnClick(R.id.btnRandom)
    public void btnRandom_OnClick() {
        Random generator = new Random();
        etInput1.setText(generator.nextInt(8) + "," + generator.nextInt(8) + "," + generator.nextInt(8)
                + "," + generator.nextInt(8) + "," + generator.nextInt(8) + "," + generator.nextInt(8)
                + "," + generator.nextInt(8) + "," + generator.nextInt(8) + "," + generator.nextInt(8)
        );

        tvOutput1.setText("");
        tvOutput2.setText("");
        tvOutput3.setText("");
    }

    @OnClick(R.id.btnReset)
    public void btnReset_OnClick() {
        etInput1.setText("2,1,1,3,1,2,3,3,4");

        tvOutput1.setText("");
        tvOutput2.setText("");
        tvOutput3.setText("");
    }

    @OnClick(R.id.btnRun)
    public void btnRun_OnClick() {

        String strInput = etInput1.getText().toString();
        String[] strInputs = strInput.split(",");
        if (strInputs.length == 0 || TextUtils.isEmpty(strInput)) {
            Toast.makeText(getActivity(), "Floats separated by \",\" required", Toast.LENGTH_SHORT).show();
            return;
        }

        int nFeatures = 3;
        int nClasses = 3;

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
            tvOutput1.setText(Arrays.toString(results));
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        long[] resultsOfPredition = {};
        try {
            resultsOfPredition = prediction(inputFloatsX, nFeatures);
            tvOutput2.setText(Arrays.toString(resultsOfPredition));
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }


        if (!strInput.equals("2,1,1,3,1,2,3,3,4")) {
            return;
        }

        float[] inputFloatsY = new float[] {0, 0, 1, 0, 0, 1, 0, 0, 1};
        float[] resultsOfAccuracy = {};
        try {
            resultsOfAccuracy = accuracy(inputFloatsX, inputFloatsY, nFeatures, nClasses);
            tvOutput3.setText(Arrays.toString(resultsOfAccuracy));
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

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

    private long[] prediction(float[] inputFloats, int nFeatures) {

        int nInstance = inputFloats.length / nFeatures;
        FloatBuffer.wrap(inputFloats);

        inferenceInterface.feed(INPUT_NODE_X, inputFloats, nInstance, nFeatures);
        inferenceInterface.run(OUTPUT_NODES_PRED, logStats);

        long[] nResult = new long[nInstance * 1];
        inferenceInterface.fetch(OUTPUT_NODE_PRED, nResult);
        return nResult;
    }

    private float[] accuracy(float[] inputFloatsX, float[] inputFloatsY, int nFeatures, int nClasses) {

        int nInstance = inputFloatsX.length / nFeatures;
        FloatBuffer.wrap(inputFloatsX);

        inferenceInterface.feed(INPUT_NODE_X, inputFloatsX, nInstance, nFeatures);
        inferenceInterface.feed(INPUT_NODE_Y, inputFloatsY, nInstance, nClasses);
        inferenceInterface.run(OUTPUT_NODES_ACCU, logStats);

        float[] result = new float[1];
        inferenceInterface.fetch(OUTPUT_NODE_ACCU, result);
        return result;
    }

    private void initInfo() {
        String strInfo = "The optimized model source: <br/>";
        strInfo = strInfo + "<a href='https://github.com/nalsil/DeepLearningZeroToAll/blob/master/lab-07-1-learning_rate_and_evaluation_model.py'>* lab-07-1-learning_rate_and_evaluation</a>";
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