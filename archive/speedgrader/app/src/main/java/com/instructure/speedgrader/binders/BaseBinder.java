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

package com.instructure.speedgrader.binders;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.instructure.pandautils.utils.ColorUtils;
import com.instructure.speedgrader.R;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.Submission;
import com.instructure.speedgrader.util.StringUtilities;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class BaseBinder {

    private static String NO_GRADE_INDICATOR = "-";

    public static void setVisible(View v) {
        if (v == null) return;
        v.setVisibility(View.VISIBLE);
    }

    public static void setInvisible(View v) {
        if (v == null) return;
        v.setVisibility(View.INVISIBLE);
    }

    public static void setGone(View v) {
        if (v == null) return;
        v.setVisibility(View.GONE);
    }

    public static void ifHasTextSetVisibleElseGone(TextView v) {
        if (v == null) return;
        if (TextUtils.isEmpty(v.getText())) {
            setGone(v);
        } else {
            setVisible(v);
        }
    }

    public static void ifHasTextSetVisibleElseInvisible(TextView v) {
        if (v == null) return;
        if (TextUtils.isEmpty(v.getText())) {
            setInvisible(v);
        } else {
            setVisible(v);
        }
    }

    public static float getListItemHeight(Context context) {
        TypedValue value = new TypedValue();
        DisplayMetrics metrics = new DisplayMetrics();
        context.getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value, true);
        ((WindowManager) (context.getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay().getMetrics(metrics);
        return TypedValue.complexToDimension(value.data, metrics);
    }

    public static String buildCollapsedTitle(Context context, String group, int childCount) {
        if (childCount == 0) {
            return group;
        }
        return String.format("%s (%d %s)", group, childCount, context.getString(childCount == 1 ? R.string.singleItem : R.string.multipleItems));
    }

    public static String getHtmlAsText(String html) {
        if(!TextUtils.isEmpty(html)) {
            return simplifyHTML(Html.fromHtml(html));
        }
        return null;
    }
    /**
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

    //format the points possible field
    public static String getPointsPossible(double points_possible) {
        //if the points possible ends in .0, strip off the .0 and just put the
        //number to take up less space
        double points = points_possible - (int)points_possible;
        if(points == 0) {
            NumberFormat f = NumberFormat.getInstance(Locale.getDefault());
            if (f instanceof DecimalFormat) {
                ((DecimalFormat)f).setDecimalSeparatorAlwaysShown(false);
            }
            return f.format(points_possible);
        }
        return Double.toString(points_possible);
    }

    public static void setGrade(Submission submission, double possiblePoints, TextView textView, Context context) {
        String grade = getGrade(submission, possiblePoints, context);
        if(TextUtils.isEmpty(grade)) {
            textView.setText("");
        } else {
            textView.setText(grade);
        }
    }

    public static String getGrade(Submission submission, double possiblePoints, Context context) {
        if (submission != null) {
            if(submission.isExcused()) {
                return context.getString(R.string.excused) + "/" + getPointsPossible(possiblePoints);
            }
            if (submission.getGrade() != null){
                if(StringUtilities.isStringNumeric(submission.getGrade())) {
                    //format the grade so there aren't too many numbers after the decimal point
                    String grade = submission.getGrade();
                    if(grade.contains(".")) {
                        //we have a decimal point, only show 2 max decimal places
                        int index = grade.indexOf(".");
                        if(index + 3 < grade.length()) {
                            grade = grade.substring(0, index + 3);
                        }
                    }
                    return grade + "/" + getPointsPossible(possiblePoints);
                }
            }
            return submission.getGrade();
        } else {
            if (possiblePoints > 0) {
                return NO_GRADE_INDICATOR + "/" + getPointsPossible(possiblePoints);
            } else {
                return NO_GRADE_INDICATOR;
            }
        }
    }

    public static boolean hasGrade(Submission submission) {
        if(submission != null) {
            if(!TextUtils.isEmpty(submission.getGrade())) {
                return true;
            }
        }
        return false;
    }

    public static void setupGradeText(Context context, TextView textView, Assignment assignment, Submission submission, int color) {
        if(context == null || textView == null || assignment == null) {
            return;
        }

        final boolean hasGrade = hasGrade(submission);
        final String grade = getGrade(submission, assignment.getPointsPossible(), context);
        if(hasGrade) {
            textView.setText(grade);
            textView.setBackgroundDrawable(createGradeIndicatorBackground(context, color));
        } else {
            textView.setText(grade);
            textView.setBackgroundDrawable(null);
        }
    }

    public static ShapeDrawable createIndicatorBackground(int color) {
        ShapeDrawable circle = new ShapeDrawable(new OvalShape());
        circle.getPaint().setColor(color);
        return circle;
    }

    public static Drawable createGradeIndicatorBackground(Context context, int color) {
        Drawable shape = context.getResources().getDrawable(R.drawable.grade_background);
        return ColorUtils.colorIt(color, shape);
    }

    public static void setCleanText(TextView textView, String text) {
        if(!TextUtils.isEmpty(text)) {
            textView.setText(text);
        } else {
            textView.setText("");
        }
    }

    public static void setCleanText(TextView textView, String text, String defaultText) {
        if(!TextUtils.isEmpty(text)) {
            textView.setText(text);
        } else {
            textView.setText(defaultText);
        }
    }

    public static int getAssignmentIcon(Assignment assignment) {
        if(assignment == null) {
            return 0;
        }
        int drawable = 0;
        if(assignment.getSubmissionTypes().contains(Assignment.SUBMISSION_TYPE.ONLINE_QUIZ)) {
            drawable = R.drawable.ic_cv_quizzes_fill;
        }else if(assignment.getSubmissionTypes().contains(Assignment.SUBMISSION_TYPE.DISCUSSION_TOPIC)) {
            drawable = R.drawable.ic_cv_discussions_fill;
        } else {
            drawable = R.drawable.ic_cv_assignments_fill;
        }
        return drawable;
    }

    public static void updateShadows(boolean isFirstItem, boolean isLastItem, View top, View bottom) {
        if(isFirstItem) {
            setVisible(top);
        } else {
            setInvisible(top);
        }

        if(isLastItem) {
            setVisible(bottom);
        } else {
            setInvisible(bottom);
        }
    }
}
