/*
 * Copyright (C) 2013 The Android Open Source Project
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

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;

import androidx.preference.PreferenceManager;

import org.omnirom.deskclock.LogUtils;
import org.omnirom.deskclock.SettingsActivity;
import org.omnirom.deskclock.base.BaseKlaxon;
import org.omnirom.deskclock.provider.AlarmInstance;

/**
 * Manages playing ringtone and vibrating the device.
 */
public class AlarmKlaxon {

    private static boolean sStarted = false;
    private static boolean sPreAlarmMode = false;

    private static int getAudioStream(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String stream = prefs.getString(SettingsActivity.KEY_AUDIO_STREAM, "1");
        int streamInt = Integer.decode(stream).intValue();
        return streamInt == 0 ? AudioManager.STREAM_MUSIC : AudioManager.STREAM_ALARM;
    }

    public static void stop(Context context) {
        if (sStarted) {
            LogUtils.v("AlarmKlaxon.stop()");

            sStarted = false;


            sPreAlarmMode = false;

            BaseKlaxon.stop(context);
        }
    }

    public static void start(final Context context, AlarmInstance instance) {

        // Make sure we are stop before starting
        stop(context);

        LogUtils.v("AlarmKlaxon.start() " + instance);

        sPreAlarmMode = false;
        if (instance.mAlarmState == AlarmInstance.PRE_ALARM_STATE) {
            sPreAlarmMode = true;
        }

        sStarted = true;

        Uri alarmNoise = null;
        if (sPreAlarmMode) {
            alarmNoise = instance.mPreAlarmRingtone;
        } else {
            alarmNoise = instance.mRingtone;
        }
        boolean vibrate = instance.mVibrate;

        BaseKlaxon.start(context, getAudioStream(context), instance.getIncreasingVolume(sPreAlarmMode), getVolumeChangeDelay(context), instance.getRandomMode(sPreAlarmMode), sPreAlarmMode ? instance.mPreAlarmVolume : instance.mAlarmVolume, alarmNoise,vibrate);
    }

    private static long getVolumeChangeDelay(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String speed = prefs.getString(SettingsActivity.KEY_VOLUME_INCREASE_SPEED, "5");
        int speedInt = Integer.decode(speed).intValue();
        return speedInt * 1000;
    }
}
