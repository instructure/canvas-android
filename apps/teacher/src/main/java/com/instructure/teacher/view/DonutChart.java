package com.instructure.teacher.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import androidx.annotation.ColorInt;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.instructure.teacher.R;

public class DonutChart extends View {

    private static final float ANIM_DURATION = 500f;

    private float mRadius;

    private Paint mPaint;
    private TextPaint mTextPaint;

    private Path mPath;

    private RectF mOuterCircle;
    private RectF mInnerCircle;

    private int mSelected = 0;
    private int mTotal = 0;
    private int mSelectedColor;
    private int mGrayColor;
    private String mCenterText = "";
    private float mCenterTextSize;

    private float mTextX;
    private float mTextY;

    private boolean mShouldStartAnimation = false;
    private long mAnimStartTime;
    private DecelerateInterpolator mAnimInterpolator = new DecelerateInterpolator();

    public DonutChart(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.DonutChart,
                0, 0
        );

        try {
            mRadius = a.getDimension(R.styleable.DonutChart_dc_radius, 40.0f);
            mCenterText = a.getString(R.styleable.DonutChart_dc_center_text);
            mCenterTextSize = a.getDimension(R.styleable.DonutChart_dc_center_text_size, 16f);
        } finally {
            a.recycle();
        }

        // Get screen density
        final float scale = getContext().getResources().getDisplayMetrics().density;

        mRadius = mRadius * scale + .5f;
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mRadius / 14.0f);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER); //Draw text from center
        mTextPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

        // Convert the dips to pixels
        float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mCenterTextSize, getResources().getDisplayMetrics());
        mTextPaint.setTextSize(textSize);

        mGrayColor = context.getResources().getColor(R.color.defaultUnselectedDonutGray);

        mPath = new Path();

        mOuterCircle = new RectF();
        mInnerCircle = new RectF();

        float adjust;

        adjust = .038f * mRadius;
        mOuterCircle.set(adjust, adjust, mRadius *2-adjust, mRadius *2-adjust);

        adjust = .276f * mRadius;
        mInnerCircle.set(adjust, adjust, mRadius *2-adjust, mRadius *2-adjust);

    }

    public void setSelected(int selected) {
        mSelected = selected;
        mShouldStartAnimation = true;
        invalidate();
    }

    public void setTotal(int total) {
        mTotal = total;
        mShouldStartAnimation = true;
        invalidate();
    }

    public void setSelectedColor(@ColorInt int selectedColor) {
        mSelectedColor = selectedColor;
    }

    public void setCenterText(String centerText) {
        mCenterText = centerText;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mShouldStartAnimation) {
            mShouldStartAnimation = false;
            mAnimStartTime = System.currentTimeMillis();
        }

        float animProgress;
        if (mAnimStartTime == 0 || System.currentTimeMillis() > (mAnimStartTime + (long) ANIM_DURATION)) {
            animProgress = 1f;
        } else {
            animProgress = (System.currentTimeMillis() - mAnimStartTime) / ANIM_DURATION;
        }

        mPaint.setShader(null);

        float interpolatedProgress = mAnimInterpolator.getInterpolation(animProgress);
        if (interpolatedProgress > 1f) interpolatedProgress = 1f;

        float endGray = 359.9999f * interpolatedProgress * mSelected / mTotal;

        // gray
        mPaint.setColor(mGrayColor);
        drawDonut(canvas, mPaint, 0, 359.9999f);

        // theme color
        mPaint.setColor(mSelectedColor);
        // 270 == top of the circle
        drawDonut(canvas, mPaint, 270, endGray);

        if (mCenterText != null) {
            String drawText = mCenterText;
            if (animProgress < 1f) {
                try {
                    int count = Integer.parseInt(mCenterText);
                    count = (int) (count * interpolatedProgress);
                    drawText = String.valueOf(count);
                } catch (NumberFormatException ignore) {}
            }
            canvas.drawText(drawText, mTextX, mTextY, mTextPaint);
        }

        if (animProgress < 1f) invalidate();
    }

    public void drawDonut(Canvas canvas, Paint paint, float start,float sweep){
        mPath.reset();
        mPath.arcTo(mOuterCircle, start, sweep, false);
        mPath.arcTo(mInnerCircle, start+sweep, -sweep, false);
        mPath.close();
        canvas.drawPath(mPath, paint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // get the starting x and y coordinates of the text in the middle of the view
        Paint.FontMetrics metrics = mTextPaint.getFontMetrics();
        float height = Math.abs(metrics.top - metrics.bottom);
        mTextX = getWidth() / 2;
        mTextY = (getHeight() / 2) + (height / 4);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int desiredWidth = (int) mRadius *2;
        int desiredHeight = (int) mRadius *2;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //70dp exact
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        }else if (widthMode == MeasureSpec.AT_MOST) {
            //wrap content
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

}
