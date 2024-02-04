/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.omnirom.deskclock.timer.TimerView;

import java.util.Locale;


public class TimerSetupView extends LinearLayout implements Button.OnClickListener,
        Button.OnLongClickListener{

    protected int mInputSize = 6;

    protected final Button mNumbers [] = new Button [11];
    protected int mInput [] = new int [mInputSize];
    protected int mInputPointer = -1;

    protected ImageView mStart;
    protected Button mDelete;
    protected TextView mEnteredTime;
    protected final Context mContext;

    private final AnimatorListenerAdapter mHideFabAnimatorListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            if (mStart != null) {
                mStart.setScaleX(1.0f);
                mStart.setScaleY(1.0f);
                mStart.setVisibility(View.GONE);
            }
        }
    };

    private final AnimatorListenerAdapter mShowFabAnimatorListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationStart(Animator animation) {
            if (mStart != null) {
                mStart.setVisibility(View.VISIBLE);
            }
        }
    };

    public TimerSetupView(Context context) {
        this(context, null);
    }

    public TimerSetupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater layoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.time_setup_view, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mEnteredTime = findViewById(R.id.timer_time_text);
        mDelete = findViewById(R.id.timer_setup_delete);
        mDelete.setOnClickListener(this);
        mDelete.setOnLongClickListener(this);

        mNumbers[1] = findViewById(R.id.timer_setup_digit_1);
        mNumbers[2] = findViewById(R.id.timer_setup_digit_2);
        mNumbers[3] = findViewById(R.id.timer_setup_digit_3);

        mNumbers[4] = findViewById(R.id.timer_setup_digit_4);
        mNumbers[5] = findViewById(R.id.timer_setup_digit_5);
        mNumbers[6] = findViewById(R.id.timer_setup_digit_6);

        mNumbers[7] = findViewById(R.id.timer_setup_digit_7);
        mNumbers[8] = findViewById(R.id.timer_setup_digit_8);
        mNumbers[9] = findViewById(R.id.timer_setup_digit_9);

        mNumbers[0] = findViewById(R.id.timer_setup_digit_0);
        mNumbers[10] = findViewById(R.id.timer_setup_digit_00);

        for (int i = 0; i < 11; i++) {
            mNumbers[i].setOnClickListener(this);
            mNumbers[i].setText(i == 10 ? String.format(
                    Locale.getDefault(), "" + "%0" + 2 + "d", 0) :
                    String.format("%d", i));
            mNumbers[i].setTag(R.id.numbers_key, new Integer(i));
        }
        updateTime();
    }

    public void registerStartButton(ImageView start) {
        mStart = start;
        initializeStartButtonVisibility();
    }

    private void initializeStartButtonVisibility() {
        if (mStart != null) {
            updateStartButton();
            //mStart.setVisibility(isInputHasValue() ? View.VISIBLE : View.GONE);
        }
    }

    private void updateStartButton() {
        setFabButtonVisibility(isInputHasValue() /* show or hide */);
    }

    public void updateDeleteButtonAndDivider() {
        final boolean enabled = isInputHasValue();
        if (mDelete != null) {
            mDelete.setEnabled(enabled);
        }
    }

    private boolean isInputHasValue() {
        return mInputPointer != -1;
    }

    private void setFabButtonVisibility(boolean show) {
        final int finalVisibility = show ? View.VISIBLE : View.GONE;
        if (mStart == null || mStart.getVisibility() == finalVisibility) {
            // Fab is not initialized yet or already shown/hidden
            return;
        }

        final Animator scaleAnimator = AnimatorUtils.getScaleAnimator(
                mStart, show ? 0.0f : 1.0f, show ? 1.0f : 0.0f);
        scaleAnimator.setDuration(AnimatorUtils.ANIM_DURATION_SHORT);
        scaleAnimator.addListener(show ? mShowFabAnimatorListener : mHideFabAnimatorListener);
        scaleAnimator.start();
    }

    @Override
    public void onClick(View v) {
        doOnClick(v);
        updateStartButton();
        updateDeleteButtonAndDivider();
    }

    private void append(int digit) {
        // pressing "0" as the first digit does nothing
        if (mInputPointer == -1 && digit == 0) {
            return;
        }
        if (mInputPointer < mInputSize - 1) {
            for (int i = mInputPointer; i >= 0; i--) {
                mInput[i+1] = mInput[i];
            }
            mInputPointer++;
            mInput [0] = digit;
            updateTime();
        }
    }

    protected void doOnClick(View v) {

        Integer val = (Integer) v.getTag(R.id.numbers_key);
        // A number was pressed
        if (val != null) {
            if (val == 10) {
                append(0);
                append(0);
            } else {
                append(val);
            }
            return;
        }

        // other keys
        if (v == mDelete) {
            if (mInputPointer >= 0) {
                for (int i = 0; i < mInputPointer; i++) {
                    mInput[i] = mInput[i + 1];
                }
                mInput[mInputPointer] = 0;
                mInputPointer--;
                updateTime();
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v == mDelete) {
            reset();
            updateStartButton();
            updateDeleteButtonAndDivider();
            return true;
        }
        return false;
    }

    protected void updateTime() {
        mEnteredTime.setText(mInput[5] + "" + mInput[4] +
                mContext.getString(R.string.hours_label_new) +
                mInput[3] + mInput[2] +
                mContext.getString(R.string.minutes_label_new) +
                mInput[1] + mInput[0]);
    }

    public void reset() {
        for (int i = 0; i < mInputSize; i ++) {
            mInput[i] = 0;
        }
        mInputPointer = -1;
        updateTime();
    }

    public int getTime() {
        return mInput[5] * 36000 + mInput[4] * 3600 + mInput[3] * 600 + mInput[2] * 60 + mInput[1] * 10 + mInput[0];
    }

    public void saveEntryState(Bundle outState, String key) {
        outState.putIntArray(key, mInput);
    }

    public void restoreEntryState(Bundle inState, String key) {
        int[] input = inState.getIntArray(key);
        if (input != null && mInputSize == input.length) {
            for (int i = 0; i < mInputSize; i++) {
                mInput[i] = input[i];
                if (mInput[i] != 0) {
                    mInputPointer = i;
                }
            }
            updateTime();
        }
        initializeStartButtonVisibility();
    }



}
