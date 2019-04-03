package com.instructure.loginapi.login.util;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

@Deprecated
public class SoftKeyboardUtil {

    public interface OnSoftKeyBoardHideListener{
        void onSoftKeyBoardVisibilityChanged(boolean isVisible);
    }

    public static void observeSoftKeyBoard(Activity activity , final OnSoftKeyBoardHideListener listener){
        final View decorView = activity.getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                decorView.getWindowVisibleDisplayFrame(rect);

                final int displayHeight = rect.bottom - rect.top;
                final int height = decorView.getHeight();
                final boolean hide = (double)displayHeight / height > 0.8 ;

                if(listener != null) {
                    listener.onSoftKeyBoardVisibilityChanged(!hide);
                }
            }
        });
    }
}