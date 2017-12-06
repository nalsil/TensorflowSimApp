package com.nalsil.tensorflowsimapp;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.nalsil.tensorflowsimapp.view.DrawModel;
import com.nalsil.tensorflowsimapp.view.DrawView;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 * A simple {@link Fragment} subclass.
 */
public class MnistIntroductionFragment extends Fragment  {

    private final static String TAG = MnistIntroductionFragment.class.getSimpleName();
    private FirebaseAnalytics mFirebaseAnalytics;

    private Unbinder unbinder;

    @BindView(R.id.tvOutput1)TextView tvOutput1;
    @BindView(R.id.tvOutput2)TextView tvOutput2;
    @BindView(R.id.spFailed)Spinner spFailed;
    @BindView(R.id.layoutBusy)LinearLayout layoutBusy;

    @BindView(R.id.tvInfo)TextView tvInfo;
    @BindView(R.id.adView)
    AdView mAdView;

    private FilePickerDialog dialog;
    private final static int REQUEST_EXT_STORAGE_PERMIT = 9001;
    private final static int REQUEST_SHARE = 9002;

    private String strModelFileFormat = "file:///android_asset/%s";
    private String strUrlFormat = "<a href='%s'>%s</a>";
    private static final String INPUT_NODE_X = "X";
    private static final String INPUT_NODE_Y = "Y";
    private static final String INPUT_NODE_KEEP_PROB = "keep_prob";
    private static final String OUTPUT_NODE_HYPO = "hypothesis";
    private static final String OUTPUT_NODE_PRED = "prediction";
    private static final String OUTPUT_NODE_ACCU = "accuracy";
    private static final String[] OUTPUT_NODES_HYPO = new String[] {OUTPUT_NODE_HYPO};
    private static final String[] OUTPUT_NODES_PRED = new String[] {OUTPUT_NODE_PRED};
    private static final String[] OUTPUT_NODES_ACCU = new String[] {OUTPUT_NODE_ACCU};

    private boolean logStats = false;
    private TensorFlowInferenceInterface inferenceInterface;

    private long[] arrTestLabel;
    private float[] arrTestLabelOneHot;
    private FloatBuffer bufTestImages;
    private int nEvalCount = 10000;
    private static int[] arrDisplayImage = new int[28*28];
    private String strShareFilename = "";
    private String strShareFullFilename = "";

    // ================ For Mnist ================
    private static final int PIXEL_WIDTH = 28;
    private DrawModel mModel;
    private float mLastX;
    private float mLastY;
    private PointF mTmpPoint = new PointF();

    // ================ For Arguments ================
    private String strTitle = "";
    private String strUrl = "";
    private String strUrlTitle = "";
    private String strModelFile = "";

    @BindView(R.id.vwDraw)DrawView mDrawView;

    public MnistIntroductionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mnist_introduction, container, false);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        unbinder = ButterKnife.bind(this, view);
        initArgs();
        initInfo();
        initAds();
        initTensorFlow();
        initMnistView();

        return view;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();

        if (inferenceInterface != null) {
            inferenceInterface.close();
        }
    }

    @Override
    public void onResume() {
        mDrawView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mDrawView.onPause();
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SHARE) {
            if (TextUtils.isEmpty(strShareFullFilename)) return;

            File deleteFor = new File(strShareFullFilename);
            if (deleteFor.exists()) {
                deleteFor.delete();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case FilePickerDialog.EXTERNAL_READ_PERMISSION_GRANT: {
                if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(dialog!=null) {
                        //Show dialog if the read permission has been granted.
                        dialog.show();
                    }
                } else {
                    //Permission has not been granted. Notify the user.
                    Toast.makeText(getActivity(), "This app needs the external_read_permission.", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    public void onRequestPermission()
    {
        int permissionReadStorage = ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWriteStorage = ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionReadStorage == PackageManager.PERMISSION_DENIED || permissionWriteStorage == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[] {android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, FilePickerDialog.EXTERNAL_READ_PERMISSION_GRANT);
        } else {
            dialog.show();
        }
    }


    @OnClick(R.id.btnDetect)
    public void btnDetect_OnClick() {

        float pixels[] = mDrawView.getPixelData();

        try {
            float[] results = hypothesis(FloatBuffer.wrap(pixels), PIXEL_WIDTH * PIXEL_WIDTH, 10);
            tvOutput2.setText(Arrays.toString(results));
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        try {
            long[] results = predict(FloatBuffer.wrap(pixels), PIXEL_WIDTH * PIXEL_WIDTH, 10);
            tvOutput1.setText("Number is " + Arrays.toString(results));
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btnClear)
    public void btnClear_OnClick() {
        mModel.clear();
        mDrawView.reset();
        mDrawView.invalidate();

        tvOutput1.setText("");
        tvOutput2.setText("");
    }

    @OnClick(R.id.btnSave)
    public void btnSave_OnClick() {

        Date d = new Date();
        CharSequence strDateTime = DateFormat.format("yyyyMMddhhmmss", d.getTime());
        String strFilename = strDateTime + ".mnist";
        File file = new File(getContext().getFilesDir().toString(), strFilename);
        String strFullname = file.getAbsolutePath();
        saveImage(strFullname);
    }

    @OnClick(R.id.btnLoad)
    public void btnLoad_OnClick() {

        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        //properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.root = new File(getContext().getFilesDir().toString());
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;

        dialog = new FilePickerDialog(getContext(), properties);
        dialog.setTitle("Select a saved mnist file");

        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {

                //files is the array of the paths of files selected by the Application User.
                if (files == null || files.length == 0) {
                    return;
                }
                String strFilename = files[0];
                loadImage(strFilename);
            }

            @Override
            public void onCancel() {
            }

        });
        dialog.setProperties(properties);
        //dialog.show();
        onRequestPermission();

    }

    @OnClick(R.id.btnDelete)
    public void btnDelete_OnClick() {

        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.MULTI_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        //properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.root = new File(getContext().getFilesDir().toString());
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;

        dialog = new FilePickerDialog(getContext(), properties);
        dialog.setTitle("Select a saved mnist file");

        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {

                //files is the array of the paths of files selected by the Application User.
                if (files == null || files.length == 0) {
                    return;
                }
                deleteImages(files);
            }

            @Override
            public void onCancel() {
            }

        });
        dialog.setProperties(properties);
        //dialog.show();
        onRequestPermission();
    }

    @OnClick(R.id.btnAccuracy)
    public void btnAccuracy_OnClick() {

        //layoutBusy.setVisibility(View.VISIBLE);
        if (strModelFile.contains("cnn")) {
            nEvalCount = 1000;
        }

        arrTestLabel = readMnistLabel("t10k-labels.idx1-ubyte");
        arrTestLabelOneHot = readMnistLabelwithOneHotEncoding("t10k-labels.idx1-ubyte");
        bufTestImages = readMnistImageBuf("t10k-images.idx3-ubyte");

        List<KeyNameValue> arrSpinnerData = new ArrayList<KeyNameValue>();

        long[] arrPredicted = {};
        try {
            arrPredicted = predict(bufTestImages, 28 * 28, 10);
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        for(int i=0; i<arrPredicted.length; i++) {
            String strPredictResult= "";
            if(arrPredicted[i] != arrTestLabel[i]) {
                strPredictResult = "F";
                String strMsg = String.format("%04d-%s-L:%d-P:%d", i, strPredictResult, arrTestLabel[i], arrPredicted[i]);
                arrSpinnerData.add(new KeyNameValue(i, strMsg));
            } else {
                strPredictResult = "T";
            }
        }

        float[] accu = {};
        try {
            accu = accuracy(bufTestImages, arrTestLabelOneHot, 28 * 28, 10);
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        String strMsg = String.format("Accuracy[# of Fail: %d ]= ", arrSpinnerData.size());
        tvOutput1.setText(strMsg + Arrays.toString(accu) );

        //====================== create dataAdaptor for spFailed ======================
        ArrayAdapter<KeyNameValue> dataAdapter = new ArrayAdapter<KeyNameValue>(getContext(), android.R.layout.simple_spinner_item, arrSpinnerData);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFailed.setAdapter(dataAdapter);
        spFailed.setSelection(0);
        spFailed.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        loadImageFromTest();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                }
        );

        layoutBusy.setVisibility(View.GONE);
    }

    @OnClick(R.id.btnLoadToView)
    public void btnLoadToView_OnClick() {
        loadImageFromTest();
    }

    @OnClick(R.id.btnShare)
    public void btnShare_OnClick() {

        Date d = new Date();
        CharSequence strDateTime = DateFormat.format("yyyyMMddhhmmss", d.getTime());

        strShareFilename = strDateTime + ".mnist";
        File fOutfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), strShareFilename);
        strShareFullFilename = fOutfile.getAbsolutePath();

        saveImage(strShareFullFilename);

        sharePublicFileProvider(this, strShareFilename,
                getString(R.string.app_name),
                "Share a mnist file: " + strShareFilename,
                REQUEST_SHARE);
    }


    private void initTensorFlow() {
        inferenceInterface = new TensorFlowInferenceInterface(this.getActivity().getAssets(), String.format(strModelFileFormat, strModelFile));
    }

    private float[] hypothesis(FloatBuffer floatBuf, int nFeatures, int nClasses) {

        int nInstance = (floatBuf.capacity() / nFeatures);

        inferenceInterface.feed(INPUT_NODE_X, floatBuf, nInstance, nFeatures);
        if (strModelFile.contains("dropout") || strModelFile.contains("deep_cnn")) {
            inferenceInterface.feed(INPUT_NODE_KEEP_PROB, new float[]{1.0f}, 1, 1);
        }

        inferenceInterface.run(OUTPUT_NODES_HYPO, logStats);

        float[] result = new float[nInstance * nClasses];
        inferenceInterface.fetch(OUTPUT_NODE_HYPO, result);
        return result;
    }

    private long[] predict(FloatBuffer floatBuf, int nFeatures, int nClasses) {

        int nInstance = (floatBuf.capacity() / nFeatures);
        floatBuf.rewind();

        inferenceInterface.feed(INPUT_NODE_X, floatBuf, nInstance, nFeatures);
        if (strModelFile.contains("dropout") || strModelFile.contains("deep_cnn")) {
            inferenceInterface.feed(INPUT_NODE_KEEP_PROB, new float[]{1.0f}, 1, 1);
        }

        inferenceInterface.run(OUTPUT_NODES_PRED, logStats);

        long[] result = new long[nInstance * 1];
        inferenceInterface.fetch(OUTPUT_NODE_PRED, result);
        return result;
    }

    private float[] accuracy(FloatBuffer floatBuf, float[] inputFloatsY, int nFeatures, int nClasses) {

        int nInstance = (floatBuf.capacity() / nFeatures);
        floatBuf.rewind();

        inferenceInterface.feed(INPUT_NODE_X, floatBuf, nInstance, nFeatures);
        inferenceInterface.feed(INPUT_NODE_Y, inputFloatsY, nInstance, nClasses);
        if (strModelFile.contains("dropout") || strModelFile.contains("deep_cnn")) {
            inferenceInterface.feed(INPUT_NODE_KEEP_PROB, new float[]{1.0f}, 1, 1);
        }

        inferenceInterface.run(OUTPUT_NODES_ACCU, logStats);

        float[] result = new float[1];
        inferenceInterface.fetch(OUTPUT_NODE_ACCU, result);
        return result;
    }

    private long[] readMnistLabel(String strFilename) {

        long[] arrLongLabel = {};
        try {
            InputStream stream = getContext().getAssets().open(strFilename);

            int size = stream.available();
            if (size <= 0) {
                return null;
            }

            byte[] bHead = new byte[8];
            stream.read(bHead);

            arrLongLabel = new long[ nEvalCount];
            byte[] bBody = new byte[ nEvalCount];
            stream.read(bBody);

            for(int i=0; i<nEvalCount; i++) {
                arrLongLabel[i] = (long)bBody[i];
            }
            stream.close();
        } catch (Exception ex) {
            Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return arrLongLabel;
    }

    private float[] readMnistLabelwithOneHotEncoding(String strFilename) {

        float[] arrFloatLabel = {};
        try {
            InputStream stream = getContext().getAssets().open(strFilename);

            int size = stream.available();
            if (size <= 0) {
                return null;
            }

            byte[] bHead = new byte[8];
            stream.read(bHead);

            arrFloatLabel = new float[nEvalCount*10];
            byte[] bBody = new byte[nEvalCount];
            stream.read(bBody);

            for(int i=0; i<nEvalCount; i++) {
                int nIdx = (bBody[i] & 0xff);
                int newIdx = nIdx + i*10;
                arrFloatLabel[newIdx] = 1.0f;
            }
            stream.close();
        } catch (Exception ex) {
            Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return arrFloatLabel;
    }

    private FloatBuffer readMnistImageBuf(String strFilename) {

        FloatBuffer buf = FloatBuffer.allocate(nEvalCount*28*28);
        try {
            InputStream stream = getContext().getAssets().open(strFilename);

            int size = stream.available();
            if (size <= 0) {
                return null;
            }

            byte[] bHead = new byte[16];
            stream.read(bHead);

            byte[] bBody = new byte[100*28*28];
            for(int i=0; i<(int)(nEvalCount/100); i++) {
                stream.read(bBody);
                for(int j=0; j<100*28*28; j++) {
                    buf.put( (float)(bBody[j] & 0xff));
                }
            }
            stream.close();
        } catch (Exception ex) {
            Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return buf;
    }

    private void initMnistView() {

        tvOutput2.setSelected(true);

        mModel = new DrawModel(PIXEL_WIDTH, PIXEL_WIDTH);
        mDrawView.setModel(mModel);
        mDrawView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction() & MotionEvent.ACTION_MASK;

                if (action == MotionEvent.ACTION_DOWN) {
                    processTouchDown(motionEvent);
                    return true;

                } else if (action == MotionEvent.ACTION_MOVE) {
                    processTouchMove(motionEvent);
                    return true;

                } else if (action == MotionEvent.ACTION_UP) {
                    processTouchUp();
                    return true;
                }
                return false;
            }
        });
    }

    private void processTouchDown(MotionEvent event) {
        mLastX = event.getX();
        mLastY = event.getY();
        mDrawView.calcPos(mLastX, mLastY, mTmpPoint);
        float lastConvX = mTmpPoint.x;
        float lastConvY = mTmpPoint.y;
        mModel.startLine(lastConvX, lastConvY);
    }

    private void processTouchMove(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        mDrawView.calcPos(x, y, mTmpPoint);
        float newConvX = mTmpPoint.x;
        float newConvY = mTmpPoint.y;
        mModel.addLineElem(newConvX, newConvY);

        mLastX = x;
        mLastY = y;
        mDrawView.invalidate();
    }

    private void processTouchUp() {
        mModel.endLine();
        btnDetect_OnClick();
    }

    private void saveImage(String strFilename) {
        ;
        int[] arrData = mDrawView.getPixels();
        byte[] arrBytes = new byte[arrData.length];

        for(int i=0; i<arrData.length; i++) {
            arrBytes[i] = (byte)arrData[i];
        }

        File fOutDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!fOutDir.exists()) {
            fOutDir.mkdir();
        }

        FileOutputStream fos = null;
        try {
            //fos = getContext().openFileOutput(strFilename, Context.MODE_PRIVATE);
            fos = new FileOutputStream(strFilename);
            fos.write(arrBytes);
            fos.close();

            String strMsg = String.format("The %s file has been saved successfully.", strFilename);
            Toast.makeText(getContext(), strMsg, Toast.LENGTH_SHORT).show();
        } catch (IOException ex) {
            Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadImage(String strFilename) {

        byte[] arrBytes = new byte[28*28];

        FileInputStream fos = null;
        try {
            //fos = getContext().openFileInput(strFilename);
            fos = new FileInputStream(strFilename);
            fos.read(arrBytes);
            fos.close();
        } catch (Exception ex) {
            Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        int[] arrData = new int[arrBytes.length];
        for(int i=0; i<arrBytes.length; i++) {
            arrData[i] = (int)arrBytes[i];
        }

        mModel.clear();
        mDrawView.reset();
        mDrawView.invalidate();

        mDrawView.setPixels(arrData, arrData.length);
        mDrawView.invalidate();
    }

    private void loadImageFromTest() {

        if (bufTestImages == null) {
            return;
        }

        //int nPos = spFailed.getSelectedItemPosition();
        KeyNameValue keyItem = (KeyNameValue)spFailed.getSelectedItem();
        int nPos = keyItem.getId();
        bufTestImages.position(nPos*28*28);
        for(int i=0; i<28*28; i++) {
            arrDisplayImage[i] = (int)bufTestImages.get();
        }

        mModel.clear();
        mDrawView.reset();
        mDrawView.invalidate();

        mDrawView.setPixels(arrDisplayImage, 28*28);
        mDrawView.invalidate();
    }


    private void deleteImages(String[] arrFilenames) {

        try {
            for (String strFilename : arrFilenames) {
                File file = new File(strFilename);
                file.delete();
            }
        } catch (Exception ex) {
            Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void sharePublicFileProvider(Fragment fragBase, String strFileName, String subject, String Body, int nReqest_code) {

        File fOutfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), strFileName);
        Uri fileUri = FileProvider.getUriForFile(fragBase.getContext(),
                BuildConfig.APPLICATION_ID + ".provider",
                fOutfile);

        Log.d(TAG, "sending "+fileUri.toString()+" ...");

        Intent shareIntent = new Intent();
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, Body);
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.setType("application/octet-stream");
        shareIntent.setAction(Intent.ACTION_SEND);
        fragBase.startActivityForResult(Intent.createChooser(shareIntent, "Share a file."), nReqest_code);
    }

    private void initArgs() {
        if (getArguments() != null) {
            strTitle = getArguments().getString(Constants.fragMnistTitle);
            strUrl = getArguments().getString(Constants.fragMnistUrl);
            strUrlTitle = getArguments().getString(Constants.fragMnistUrlTitle);
            strModelFile = getArguments().getString(Constants.fragMnistModelFile);
        }
    }

    private void initInfo() {
        String strInfo = "The optimized model source: <br/>";
        strInfo = strInfo + String.format(strUrlFormat, strUrl, strUrlTitle);
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

    public class KeyNameValue {

        private int id;
        private String name;

        public KeyNameValue(int id, String name) {
            this.id = id;
            this.name = name;
        }


        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        //to display object as a string in spinner
        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof KeyNameValue){
                KeyNameValue c = (KeyNameValue)obj;
                if(c.getName().equals(name) && c.getId()==id ) return true;
            }

            return false;
        }

    }

}
