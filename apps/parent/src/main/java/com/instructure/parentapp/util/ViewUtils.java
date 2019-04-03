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
package com.instructure.parentapp.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ScaleXSpan;

import com.instructure.canvasapi2.models.Assignment;
import com.instructure.canvasapi2.utils.NumberHelper;
import com.instructure.pandautils.utils.AssignmentUtils2;
import com.instructure.parentapp.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class ViewUtils {

    public static Spannable applyKerning(CharSequence src, float kerning) {
        if (src == null) return null;
        final int srcLength = src.length();
        if (srcLength < 2) return src instanceof Spannable
                ? (Spannable)src
                : new SpannableString(src);

        final String nonBreakingSpace = "\u00A0";
        final SpannableStringBuilder builder = src instanceof SpannableStringBuilder
                ? (SpannableStringBuilder)src
                : new SpannableStringBuilder(src);
        for (int i = src.length() - 1; i >= 1; i--)
        {
            builder.insert(i, nonBreakingSpace);
            builder.setSpan(new ScaleXSpan(kerning), i, i + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return builder;
    }

    /**
     * Returns darker version of specified <code>color</code>.
     */
    public static int darker (int color, float factor) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green( color );
        int b = Color.blue( color );

        return Color.argb( a,
                Math.max( (int)(r * factor), 0 ),
                Math.max( (int)(g * factor), 0 ),
                Math.max( (int)(b * factor), 0 ) );
    }

    public static int getAssignmentIcon(Assignment assignment) {
        if(assignment == null) {
            return 0;
        }
        int drawable = 0;
        if(assignment.getSubmissionTypes().contains(Assignment.SubmissionType.ONLINE_QUIZ)) {
            drawable = R.drawable.ic_cv_quizzes_fill;
        }else if(assignment.getSubmissionTypes().contains(Assignment.SubmissionType.DISCUSSION_TOPIC)) {
            drawable = R.drawable.ic_cv_discussions_fill;
        } else {
            drawable = R.drawable.ic_cv_assignments_fill;
        }
        return drawable;
    }

    public static String getGradeText(int assignmentState, double score, double pointsPossible, Context context) {
        String gradeText;
        if(assignmentState == AssignmentUtils2.ASSIGNMENT_STATE_GRADED) {
            gradeText = context.getResources().getString(R.string.submitted);
        } else if (assignmentState == AssignmentUtils2.ASSIGNMENT_STATE_GRADED_MISSING) {
            gradeText = context.getResources().getString(R.string.missing);
        } else {
            //late case
            gradeText = context.getResources().getString(R.string.late);
        }

        gradeText += "   " + getPercentGradeForm(score, pointsPossible);

        return gradeText;
    }

    public static String getPointsGradeForm(double score, double pointsPossible) {
        double correctPoints = round(score, 2);
        double correctPointsPossible = round(pointsPossible, 2);

        return "(" + getFormattedPoints(correctPoints) + "/" + getFormattedPoints(correctPointsPossible) + ")";
    }

    public static String getPointsPossibleMissing(double pointsPossible) {
        double correctPointsPossible = round(pointsPossible, 2);

        return "- " + "/" + getFormattedPoints(correctPointsPossible);
    }

    public static String getPercentGradeForm(double score, double pointsPossible) {
        if(pointsPossible == 0) {
            //don't want a divide by 0 error
            return Double.toString(score);
        }
        double percent = (score / pointsPossible) * 100;

        return NumberHelper.INSTANCE.doubleToPercentage(percent);
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    //format the points possible field
    public static String getFormattedPoints(double points) {
        //if the  ends in .0, strip off the .0 and just put the
        //number to take up less space
        double formattedPoints = points - (int)points;
        if(formattedPoints == 0) {
            NumberFormat f = NumberFormat.getInstance(Locale.getDefault());
            if (f instanceof DecimalFormat) {
                ((DecimalFormat)f).setDecimalSeparatorAlwaysShown(false);
            }
            return f.format(points);
        }
        return Double.toString(points);
    }

    public static void setStatusBarColor(Context context, int color) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ((Activity)context).getWindow().setStatusBarColor(color);
        }
    }

    public static void setScreen(Activity activity) {
        if(!activity.getResources().getBoolean(R.bool.isTablet)) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
}
