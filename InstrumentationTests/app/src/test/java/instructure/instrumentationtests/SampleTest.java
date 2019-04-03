/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package instructure.instrumentationtests;

import com.instructure.canvasapi2.utils.DateHelper;

import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import instructure.instrumentationtests.robo.RoboTestCase;

public class SampleTest extends RoboTestCase {

    @Test
    public void dateTest() {
        GregorianCalendar now = new GregorianCalendar();
        GregorianCalendar later = new GregorianCalendar();
        later.add(Calendar.DAY_OF_WEEK, 1);

        assertTrue(DateHelper.compareDays(now, later) == -1);

        assertTrue(DateHelper.compareDays(now, now) == 0);

        assertTrue(DateHelper.compareDays(later, now) == 1);
    }

    @Test
    public void resourceTest() {
        String logIn = context().getResources().getString(R.string.login);
        assertEquals(logIn, "Login");
    }
}
