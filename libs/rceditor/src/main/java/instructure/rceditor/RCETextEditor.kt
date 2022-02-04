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

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.text.Html
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.webkit.WebView
import androidx.annotation.RestrictTo
import jp.wasabeef.richeditor.RichEditor

@RestrictTo(RestrictTo.Scope.LIBRARY)
class RCETextEditor @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = android.R.attr.webViewStyle
) : RichEditor(context, attrs, defStyleAttr) {

    fun applyHtml(contents: String, title: String = "") {
        super.setHtml(formatHTML(contents, title))
        loadCSS("rce_style.css")
    }

    private fun checkForMathTags(content: String) {
        // If this html that we're about to load has a math tag and isn't just an image we want to parse it with MathJax.
        // This is the version that web currently uses (the 2.7.1 is the version number) and this is the check that they do to
        // decide if they'll run the MathJax script on the webview
        if (content.contains("<math") && !content.contains("<img class='equation_image'")) {
            val jsCSSImport = "(function() {" +
                    "    var head  = document.getElementsByTagName(\"head\")[0];" +
                    "    var script  = document.createElement(\"script\");" +
                    "    script.type= 'text/javascript';" +
                    "    script.src= \"https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.1/MathJax.js?config=TeX-AMS-MML_HTMLorMML\";" +
                    "    head.appendChild(script);" +
                    "}) ();"
            exec("javascript:$jsCSSImport")
        }
    }

    private fun formatHTML(html: String, title: String): String {
        var contents = html
        if (0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        contents = applyWorkAroundForDoubleSlashesAsUrlSource(contents)
        checkForMathTags(contents)

        // Note: loading with a base url for the referrer does not work.
        setupAccessibilityContentDescription(contents, title)
        return contents
    }

    fun setupRtl() {
        val addRtl = "(function() {" +
                "    document.body.style.direction = \"rtl\";" +
                "}) ();"
        exec("javascript:$addRtl")
    }

    private fun setupAccessibilityContentDescription(formattedHtml: String, title: String?) {
        // Remove all html tags and set content description for accessibility
        // call toString on fromHTML because certain Spanned objects can cause this to crash
        var description = formattedHtml
        if (title != null) description = "$title $formattedHtml"
        contentDescription = simplifyHTML(Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY))
    }

    val accessibilityContentDescription: String
        get() = contentDescription?.toString().orEmpty()

    override fun getHtml(): String? = RCEUtils.sanitizeHTML(super.getHtml())

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        requestDisallowInterceptTouchEvent(true)
        return super.onTouchEvent(event)
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
        requestDisallowInterceptTouchEvent(!clampedY)
    }

    companion object {
        fun applyWorkAroundForDoubleSlashesAsUrlSource(html: String): String {
            if (html.isBlank()) return ""
            // Fix for embedded videos that have // instead of http://
            return html.replace("href=\"//".toRegex(), "href=\"http://")
                .replace("href='//".toRegex(), "href='http://")
                .replace("src=\"//".toRegex(), "src=\"http://")
                .replace("src='//".toRegex(), "src='http://")
        }

        /*
         * The fromHTML method can cause a character that looks like [obj]
         * to show up. This is undesired behavior most of the time.
         *
         * Replace the [obj] with an empty space
         * [obj] is char 65532 and an empty space is char 32
         * @param sequence The fromHTML typically
         * @return The modified charSequence
         */
        fun simplifyHTML(sequence: CharSequence?): String {
            if (sequence != null) {
                var toReplace: CharSequence = sequence
                toReplace = toReplace.toString().replace(65532.toChar(), 32.toChar()).trim { it <= ' ' }
                return toReplace.toString()
            }
            return ""
        }
    }
}
