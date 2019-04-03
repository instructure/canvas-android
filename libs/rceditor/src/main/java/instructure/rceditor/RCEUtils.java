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

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestrictTo(RestrictTo.Scope.LIBRARY)
class RCEUtils {

    //<span style="color: rgb(139, 150, 158);">Gray</span>
    private static final String RBG_REGEX = "rgb\\((\\d+),\\s*(\\d+),\\s*(\\d+)\\);";
    //<span style=";"></span>
    private static final String SPAN_REGEX = "<span\\s*style=\"\\s*;\">\\s*</span>";
    //<span style="color: #1482c8;"></span>
    private static final String HEX_COMMA_REGEX = "#([A-Fa-f0-9]{3,8});";

    public static void colorIt(int color, ImageView view) {
        Drawable drawable = view.getDrawable();
        if(drawable == null) return;

        drawable.mutate().setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        view.setImageDrawable(drawable);
    }

    public static void colorIt(int color, ImageButton view) {
        Drawable drawable = view.getDrawable();
        if(drawable == null) return;

        drawable.mutate().setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        view.setImageDrawable(drawable);
    }

    @Nullable
    private static String workaround_RBG_2_HEX(String html) {
        try {
            Pattern pattern = Pattern.compile(RBG_REGEX);
            Matcher action = pattern.matcher(html);
            StringBuffer sb = new StringBuffer(html.length());
            while (action.find()) {
                String cleanValue = action.group().replace("rgb(", "");//removes prefix
                cleanValue = cleanValue.replace(");", "");//removes suffix
                cleanValue = cleanValue.replaceAll("\\s", "");//Clean up whitespace
                List<String> values = Arrays.asList(cleanValue.split(","));

                String hex = String.format("#%02x%02x%02x",
                        Integer.valueOf(values.get(0)),
                        Integer.valueOf(values.get(1)),
                        Integer.valueOf(values.get(2)));

                action.appendReplacement(sb, Matcher.quoteReplacement(hex));
            }
            action.appendTail(sb);
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private static String workaroundInvalidSpan(String html) {
        try {
            Pattern pattern = Pattern.compile(SPAN_REGEX);
            Matcher action = pattern.matcher(html);
            StringBuffer sb = new StringBuffer(html.length());
            while (action.find()) {
                action.appendReplacement(sb, Matcher.quoteReplacement(""));
            }
            action.appendTail(sb);
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static String workaroundInvalidHex(String html) {
        try {
            Pattern pattern = Pattern.compile(HEX_COMMA_REGEX);
            Matcher action = pattern.matcher(html);
            StringBuffer sb = new StringBuffer(html.length());
            while (action.find()) {
                String cleanValue = action.group().replace(";", "");
                action.appendReplacement(sb, Matcher.quoteReplacement(cleanValue));
            }
            action.appendTail(sb);
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * When we do a PUT on the HTML there is often small things that'll cause the HTML to be invalid by the server and replaced
     * by <p>&nbsp;</p>. In order to avoid content getting nuked we use these workarounds. It is possible that the actual error
     * is due to improper encoding on our end.
     * @param html A valid HTML string
     * @return A String or null value that has been sanitized.
     */
    @Nullable
    static String sanitizeHTML(String html) {
//        Log.d("HTML", "VALIDATE(NONE): " + html);
        String validated = workaround_RBG_2_HEX(html);
//        Log.d("HTML", "VALIDATE(RGB): " + validated);
        if(validated == null) return null;
        validated = workaroundInvalidSpan(validated);
//        Log.d("HTML", "VALIDATE(SPAN;): " + validated);
        if(validated == null) return null;
        validated = workaroundInvalidHex(validated);
//        Log.d("HTML", "VALIDATE(HEX;): " + validated);
        return validated;
    }

    static ColorStateList makeEditTextColorStateList(@ColorInt int defaultColor, @ColorInt int tintColor) {
        int[][] states = new int[][] {
                new int[] {-android.R.attr.state_enabled},
                new int[] {android.R.attr.state_focused, -android.R.attr.state_pressed},
                new int[] {android.R.attr.state_focused, android.R.attr.state_pressed},
                new int[] {-android.R.attr.state_focused, android.R.attr.state_pressed},
                new int[] {android.R.attr.state_checked},
                new int[] {}
        };

        int[] colors = new int[] {
                defaultColor,
                tintColor,
                tintColor,
                tintColor,
                tintColor,
                defaultColor
        };

        return new ColorStateList(states, colors);
    }

    static int increaseAlpha(@ColorInt int color) {
        int a = 0x32;
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb(a, r, g, b);
    }
}
