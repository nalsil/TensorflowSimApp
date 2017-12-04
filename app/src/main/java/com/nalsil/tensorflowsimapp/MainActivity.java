package com.nalsil.tensorflowsimapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.nalsil.tensorflowsimapp.about.AboutActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = MainActivity.class.getSimpleName();
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private MaterialDialog dialog;
    private Menu menuNav;

    private static String deviceId;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNavigationItemById(R.id.nav_toc);;
            }
        });

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        nvDrawer = (NavigationView) findViewById(R.id.nav_view);
        nvDrawer.setNavigationItemSelectedListener(this);
        menuNav = nvDrawer.getMenu();


        //============================= Start of Version =============================
        nvDrawer.setNavigationItemSelectedListener(this);
        View headerView = nvDrawer.getHeaderView(0);

        // 23.1.0
        //TextView tvVersion = (TextView) navigationView.findViewById(R.id.tvVersion);
        //23.1.1
        TextView tvVersion = (TextView) headerView.findViewById(R.id.tvVersion);

        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            String versionInfo = packageInfo.versionName;
            versionInfo += " - " + packageInfo.versionCode + " SDK:" + Build.VERSION.SDK_INT;
            tvVersion.setText(versionInfo);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Version Information", e);
        }
        //=============================  End of Version =============================


        // =============== For ad-DeviceID ================
        String androidId = android.provider.Settings.Secure.getString(this.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        deviceId = MD5(androidId).toUpperCase();
        // =============== For ad-DeviceID ================


        onNavigationItemById(R.id.nav_toc);
        runOnce();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_accessinfo) {
            showPermissionInfo();
            return true;
        } else if (id == R.id.action_about) {
                startActivity(new Intent(this, AboutActivity.class));
                return true;
        }


        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        onNavigationItemById(item.getItemId());

        // Close the navigation drawer
        mDrawer.closeDrawers();
        return true;
    }

    private void onNavigationItemById(int id) {

        Fragment fragment = null;
        Class fragmentClass;

        switch (id) {
            case R.id.nav_toc:
                fragmentClass = TOCFragment.class;
                break;
            case R.id.nav_linearregression:
                fragmentClass = LinearRegressionFragment.class;
                break;
            case R.id.nav_MinimizingCostGradientUpdate:
                fragmentClass = MinimizingCostGradientUpdateFragment.class;
                break;
            case R.id.nav_MultiVariableMatmulLinearRegression:
                fragmentClass = MultiVariableMatmulLinearRegressionFragment.class;
                break;
            case R.id.nav_LogisticRegression:
                fragmentClass = LogisticRegressionFragment.class;
                break;
            case R.id.nav_SoftmaxClassifier:
                fragmentClass = SoftmaxClassifierFragment.class;
                break;
            case R.id.nav_LearningRateAndEvaluation:
                fragmentClass = LearningRateAndEvaluationFragment.class;
                break;
            case R.id.nav_MnistIntroduction:
                fragmentClass = MnistIntroductionFragment.class;
                break;
            case R.id.nav_XorNNWideDeep:
                fragmentClass = XorNNWideDeepFragment.class;
                break;

            default:
                fragmentClass = TOCFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        setTitleById(id);
    }


    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // NOTE 1: Make sure to override the method with only a single `Bundle` argument
    // Note 2: Make sure you implement the correct `onPostCreate(Bundle savedInstanceState)` method.
    // There are 2 signatures and only `onPostCreate(Bundle state)` shows the hamburger icon.
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusEvents.MessageEvent event) {

        String strItem = event.strNavigationItem;
        if (strItem.equals(TOCFragment.class.getSimpleName())) {
            onNavigationItemById(R.id.nav_toc);
        } else if (strItem.equals(LinearRegressionFragment.class.getSimpleName())) {
            onNavigationItemById(R.id.nav_linearregression);
        } else if (strItem.equals(MinimizingCostGradientUpdateFragment.class.getSimpleName())) {
            onNavigationItemById(R.id.nav_MinimizingCostGradientUpdate);
        } else if (strItem.equals(MultiVariableMatmulLinearRegressionFragment.class.getSimpleName())) {
            onNavigationItemById(R.id.nav_MultiVariableMatmulLinearRegression);
        } else if (strItem.equals(LogisticRegressionFragment.class.getSimpleName())) {
            onNavigationItemById(R.id.nav_LogisticRegression);
        } else if (strItem.equals(SoftmaxClassifierFragment.class.getSimpleName())) {
            onNavigationItemById(R.id.nav_SoftmaxClassifier);
        } else if (strItem.equals(LearningRateAndEvaluationFragment.class.getSimpleName())) {
            onNavigationItemById(R.id.nav_LearningRateAndEvaluation);
        } else if (strItem.equals(MnistIntroductionFragment.class.getSimpleName())) {
            onNavigationItemById(R.id.nav_MnistIntroduction);
        } else if (strItem.equals(XorNNWideDeepFragment.class.getSimpleName())) {
            onNavigationItemById(R.id.nav_XorNNWideDeep);
        } else {
            onNavigationItemById(R.id.nav_toc);
        }
    };

    private void setTitleById(int id) {
        MenuItem menuItem = menuNav.findItem(id);
        if ( menuItem != null ) {
            // Highlight the selected item has been done by NavigationView
            menuItem.setChecked(true);
            // Set action bar title
            setTitle(menuItem.getTitle());
        } else {
            setTitle(R.string.app_name);
        }
    }


    private void runOnce()  {
        String strRunOnce = "pref_runOnce";

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(!prefs.getBoolean(strRunOnce, false)) {

            // run your one time code
            showPermissionInfo();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(strRunOnce, true);
            editor.commit();
        }
    }


    private void showPermissionInfo() {

        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .customView(R.layout.dialog_permission_info, true)
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                    }
                });

        dialog = builder.build();
        dialog.show();
    }


    public static String getDeviceId() {
        return deviceId;
    }

    private static String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }


}
