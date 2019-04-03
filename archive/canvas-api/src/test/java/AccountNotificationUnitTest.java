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

import com.google.gson.Gson;
import com.instructure.canvasapi.model.AccountDomain;
import com.instructure.canvasapi.model.AccountNotification;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import junit.framework.Assert;


@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)

public class AccountNotificationUnitTest extends Assert {

    @Test
    public void testAccountNotifications() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        AccountNotification[] accountNotifications = gson.fromJson(accountNotificationsJSON, AccountNotification[].class);

        assertNotNull(accountNotifications);

        assertEquals(2, accountNotifications.length);

        for(AccountNotification accountNotification : accountNotifications){
            assertNotNull(accountNotification.getId());
            assertNotNull(accountNotification.getSubject());
            assertNotNull(accountNotification.getMessage());
            assertNotNull(accountNotification.getStartDate());
            assertNotNull(accountNotification.getEndDate());
            assertNotNull(accountNotification.getIcon());
        }
    }

    private static final String accountNotificationsJSON = "["
            +"{\"end_at\":\"2015-03-18T06:00:00Z\","
            +"\"icon\":\"warning\","
            +"\"id\":3038,"
            +"\"message\":\"\\u003Cp\\u003EGood weather warning.\\u003C/p\\u003E\","
            +"\"start_at\":\"2015-03-16T06:00:00Z\","
            +"\"subject\":\"Good Weather Warning\","
            +"\"role_ids\":[2441,1642,2442,2443],"
            +"\"roles\":"
                +"["
                    +"\"StudentEnrollment\",\"Nerd\","
                    +"\"TeacherEnrollment\",\"TaEnrollment\""
                +"]"
            +"},"
            +"{\"end_at\":\"2015-03-18T06:00:00Z\","
                +"\"icon\":\"warning\","
                +"\"id\":3038,"
                +"\"message\":\"\\u003Cp\\u003EGood weather warning.\\u003C/p\\u003E\","
                +"\"start_at\":\"2015-03-16T06:00:00Z\","
                +"\"subject\":\"Good Weather Warning\","
                +"\"role_ids\":[2441,1642,2442,2443],"
            +"\"roles\":["
                +"\"StudentEnrollment\",\"Nerd\","
                +"\"TeacherEnrollment\",\"TaEnrollment\""
                +"]"
            +"}"
        +"]";
}