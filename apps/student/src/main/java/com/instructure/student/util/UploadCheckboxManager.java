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

package com.instructure.student.util;

import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.CheckedTextView;

import com.instructure.student.R;
import com.instructure.pandautils.dialogs.UploadFilesDialog;

import java.util.ArrayList;
import java.util.List;

public class UploadCheckboxManager {

    public interface OnOptionCheckedListener{
        public void onUserFilesSelected();
        public void onCourseFilesSelected();
        public void onAssignmentFilesSelected();
    }

    List<CheckedTextView> checkBoxes = new ArrayList<>();
    private CheckedTextView currentCheckBox;
    private View selectionIndicator;
    private boolean isAnimating = false;
    private OnOptionCheckedListener listener;

    public UploadCheckboxManager(OnOptionCheckedListener listener, View selectionIndicator){
        this.selectionIndicator = selectionIndicator;
        this.listener = listener;
    }

    public void add(CheckedTextView checkBox){
        if(checkBoxes.size() == 0){
            currentCheckBox = checkBox;
            setInitialIndicatorHeight();
        }
        checkBoxes.add(checkBox);
        checkBox.setOnClickListener(destinationClickListener);
    }

    public CheckedTextView getSelectedCheckBox(){
        return  currentCheckBox;
    }

    public UploadFilesDialog.FileUploadType getSelectedType(){
        switch (currentCheckBox.getId()){
            case R.id.myFilesCheckBox:
                return UploadFilesDialog.FileUploadType.USER;
            case R.id.assignmentCheckBox:
                return UploadFilesDialog.FileUploadType.ASSIGNMENT;
        }
        return UploadFilesDialog.FileUploadType.USER;
    }


    public void setInitialIndicatorHeight(){
        this.selectionIndicator.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    UploadCheckboxManager.this.selectionIndicator.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }else{
                    UploadCheckboxManager.this.selectionIndicator.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                if(currentCheckBox != null){
                    UploadCheckboxManager.this.selectionIndicator.getLayoutParams().height = ((View)currentCheckBox.getParent()).getHeight();
                    UploadCheckboxManager.this.selectionIndicator.setLayoutParams(UploadCheckboxManager.this.selectionIndicator.getLayoutParams());
                }
                listener.onUserFilesSelected();
            }
        });
    }

    public void moveIndicator(final CheckedTextView newCurrentCheckBox){
        Animation moveAnimation = getAnimation(newCurrentCheckBox);
        selectionIndicator.startAnimation(moveAnimation);
        moveAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                currentCheckBox = newCurrentCheckBox;
                isAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    private AnimationSet getAnimation(final CheckedTextView toCheckBox){
        final View toView = ((View) toCheckBox.getParent());
        final View fromView = ((View) currentCheckBox.getParent());

        // get ratio between current height and new height
        final float toRatio = ((float)toView.getHeight())/((float)selectionIndicator.getHeight());
        final float fromRatio = ((float)fromView.getHeight())/((float)selectionIndicator.getHeight());

        ScaleAnimation scaleAnimation
                = new ScaleAnimation(1.f,        // fromXType
                1.f,        // toX
                fromRatio,  // fromY
                toRatio,    // toY
                .5f,        // pivotX
                0.0f);      // pivotY

        TranslateAnimation translateAnimation
                = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,       // fromXType, fromXValue
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
            if(isAnimating){return;}
            CheckedTextView checkedTextView = (CheckedTextView) v;
            if (!checkedTextView.isChecked()) {
                checkedTextView.setChecked(true);
                notifyListener(checkedTextView);
                moveIndicator(checkedTextView);
                for(CheckedTextView checkBox : checkBoxes){
                    if(checkBox.getId() != checkedTextView.getId()){
                        checkBox.setChecked(false);
                    }
                }
            }
        }
    };

    private void notifyListener(CheckedTextView checkedTextView){
        switch (checkedTextView.getId()){
            case R.id.myFilesCheckBox:
                listener.onUserFilesSelected();
                break;
            case R.id.assignmentCheckBox:
                listener.onAssignmentFilesSelected();
                break;
        }
    }
}
