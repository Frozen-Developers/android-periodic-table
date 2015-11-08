package com.frozendevs.periodictable.fragment;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.webkit.WebView;

import com.frozendevs.periodictable.R;

public class AboutFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.about_screen);

        String versionName;
        try {
            versionName = getContext().getPackageManager().getPackageInfo(
                    getContext().getPackageName(), 0).versionName;
        } catch (Exception e) {
            versionName = getString(R.string.preference_version_number_unknown);
        }
        findPreference("version").setSummary(versionName);

        findPreference("licences").setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        WebView webView = new WebView(getContext());
                        webView.loadUrl("file:///android_asset/html/licenses.html");

                        AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
                        dialog.setTitle(R.string.preference_open_source_licences);
                        dialog.setView(webView);
                        dialog.show();

                        return true;
                    }
                });
    }
}
