package com.nalsil.tensorflowsimapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 * A simple {@link Fragment} subclass.
 */
public class LinearRegressionFragment extends Fragment {

    private final static String TAG = LinearRegressionFragment.class.getSimpleName();
    private FirebaseAnalytics mFirebaseAnalytics;

    private Unbinder unbinder;

    @BindView(R.id.etInput)EditText etInput;
    @BindView(R.id.btnRun)Button btnRun;
    @BindView(R.id.tvOutput)TextView tvOutput;
    @BindView(R.id.webview)WebView webview;
    @BindView(R.id.tvInfo)TextView tvInfo;
    @BindView(R.id.adView) AdView mAdView;


    private static final String MODEL_FILE = "file:///android_asset/optimized_lab_02_3_linear_regression.pb";
    private static final String INPUT_NODE = "x";
    private static final String OUTPUT_NODE = "hypothesis";

    private String[] OUTPUT_NODES;
    private boolean logStats = false;
    private TensorFlowInferenceInterface inferenceInterface;

    public LinearRegressionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_linear_regression, container, false);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        unbinder = ButterKnife.bind(this, view);
        initInfo();
        initGraph();
        initAds();
        initTensorFlow();

        return view;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.btnRun)
    public void btnRun_OnClick() {

        String strInput = etInput.getText().toString();
        String[] strInputs = strInput.split(",");
        if (strInputs.length == 0 ) {
            Toast.makeText(getActivity(), "Floats separated by \",\" required", Toast.LENGTH_SHORT).show();
            return;
        }

        int idx = 0;
        float[] inputFloats = new float[strInputs.length];
        for(String strItem : strInputs) {
            inputFloats[idx++] = Float.parseFloat(strItem);
        }

        float[] results;
        try {
            results = hypothesis(inputFloats);
            tvOutput.setText(Arrays.toString(results));
            loadGraph(inputFloats, results);
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }

    private void initTensorFlow() {
        inferenceInterface = new TensorFlowInferenceInterface(this.getActivity().getAssets(), MODEL_FILE);
        OUTPUT_NODES = new String[] {OUTPUT_NODE};
    }

    private float[] hypothesis(float[] inputFloats) {
        inferenceInterface.feed(INPUT_NODE, inputFloats, inputFloats.length, 1);
        inferenceInterface.run(OUTPUT_NODES, logStats);

        float[] result = new float[inputFloats.length];
        inferenceInterface.fetch(OUTPUT_NODE, result);
        return result;
    }

    private void initGraph() {

        // Prepare webview: add zoom controls and start zoomed out
        final WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);
        webSettings.setUseWideViewPort(true);
        webview.setWebChromeClient(new WebChromeClient());
        webview.setInitialScale(1);

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                String strUrl = "javascript:initGraph()";
                webview.loadUrl(strUrl);
            }
        });

        // Load base html from the assets directory
        webview.loadUrl("file:///android_asset/html/lab_02_3_linear_regression_graph.html");
    }

    private void loadGraph(final float[] inputFloats, final float[] results) {
        String strJsonObj = buildData(inputFloats, results);
        String strUrl = "javascript:initGraph(" + strJsonObj + ")";
        webview.loadUrl(strUrl);
    }

    private String buildData(float[] inputFloats, float[] results) {
        String strJsonObj = "";

        JSONObject jsonObj = new JSONObject();
        JSONArray arrData = new JSONArray();

        JSONArray arrX1 = new JSONArray();
        JSONArray arrX2 = new JSONArray();
        JSONArray arrData1 = new JSONArray();
        JSONArray arrData2 = new JSONArray();

        try {
            arrX1.put("x1");
            arrX1.put(1);
            arrX1.put(2);
            arrX1.put(3);
            arrX1.put(4);
            arrData.put(arrX1);

            arrData1.put("data1");
            arrData1.put(0);
            arrData1.put(-1);
            arrData1.put(-2);
            arrData1.put(-3);
            arrData.put(arrData1);

            arrX2.put("x2");
            for(float item : inputFloats) {
                arrX2.put(item);
            }
            arrData.put(arrX2);

            arrData2.put("data2");
            for(float item : results) {
                arrData2.put(item);
            }
            arrData.put(arrData2);

            jsonObj.put("columns", arrData);
            strJsonObj = jsonObj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText( getActivity(),"Error: " + e.getMessage(),Toast.LENGTH_LONG).show();
        }

        Log.d(TAG, "strJsonObj=" + strJsonObj);

        return strJsonObj;
    }

    private void initInfo() {
        String strInfo = "The optimized model source: <br/>";
        strInfo = strInfo + "<a href='https://github.com/nalsil/DeepLearningZeroToAll/blob/master/lab-02-3-linear_regression_tensorflow.org_model.py'>* lab-02-3-linear_regression_tensorflow.org</a>";
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
