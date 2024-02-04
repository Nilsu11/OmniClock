/*
 *  Copyright (C) 2016 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.omnirom.alarmclock;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import org.omnirom.deskclock.R;
import org.omnirom.deskclock.Utils;
import org.omnirom.deskclock.preference.ColorPickerPreference;
import org.omnirom.deskclock.preference.FontPreference;

public class BinaryClockAppWidgetConfigure extends AppCompatActivity {

    public static final String KEY_CLOCK_FONT = "binary_clock_font";
    public static final String KEY_CLOCK_COLOR = "binary_clock_color";
    public static final String KEY_CLOCK_SHADOW = "binary_clock_shadow";
    public static final String KEY_SHOW_ALARM = "binary_show_alarm";
    public static final String KEY_SHOW_DATE = "binary_show_date";

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private PrefsFrag f;
    public void handleOkClick(View v) {
        f.handleOkClick(v);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(Utils.getThemeResourceId(this));
        //setBackgroundColor(Utils.getViewBackgroundColor(this));

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID,
        // finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        f = new PrefsFrag();
        setContentView(R.layout.settings_widget);
        View v = findViewById(R.id.main);
        v.setBackgroundColor(Utils.getViewBackgroundColor(this));

        // Create the prefs fragment in code to ensure it's created before PreferenceDialogFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main, f, PREFS_FRAGMENT_TAG)
                    .disallowAddToBackStack()
                    .commit();
        }

    }

    private static final String PREFS_FRAGMENT_TAG = "prefs_fragment";

    public static class PrefsFrag extends PreferenceFragmentCompat implements
            Preference.OnPreferenceChangeListener {

        private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        private FontPreference mClockFont;

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            addPreferencesFromResource(R.xml.binary_clock_appwidget_configure);
            initPreference(KEY_SHOW_ALARM);
            initPreference(KEY_SHOW_DATE);
            initPreference(KEY_CLOCK_SHADOW);
            initColorPreference(KEY_CLOCK_COLOR, getResources().getColor(R.color.binary_clock_dot_color));

            mClockFont = (FontPreference) findPreference(KEY_CLOCK_FONT);
            mClockFont.setKey(KEY_CLOCK_FONT + "_" + String.valueOf(mAppWidgetId));
            mClockFont.setSummary("Roboto Light");
            mClockFont.setOnPreferenceChangeListener(this);
        }

        public void handleOkClick(View v) {
            BinaryClockAppWidgetProvider.updateAfterConfigure(getActivity(), mAppWidgetId);
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            getActivity().setResult(RESULT_OK, resultValue);
            getActivity().finish();
        }

        private void initPreference(String key) {
            CheckBoxPreference b = (CheckBoxPreference) findPreference(key);
            b.setKey(key + "_" + String.valueOf(mAppWidgetId));
            b.setDefaultValue(true);
            b.setChecked(true);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            prefs.edit().putBoolean(b.getKey(), true).commit();
        }

        private void initColorPreference(String key, int defaultValue) {
            ColorPickerPreference c = (ColorPickerPreference) findPreference(key);
            c.setKey(key + "_" + String.valueOf(mAppWidgetId));
            c.setColor(defaultValue);
            String hexColor = String.format("#%08X", defaultValue);
            c.setSummary(hexColor);
            c.setOnPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference == mClockFont) {
                String value = (String) newValue;
                int valueIndex = mClockFont.findIndexOfValue(value);
                mClockFont.setSummary(mClockFont.getEntries()[valueIndex]);
                return true;
            } else if (preference instanceof ColorPickerPreference) {
                ColorPickerPreference c = (ColorPickerPreference) preference;
                String hexColor = String.format("#%08X", c.getColor());
                preference.setSummary(hexColor);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                prefs.edit().putInt(c.getKey(), c.getColor()).commit();
                return true;
            }
            return false;
        }
    }
    public static void clearPrefs(Context context, int id) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().remove(KEY_CLOCK_FONT + "_" + id).commit();
        prefs.edit().remove(KEY_CLOCK_COLOR + "_" + id).commit();
        prefs.edit().remove(KEY_CLOCK_SHADOW + "_" + id).commit();
        prefs.edit().remove(KEY_SHOW_ALARM + "_" + id).commit();
        prefs.edit().remove(KEY_SHOW_DATE + "_" + id).commit();
    }
    public static void remapPrefs(Context context, int oldId, int newId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        int oldInt = prefs.getInt(KEY_CLOCK_COLOR + "_" + oldId, context.getResources().getColor(R.color.binary_clock_dot_color));
        prefs.edit().putInt(KEY_CLOCK_COLOR + "_" + newId, oldInt).commit();
        boolean oldValue = prefs.getBoolean(KEY_SHOW_ALARM + "_" + oldId, false);
        prefs.edit().putBoolean(KEY_SHOW_ALARM + "_" + newId, oldValue).commit();
        oldValue = prefs.getBoolean(KEY_SHOW_DATE + "_" + oldId, false);
        prefs.edit().putBoolean(KEY_SHOW_DATE + "_" + newId, oldValue).commit();
        oldValue = prefs.getBoolean(KEY_CLOCK_SHADOW + "_" + oldId, false);
        prefs.edit().putBoolean(KEY_CLOCK_SHADOW + "_" + newId, oldValue).commit();
        String oldString = prefs.getString(KEY_CLOCK_FONT + "_" + oldId, null);
        prefs.edit().putString(KEY_CLOCK_FONT + "_" + newId, oldString).commit();

        prefs.edit().remove(KEY_CLOCK_FONT + "_" + oldId).commit();
        prefs.edit().remove(KEY_CLOCK_COLOR + "_" + oldId).commit();
        prefs.edit().remove(KEY_CLOCK_SHADOW + "_" + oldId).commit();
        prefs.edit().remove(KEY_SHOW_ALARM + "_" + oldId).commit();
        prefs.edit().remove(KEY_SHOW_DATE + "_" + oldId).commit();
    }
}
