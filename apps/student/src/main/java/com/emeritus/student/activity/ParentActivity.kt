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

package com.emeritus.student.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.instructure.canvasapi2.utils.Logger
import com.instructure.pandautils.activities.BaseActionBarActivity
import com.instructure.pandautils.utils.Const
import com.emeritus.student.R
import com.emeritus.student.util.LoggingUtility

abstract class ParentActivity : BaseActionBarActivity(), OnTouchListener {

    private val uploadStartedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            showMessage(getString(R.string.notoriousSubmissionInProgress))
        }
    }

    private val uploadFinishedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            showMessage(getString(R.string.notoriousSubmissionSuccessful))
        }
    }

    val context: Context
        get() = this

    val isTablet: Boolean
        get() = resources.getBoolean(R.bool.isDeviceTablet)
    val isLandscape: Boolean
        get() = resources.getBoolean(R.bool.isLandscape)

    public override fun onCreate(savedInstanceState: Bundle?) {
        // Add progress indicator to action bar
        super.onCreate(savedInstanceState)
        LoggingUtility.log(Log.DEBUG, this.javaClass.simpleName + " --> On Create")

        try {
            //Fixes an error with devices with a menu key when clicking the overflow menu item.
            val config = ViewConfiguration.get(this)
            val menuKeyField = ViewConfiguration::class.java.getDeclaredField("sHasPermanentMenuKey")
            if (menuKeyField != null) {
                menuKeyField.isAccessible = true
                menuKeyField.setBoolean(config, false)
            }
        } catch (e: Exception) {
            // Ignore
            Logger.e("CAN IGNORE: " + e)
        }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        //first saving my state, so the bundle wont be empty.
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE")
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        //register some broadcast receivers so that we can receive some communication from the upload service and display messages to the user
        LocalBroadcastManager.getInstance(context).registerReceiver(uploadStartedReceiver, IntentFilter(Const.UPLOAD_STARTED))
        LocalBroadcastManager.getInstance(context).registerReceiver(uploadFinishedReceiver, IntentFilter(Const.UPLOAD_SUCCESS))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(context).unregisterReceiver(uploadStartedReceiver)
        LocalBroadcastManager.getInstance(context).unregisterReceiver(uploadFinishedReceiver)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //go back one level
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // http://stackoverflow.com/questions/26833242/nullpointerexception-phonewindowonkeyuppanel1002-main
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_MENU && "LGE".equals(Build.BRAND, ignoreCase = true)) {
            true
        } else super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU && "LGE".equals(Build.BRAND, ignoreCase = true)) {
            openOptionsMenu()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return false
    }

    fun showMessage(message: String?) {
        if (!TextUtils.isEmpty(message)) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    fun showMessage(extras: Bundle?) {
        if (extras == null) {
            return
        }
        if (extras.containsKey(Const.MESSAGE_TYPE) && extras.containsKey(Const.MESSAGE)) {
            showMessage(extras.getString(Const.MESSAGE))
        }
    }

    /**
     * Reads information out of the intent extras.
     */
    protected open fun handleIntent() {
        LoggingUtility.logIntent(intent)
    }

    companion object {

        /**
         *
         * @param context Android Context
         * @param type The class of Activity that will be started.
         * @param layoutId The R layout integer, such as R.layout.example_layout.
         * @return An Intent
         */
        fun createIntent(context: Context?, type: Class<out ParentActivity>?, layoutId: Int): Intent {
            val intent: Intent

            if (context != null) {
                intent = Intent(context, type)
            } else { //used for unit tests
                intent = Intent()
            }

            intent.putExtra(Const.LAYOUT_ID, layoutId)

            //Done to know where we just came from
            if (context != null)
                intent.putExtra(Const.__PREVIOUS, context.javaClass.name)

            if (type != null)
                intent.putExtra(Const.__CURRENT, type.name)
            return intent
        }

        protected fun createIntent(context: Context?, type: Class<out ParentActivity>?): Intent {
            val intent: Intent

            if (context != null) {
                intent = Intent(context, type)
            } else { //used for unit tests
                intent = Intent()
            }

            //Done to know where we just came from
            if (context != null)
                intent.putExtra(Const.__PREVIOUS, context.javaClass.name)

            if (type != null)
                intent.putExtra(Const.__CURRENT, type.name)
            return intent
        }
    }
}

