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
import com.google.gson.reflect.TypeToken;
import com.instructure.canvasapi.model.Attachment;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.List;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class AttachmentUnitTest extends Assert{

    @Test
    public void test1() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        List<Attachment> list = gson.fromJson(JSON, new TypeToken<List<Attachment>>(){}.getType());
        assertNotNull(list);

        Attachment attachment = list.get(0);

        assertTrue(attachment.getId() > 0);
        assertNotNull(attachment.getDisplayName());
        assertNotNull(attachment.getFilename());
        assertNotNull(attachment.getUrl());
        assertNotNull(attachment.getThumbnailUrl());
        assertNotNull(attachment.getMimeType());
    }

    final String JSON = "[\n" +
            "{\n" +
            "\"id\": 52795562,\n" +
            "\"content-type\": \"image/jpeg\",\n" +
            "\"display_name\": \"thankyou.jpg\",\n" +
            "\"filename\": \"thankyou.jpg\",\n" +
            "\"url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"size\": 151357,\n" +
            "\"created_at\": \"2014-07-14T16:18:57Z\",\n" +
            "\"updated_at\": \"2014-07-14T16:18:59Z\",\n" +
            "\"unlock_at\": null,\n" +
            "\"locked\": false,\n" +
            "\"hidden\": false,\n" +
            "\"lock_at\": null,\n" +
            "\"hidden_for_user\": false,\n" +
            "\"thumbnail_url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"locked_for_user\": false\n" +
            "}\n" +
            "]";

}
