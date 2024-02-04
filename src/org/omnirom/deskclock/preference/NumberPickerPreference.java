/*
 * Based on: http://www.lukehorvat.com/blog/android-numberpickerdialogpreference/
 * Thanks to the original author!
 */

package org.omnirom.deskclock.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;

import org.omnirom.deskclock.R;
/**
* A {@link Preference} that provides a user with the means to select an integer from a {@link NumberPicker}, and persist it.
*
* @author lukehorvat
*
*/
public class NumberPickerPreference extends Preference
{
    protected static final int DEFAULT_MIN_VALUE = 0;
    protected static final int DEFAULT_MAX_VALUE = 100;
    protected static final int DEFAULT_VALUE = 0;

    protected int mMinValue;
    protected int mMaxValue;
    protected int mValue;
    protected NumberPicker mNumberPicker;

    public Context mContext;
    public AlertDialog mDialog;

    public NumberPickerPreference(Context context) {
        this(context, null);
    }

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        setMinValue(DEFAULT_MIN_VALUE);
        setMaxValue(DEFAULT_MAX_VALUE);
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        setValue(restore ? getPersistedInt(DEFAULT_VALUE) : (Integer) defaultValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, DEFAULT_VALUE);
    }

    @Override
    public void onClick() {
        if (mDialog == null) {
            mDialog = new AlertDialog.Builder(mContext)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> onDialogClosed(true))
                    .setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> onDialogClosed(false))
                    .setView(createDialogView(null))
                    .create();
        } if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }

    public View createDialogView(View view) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.preference_number_picker, null);
        }

        mNumberPicker = (NumberPicker) view.findViewById(R.id.number_picker);
        mNumberPicker.setMinValue(mMinValue);
        mNumberPicker.setMaxValue(mMaxValue);
        mNumberPicker.setValue(mValue);
        return view;
    }

    public int getMinValue() {
        return mMinValue;
    }

    public void setMinValue(int minValue) {
        mMinValue = minValue;
        setValue(Math.max(mValue, mMinValue));
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public void setMaxValue(int maxValue) {
        mMaxValue = maxValue;
        setValue(Math.min(mValue, mMaxValue));
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        value = Math.max(Math.min(value, mMaxValue), mMinValue);

        if (value != mValue) {
            mValue = value;
            persistInt(value);
            notifyChanged();
        }
    }

    protected int getSelectedValue() {
        return mNumberPicker.getValue();
    }

    protected void onDialogClosed(boolean positiveResult) {

        // when the user selects "OK", persist the new value
        if (positiveResult) {
            int value = getSelectedValue();
            if (callChangeListener(value)) {
                setValue(value);
            }
        }
    }
}
