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
import com.instructure.canvasapi.model.Avatar;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.List;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class AvatarUnitTest extends Assert{

    @Test
    public void test1() {
        ///users/self/avatars
        final Gson gson = CanvasRestAdapter.getGSONParser();
        final List<Avatar> list = gson.fromJson(JSON, new TypeToken<List<Avatar>>(){}.getType());

        for(Avatar a : list) {
            assertNotNull(a);
            assertNotNull(a.getDisplayName());
            assertNotNull(a.getUrl());
            assertNotNull(a.getToken());
            assertNotNull(a.getType());
        }
    }

    final String JSON = "[\n" +
            "{\n" +
            "\"url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"type\": \"gravatar\",\n" +
            "\"display_name\": \"gravatar pic\",\n" +
            "\"token\": \"71ad3ca870b57cfdeb739b47a18b6c2c42a5435f\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": 52800462,\n" +
            "\"content-type\": \"image/jpeg\",\n" +
            "\"display_name\": \"profile.jpg\",\n" +
            "\"filename\": \"profile.jpg\",\n" +
            "\"url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"size\": 3910,\n" +
            "\"created_at\": \"2014-07-14T16:41:08Z\",\n" +
            "\"updated_at\": \"2014-07-14T16:41:10Z\",\n" +
            "\"unlock_at\": null,\n" +
            "\"locked\": false,\n" +
            "\"hidden\": false,\n" +
            "\"lock_at\": null,\n" +
            "\"hidden_for_user\": false,\n" +
            "\"thumbnail_url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"locked_for_user\": false,\n" +
            "\"type\": \"attachment\",\n" +
            "\"token\": \"3eb01ca18bdea281407a2beb651c2ac55a1bcaf0\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": 51168398,\n" +
            "\"content-type\": \"image/jpeg\",\n" +
            "\"display_name\": \"profilePic-4.jpg\",\n" +
            "\"filename\": \"profilePic.jpg\",\n" +
            "\"url\": \"https://mobiledev.instructure.com/images/thumbnails/51168398/dTmaGtbBfx3GlefATOpdmAv8LPJW0Rg3asCDyuXE\",\n" +
            "\"size\": 14487,\n" +
            "\"created_at\": \"2014-05-27T17:47:32Z\",\n" +
            "\"updated_at\": \"2014-05-27T17:47:33Z\",\n" +
            "\"unlock_at\": null,\n" +
            "\"locked\": false,\n" +
            "\"hidden\": false,\n" +
            "\"lock_at\": null,\n" +
            "\"hidden_for_user\": false,\n" +
            "\"thumbnail_url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"locked_for_user\": false,\n" +
            "\"type\": \"attachment\",\n" +
            "\"token\": \"5a7ff4def15ea9c05615e080a327b22c5d3c45b7\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": 50823190,\n" +
            "\"content-type\": \"image/jpeg\",\n" +
            "\"display_name\": \"profilePic-3.jpg\",\n" +
            "\"filename\": \"profilePic.jpg\",\n" +
            "\"url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"size\": 1463,\n" +
            "\"created_at\": \"2014-05-15T19:01:24Z\",\n" +
            "\"updated_at\": \"2014-05-15T19:01:25Z\",\n" +
            "\"unlock_at\": null,\n" +
            "\"locked\": false,\n" +
            "\"hidden\": false,\n" +
            "\"lock_at\": null,\n" +
            "\"hidden_for_user\": false,\n" +
            "\"thumbnail_url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"locked_for_user\": false,\n" +
            "\"type\": \"attachment\",\n" +
            "\"token\": \"a11ce733fad551b8d7d98dc757cae33333c6a31b\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": 50823185,\n" +
            "\"content-type\": \"image/jpeg\",\n" +
            "\"display_name\": \"profilePic.jpg\",\n" +
            "\"filename\": \"profilePic.jpg\",\n" +
            "\"url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"size\": 1463,\n" +
            "\"created_at\": \"2014-05-15T19:00:57Z\",\n" +
            "\"updated_at\": \"2014-05-15T19:00:58Z\",\n" +
            "\"unlock_at\": null,\n" +
            "\"locked\": false,\n" +
            "\"hidden\": false,\n" +
            "\"lock_at\": null,\n" +
            "\"hidden_for_user\": false,\n" +
            "\"thumbnail_url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"locked_for_user\": false,\n" +
            "\"type\": \"attachment\",\n" +
            "\"token\": \"346da504821d30fae8428360b4f3007f279f462f\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": 49872240,\n" +
            "\"content-type\": \"image/jpeg\",\n" +
            "\"display_name\": \"IMG_20140321_195853.jpg\",\n" +
            "\"filename\": \"IMG_20140321_195853.jpg\",\n" +
            "\"url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"size\": 1694719,\n" +
            "\"created_at\": \"2014-04-24T16:06:22Z\",\n" +
            "\"updated_at\": \"2014-04-24T16:06:24Z\",\n" +
            "\"unlock_at\": null,\n" +
            "\"locked\": false,\n" +
            "\"hidden\": false,\n" +
            "\"lock_at\": null,\n" +
            "\"hidden_for_user\": false,\n" +
            "\"thumbnail_url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"locked_for_user\": false,\n" +
            "\"type\": \"attachment\",\n" +
            "\"token\": \"3172c8eb7fbb14d3158e5af190269c1cfa43933d\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": 49872235,\n" +
            "\"content-type\": \"image/jpeg\",\n" +
            "\"display_name\": \"IMG_20140322_083110.jpg\",\n" +
            "\"filename\": \"IMG_20140322_083110.jpg\",\n" +
            "\"url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"size\": 2395180,\n" +
            "\"created_at\": \"2014-04-24T16:06:18Z\",\n" +
            "\"updated_at\": \"2014-04-24T16:06:20Z\",\n" +
            "\"unlock_at\": null,\n" +
            "\"locked\": false,\n" +
            "\"hidden\": false,\n" +
            "\"lock_at\": null,\n" +
            "\"hidden_for_user\": false,\n" +
            "\"thumbnail_url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"locked_for_user\": false,\n" +
            "\"type\": \"attachment\",\n" +
            "\"token\": \"20304a40e120a8fc5fb1ccf8dd2cf282fb050ca4\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": 49869976,\n" +
            "\"content-type\": \"image/jpeg\",\n" +
            "\"display_name\": \"IMG_20140413_171121.jpg\",\n" +
            "\"filename\": \"IMG_20140413_171121.jpg\",\n" +
            "\"url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"size\": 2021615,\n" +
            "\"created_at\": \"2014-04-24T15:44:23Z\",\n" +
            "\"updated_at\": \"2014-04-24T15:44:25Z\",\n" +
            "\"unlock_at\": null,\n" +
            "\"locked\": false,\n" +
            "\"hidden\": false,\n" +
            "\"lock_at\": null,\n" +
            "\"hidden_for_user\": false,\n" +
            "\"thumbnail_url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"locked_for_user\": false,\n" +
            "\"type\": \"attachment\",\n" +
            "\"token\": \"697541233761bb60051afb36e6a7e2059dbc37fd\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": 49411091,\n" +
            "\"content-type\": \"image/jpeg\",\n" +
            "\"display_name\": \"profilePic-5.jpg\",\n" +
            "\"filename\": \"profilePic.jpg\",\n" +
            "\"url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"size\": 8778,\n" +
            "\"created_at\": \"2014-04-09T22:18:08Z\",\n" +
            "\"updated_at\": \"2014-04-09T22:18:09Z\",\n" +
            "\"unlock_at\": null,\n" +
            "\"locked\": false,\n" +
            "\"hidden\": false,\n" +
            "\"lock_at\": null,\n" +
            "\"hidden_for_user\": false,\n" +
            "\"thumbnail_url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"locked_for_user\": false,\n" +
            "\"type\": \"attachment\",\n" +
            "\"token\": \"58ecf5d77d594c63d06fd6a6de1574ee3fcd7d26\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": 49410466,\n" +
            "\"content-type\": \"image/jpeg\",\n" +
            "\"display_name\": \"profilePic-2.jpg\",\n" +
            "\"filename\": \"profilePic.jpg\",\n" +
            "\"url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"size\": 6977,\n" +
            "\"created_at\": \"2014-04-09T21:51:12Z\",\n" +
            "\"updated_at\": \"2014-04-09T21:51:13Z\",\n" +
            "\"unlock_at\": null,\n" +
            "\"locked\": false,\n" +
            "\"hidden\": false,\n" +
            "\"lock_at\": null,\n" +
            "\"hidden_for_user\": false,\n" +
            "\"thumbnail_url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"locked_for_user\": false,\n" +
            "\"type\": \"attachment\",\n" +
            "\"token\": \"f48457208984e286a4d4a50f685e72d5009a3023\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": 49410370,\n" +
            "\"content-type\": \"image/jpeg\",\n" +
            "\"display_name\": \"profilePic-1.jpg\",\n" +
            "\"filename\": \"profilePic.jpg\",\n" +
            "\"url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"size\": 11694,\n" +
            "\"created_at\": \"2014-04-09T21:46:08Z\",\n" +
            "\"updated_at\": \"2014-04-09T21:46:09Z\",\n" +
            "\"unlock_at\": null,\n" +
            "\"locked\": false,\n" +
            "\"hidden\": false,\n" +
            "\"lock_at\": null,\n" +
            "\"hidden_for_user\": false,\n" +
            "\"thumbnail_url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"locked_for_user\": false,\n" +
            "\"type\": \"attachment\",\n" +
            "\"token\": \"404e494ff054b89909ce63a73ce0bc7af1ffd38f\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": 49013380,\n" +
            "\"content-type\": \"image/jpeg\",\n" +
            "\"display_name\": \"That Board_8.jpg\",\n" +
            "\"filename\": \"That+Board_8.jpg\",\n" +
            "\"url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"size\": 312626,\n" +
            "\"created_at\": \"2014-04-01T14:56:04Z\",\n" +
            "\"updated_at\": \"2014-04-01T14:56:05Z\",\n" +
            "\"unlock_at\": null,\n" +
            "\"locked\": false,\n" +
            "\"hidden\": false,\n" +
            "\"lock_at\": null,\n" +
            "\"hidden_for_user\": false,\n" +
            "\"thumbnail_url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"locked_for_user\": false,\n" +
            "\"type\": \"attachment\",\n" +
            "\"token\": \"7739b877293b27b3fc6b02aad1b3b496a8eb89fa\"\n" +
            "},\n" +
            "{\n" +
            "\"url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"type\": \"no_pic\",\n" +
            "\"display_name\": \"no pic\",\n" +
            "\"token\": \"4e468a12ffe00af8549f0f440a7e84d2f2a39578\"\n" +
            "}\n" +
            "]";

}
