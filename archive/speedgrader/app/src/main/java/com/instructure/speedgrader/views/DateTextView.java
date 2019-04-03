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
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.instructure.speedgrader.R;
import com.instructure.speedgrader.dialogs.DatePickerFragment;
import com.instructure.speedgrader.util.ViewUtils;

import java.io.Serializable;
import java.text.Format;
import java.util.Date;

public class DateTextView extends TextView implements Serializable {

    private Format simpleDateFormat;
    private Date date;
    private boolean shouldModifyTime = false;
    private String emptyDateText;

    private int color;

    Drawable drawable;

    boolean isEnabled = true;

    public DateTextView(Context context) {
        super(context);
        init(context, null);
    }

    public DateTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DateTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public void shouldModifyTime(boolean modifyTime){
        this.shouldModifyTime = modifyTime;
    }


    public void init(Context context, AttributeSet attrs){
        setGravity(Gravity.CENTER);

        setCompoundDrawablePadding((int) ViewUtils.convertDipsToPixels(10, context));
        this.setClickable(true);



        if(context != null && attrs != null){
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.DateTextView,
                    0, 0);

            try {
                color = a.getColor(R.styleable.DateTextView_drawableColor, -1);
                this.emptyDateText = a.getString(R.styleable.DateTextView_emptyDateText);
            } finally {
                a.recycle();
            }


        }
        drawable =context.getResources().getDrawable(R.drawable.ic_cv_calendar);

        invalidateColor();

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                 if(isEnabled){
                    DatePickerFragment datePickerDialog = DatePickerFragment.getInstance(DateTextView.this, shouldModifyTime);
                    datePickerDialog.show((((FragmentActivity)getContext()).getSupportFragmentManager()), DatePickerFragment.tag);
                }
            }
        });
    }


    public void setDate(Date date){
        this.date = date;
        invalidate();
    }

    public void setColor(int color){
        this.color = color;
        invalidateColor();
    }

    public void invalidateColor(){
        if(color != -1) {
            PorterDuff.Mode mode = PorterDuff.Mode.SRC_ATOP;
            drawable = drawable.mutate();
            drawable.setColorFilter(color, mode);
        }
        setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
    }

    public Date getDate(){
        return date;
    }
    public String getEmptyDateText(){
        if(emptyDateText != null){
            return  emptyDateText;
        }

        return getContext().getString(R.string.dateTextDefault);
    }

    public void setSimpleDateFormat(Format simpleDateFormat){
        this.simpleDateFormat = simpleDateFormat;
        invalidate();
    }

    public void invalidate(){
        if(simpleDateFormat == null){
            return;
        } else if(date == null){
            setText(getEmptyDateText());

            return;
        }
        setText(simpleDateFormat.format(date));
    }

    public boolean isEnabled(){
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled){
        this.isEnabled = isEnabled;
        setColor(getResources().getColor(R.color.dividerColor));
        invalidate();
    }
}
