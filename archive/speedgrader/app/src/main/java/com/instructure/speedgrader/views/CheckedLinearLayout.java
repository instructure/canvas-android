/*
 * Copyright (C) 2016 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.speedgrader.views;

import android.content.Context;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.instructure.speedgrader.R;

public class CheckedLinearLayout extends LinearLayout {

    private TextView leftIndicator;
    private TextView rightIndicator;
    private boolean isLeftChecked = true;
    private int leftTextColorActive = Color.parseColor("#bebeb9");
    private int leftTextColorPassive = Color.WHITE;
    private int rightTextColorActive = Color.parseColor("#bebeb9");
    private int rightTextColorPassive = Color.WHITE;

    private OnSwitchListener mCallbacks;

    public interface OnSwitchListener {
        public void onSwitch(boolean isLeftChecked);
    }

    public CheckedLinearLayout(Context context) {
        super(context);
        setupViews();
    }

    public CheckedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupViews();
    }

    public CheckedLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupViews();
    }

    public void setLeftAsChecked(boolean isChecked) {
        if (isChecked) {
            leftIndicator.setBackgroundResource(R.drawable.left_indicator);
            leftIndicator.setTextColor(leftTextColorActive);
            rightIndicator.setTextColor(rightTextColorPassive);
            rightIndicator.setBackgroundColor(Color.TRANSPARENT);
        } else {
            rightIndicator.setBackgroundResource(R.drawable.right_indicator);
            rightIndicator.setTextColor(rightTextColorActive);
            leftIndicator.setTextColor(leftTextColorPassive);
            leftIndicator.setBackgroundColor(Color.TRANSPARENT);
        }
        isLeftChecked = isChecked;
    }

    public void setCallbacks(OnSwitchListener callbacks) {
        mCallbacks = callbacks;
    }

    public void setLeftIndicatorText(String text) {
        leftIndicator.setText(text);
    }

    public void setRightIndicatorText(String text) {
        rightIndicator.setText(text);
    }

    public void setLeftIndicatorTextColorActive(int color) {
        leftTextColorActive = color;
    }

    public void setRightIndicatorTextColorActive(int color) {
        rightTextColorActive = color;
    }

    public void setLeftIndicatorTextColorPassive(int color) {
        leftTextColorPassive = color;
    }

    public void setRightIndicatorTextColorPassive(int color) {
        rightTextColorPassive = color;
    }

    private void setupViews() {
        setBackgroundResource(R.drawable.switch_container_bg);
        setOrientation(HORIZONTAL);
        setClickable(true);
        setOnClickListener(mSwitchListener);

        leftIndicator = new TextView(getContext());
        rightIndicator = new TextView(getContext());
        setupTextView(leftIndicator);
        setupTextView(rightIndicator);
    }

    private void setupTextView(TextView t) {
        t.setSingleLine(true);
        t.setMaxLines(1);
        t.setEllipsize(TextUtils.TruncateAt.END);
        t.setLines(1);
        t.setGravity(Gravity.CENTER);
        t.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1F));
        addView(t);
    }

    private OnClickListener mSwitchListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            setLeftAsChecked(!isLeftChecked);
            if (mCallbacks != null) {
                mCallbacks.onSwitch(isLeftChecked);
            }
        }
    };

    private void restoreState(
            String leftText, String rightText,
            int leftTextColorActive, int leftTextColorPassive,
            int rightTextColorActive, int rightTextColorPassive,
            boolean isLeftChecked) {

        this.leftTextColorActive = leftTextColorActive;
        this.leftTextColorPassive = leftTextColorPassive;
        this.rightTextColorActive = rightTextColorActive;
        this.rightTextColorPassive = rightTextColorPassive;

        leftIndicator.setText(leftText);
        rightIndicator.setText(rightText);
        this.isLeftChecked = isLeftChecked;
        setLeftAsChecked(isLeftChecked);
    }


//SAVE STATE

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);

        ss.leftText = leftIndicator.getText().toString();
        ss.rightText = rightIndicator.getText().toString();
        ss.leftTextColorActive = leftTextColorActive;
        ss.leftTextColorPassive = leftTextColorPassive;
        ss.rightTextColorActive = rightTextColorActive;
        ss.rightTextColorPassive = rightTextColorPassive;
        ss.isLeftChecked = isLeftChecked;

        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        //begin boilerplate code so parent classes can restore state
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        restoreState(ss.leftText, ss.rightText,
                ss.leftTextColorActive, ss.leftTextColorPassive,
                ss.rightTextColorActive, ss.rightTextColorPassive,
                ss.isLeftChecked);
    }

    static class SavedState extends BaseSavedState {

        String leftText = "";
        String rightText = "";
        int leftTextColorActive = Color.parseColor("#bebeb9");
        int leftTextColorPassive = Color.WHITE;
        int rightTextColorActive = Color.parseColor("#bebeb9");
        int rightTextColorPassive = Color.WHITE;
        boolean isLeftChecked = true;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);

            this.leftText = in.readString();
            this.rightText = in.readString();
            this.leftTextColorActive = in.readInt();
            this.leftTextColorPassive = in.readInt();
            this.rightTextColorActive = in.readInt();
            this.rightTextColorPassive = in.readInt();
            this.isLeftChecked = (in.readInt() == 1);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);

            out.writeString(this.leftText);
            out.writeString(this.rightText);
            out.writeInt(this.leftTextColorActive);
            out.writeInt(this.leftTextColorPassive);
            out.writeInt(this.rightTextColorActive);
            out.writeInt(this.rightTextColorPassive);
            out.writeInt(this.isLeftChecked == true ? 1 : 0);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}