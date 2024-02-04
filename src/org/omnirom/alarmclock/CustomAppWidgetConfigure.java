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
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import org.omnirom.deskclock.R;
import org.omnirom.deskclock.Utils;
import org.omnirom.deskclock.preference.FontPreference;
import org.omnirom.deskclock.preference.ColorPickerPreference;

public class CustomAppWidgetConfigure extends AppCompatActivity {

    public static final String KEY_SHOW_ALARM = "show_alarm";
    public static final String KEY_SHOW_DATE = "show_date";
    public static final String KEY_CLOCK_FONT = "clock_font";
    public static final String KEY_CLOCK_COLOR = "clock_color";
    public static final String KEY_CLOCK_SHADOW = "clock_shadow";
    public static final String KEY_WORLD_CLOCKS = "world_clocks";

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private PrefsFrag f;
    public void handleOkClick(View v) {
        f.handleOkClick(v);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(Utils.getThemeResourceId(this));

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

        private ColorPickerPreference mClockColor;

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {

            addPreferencesFromResource(R.xml.custom_appwidget_configure);
            initPreference(KEY_SHOW_ALARM);
            initPreference(KEY_SHOW_DATE);
            initPreference(KEY_CLOCK_SHADOW);
            initPreference(KEY_WORLD_CLOCKS);

            mClockFont = (FontPreference) findPreference(KEY_CLOCK_FONT);
            mClockFont.setKey(KEY_CLOCK_FONT + "_" + String.valueOf(mAppWidgetId));
            mClockFont.setSummary("Roboto Light");
            mClockFont.setOnPreferenceChangeListener(this);

            mClockColor = (ColorPickerPreference) findPreference(KEY_CLOCK_COLOR);
            mClockColor.setKey(KEY_CLOCK_COLOR + "_" + String.valueOf(mAppWidgetId));
            mClockColor.setColor(Color.WHITE);
            String hexColor = String.format("#%08X", Color.WHITE);
            mClockColor.setSummary(hexColor);
            mClockColor.setOnPreferenceChangeListener(this);


        }

        public void handleOkClick(View v) {
            CustomAppWidgetProvider.updateAfterConfigure(getActivity(), mAppWidgetId);
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
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            prefs.edit().putBoolean(b.getKey(), true).commit();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference == mClockFont) {
                String value = (String) newValue;
                int valueIndex = mClockFont.findIndexOfValue(value);
                mClockFont.setSummary(mClockFont.getEntries()[valueIndex]);
            } else if (preference == mClockColor) {
                String hexColor = String.format("#%08X", mClockColor.getColor());
                mClockColor.setSummary(hexColor);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                prefs.edit().putInt(mClockColor.getKey(), mClockColor.getColor()).commit();
            }
            return true;
        }
    }


    public static void clearPrefs(Context context, int id) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().remove(KEY_SHOW_ALARM + "_" + id).commit();
        prefs.edit().remove(KEY_SHOW_DATE + "_" + id).commit();
        prefs.edit().remove(KEY_CLOCK_FONT + "_" + id).commit();
        prefs.edit().remove(KEY_CLOCK_COLOR + "_" + id).commit();
        prefs.edit().remove(KEY_CLOCK_SHADOW + "_" + id).commit();
        prefs.edit().remove(KEY_WORLD_CLOCKS + "_" + id).commit();
    }


    public static void remapPrefs(Context context, int oldId, int newId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        boolean oldValue = prefs.getBoolean(KEY_SHOW_ALARM + "_" + oldId, false);
        prefs.edit().putBoolean(KEY_SHOW_ALARM + "_" + newId, oldValue).commit();
        oldValue = prefs.getBoolean(KEY_SHOW_DATE + "_" + oldId, false);
        prefs.edit().putBoolean(KEY_SHOW_DATE + "_" + newId, oldValue).commit();
        oldValue = prefs.getBoolean(KEY_WORLD_CLOCKS + "_" + oldId, false);
        prefs.edit().putBoolean(KEY_WORLD_CLOCKS + "_" + newId, oldValue).commit();
        oldValue = prefs.getBoolean(KEY_CLOCK_SHADOW + "_" + oldId, false);
        prefs.edit().putBoolean(KEY_CLOCK_SHADOW + "_" + newId, oldValue).commit();
        String oldString = prefs.getString(KEY_CLOCK_FONT + "_" + oldId, null);
        prefs.edit().putString(KEY_CLOCK_FONT + "_" + newId, oldString).commit();
        int oldInt = prefs.getInt(KEY_CLOCK_COLOR + "_" + oldId, Color.WHITE);
        prefs.edit().putInt(KEY_CLOCK_COLOR + "_" + newId, oldInt).commit();

        prefs.edit().remove(KEY_SHOW_ALARM + "_" + oldId).commit();
        prefs.edit().remove(KEY_SHOW_DATE + "_" + oldId).commit();
        prefs.edit().remove(KEY_CLOCK_FONT + "_" + oldId).commit();
        prefs.edit().remove(KEY_CLOCK_COLOR + "_" + oldId).commit();
        prefs.edit().remove(KEY_CLOCK_SHADOW + "_" + oldId).commit();
        prefs.edit().remove(KEY_WORLD_CLOCKS + "_" + oldId).commit();
    }
}
