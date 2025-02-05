package io.cobrowse.standalone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDexApplication;
import androidx.preference.PreferenceManager;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;

import io.cobrowse.CobrowseIO;
import io.cobrowse.Session;

public class MainApplication extends MultiDexApplication
        implements CobrowseIO.SessionControlsDelegate,
                   SharedPreferences.OnSharedPreferenceChangeListener,
                   Application.ActivityLifecycleCallbacks {

    private static final String TAG = "MainApplication";
    private final List<Activity> createdActivities = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityLifecycleCallbacks(this);

        PreferenceManager
                .getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        CobrowseIO.instance().setDelegate(this);

        CobrowseIO.instance().api(CobrowsePreferences.getApi(this));
        CobrowseIO.instance().license(CobrowsePreferences.getLicense(this));
        CobrowseIO.instance().start();

        if (FirebaseApp.getApps(this).size() > 0) {
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult
                    -> CobrowseIO.instance().setDeviceToken(instanceIdResult.getToken()));
        } else {
            Log.e(TAG, "Firebase app is not initialized. Did you copy your `google-services.json` file?");
        }
    }

    private void updateCobrowseConfiguration() {
        CobrowseIO.instance().stop();
        CobrowseIO.instance().api(CobrowsePreferences.getApi(this));
        CobrowseIO.instance().license(CobrowsePreferences.getLicense(this));
        CobrowseIO.instance().start();
    }

    private void updateCobrowseSessionControls() {
        boolean hide = CobrowsePreferences.shouldHideSessionControls(this);
        if (!hide && CobrowseIO.instance().currentSession() == null) {
            // Do not show custom controls if there is no session
            return;
        }

        for (Activity next : createdActivities) {
            if (hide) {
                removeSessionControls(next);
            } else {
                injectSessionControls(next);
            }
        }
    }

    private void injectSessionControls(@NonNull Activity activity) {
        if (activity.getWindow().findViewById(R.id.layout_cobrowse_session_controls) != null) {
            return;
        }

        @SuppressLint("InflateParams")
        View customControls = LayoutInflater.from(activity).inflate(R.layout.layout_cobrowse_session_controls, null);
        customControls.findViewById(R.id.layout_cobrowse_session_controls_fab)
                .setOnClickListener(v -> {
                    Session current = CobrowseIO.instance().currentSession();
                    if (current != null) {
                        current.end(null);
                    }
                });
        ((ViewGroup) activity.getWindow().findViewById(android.R.id.content)).addView(customControls);
    }

    private void removeSessionControls(@NonNull Activity activity) {
        View customControls = activity.getWindow().findViewById(R.id.layout_cobrowse_session_controls);
        if (customControls != null) {
            ViewGroup parent = (ViewGroup)customControls.getParent();
            if (parent != null) parent.removeView(customControls);
        }
    }

    //<editor-fold desc="CobrowseIO.SessionControlsDelegate implementation">

    @Override
    public void showSessionControls(@Nullable Activity activity, @NonNull Session session) {
        if (CobrowsePreferences.shouldHideSessionControls(this))
            return;
        if (activity != null) {
            injectSessionControls(activity);
        }
    }

    @Override
    public void hideSessionControls(@Nullable Activity activity, @NonNull Session session) {
        if (CobrowsePreferences.shouldHideSessionControls(this))
            return;
        if (activity != null) {
            removeSessionControls(activity);
        }
    }

    @Override
    public void sessionDidUpdate(@NonNull Session session) {
    }

    @Override
    public void sessionDidEnd(@NonNull Session session) {
    }

    //</editor-fold>

    //<editor-fold desc="SharedPreferences.OnSharedPreferenceChangeListener implementation">

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case CobrowsePreferences.KEY_API:
            case CobrowsePreferences.KEY_LICENSE:
                updateCobrowseConfiguration();
                break;
            case CobrowsePreferences.KEY_HIDE_SESSION_CONTOLS:
                updateCobrowseSessionControls();
                break;
            default:
                break;
        }
    }

    //</editor-fold>

    //<editor-fold desc="Application.ActivityLifecycleCallbacks implementation">

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        createdActivities.add(activity);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        createdActivities.remove(activity);
    }

    //</editor-fold>
}
