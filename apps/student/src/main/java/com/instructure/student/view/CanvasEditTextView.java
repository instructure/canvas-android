/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.appcompat.widget.AppCompatEditText;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.instructure.student.R;
import com.instructure.pandautils.utils.ColorKeeper;

/**
 * By Default, this will appear as a single line edit text with:
 *
 * -Left image/layout hidden (attach icon)
 * -Right image/layout shown (check box)
 *
 * By calling the various setters...
 *
 * The images may be changed:
 * -shown
 * -hidden
 * -icon changed
 *
 * The EditText may be:
 * -set
 * -listened
 * -add various text watchers for hiding/showing images
 *
 */
public class CanvasEditTextView extends RelativeLayout {

    public interface CanvasEditTextViewRightListener { void onRightButtonClicked(); }
    public interface CanvasEditTextViewLeftListener{ void onLeftButtonClicked(); }

    private CanvasEditTextViewRightListener mRightListener;
    private CanvasEditTextViewLeftListener  mLeftListener;

    private RelativeLayout mRightButtonLayout;
    private RelativeLayout mLeftButtonLayout;
    private IndicatorCircleView mLeftButtonIndicator;
    private ImageView mRightImage;
    private ProgressBar mRightProgressBar;
    private ImageView mLeftImage;
    private EditText mEditText;

    private Paint mBorderPaint;

    private boolean mShowTopBorder;
    private String mHintText;
    private float mTextSize;
    private int mColorTint;
    private int mMaxLines;
    private int mDrawableSize;

    public CanvasEditTextView(Context context) {
        this(context, null, 0);
    }

    public CanvasEditTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CanvasEditTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CanvasEditText, 0, 0);

        try {
            mColorTint = a.getColor(R.styleable.CanvasEditText_tintColor, context.getResources().getColor(R.color.defaultTextGray));
            mHintText = a.getString(R.styleable.CanvasEditText_hintText);
            mTextSize = a.getDimensionPixelSize(R.styleable.CanvasEditText_textSize, (int) ViewUtils.convertDipsToPixels(14, context));
            mShowTopBorder = a.getBoolean(R.styleable.CanvasEditText_hasTopBorder, false);
            mMaxLines = a.getInt(R.styleable.CanvasEditText_maxLines, 8);
            mDrawableSize = a.getDimensionPixelSize(R.styleable.CanvasEditText_buttonDrawableSize, (int) ViewUtils.convertDipsToPixels(36, context));
        } finally {
            a.recycle();
        }

        if(context instanceof CanvasEditTextViewRightListener){
            mRightListener = ((CanvasEditTextViewRightListener)context);
        }

        if(context instanceof CanvasEditTextViewLeftListener){
            mLeftListener = ((CanvasEditTextViewLeftListener)context);
        }

        if(mShowTopBorder){
            mBorderPaint = new Paint();
            mBorderPaint.setColor(context.getResources().getColor(R.color.dividerColor));
            mBorderPaint.setStyle(Paint.Style.STROKE);
            mBorderPaint.setStrokeWidth(ViewUtils.convertDipsToPixels(1, context));
        }

        configureViews(context);
    }

    private void configureViews(Context context){

        mEditText = initEditText(context, mHintText, mColorTint, mTextSize, mMaxLines);

        mLeftImage  =  initLeftImage(context, mDrawableSize);
        mRightImage = initRightButton(context, mDrawableSize);
        mRightProgressBar = initRightProgress(context, mDrawableSize);

        mRightButtonLayout = initRightButtonLayout(context, mRightImage, mRightProgressBar);
        mLeftButtonLayout  = initLeftButtonLayout (context, mLeftImage);
        mLeftButtonIndicator = initLeftButtonIndicator(context);

        addView(mEditText);
        addView(mRightButtonLayout);
        addView(mLeftButtonLayout);
        addView(mLeftButtonIndicator);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(mShowTopBorder && mBorderPaint != null){
            canvas.drawLine(0, 0, getWidth(), 0, mBorderPaint);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //region View Layout Helpers
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public static EditText initEditText(Context context, String hint, int hintColor, float textSize, int maxLines){
        final AppCompatEditText editText = new AppCompatEditText(context);
        editText.setId(R.id.canvasEditText);
        editText.setEnabled(true);
        editText.setHint(hint);
        editText.setHintTextColor(hintColor);
        editText.setSingleLine(false);
        editText.setMaxLines(maxLines);
        editText.setVerticalScrollBarEnabled(true);
        editText.setTextColor(context.getResources().getColor(R.color.canvasTextDark));
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        editText.setRawInputType(editText.getInputType() | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        final int padding = context.getResources().getDimensionPixelOffset(R.dimen.canvas_edit_text_view_padding);
        editText.setPadding(padding, 0, padding, 0);

        ColorDrawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
        editText.setBackgroundDrawable(colorDrawable);

        RelativeLayout.LayoutParams editTextParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        editTextParams.addRule(RelativeLayout.END_OF, R.id.leftLayout);
        editTextParams.addRule(RelativeLayout.START_OF, R.id.rightLayout);
        editTextParams.addRule(RelativeLayout.CENTER_VERTICAL);
        editText.setLayoutParams(editTextParams);

        return editText;
    }

    public IndicatorCircleView initLeftButtonIndicator(final Context context) {
        int size = (int) context.getResources().getDimension(R.dimen.conversation_attachment_indicator_size);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(size, size);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        layoutParams.addRule(RelativeLayout.ABOVE);
        IndicatorCircleView indicatorCircleView = new IndicatorCircleView(context);
        indicatorCircleView.setLayoutParams(layoutParams);
        indicatorCircleView.setBackgroundColor(context.getResources().getColor(R.color.canvasRed));
        indicatorCircleView.setVisibility(GONE);
        return indicatorCircleView;
    }

    public RelativeLayout initRightButtonLayout(final Context context, ImageView rightImage, ProgressBar progressBar){
        RelativeLayout layout = new RelativeLayout(context);
        RelativeLayout.LayoutParams rightButtonLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rightButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        rightButtonLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);

        layout.setId(R.id.rightLayout);
        layout.setBackground(getSelectionIndicator(context));
        layout.setLayoutParams(rightButtonLayoutParams);

        layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRightListener != null) {
                    mRightListener.onRightButtonClicked();
                }
            }
        });

        layout.addView(rightImage);
        layout.addView(progressBar);

        return layout;
    }

    public RelativeLayout initLeftButtonLayout(final Context context, ImageView leftImage){
        RelativeLayout layout = new RelativeLayout(context);

        RelativeLayout.LayoutParams leftButtonParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        leftButtonParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        leftButtonParams.addRule(RelativeLayout.CENTER_VERTICAL);

        layout.setId(R.id.leftLayout);
        layout.setBackground(getSelectionIndicator(context));
        layout.setLayoutParams(leftButtonParams);
        layout.setVisibility(VISIBLE);
        layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLeftListener != null) {
                    mLeftListener.onLeftButtonClicked();
                }
            }
        });

        layout.addView(leftImage);

        // left button is set to invisible by default.
        layout.setVisibility(View.GONE);

        return layout;
    }

    public static ImageView initLeftImage(Context context, int drawableSize){
        ImageView imageView = new ImageView(context);

        int margins = context.getResources().getDimensionPixelOffset(R.dimen.canvasEditText_margin);

        RelativeLayout.LayoutParams attachImageLayoutParams = new LayoutParams(drawableSize, drawableSize);
        attachImageLayoutParams.addRule(CENTER_VERTICAL);
        attachImageLayoutParams.setMargins(margins, margins, margins, margins);

        imageView.setLayoutParams(attachImageLayoutParams);
        return imageView;
    }

    public static ImageView initRightButton(Context context, int drawableSize) {
        ImageView imageView = new ImageView(context);

        int margins = context.getResources().getDimensionPixelOffset(R.dimen.canvasEditText_margin);

        RelativeLayout.LayoutParams buttonImageLayoutParams = new LayoutParams(drawableSize, drawableSize);
        buttonImageLayoutParams.addRule(CENTER_VERTICAL);
        buttonImageLayoutParams.setMargins(margins, margins, margins, margins);

        imageView.setImageDrawable(ColorKeeper.getColoredDrawable(context, R.drawable.vd_check_white_24dp, Color.BLACK));
        imageView.setLayoutParams(buttonImageLayoutParams);
        return imageView;
    }

    public static ProgressBar initRightProgress(Context context, int drawableSize) {
        ProgressBar progressBar = new ProgressBar(context);

        int margins = context.getResources().getDimensionPixelOffset(R.dimen.canvasEditText_margin);

        RelativeLayout.LayoutParams buttonImageLayoutParams = new LayoutParams(drawableSize, drawableSize);
        buttonImageLayoutParams.addRule(CENTER_VERTICAL);
        buttonImageLayoutParams.setMargins(margins, margins, margins, margins);

        progressBar.setLayoutParams(buttonImageLayoutParams);
        progressBar.setVisibility(GONE);
        return progressBar;
    }

    public static Drawable getSelectionIndicator(Context context){
        int[] attrs = { android.R.attr.selectableItemBackground };
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs);
        return ta.getDrawable(0);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //region Getters and Setters
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public void setText(String text, boolean showKeyboard){
        mEditText.setText(text);

        if(showKeyboard){
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
            mEditText.setSelection(mEditText.getText().length());
        }else{
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        }
    }

    public IndicatorCircleView getLeftButtonIndicator() {
        return mLeftButtonIndicator;
    }

    public void setLeftButtonIndicator(IndicatorCircleView leftButtonIndicator) {
        this.mLeftButtonIndicator = leftButtonIndicator;
    }

    public String getText(){
        return mEditText.getText().toString();
    }

    public void setHint(String hint){
        mEditText.setHint(hint);
    }

    public void setTextSize(int size){
        mEditText.setTextSize(size);
    }

    public void setEditTextRightListener(CanvasEditTextViewRightListener listener){
        mRightListener = listener;
    }

    public void setEditTextLeftListener(CanvasEditTextViewLeftListener listener){
        mLeftListener = listener;
    }

    public void setRightButtonImage(int resourceId){
        mRightImage.setImageDrawable(getTintedDrawable(resourceId));
    }

    public void showRightImage(){
        mRightButtonLayout.setVisibility(View.VISIBLE);
    }

    public void hideRightImage(){
        mRightButtonLayout.setVisibility(View.GONE);
    }

    public void setRightProgressBarLoading(boolean isLoading) {
        mRightImage.setVisibility(isLoading ? GONE : VISIBLE);
        mRightProgressBar.setVisibility(isLoading ? VISIBLE : GONE);
    }

    public void setLeftButtonImage(int resourceId){
        mLeftButtonLayout.setVisibility(VISIBLE);
        mLeftImage.setImageDrawable(getTintedDrawable(resourceId));
    }

    private Drawable getTintedDrawable( int resourceId){
        return ColorKeeper.getColoredDrawable(mLeftImage.getContext().getApplicationContext(), resourceId, mColorTint);
    }

    public void showLeftImage(){
        mLeftButtonLayout.setVisibility(View.VISIBLE);
    }

    public void hideLeftImage(){
        mLeftButtonLayout.setVisibility(View.GONE);
    }

    /**
     * Hides right image button before text is entered
     */
    public void setHideRightBeforeText(){
        mEditText.addTextChangedListener(rightTextWatcherBefore);
        mRightButtonLayout.setVisibility(View.INVISIBLE);
    }

    /**
     * Hides right image button after text is entered
     */
    public void setHideRightAfterText(){
        mEditText.addTextChangedListener(rightTextWatcherAfter);
        mRightButtonLayout.setVisibility(View.VISIBLE);
    }

    public void disableRightButton(){
        mRightButtonLayout.setEnabled(false);
    }

    public void enableRightButton(){
        mRightButtonLayout.setEnabled(true);
    }

    /**
     * Hides left image button before text is entered
     */
    public void setHideLeftBeforeText(){
        mEditText.addTextChangedListener(leftTextWatcherBefore);
        mLeftButtonLayout.setVisibility(View.INVISIBLE);
    }

    /**
     * Hides left image button after text is entered
     */
    public void setHideLeftAfterText(){
        mEditText.addTextChangedListener(leftTextWatcherAfter);
        mLeftButtonLayout.setVisibility(View.VISIBLE);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //region TextWatchers
    ////////////////////////////////////////////////////////////////////////////////////////////////
    protected TextWatcher leftTextWatcherBefore = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            mLeftButtonLayout.setEnabled(true);
            mLeftButtonLayout.setVisibility(View.VISIBLE);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if(TextUtils.isEmpty(s)) {
                mLeftButtonLayout.setEnabled(false);
                mLeftButtonLayout.setVisibility(View.INVISIBLE);
            }
        }
    };

    protected TextWatcher leftTextWatcherAfter = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if(TextUtils.isEmpty(s)) {
                mLeftButtonLayout.setEnabled(false);
                mLeftButtonLayout.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mLeftButtonLayout.setEnabled(true);
            mLeftButtonLayout.setVisibility(View.VISIBLE);
        }
    };


    protected TextWatcher rightTextWatcherBefore = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            mRightButtonLayout.setEnabled(true);
            mRightButtonLayout.setVisibility(View.VISIBLE);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if(TextUtils.isEmpty(s)) {
                mRightButtonLayout.setEnabled(false);
                mRightButtonLayout.setVisibility(View.INVISIBLE);
            }
        }
    };

    protected TextWatcher rightTextWatcherAfter = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if(TextUtils.isEmpty(s)) {
                mRightButtonLayout.setEnabled(false);
                mRightButtonLayout.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            mRightButtonLayout.setEnabled(true);
            mRightButtonLayout.setVisibility(View.VISIBLE);
        }
    };
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////
}
