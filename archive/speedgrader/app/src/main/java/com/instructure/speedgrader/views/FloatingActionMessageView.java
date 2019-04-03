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
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.instructure.loginapi.login.util.Utils;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.speedgrader.R;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

public class FloatingActionMessageView extends FrameLayout implements FloatingActionsMenu.OnFloatingActionsMenuUpdateListener {

    CardView composeLayout;
    Context context;
    AnimationSet openAnimation;
    AnimationSet closeAnimation;
    FloatingActionsMenu actionsMenu;
    int courseColor = Color.parseColor("#9D9E9E"); // set gray as default color for attach button
    View rootView;
    EditText composeEditText;
    FloatingActionButton actionSend;
    FloatingActionButton actionAttach;
    CircularProgressDrawable progressDrawable;

    private boolean isOpened;

    private OnMessageActionsClickedListener mListener;

    public interface OnMessageActionsClickedListener {
        void onSendButtonClicked(String message);
        void onAttachButtonClicked();
        void onMenuExpanded();
        void onMenuCollapsed();
    }

    public FloatingActionMessageView(Context context){
        super(context);
        initViews(context);
    }

    public FloatingActionMessageView(Context context, AttributeSet attrs){
        super(context, attrs);
        initViews(context);
    }

    public FloatingActionMessageView initViews(Context context) {
        this.context = context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        rootView = inflater.inflate(R.layout.floating_action_message_view, this, true);

        actionsMenu = (FloatingActionsMenu) rootView.findViewById(R.id.actionMenu);
        actionsMenu.setOnFloatingActionsMenuUpdateListener(this);
        composeLayout = (CardView) rootView.findViewById(R.id.composeLayout);
        composeEditText = (EditText) rootView.findViewById(R.id.postCommentEditText);
        progressDrawable = new CircularProgressDrawable(getResources().getColor(R.color.white), Utils.convertDipsToPixels(2, context));

        initAnimations();

        return this;
    }

    public FloatingActionMessageView build(){
        //Create the Send Action Button
        actionSend = new FloatingActionButton(getContext());
        actionSend.setSize(FloatingActionButton.SIZE_MINI);
        actionSend.setColorNormal(getResources().getColor(R.color.courseChartreuse));
        actionSend.setColorDisabled(getResources().getColor(R.color.courseChartreuse));
        actionSend.setColorPressed(getResources().getColor(R.color.courseChartreuseDark));
        final Drawable sendDrawable = CanvasContextColor.getColoredDrawable(getContext(), R.drawable.ic_cv_send_thin_white_fill, getResources().getColor(R.color.white));
        actionSend.setIconDrawable(sendDrawable);
        actionSend.setRotation(0);
        // actionSend.setTitle("Send Reply");
        actionSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null){
                    mListener.onSendButtonClicked(composeEditText.getText().toString());
                }
            }
        });

        // Create the Attach Button
        actionAttach = new FloatingActionButton(getContext());
        actionAttach.setSize(FloatingActionButton.SIZE_MINI);
        actionAttach.setColorNormal(getResources().getColor(R.color.courseYellow));
        actionAttach.setColorPressed(getResources().getColor(R.color.courseYellowDark));
        final Drawable attachDrawable = CanvasContextColor.getColoredDrawable(getContext(), R.drawable.ic_cv_attachment_fill, getResources().getColor(R.color.white));
        actionAttach.setIconDrawable(attachDrawable);
        actionAttach.setRotation(0);
        // actionAttach.setTitle("Send with attachment");
        actionAttach.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onAttachButtonClicked();
                }
            }
        });

        // add buttons
        actionsMenu.addButton(actionAttach);
        actionsMenu.addButton(actionSend);

        return this;
    }

    public FloatingActionMessageView setListener(OnMessageActionsClickedListener listener){
        mListener = listener;
        return this;
    }

    // Unfortunately the FABmenu library doesn't allow you to programatically set the color of the menu button.
    // Setting the course color for the FAB view will only change the attach action.
    public FloatingActionMessageView setColor(int color){
        courseColor = color;

        return this;
    }

    // Returns the WindowToken for the edittext. Used to manually dismiss the keyboard after sending
    // a message
    public IBinder getEditTextWindowToken(){
        if(composeEditText != null){
            return composeEditText.getWindowToken();
        }
        return null;
    }

    public void setSendActionToProgress(){
        progressDrawable.start();
        actionSend.setIconDrawable(progressDrawable);
        composeEditText.setEnabled(false);
    }

    public void resetSendActionDrawable(boolean shouldClose){
        actionSend.setIconDrawable(CanvasContextColor.getColoredDrawable(getContext(), R.drawable.ic_cv_send_thin_white, getResources().getColor(R.color.white)));
        composeEditText.setEnabled(true);
        progressDrawable.stop();
        clearComposeEditText();

        if(shouldClose){
            actionsMenu.collapse();
        }
    }

    public FloatingActionsMenu getActionsMenu() {
        return actionsMenu;
    }

    public void enableSendButton(boolean isEnabled){
        if(isEnabled){
            resetSendActionDrawable(false);
        }
        else{
            setSendActionToProgress();
        }
        actionSend.setEnabled(isEnabled);
    }

    public EditText getComposeEditText(){
        return composeEditText;
    }

    public void clearComposeEditText(){
        composeEditText.setText("");
    }

    private void initAnimations(){

        // Fade the button from invisible to visible
        AlphaAnimation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(200);

        // Scale out; right to left
        ScaleAnimation scaleOut = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF,1.0f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleOut.setDuration(200);

        // Run the animations above in sequence on the final button. Looks horrible.
        openAnimation = new AnimationSet(true);
        openAnimation.addAnimation(fadeIn);
        openAnimation.addAnimation(scaleOut);
        openAnimation.setFillAfter(true); // make animation permanent


        // Fade the button from visible to invisible
        AlphaAnimation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(200);

        // Scale in; left to right
        ScaleAnimation closeAnim = new ScaleAnimation(1.0f, 0.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF,1.0f, Animation.RELATIVE_TO_SELF, 0.5f);
        closeAnim.setDuration(200);

        closeAnimation = new AnimationSet(true);
        closeAnimation.addAnimation(fadeOut);
        closeAnimation.addAnimation(closeAnim);
        closeAnimation.setFillAfter(true); // make animations permanent

        closeAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                // Set views to gone, since our animations only make them invisible.
                composeEditText.setVisibility(View.GONE);
                composeLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    @Override
    public void onMenuExpanded() {
        if(mListener != null){
            mListener.onMenuExpanded();
        }

        isOpened = true;
        // set views from gone to invisible before animating them in.
        composeLayout.setVisibility(View.INVISIBLE);
        composeEditText.setVisibility(View.VISIBLE);
        composeLayout.startAnimation(openAnimation);
    }

    @Override
    public void onMenuCollapsed() {
        if(mListener != null){
            mListener.onMenuCollapsed();
        }
        isOpened = false;
        composeLayout.startAnimation(closeAnimation);
        rootView.setBackgroundColor(getResources().getColor(R.color.transparent));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        progressDrawable.stop();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState =  super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.isOpened = isOpened;
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if(state instanceof  SavedState){
            SavedState savedState = (SavedState) state;
            isOpened = savedState.isOpened;
            if(isOpened){
                composeEditText.setVisibility(VISIBLE);
                composeLayout.setVisibility(VISIBLE);
                rootView.setBackgroundColor(getResources().getColor(R.color.white));
            }

            super.onRestoreInstanceState(savedState.getSuperState());
        }
        else{
            super.onRestoreInstanceState(state);
        }
    }

    public static class SavedState extends BaseSavedState {
        public boolean isOpened;

        public SavedState(Parcelable parcel) {
            super(parcel);
        }

        private SavedState(Parcel in) {
            super(in);
            isOpened = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(isOpened ? 1 : 0);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}