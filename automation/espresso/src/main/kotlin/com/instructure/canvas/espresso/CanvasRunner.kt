package com.instructure.canvas.espresso

import android.app.UiAutomation
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.runner.AndroidJUnitRunner
import com.instructure.canvas.espresso.mockcanvas.MockCanvasInterceptor
import com.instructure.canvasapi2.CanvasRestAdapter
import com.jakewharton.espresso.OkHttp3IdlingResource

open class CanvasRunner : AndroidJUnitRunner() {

    private var resource: IdlingResource? = null

    override fun onStart() {
        val client = CanvasRestAdapter.okHttpClient
                .newBuilder()
                .addInterceptor(MockCanvasInterceptor())
                .build()
        CanvasRestAdapter.client = client
        resource = OkHttp3IdlingResource.create("okhttp", client)
        IdlingRegistry.getInstance().register(resource)
        setupDialogHandlers()
        super.onStart()
    }

    /**
     * Set up dismissal actions for nuisance dialogs (for Android 18 and above)
     */
    protected open fun setupDialogHandlers() { // Allow this to be overridden
        if(android.os.Build.VERSION.SDK_INT >= 18) {
            getUiAutomation().setOnAccessibilityEventListener (
                    object : UiAutomation.OnAccessibilityEventListener {
                        override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
                            if (p0?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                                    && p0.source != null /* Fix for API 27 */) {
                                val rootNode = p0.source
                                dialogDismissalLogic(rootNode,"has stopped", "close app")
                            }
                        }
                    }
            )
            Log.v("dialogs", "Done with setup")
        }
    }

    /** Handling logic for a dialog with a specific message
     * [messageSubString] can be any part of the target dialog message, case-insensitive
     * [buttonSubString] can be any part of the text associated with the desired dialog dismissal
     *                   button to push, case-insensitive
     **/
    protected fun dialogDismissalLogic(rootNode : AccessibilityNodeInfo?, messageSubString: String, buttonSubString: String) {
        val matchingTextList = rootNode?.findAccessibilityNodeInfosByText(messageSubString)
        if (matchingTextList != null && matchingTextList.size > 0) {
            val matchingButtonList = rootNode?.findAccessibilityNodeInfosByText(buttonSubString)
            if (matchingButtonList != null && matchingButtonList.size > 0) {
                matchingButtonList.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.v("dialogs", "Dismissed " + matchingTextList[0].text)
                matchingButtonList[0].recycle()
            }
            matchingTextList[0].recycle()
        }

    }

    override fun finish(resultCode: Int, results: Bundle) {
        if( resource != null ) {
            IdlingRegistry.getInstance().unregister(resource)
        }
        super.finish(resultCode, results)
    }

}
