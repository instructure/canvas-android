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
package instructure.rceditor;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

import jp.wasabeef.richeditor.RichEditor;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class RCETextEditor extends RichEditor {

    public RCETextEditor(Context context) {
        super(context);
    }

    public RCETextEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RCETextEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public void applyHtml(String contents) {
        applyHtml(contents, "");
    }

    public void applyHtml(String contents, String title) {
        super.setHtml(formatHTML(contents, title));
        loadCSS("rce_style.css");
    }

    private void checkForMathTags(String content) {
        // If this html that we're about to load has a math tag and isn't just an image we want to parse it with MathJax.
        // This is the version that web currently uses (the 2.7.1 is the version number) and this is the check that they do to
        // decide if they'll run the MathJax script on the webview
        if(content.contains("<math") && !content.contains("<img class='equation_image'")) {

            String jsCSSImport = "(function() {" +
                    "    var head  = document.getElementsByTagName(\"head\")[0];" +
                    "    var script  = document.createElement(\"script\");" +
                    "    script.type= 'text/javascript';" +
                    "    script.src= \"https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.1/MathJax.js?config=TeX-AMS-MML_HTMLorMML\";" +
                    "    head.appendChild(script);" +
                    "}) ();";
            exec("javascript:" + jsCSSImport + "");

        }
    }

    private String formatHTML(String contents, String title) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (getContext().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }


        contents = applyWorkAroundForDoubleSlashesAsUrlSource(contents);
        checkForMathTags(contents);

        //Note: loading with a base url for the referrer does not work.

        setupAccessibilityContentDescription(contents, title);

        return contents;
    }

    @Override
    public void loadData(String data, String mimeType, String encoding) {
        super.loadData(data, mimeType, encoding);
    }

    @Override
    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
    }

    public void setupRtl() {
        String addRtl = "(function() {" +
                "    document.body.style.direction = \"rtl\";" +
                "}) ();";
        exec("javascript:" + addRtl + "");
    }

    private void setupAccessibilityContentDescription(String formattedHtml, String title) {
        //Remove all html tags and set content description for accessibility
        // call toString on fromHTML because certain Spanned objects can cause this to crash
        String contentDescription = formattedHtml;
        if (title != null) {
            contentDescription = title + " " + formattedHtml;
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.setContentDescription(simplifyHTML(Html.fromHtml(contentDescription, Html.FROM_HTML_MODE_LEGACY)));
        } else {
            this.setContentDescription(simplifyHTML(Html.fromHtml(contentDescription)));
        }
    }

    public String getAccessibilityContentDescription() {
        CharSequence charSequence = getContentDescription();
        if(charSequence == null) {
            return "";
        } else {
            return charSequence.toString();
        }
    }

    public static String applyWorkAroundForDoubleSlashesAsUrlSource(String html) {
        if(TextUtils.isEmpty(html)) return "";
        // Fix for embedded videos that have // instead of http://
        html = html.replaceAll("href=\"//", "href=\"http://");
        html = html.replaceAll("href='//", "href='http://");
        html = html.replaceAll("src=\"//", "src=\"http://");
        html = html.replaceAll("src='//", "src='http://");
        return html;
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
    public static String simplifyHTML(CharSequence sequence) {
        if(sequence != null) {
            CharSequence toReplace = sequence;
            toReplace = toReplace.toString().replace(((char) 65532), (char) 32).trim();
            return toReplace.toString();
        }
        return "";
    }

    @Override
    @Nullable
    public String getHtml() {
        return RCEUtils.sanitizeHTML(super.getHtml());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        requestDisallowInterceptTouchEvent(true);
        return super.onTouchEvent(event);
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        requestDisallowInterceptTouchEvent(!clampedY);
    }
}
