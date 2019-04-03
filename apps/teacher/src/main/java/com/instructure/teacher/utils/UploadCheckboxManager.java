/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */

package com.instructure.teacher.utils;

import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.CheckedTextView;

import com.instructure.pandautils.dialogs.UploadFilesDialog;

import java.util.ArrayList;
import java.util.List;

public class UploadCheckboxManager {

    public interface OnOptionCheckedListener{
        void onUserFilesSelected();
        void onCourseFilesSelected();
        void onAssignmentFilesSelected();
    }

    private List<CheckedTextView> mCheckBoxes = new ArrayList<>();
    private CheckedTextView mCurrentCheckBox;
    private View mSelectionIndicator;
    private boolean mIsAnimating = false;
    private OnOptionCheckedListener mListener;

    public UploadCheckboxManager(OnOptionCheckedListener listener, View selectionIndicator){
        this.mSelectionIndicator = selectionIndicator;
        this.mListener = listener;
    }

    public void add(CheckedTextView checkBox){
        if(mCheckBoxes.size() == 0){
            mCurrentCheckBox = checkBox;
            setInitialIndicatorHeight();
        }
        mCheckBoxes.add(checkBox);
        checkBox.setOnClickListener(destinationClickListener);
    }

    public CheckedTextView getSelectedCheckBox(){
        return mCurrentCheckBox;
    }

    public UploadFilesDialog.FileUploadType getSelectedType(){
//        switch (mCurrentCheckBox.getId()){
//            case R.id.myFilesCheckBox:
//                return FileUploadDialog.FileUploadType.USER;
//            case R.id.courseFilesCheckBox:
//                return FileUploadDialog.FileUploadType.COURSE;
//            case R.id.assignmentCheckBox:
//                return FileUploadDialog.FileUploadType.ASSIGNMENT;
//        }
        return UploadFilesDialog.FileUploadType.USER;
    }


    public void setInitialIndicatorHeight(){
        this.mSelectionIndicator.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    UploadCheckboxManager.this.mSelectionIndicator.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }else{
                    UploadCheckboxManager.this.mSelectionIndicator.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                if(mCurrentCheckBox != null){
                    UploadCheckboxManager.this.mSelectionIndicator.getLayoutParams().height = ((View) mCurrentCheckBox.getParent()).getHeight();
                    UploadCheckboxManager.this.mSelectionIndicator.setLayoutParams(UploadCheckboxManager.this.mSelectionIndicator.getLayoutParams());
                }
                mListener.onUserFilesSelected();
            }
        });
    }

    public void moveIndicator(final CheckedTextView newCurrentCheckBox){
        Animation moveAnimation = getAnimation(newCurrentCheckBox);
        mSelectionIndicator.startAnimation(moveAnimation);
        moveAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIsAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mCurrentCheckBox = newCurrentCheckBox;
                mIsAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    private AnimationSet getAnimation(final CheckedTextView toCheckBox){
        final View toView = ((View) toCheckBox.getParent());
        final View fromView = ((View) mCurrentCheckBox.getParent());

        // get ratio between current height and new height
        final float toRatio = ((float)toView.getHeight())/((float) mSelectionIndicator.getHeight());
        final float fromRatio = ((float)fromView.getHeight())/((float) mSelectionIndicator.getHeight());

        ScaleAnimation scaleAnimation
                = new ScaleAnimation(   1.f,        // fromXType
                                        1.f,        // toX
                                        fromRatio,  // fromY
                                        toRatio,    // toY
                                        .5f,        // pivotX
                                        0.0f);      // pivotY

        TranslateAnimation translateAnimation
                = new TranslateAnimation(   Animation.RELATIVE_TO_SELF, 0.0f,       // fromXType, fromXValue
                                            Animation.RELATIVE_TO_SELF, 0.0f,       // toXType, toXValue
                                            Animation.ABSOLUTE, fromView.getTop(),  // fromYType, fromYValue
                                            Animation.ABSOLUTE, toView.getTop());   // toYTyp\e, toYValue

        translateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        translateAnimation.setFillAfter(true);

        AnimationSet animSet = new AnimationSet(true);
        animSet.addAnimation(scaleAnimation);
        animSet.addAnimation(translateAnimation);
        animSet.setFillAfter(true);
        animSet.setDuration(200);

        return animSet;
    }

    private View.OnClickListener destinationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mIsAnimating){return;}
            CheckedTextView checkedTextView = (CheckedTextView) v;
            if (!checkedTextView.isChecked()) {
                checkedTextView.setChecked(true);
                notifyListener(checkedTextView);
                moveIndicator(checkedTextView);
                for(CheckedTextView checkBox : mCheckBoxes){
                    if(checkBox.getId() != checkedTextView.getId()){
                        checkBox.setChecked(false);
                    }
                }
            }
        }
    };

    private void notifyListener(CheckedTextView checkedTextView){
        // TODO
        /*switch (checkedTextView.getId()){
            case R.id.myFilesCheckBox:
                mListener.onUserFilesSelected();
                break;
            case R.id.courseFilesCheckBox:
                mListener.onCourseFilesSelected();
                break;
            case R.id.assignmentCheckBox:
                mListener.onAssignmentFilesSelected();
                break;
        }*/
    }
}
