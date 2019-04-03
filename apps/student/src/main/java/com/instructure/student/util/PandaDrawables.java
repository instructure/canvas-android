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

package com.instructure.student.util;

import com.instructure.student.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PandaDrawables {

    private static final List<Integer> bodies = new ArrayList<Integer>(Arrays.asList(
            R.drawable.pandify_body_1,
            R.drawable.pandify_body_2,
            R.drawable.pandify_body_3,
            R.drawable.pandify_body_4,
            R.drawable.pandify_body_5,
            R.drawable.pandify_body_6,
            R.drawable.pandify_body_7,
            R.drawable.pandify_body_8,
            R.drawable.pandify_body_9,
            R.drawable.pandify_body_10,
            R.drawable.pandify_body_11,
            R.drawable.pandify_body_12,
            R.drawable.pandify_body_13
    ));

    private static final List<Integer> heads = new ArrayList<Integer>(Arrays.asList(
            R.drawable.pandify_head_02,
            R.drawable.pandify_head_03,
            R.drawable.pandify_head_04,
            R.drawable.pandify_head_05,
            R.drawable.pandify_head_06,
            R.drawable.pandify_head_07,
            R.drawable.pandify_head_08,
            R.drawable.pandify_head_09,
            R.drawable.pandify_head_10
    ));

    private static final List<Integer> legs = new ArrayList<Integer>(Arrays.asList(
            R.drawable.pandify_feet_1,
            R.drawable.pandify_feet_2,
            R.drawable.pandify_feet_3,
            R.drawable.pandify_feet_4,
            R.drawable.pandify_feet_5
    ));

    public static List<Integer> getBodies() {
        return bodies;
    }

    public static List<Integer> getHeads() {
        return heads;
    }

    public static List<Integer> getLegs() {
        return legs;
    }
}
