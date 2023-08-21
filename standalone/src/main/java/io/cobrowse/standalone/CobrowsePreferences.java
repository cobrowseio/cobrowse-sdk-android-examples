package io.cobrowse.standalone;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

public final class CobrowsePreferences {

    public static final String KEY_API = "v1_cobrowse_api";
    public static final String KEY_LICENSE = "v1_cobrowse_license";
    public static final String KEY_HIDE_SESSION_CONTOLS = "v1_cobrowse_hide_session_controls";

    private CobrowsePreferences() { }

    @NonNull
    public static String getApi(@NonNull Context context) {
        String api = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(KEY_API, null);

        return api != null && !api.isEmpty()
                ? api
                : "https://cobrowse.io";
    }

    @NonNull
    public static String getLicense(@NonNull Context context) {
        String license = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(KEY_LICENSE, null);

        return license != null && !license.isEmpty()
                ? license
                : "trial";
    }

    public static boolean shouldHideSessionControls(@NonNull Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(KEY_HIDE_SESSION_CONTOLS, false);
    }

}
