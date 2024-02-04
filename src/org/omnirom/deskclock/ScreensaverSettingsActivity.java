/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.omnirom.deskclock;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

/**
 * Settings for the Alarm Clock Dream (Screensaver).
 */
public class ScreensaverSettingsActivity extends AppCompatActivity {

    static final String KEY_CLOCK_STYLE =
            "screensaver_clock_style";
    static final String KEY_NIGHT_MODE =
            "screensaver_night_mode";

    private static final String PREFS_FRAGMENT_TAG = "prefs_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the prefs fragment in code to ensure it's created before PreferenceDialogFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main, new PrefsFragment(), PREFS_FRAGMENT_TAG)
                    .disallowAddToBackStack()
                    .commit();
        }
    }

    private static class PrefsFragment extends PreferenceFragmentCompat
            implements Preference.OnPreferenceChangeListener {
        @Override
        public void onResume() {
            super.onResume();
            refresh();
        }

        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {
            if (KEY_CLOCK_STYLE.equals(pref.getKey())) {
                final ListPreference listPref = (ListPreference) pref;
                final int idx = listPref.findIndexOfValue((String) newValue);
                listPref.setSummary(listPref.getEntries()[idx]);
            } else if (KEY_NIGHT_MODE.equals(pref.getKey())) {
                boolean state = ((CheckBoxPreference) pref).isChecked();
            }
            return true;
        }

        private void refresh() {
            ListPreference listPref = (ListPreference) findPreference(KEY_CLOCK_STYLE);
            listPref.setSummary(listPref.getEntry());
            listPref.setOnPreferenceChangeListener(this);

            Preference pref = findPreference(KEY_NIGHT_MODE);
            pref.setOnPreferenceChangeListener(this);
        }
        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            addPreferencesFromResource(R.xml.dream_settings);
        }
    }
}
