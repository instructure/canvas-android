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
import com.instructure.canvasapi.model.QuizPermission;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class QuizPermissionUnitTest extends Assert {

    @Test
    public void testQuizPermissions() {
        Gson gson = CanvasRestAdapter.getGSONParser();

        QuizPermission quizPermission = gson.fromJson(quizPermissionJSON, QuizPermission.class);

        assertNotNull(quizPermission);

        assertFalse(quizPermission.canManage());
        assertFalse(quizPermission.canReadStatistics());
        assertTrue(quizPermission.canRead());
        assertFalse(quizPermission.canUpdate());
        assertFalse(quizPermission.canDelete());
        assertFalse(quizPermission.canCreate());
        assertTrue(quizPermission.canSubmit());
        assertFalse(quizPermission.canGrade());
        assertFalse(quizPermission.canReviewGrades());
        assertFalse(quizPermission.canViewAnswerAudits());
    }


    String quizPermissionJSON =
            "{\n" +
            "\"read_statistics\": false,\n" +
            "\"manage\": false,\n" +
            "\"read\": true,\n" +
            "\"update\": false,\n" +
            "\"delete\": false,\n" +
            "\"create\": false,\n" +
            "\"submit\": true,\n" +
            "\"grade\": false,\n" +
            "\"review_grades\": false,\n" +
            "\"view_answer_audits\": false\n" +
            "}";
}
