// Copyright 2015 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.preferences.privacy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.content.Intent;
import android.view.WindowManager;

import org.chromium.base.CommandLine;
import org.chromium.chrome.R;
import org.chromium.chrome.browser.ApplicationLifetime;
import org.chromium.chrome.browser.ChromeSwitches;
import org.chromium.chrome.browser.contextualsearch.ContextualSearchFieldTrial;
import org.chromium.chrome.browser.help.HelpAndFeedback;
import org.chromium.chrome.browser.incognito.IncognitoOnlyModeUtil;
import org.chromium.chrome.browser.physicalweb.PhysicalWeb;
import org.chromium.chrome.browser.precache.PrecacheLauncher;
import org.chromium.chrome.browser.preferences.ChromeBaseCheckBoxPreference;
import org.chromium.chrome.browser.preferences.ManagedPreferenceDelegate;
import org.chromium.chrome.browser.preferences.PrefServiceBridge;
import org.chromium.chrome.browser.profiles.Profile;

/**
 * Fragment to keep track of the all the privacy related preferences.
 */
public class PrivacyPreferences extends PreferenceFragment
        implements OnPreferenceChangeListener {
    private static final String PREF_NAVIGATION_ERROR = "navigation_error";
    private static final String PREF_SEARCH_SUGGESTIONS = "search_suggestions";
    private static final String PREF_BLOCK_SCREEN_OBSERVERS = "block_screen_observers";
    private static final String PREF_SAFE_BROWSING_EXTENDED_REPORTING =
            "safe_browsing_extended_reporting";
    private static final String PREF_SAFE_BROWSING_SCOUT_REPORTING =
            "safe_browsing_scout_reporting";
    private static final String PREF_SAFE_BROWSING = "safe_browsing";
    private static final String PREF_CONTEXTUAL_SEARCH = "contextual_search";
    private static final String PREF_NETWORK_PREDICTIONS = "network_predictions";
    private static final String PREF_INCOGNITO_ONLY = "incognito_only";
    private static final String PREF_DO_NOT_TRACK = "do_not_track";
    private static final String PREF_USAGE_AND_CRASH_REPORTING = "usage_and_crash_reports";
    private static final String PREF_PHYSICAL_WEB = "physical_web";
    private static final String PREF_SECURITY_UPDATES = "security_updates_cellular";
    private static final String PREF_SECURE_CONNECT = "secure_connect";

    private ManagedPreferenceDelegate mManagedPreferenceDelegate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrivacyPreferencesManager privacyPrefManager = PrivacyPreferencesManager.getInstance();
        privacyPrefManager.migrateNetworkPredictionPreferences();
        addPreferencesFromResource(R.xml.privacy_preferences);
        getActivity().setTitle(R.string.prefs_privacy);
        setHasOptionsMenu(true);
        PrefServiceBridge prefServiceBridge = PrefServiceBridge.getInstance();

        mManagedPreferenceDelegate = createManagedPreferenceDelegate();

        ChromeBaseCheckBoxPreference networkPredictionPref =
                (ChromeBaseCheckBoxPreference) findPreference(PREF_NETWORK_PREDICTIONS);
        networkPredictionPref.setChecked(prefServiceBridge.getNetworkPredictionEnabled());
        networkPredictionPref.setOnPreferenceChangeListener(this);
        networkPredictionPref.setManagedPreferenceDelegate(mManagedPreferenceDelegate);

        ChromeBaseCheckBoxPreference navigationErrorPref =
                (ChromeBaseCheckBoxPreference) findPreference(PREF_NAVIGATION_ERROR);
        navigationErrorPref.setOnPreferenceChangeListener(this);
        navigationErrorPref.setManagedPreferenceDelegate(mManagedPreferenceDelegate);

        ChromeBaseCheckBoxPreference searchSuggestionsPref =
                (ChromeBaseCheckBoxPreference) findPreference(PREF_SEARCH_SUGGESTIONS);
        searchSuggestionsPref.setOnPreferenceChangeListener(this);
        searchSuggestionsPref.setManagedPreferenceDelegate(mManagedPreferenceDelegate);

        ChromeBaseCheckBoxPreference blockScreenObserversPref =
                (ChromeBaseCheckBoxPreference) findPreference(PREF_BLOCK_SCREEN_OBSERVERS);
        blockScreenObserversPref.setOnPreferenceChangeListener(this);
        blockScreenObserversPref.setManagedPreferenceDelegate(mManagedPreferenceDelegate);

        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (!CommandLine.getInstance().hasSwitch(ChromeSwitches.CRASH_LOG_SERVER_CMD)) {
            preferenceScreen.removePreference(findPreference(PREF_USAGE_AND_CRASH_REPORTING));
        }

        if (!ContextualSearchFieldTrial.isEnabled()) {
            preferenceScreen.removePreference(findPreference(PREF_CONTEXTUAL_SEARCH));
        }

        boolean isSafeBrowsingEnabled =
                !CommandLine.getInstance().hasSwitch(ChromeSwitches.DISABLE_SAFE_BROWSING);

        // Listen to changes to both Extended Reporting prefs.
        ChromeBaseCheckBoxPreference legacyExtendedReportingPref =
                (ChromeBaseCheckBoxPreference) findPreference(
                    PREF_SAFE_BROWSING_EXTENDED_REPORTING);
        ChromeBaseCheckBoxPreference scoutReportingPref =
                (ChromeBaseCheckBoxPreference) findPreference(PREF_SAFE_BROWSING_SCOUT_REPORTING);

        if (!isSafeBrowsingEnabled) {
            preferenceScreen.removePreference(legacyExtendedReportingPref);
            preferenceScreen.removePreference(scoutReportingPref);
        } else {
            legacyExtendedReportingPref.setOnPreferenceChangeListener(this);
            legacyExtendedReportingPref.setManagedPreferenceDelegate(mManagedPreferenceDelegate);
            scoutReportingPref.setOnPreferenceChangeListener(this);
            scoutReportingPref.setManagedPreferenceDelegate(mManagedPreferenceDelegate);

            // Remove the extended reporting preference that is NOT active.
            String extended_reporting_pref_to_remove =
                    prefServiceBridge.isSafeBrowsingScoutReportingActive()
                        ? PREF_SAFE_BROWSING_EXTENDED_REPORTING : PREF_SAFE_BROWSING_SCOUT_REPORTING;
            preferenceScreen.removePreference(findPreference(extended_reporting_pref_to_remove));
        }

        ChromeBaseCheckBoxPreference safeBrowsingPref =
                (ChromeBaseCheckBoxPreference) findPreference(PREF_SAFE_BROWSING);
        if(!isSafeBrowsingEnabled) {
            preferenceScreen.removePreference(safeBrowsingPref);
        } else {
            safeBrowsingPref.setOnPreferenceChangeListener(this);
            safeBrowsingPref.setManagedPreferenceDelegate(mManagedPreferenceDelegate);
        }

        boolean isIncognitoOnlyBrowser = IncognitoOnlyModeUtil.getInstance()
                .isIncognitoOnlyBrowser();
        ChromeBaseCheckBoxPreference incognitoOnlyPref =
                (ChromeBaseCheckBoxPreference) findPreference(PREF_INCOGNITO_ONLY);
        if(isIncognitoOnlyBrowser) {
            preferenceScreen.removePreference(incognitoOnlyPref);
        } else {
            incognitoOnlyPref.setOnPreferenceChangeListener(this);
        }

        if (!PhysicalWeb.featureIsEnabled()) {
            preferenceScreen.removePreference(findPreference(PREF_PHYSICAL_WEB));
        }

        updateSummaries();

        ChromeBaseCheckBoxPreference securityUpdatesCellular =
                (ChromeBaseCheckBoxPreference) findPreference(PREF_SECURITY_UPDATES);
        securityUpdatesCellular.setOnPreferenceChangeListener(this);


        if (PrivacyPreferencesManager.getInstance().isBlockScreenObserversEnabled()) {
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                                               WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (PREF_SEARCH_SUGGESTIONS.equals(key)) {
            PrefServiceBridge.getInstance().setSearchSuggestEnabled((boolean) newValue);
        } else if (PREF_BLOCK_SCREEN_OBSERVERS.equals(key)) {
            Intent resultIntent = getActivity().getIntent();
            resultIntent.putExtra("Secure", (boolean) newValue);
            getActivity().setResult(getActivity().RESULT_OK, resultIntent);
            PrivacyPreferencesManager.getInstance().setBlockScreenObservers(
                    (boolean) newValue);
            if ((boolean) newValue) {
                getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                                                   WindowManager.LayoutParams.FLAG_SECURE);
            }
            else {
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
            }
        } else if (PREF_SAFE_BROWSING.equals(key)) {
            PrefServiceBridge.getInstance().setSafeBrowsingEnabled((boolean) newValue);
        } else if (PREF_SAFE_BROWSING_EXTENDED_REPORTING.equals(key)
                   || PREF_SAFE_BROWSING_SCOUT_REPORTING.equals(key)) {
            PrefServiceBridge.getInstance().setSafeBrowsingExtendedReportingEnabled(
                    (boolean) newValue);
        } else if (PREF_NETWORK_PREDICTIONS.equals(key)) {
            PrefServiceBridge.getInstance().setNetworkPredictionEnabled((boolean) newValue);
            PrecacheLauncher.updatePrecachingEnabled(getActivity());
        } else if (PREF_NAVIGATION_ERROR.equals(key)) {
            PrefServiceBridge.getInstance().setResolveNavigationErrorEnabled((boolean) newValue);
        } else if (PREF_SECURITY_UPDATES.equals(key)) {
            PrefServiceBridge.getInstance().setSecurityUpdatesOnCellular((boolean) newValue);
        } else if (PREF_INCOGNITO_ONLY.equals(key)) {
            incognitoOnlyPreferenceDialog(preference, (boolean) newValue).show();
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSummaries();
    }

    /**
     * Updates the summaries for several preferences.
     */
    public void updateSummaries() {
        PrefServiceBridge prefServiceBridge = PrefServiceBridge.getInstance();

        PrivacyPreferencesManager privacyPrefManager = PrivacyPreferencesManager.getInstance();

        CharSequence textOn = getActivity().getResources().getText(R.string.text_on);
        CharSequence textOff = getActivity().getResources().getText(R.string.text_off);

        PrefServiceBridge.getInstance().setSecurityUpdatesDefault();
        CheckBoxPreference securityUpdatesCellular = (CheckBoxPreference) findPreference(
                PREF_SECURITY_UPDATES);
        securityUpdatesCellular.setChecked(prefServiceBridge.getSecurityUpdatesOnCellular());

        CheckBoxPreference navigationErrorPref = (CheckBoxPreference) findPreference(
                PREF_NAVIGATION_ERROR);
        if (navigationErrorPref != null) {
            navigationErrorPref.setChecked(
                    prefServiceBridge.isResolveNavigationErrorEnabled());
        }

        CheckBoxPreference searchSuggestionsPref = (CheckBoxPreference) findPreference(
                PREF_SEARCH_SUGGESTIONS);
        if (searchSuggestionsPref != null) {
            searchSuggestionsPref.setChecked(prefServiceBridge.isSearchSuggestEnabled());
        }

        String extended_reporting_pref = prefServiceBridge.isSafeBrowsingScoutReportingActive()
                ? PREF_SAFE_BROWSING_SCOUT_REPORTING : PREF_SAFE_BROWSING_EXTENDED_REPORTING;
        CheckBoxPreference extendedReportingPref =
                (CheckBoxPreference) findPreference(extended_reporting_pref);
        if (extendedReportingPref != null) {
            extendedReportingPref.setChecked(
                    prefServiceBridge.isSafeBrowsingExtendedReportingEnabled());
        }

        CheckBoxPreference safeBrowsingPref =
                (CheckBoxPreference) findPreference(PREF_SAFE_BROWSING);
        if (safeBrowsingPref != null) {
            safeBrowsingPref.setChecked(prefServiceBridge.isSafeBrowsingEnabled());
        }

        CheckBoxPreference incognitoOnlyPref =
                (CheckBoxPreference) findPreference(PREF_INCOGNITO_ONLY);
        if (incognitoOnlyPref != null) {
            incognitoOnlyPref.setChecked(privacyPrefManager.isIncognitoOnlyEnabled());
        }

        Preference doNotTrackPref = findPreference(PREF_DO_NOT_TRACK);
        if (doNotTrackPref != null) {
            doNotTrackPref.setSummary(prefServiceBridge.isDoNotTrackEnabled() ? textOn : textOff);
        }

        Preference contextualPref = findPreference(PREF_CONTEXTUAL_SEARCH);
        if (contextualPref != null) {
            boolean isContextualSearchEnabled = !prefServiceBridge.isContextualSearchDisabled();
            contextualPref.setSummary(isContextualSearchEnabled ? textOn : textOff);
        }

        Preference physicalWebPref = findPreference(PREF_PHYSICAL_WEB);
        if (physicalWebPref != null) {
            physicalWebPref.setSummary(privacyPrefManager.isPhysicalWebEnabled()
                    ? textOn : textOff);
        }

        Preference usageAndCrashPref = findPreference(PREF_USAGE_AND_CRASH_REPORTING);
        if (usageAndCrashPref != null) {
            usageAndCrashPref.setSummary(
                    privacyPrefManager.isUsageAndCrashReportingPermittedByUser() ? textOn
                                                                                 : textOff);
        }
    }

    private ManagedPreferenceDelegate createManagedPreferenceDelegate() {
        return new ManagedPreferenceDelegate() {
            @Override
            public boolean isPreferenceControlledByPolicy(Preference preference) {
                String key = preference.getKey();
                PrefServiceBridge prefs = PrefServiceBridge.getInstance();
                if (PREF_NAVIGATION_ERROR.equals(key)) {
                    return prefs.isResolveNavigationErrorManaged();
                }
                if (PREF_SEARCH_SUGGESTIONS.equals(key)) {
                    return prefs.isSearchSuggestManaged();
                }
                if (PREF_SAFE_BROWSING_EXTENDED_REPORTING.equals(key)
                        || PREF_SAFE_BROWSING_SCOUT_REPORTING.equals(key)) {
                    return prefs.isSafeBrowsingExtendedReportingManaged();
                }
                if (PREF_SAFE_BROWSING.equals(key)) {
                    return prefs.isSafeBrowsingManaged();
                }
                if (PREF_NETWORK_PREDICTIONS.equals(key)) {
                    return prefs.isNetworkPredictionManaged();
                }
                return false;
            }
        };
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        if(CommandLine.getInstance()
                .hasSwitch(ChromeSwitches.ENABLE_SUPPRESSED_CHROMIUM_FEATURES)) {
            MenuItem help = menu.add(
                    Menu.NONE, R.id.menu_id_targeted_help, Menu.NONE, R.string.menu_help);
            help.setIcon(R.drawable.ic_help_and_feedback);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_id_targeted_help) {
            HelpAndFeedback.getInstance(getActivity())
                    .show(getActivity(), getString(R.string.help_context_privacy),
                            Profile.getLastUsedProfile(), null);
            return true;
        }
        return false;
    }

    private AlertDialog incognitoOnlyPreferenceDialog(final Preference preference,
                                                      final boolean isChecked) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme)
                .setTitle(isChecked ? R.string.incognito_only_turn_on_title
                        : R.string.incognito_only_turn_off_title)
                .setMessage(isChecked ? R.string.incognito_only_turn_on_confirmation
                        : R.string.incognito_only_turn_off_confirmation)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        ((ChromeBaseCheckBoxPreference) preference).setChecked(!isChecked);
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PrivacyPreferencesManager.getInstance().
                                setIncognitoOnlyEnabled((boolean) isChecked);
                        dialog.dismiss();
                        ApplicationLifetime.terminate(true);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        ((ChromeBaseCheckBoxPreference) preference).setChecked(!isChecked);
                    }
                })
                .create();

        return dialog;
    }
}
