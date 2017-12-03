package com.nalsil.tensorflowsimapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
public class LogisticRegressionFragment extends Fragment {
    private final static String TAG = LogisticRegressionFragment.class.getSimpleName();
    private FirebaseAnalytics mFirebaseAnalytics;

    private Unbinder unbinder;

    @BindView(R.id.etInput1)EditText etInput1;
    @BindView(R.id.tvOutputHypothesis)TextView tvOutputHypothesis;
    @BindView(R.id.tvOutputCorrect)TextView tvOutputCorrect;
    @BindView(R.id.tvOutputAccuracy)TextView tvOutputAccuracy;

    @BindView(R.id.btnRun)Button btnRun;
    @BindView(R.id.tvInfo)TextView tvInfo;
    @BindView(R.id.adView)
    AdView mAdView;


    private static final String MODEL_FILE = "file:///android_asset/optimized_lab_05_1_logistic_regression.pb";
    private static final String INPUT_NODE_X = "X";
    private static final String INPUT_NODE_Y = "Y";
    private static final String OUTPUT_NODE_HYPO = "hypothesis";
    private static final String OUTPUT_NODE_PRED = "predicted";
    private static final String OUTPUT_NODE_ACCU = "accuracy";
    private static final String[] OUTPUT_NODES_HYPO = new String[] {OUTPUT_NODE_HYPO};
    private static final String[] OUTPUT_NODES_PRED = new String[] {OUTPUT_NODE_PRED};
    private static final String[] OUTPUT_NODES_ACCU = new String[] {OUTPUT_NODE_ACCU};

    private boolean logStats = false;
    private TensorFlowInferenceInterface inferenceInterface;

    public LogisticRegressionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_logistic_regression, container, false);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        unbinder = ButterKnife.bind(this, view);
        initInfo();
        initAds();
        initTensorFlow();

        return view;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();

        if (inferenceInterface != null) {
            inferenceInterface.close();
        }

    }

    @OnClick(R.id.btnRandom)
    public void btnRandom_OnClick() {
        Random generator = new Random();

        String strResult = "";
        for (int i=0; i<6; i++) {
            strResult += generator.nextInt(7);
            strResult += ",";
            strResult += generator.nextInt(4);
            if (i != 5) {
                strResult += ",";
            }
        }

        etInput1.setText(strResult  );

        tvOutputHypothesis.setText("");
        tvOutputCorrect.setText("");
        tvOutputAccuracy.setText("");
    }

    @OnClick(R.id.btnReset)
    public void btnReset_OnClick() {
        etInput1.setText("1,2,2,3,3,1,4,3,5,3,6,2");

        tvOutputHypothesis.setText("");
        tvOutputCorrect.setText("");
        tvOutputAccuracy.setText("");
    }

    @OnClick(R.id.btnRun)
    public void btnRun_OnClick() {

        String strInput = etInput1.getText().toString();
        String[] strInputs = strInput.split(",");
        if (strInputs.length == 0 || TextUtils.isEmpty(strInput)) {
            Toast.makeText(getActivity(), "Floats separated by \",\" required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hypothesis
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

        float[] results;
        try {
            results = hypothesis(inputFloatsX, 2);
            tvOutputHypothesis.setText(Arrays.toString(results));
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            results = predicted(inputFloatsX, 2);
            tvOutputCorrect.setText(Arrays.toString(results));
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        if (!strInput.equals("1,2,2,3,3,1,4,3,5,3,6,2")) {
            return;
        }

        try {
            results = accuracy(inputFloatsX,  new float[] {0,0,0,1,1,1},  2);
            tvOutputAccuracy.setText(Arrays.toString(results));
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void initTensorFlow() {
        inferenceInterface = new TensorFlowInferenceInterface(this.getActivity().getAssets(), MODEL_FILE);
    }

    private float[] hypothesis(float[] inputFloats, int nFeatures) {

        int nInstance = inputFloats.length / nFeatures;
        FloatBuffer.wrap(inputFloats);

        inferenceInterface.feed(INPUT_NODE_X, inputFloats, nInstance, nFeatures);
        inferenceInterface.run(OUTPUT_NODES_HYPO, logStats);

        float[] result = new float[nInstance];
        inferenceInterface.fetch(OUTPUT_NODE_HYPO, result);
        return result;
    }

    private float[] predicted(float[] inputFloats, int nFeatures) {

        int nInstance = inputFloats.length / nFeatures;
        FloatBuffer.wrap(inputFloats);

        inferenceInterface.feed(INPUT_NODE_X, inputFloats, nInstance, nFeatures);
        inferenceInterface.run(OUTPUT_NODES_PRED, logStats);

        float[] result = new float[nInstance];
        inferenceInterface.fetch(OUTPUT_NODE_PRED, result);
        return result;
    }

    private float[] accuracy(float[] inputFloats, float[] yFloats, int nFeatures) {

        int nInstance = inputFloats.length / nFeatures;
        FloatBuffer.wrap(inputFloats);

        inferenceInterface.feed(INPUT_NODE_X, inputFloats, nInstance, nFeatures);
        inferenceInterface.feed(INPUT_NODE_Y, yFloats, nInstance, 1);
        inferenceInterface.run(OUTPUT_NODES_ACCU, logStats);

        float[] result = new float[1];
        inferenceInterface.fetch(OUTPUT_NODE_ACCU, result);
        return result;
    }


    private void initInfo() {
        String strInfo = "The optimized model source: <br/>";
        strInfo = strInfo + "<a href='https://github.com/nalsil/DeepLearningZeroToAll/blob/master/lab-05-1-logistic_regression_model.py'>* lab-05-1-logistic_regression</a>";
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
