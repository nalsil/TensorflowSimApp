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
public class MultiVariableMatmulLinearRegressionFragment extends Fragment {

    private final static String TAG = MultiVariableMatmulLinearRegressionFragment.class.getSimpleName();
    private FirebaseAnalytics mFirebaseAnalytics;

    private Unbinder unbinder;

    @BindView(R.id.etInput1)EditText etInput1;
    @BindView(R.id.etInput2)EditText etInput2;
    @BindView(R.id.tvOutput1)TextView tvOutput1;
    @BindView(R.id.tvOutput2)TextView tvOutput2;

    @BindView(R.id.btnRun)Button btnRun;
    @BindView(R.id.tvInfo)TextView tvInfo;
    @BindView(R.id.adView)
    AdView mAdView;


    private static final String MODEL_FILE = "file:///android_asset/optimized_lab_04_2_multi_variable_matmul_linear_regression.pb";
    private static final String INPUT_NODE_X = "X";
    private static final String OUTPUT_NODE_HYPO = "hypothesis";


    private String[] OUTPUT_NODES_HYPO;
    private boolean logStats = false;
    private TensorFlowInferenceInterface inferenceInterface;

    public MultiVariableMatmulLinearRegressionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_multi_variable_matmul_linear_regression, container, false);
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
        etInput1.setText(generator.nextInt(100) + "," + generator.nextInt(100) + "," + generator.nextInt(100)   );
        etInput2.setText(generator.nextInt(100) + "," + generator.nextInt(100) + "," + generator.nextInt(100)
                + "," + generator.nextInt(100) + "," + generator.nextInt(100) + "," + generator.nextInt(100)   );

        tvOutput1.setText("");
        tvOutput2.setText("");

    }

    @OnClick(R.id.btnReset)
    public void btnReset_OnClick() {
        etInput1.setText("100, 70, 101");
        etInput2.setText("60, 70, 110, 90, 100, 80");

        tvOutput1.setText("");
        tvOutput2.setText("");
    }

    @OnClick(R.id.btnRun)
    public void btnRun_OnClick() {

        String strInput = etInput1.getText().toString();
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

        float[] results;
        try {
            results = hypothesis(inputFloatsX);
            tvOutput1.setText(Arrays.toString(results));
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }


        // ==== multi-Instance ====
        strInput = etInput2.getText().toString();
        strInputs = strInput.split(",");
        if (strInputs.length == 0 || TextUtils.isEmpty(strInput)) {
            Toast.makeText(getActivity(), "Floats separated by \",\" required", Toast.LENGTH_SHORT).show();
            return;
        }

        idx = 0;
        inputFloatsX = new float[strInputs.length];
        try {
            for (String strItem : strInputs) {
                inputFloatsX[idx++] = Float.parseFloat(strItem);
            }
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            results = hypothesis(inputFloatsX);
            tvOutput2.setText(Arrays.toString(results));
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initTensorFlow() {
        inferenceInterface = new TensorFlowInferenceInterface(this.getActivity().getAssets(), MODEL_FILE);
        OUTPUT_NODES_HYPO = new String[] {OUTPUT_NODE_HYPO};
    }

    private float[] hypothesis(float[] inputFloats) {

        int nInstance = inputFloats.length / 3;
        FloatBuffer.wrap(inputFloats);

        inferenceInterface.feed(INPUT_NODE_X, inputFloats, nInstance, 3);
        inferenceInterface.run(OUTPUT_NODES_HYPO, logStats);

        float[] result = new float[nInstance];
        inferenceInterface.fetch(OUTPUT_NODE_HYPO, result);
        return result;
    }

    private void initInfo() {
        String strInfo = "The optimized model source: <br/>";
        strInfo = strInfo + "<a href='https://github.com/nalsil/DeepLearningZeroToAll/blob/master/lab-04-2-multi_variable_matmul_linear_regression_model.py'>* lab-04-2-multi_variable_matmul_linear_regression</a>";
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
