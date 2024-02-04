/*
 * Copyright (C) 2012 The Android Open Source Project
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

package org.omnirom.deskclock.timer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.omnirom.deskclock.CircleTimerView;
import org.omnirom.deskclock.R;
import org.omnirom.deskclock.Utils;


public class TimerListItem extends LinearLayout {

    CountingTimerView mTimerText;
    CircleTimerView mCircleView;
    ImageView mResetButton;
    ImageButton mFab;
    ImageButton mAdd;

    long mTimerLength;



    public TimerListItem(Context context) {
        this(context, null);
    }

//    public void TimerListItem newInstance(Context context) {
//        final LayoutInflater layoutInflater =
//                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        layoutInflater.inflate(R.layout.timer_list_item, this);
//    }

    public TimerListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTimerText = (CountingTimerView) findViewById(org.omnirom.deskclock.R.id.timer_time_text);
        mCircleView = (CircleTimerView) findViewById(org.omnirom.deskclock.R.id.timer_time);
        mFab = findViewById(org.omnirom.deskclock.R.id.fab);
        mCircleView.setBackgroundResource(Utils.getCircleViewBackgroundResourceId(getContext()));
        mCircleView.setTimerMode(true);
        mResetButton = (ImageView) findViewById(org.omnirom.deskclock.R.id.reset_add);
        findViewById(R.id.alarm_item_card).setBackgroundColor(Utils.getViewBackgroundColor(getContext()));
    }

    public void set(long timerLength, long timeLeft, boolean drawRed) {
        if (mCircleView == null) {
            mCircleView = findViewById(R.id.timer_time);
            mCircleView.setTimerMode(true);
        }
        mTimerLength = timerLength;
        mCircleView.setIntervalTime(mTimerLength);
        mCircleView.setPassedTime(timerLength - timeLeft, drawRed);
        invalidate();
    }

    public void start() {
        mFab.setImageResource(R.drawable.ic_fab_pause);
        mCircleView.startIntervalAnimation();
        mTimerText.showTime(true);
        mCircleView.setVisibility(VISIBLE);
    }

    public void pause() {
        mFab.setImageResource(R.drawable.ic_fab_play);
        mCircleView.pauseIntervalAnimation();
        mTimerText.showTime(true);
        mCircleView.setVisibility(VISIBLE);
    }

    public void stop() {
        mCircleView.stopIntervalAnimation();
        mTimerText.showTime(true);
        mCircleView.setVisibility(VISIBLE);
    }

    public void timesUp() {
        mCircleView.abortIntervalAnimation();
    }

    public void done() {
        mCircleView.stopIntervalAnimation();
        mCircleView.setVisibility(VISIBLE);
        mCircleView.invalidate();
    }

    public void setLength(long timerLength) {
        mTimerLength = timerLength;
        mCircleView.setIntervalTime(mTimerLength);
        mCircleView.invalidate();
    }

    public void setTextBlink(boolean blink) {
        mTimerText.showTime(!blink);
    }

    public void setCircleBlink(boolean blink) {
        mCircleView.setVisibility(blink ? INVISIBLE : VISIBLE);
    }

    public void setResetButton(OnClickListener listener) {
        if (mResetButton == null) {
            mResetButton = (ImageView) findViewById(org.omnirom.deskclock.R.id.reset_add);
        }

        mResetButton.setImageResource(((Utils.isLightTheme(getContext()) ?
                org.omnirom.deskclock.R.drawable.ic_reset_black : org.omnirom.deskclock.R.drawable.ic_reset)));
        mResetButton.setContentDescription(getResources().getString(org.omnirom.deskclock.R.string.timer_reset));
        mResetButton.setOnClickListener(listener);
    }
    public void setAddButton(OnClickListener listener) {
        if (mAdd == null) {
            mAdd = findViewById(R.id.add);
        }

        mAdd.setImageResource(((Utils.isLightTheme(getContext()) ?
                R.drawable.ic_plusone_black : R.drawable.ic_plusone)));
        mAdd.setContentDescription(getResources().getString(org.omnirom.deskclock.R.string.timer_reset));
        mAdd.setOnClickListener(listener);
    }

    public void setFabButton(boolean isRunning, OnClickListener listener) {
        if (mFab == null) {
            mFab = findViewById(R.id.add);
        }

        mFab.setImageResource(isRunning ? R.drawable.ic_fab_pause : R.drawable.ic_fab_play);
        mFab.setContentDescription(getResources().getString(org.omnirom.deskclock.R.string.timer_reset));
        mFab.setOnClickListener(listener);
    }

    public void setTime(long time, boolean forceUpdate) {
        if (mTimerText == null) {
            mTimerText = (CountingTimerView) findViewById(org.omnirom.deskclock.R.id.timer_time_text);
        }
        mTimerText.setTime(time, false, forceUpdate);
    }

    // Used by animator to animate the size of a timer
    @SuppressWarnings("unused")
    public void setAnimatedHeight(int height) {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams != null) {
            layoutParams.height = height;
            requestLayout();
        }
    }

    public void registerVirtualButtonAction(final Runnable runnable) {
        mTimerText.registerVirtualButtonAction(runnable);
    }
}
