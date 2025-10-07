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
import android.text.Html
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebView
import androidx.annotation.RestrictTo
import jp.wasabeef.richeditor.RichEditor

@RestrictTo(RestrictTo.Scope.LIBRARY)
class RCETextEditor @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = android.R.attr.webViewStyle
) : RichEditor(context, attrs, defStyleAttr) {

    init {
        setEditorBackgroundColor(context.getColor(R.color.rce_backgroundColor))
        setEditorFontColor(context.getColor(R.color.rce_defaultTextColor))
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
    }

    var disallowInterceptTouchEvents: Boolean = true

    fun applyHtml(contents: String, title: String = "") {
        super.setHtml(formatHTML(contents, title))
        loadCSS("rce_style.css")
    }

    private fun formatHTML(html: String, title: String): String {
        var contents = html
        if (0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        contents = applyWorkAroundForDoubleSlashesAsUrlSource(contents)

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
        requestDisallowInterceptTouchEvent(disallowInterceptTouchEvents)
        return super.onTouchEvent(event)
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
        requestDisallowInterceptTouchEvent(!clampedY)
    }

    fun getSelectedText(callback: (String) -> Unit) {
        evaluateJavascript("(function(){return window.getSelection().toString()})()", object : ValueCallback<String> {
            override fun onReceiveValue(value: String?) {
                if ((value?.count() ?: 0) > 1) {
                    // We need to remove the last and first character because it returns an extra " character at the start and at the end
                    val result = value?.substring(1, value.count() - 1)
                    callback(result ?: "")
                } else {
                    callback("")
                }
            }
        })
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo?): InputConnection? {
        val editorInfo = outAttrs ?: EditorInfo()
        editorInfo.privateImeOptions = "disableSticker=true;disableGifKeyboard=true"
        return super.onCreateInputConnection(editorInfo)
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
