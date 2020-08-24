/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package instructure.rceditor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import instructure.rceditor.RCEFragment.RCEFragmentCallbacks

class RCEActivity : AppCompatActivity(), RCEFragmentCallbacks {
    private var fragment: RCEFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        setContentView(R.layout.rce_activity_layout)
    }

    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is RCEFragment) {
            this.fragment = fragment
            fragment.loadArguments(
                intent.getStringExtra(RCEConst.HTML_CONTENT),
                intent.getStringExtra(RCEConst.HTML_TITLE),
                intent.getStringExtra(RCEConst.HTML_ACCESSIBILITY_TITLE),
                intent.getIntExtra(RCEConst.THEME_COLOR, Color.BLACK),
                intent.getIntExtra(RCEConst.BUTTON_COLOR, Color.BLACK)
            )
        }
        super.onAttachFragment(fragment)
    }

    override fun onBackPressed() {
        fragment?.showExitDialog()
    }

    override fun onResult(activityResult: Int, data: Intent?) {
        if (activityResult == Activity.RESULT_OK && data != null) {
            setResult(activityResult, data)
        } else {
            setResult(activityResult)
        }
        finish()
    }

    companion object {
        fun createIntent(context: Context?, html: String?, title: String?, accessibilityTitle: String?, @ColorInt themeColor: Int, @ColorInt buttonColor: Int): Intent {
            val intent = Intent(context, RCEActivity::class.java)
            intent.putExtra(RCEConst.HTML_CONTENT, html)
            intent.putExtra(RCEConst.HTML_TITLE, title)
            intent.putExtra(RCEConst.HTML_ACCESSIBILITY_TITLE, accessibilityTitle)
            intent.putExtra(RCEConst.THEME_COLOR, themeColor)
            intent.putExtra(RCEConst.BUTTON_COLOR, buttonColor)
            return intent
        }
    }
}
