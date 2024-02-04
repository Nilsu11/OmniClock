/*
 * Copyright (C) 2014 The Android Open Source Project
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
package org.omnirom.deskclock.alarms;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.preference.PreferenceManager;

import org.omnirom.deskclock.AlarmUtils;
import org.omnirom.deskclock.LogUtils;
import org.omnirom.deskclock.SettingsActivity;
import org.omnirom.deskclock.base.BaseAlarmActivity;
import org.omnirom.deskclock.provider.AlarmInstance;

public class AlarmActivity extends BaseAlarmActivity {

    private static final String LOGTAG = AlarmActivity.class.getSimpleName();

    private AlarmInstance mAlarmInstance;
    private long mInstanceId;
    private boolean mPreAlarmMode;

    @Override
    public void snooze() {
        if (!AlarmStateManager.canSnooze(this)) {
            return;
        }
        LogUtils.v(LOGTAG, "Snoozed: " + mAlarmInstance);
        AlarmStateManager.setSnoozeState(this, mAlarmInstance, false /* showToast */);
        super.snooze();
    }

    @Override
    public void dismiss() {
        LogUtils.v(LOGTAG, "Dismissed: " + mAlarmInstance);

        try {
            AlarmStateManager.setDismissState(this, mAlarmInstance);
        } catch (Exception e) {
        }
        super.dismiss();
    }

    @Override
    public void getButtonBehavior(SharedPreferences prefs) {
        // Get the volume/camera button behavior setting
        mVolumeBehavior = prefs.getString(SettingsActivity.KEY_VOLUME_BEHAVIOR,
                SettingsActivity.DEFAULT_ALARM_ACTION);

        mFlipAction = prefs.getString(SettingsActivity.KEY_FLIP_ACTION,
                SettingsActivity.DEFAULT_ALARM_ACTION);

        mShakeAction = prefs.getString(SettingsActivity.KEY_SHAKE_ACTION,
                SettingsActivity.DEFAULT_ALARM_ACTION);

        mWaveAction = prefs.getString(SettingsActivity.KEY_WAVE_ACTION,
                SettingsActivity.DEFAULT_ALARM_ACTION);

        LogUtils.v(LOGTAG, "mVolumeBehavior: " + mVolumeBehavior + " mFlipAction " + mFlipAction  +
                " mShakeAction " + mShakeAction + " mWaveAction " + mWaveAction);
    }

    @Override
    public void beforeCreate() {
        mInstanceId = AlarmInstance.getId(getIntent().getData());
        mAlarmInstance = AlarmInstance.getInstance(this.getContentResolver(), mInstanceId);
        if (mAlarmInstance != null) {
            LogUtils.v(LOGTAG, "Displaying alarm for instance: " + mAlarmInstance);
        } else {
            // The alarm got deleted before the activity got created, so just finish()
            LogUtils.e(LOGTAG, "Error displaying alarm for intent:" + getIntent());
            finish();
            return;
        }

        mPreAlarmMode = mAlarmInstance.mAlarmState == AlarmInstance.PRE_ALARM_STATE;
        LogUtils.v(LOGTAG, "Pre-alarm mode: " + mPreAlarmMode);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        keepScreenOn = prefs.getBoolean(SettingsActivity.KEY_KEEP_SCREEN_ON, true);
        makeScreenDark = prefs.getBoolean(SettingsActivity.KEY_MAKE_SCREEN_DARK, false);
        String alarmName = null;
        if (mPreAlarmMode) {
            alarmName = mAlarmInstance.getPreAlarmRingtoneName();
        } else {
            alarmName = mAlarmInstance.getRingtoneName();
        }
        mMediaInfo = alarmName;

        mTitle = AlarmUtils.getAlarmTitle(this, mAlarmInstance);
    }

    @Override
    public void handleSnoozeButton() {
        if (!AlarmStateManager.canSnooze(this)) {
            mSnoozeButton.setVisibility(View.GONE);
        } else {
            mSnoozeButton.setVisibility(View.VISIBLE);
        }
    }
}
