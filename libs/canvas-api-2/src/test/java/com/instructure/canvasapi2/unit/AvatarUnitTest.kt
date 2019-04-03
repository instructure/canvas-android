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

package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.models.Avatar
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class AvatarUnitTest : Assert() {

    @Test
    fun test1() {
        val list: Array<Avatar> = JSON.parse()
        for (a in list) {
            Assert.assertNotNull(a)
            Assert.assertNotNull(a.displayName)
            Assert.assertNotNull(a.url)
            Assert.assertNotNull(a.token)
            Assert.assertNotNull(a.type)
        }
    }

    @Language("JSON")
    private val JSON = """
      [
        {
          "url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "type": "gravatar",
          "display_name": "gravatar pic",
          "token": "71ad3ca870b57cfdeb739b47a18b6c2c42a5435f"
        },
        {
          "id": 52800462,
          "content-type": "image/jpeg",
          "display_name": "profile.jpg",
          "filename": "profile.jpg",
          "url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "size": 3910,
          "created_at": "2014-07-14T16:41:08Z",
          "updated_at": "2014-07-14T16:41:10Z",
          "unlock_at": null,
          "locked": false,
          "hidden": false,
          "lock_at": null,
          "hidden_for_user": false,
          "thumbnail_url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "locked_for_user": false,
          "type": "attachment",
          "token": "3eb01ca18bdea281407a2beb651c2ac55a1bcaf0"
        },
        {
          "id": 51168398,
          "content-type": "image/jpeg",
          "display_name": "profilePic-4.jpg",
          "filename": "profilePic.jpg",
          "url": "https://mobiledev.instructure.com/images/thumbnails/51168398/dTmaGtbBfx3GlefATOpdmAv8LPJW0Rg3asCDyuXE",
          "size": 14487,
          "created_at": "2014-05-27T17:47:32Z",
          "updated_at": "2014-05-27T17:47:33Z",
          "unlock_at": null,
          "locked": false,
          "hidden": false,
          "lock_at": null,
          "hidden_for_user": false,
          "thumbnail_url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "locked_for_user": false,
          "type": "attachment",
          "token": "5a7ff4def15ea9c05615e080a327b22c5d3c45b7"
        },
        {
          "id": 50823190,
          "content-type": "image/jpeg",
          "display_name": "profilePic-3.jpg",
          "filename": "profilePic.jpg",
          "url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "size": 1463,
          "created_at": "2014-05-15T19:01:24Z",
          "updated_at": "2014-05-15T19:01:25Z",
          "unlock_at": null,
          "locked": false,
          "hidden": false,
          "lock_at": null,
          "hidden_for_user": false,
          "thumbnail_url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "locked_for_user": false,
          "type": "attachment",
          "token": "a11ce733fad551b8d7d98dc757cae33333c6a31b"
        },
        {
          "id": 50823185,
          "content-type": "image/jpeg",
          "display_name": "profilePic.jpg",
          "filename": "profilePic.jpg",
          "url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "size": 1463,
          "created_at": "2014-05-15T19:00:57Z",
          "updated_at": "2014-05-15T19:00:58Z",
          "unlock_at": null,
          "locked": false,
          "hidden": false,
          "lock_at": null,
          "hidden_for_user": false,
          "thumbnail_url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "locked_for_user": false,
          "type": "attachment",
          "token": "346da504821d30fae8428360b4f3007f279f462f"
        },
        {
          "id": 49872240,
          "content-type": "image/jpeg",
          "display_name": "IMG_20140321_195853.jpg",
          "filename": "IMG_20140321_195853.jpg",
          "url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "size": 1694719,
          "created_at": "2014-04-24T16:06:22Z",
          "updated_at": "2014-04-24T16:06:24Z",
          "unlock_at": null,
          "locked": false,
          "hidden": false,
          "lock_at": null,
          "hidden_for_user": false,
          "thumbnail_url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "locked_for_user": false,
          "type": "attachment",
          "token": "3172c8eb7fbb14d3158e5af190269c1cfa43933d"
        },
        {
          "id": 49872235,
          "content-type": "image/jpeg",
          "display_name": "IMG_20140322_083110.jpg",
          "filename": "IMG_20140322_083110.jpg",
          "url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "size": 2395180,
          "created_at": "2014-04-24T16:06:18Z",
          "updated_at": "2014-04-24T16:06:20Z",
          "unlock_at": null,
          "locked": false,
          "hidden": false,
          "lock_at": null,
          "hidden_for_user": false,
          "thumbnail_url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "locked_for_user": false,
          "type": "attachment",
          "token": "20304a40e120a8fc5fb1ccf8dd2cf282fb050ca4"
        },
        {
          "id": 49869976,
          "content-type": "image/jpeg",
          "display_name": "IMG_20140413_171121.jpg",
          "filename": "IMG_20140413_171121.jpg",
          "url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "size": 2021615,
          "created_at": "2014-04-24T15:44:23Z",
          "updated_at": "2014-04-24T15:44:25Z",
          "unlock_at": null,
          "locked": false,
          "hidden": false,
          "lock_at": null,
          "hidden_for_user": false,
          "thumbnail_url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "locked_for_user": false,
          "type": "attachment",
          "token": "697541233761bb60051afb36e6a7e2059dbc37fd"
        },
        {
          "id": 49411091,
          "content-type": "image/jpeg",
          "display_name": "profilePic-5.jpg",
          "filename": "profilePic.jpg",
          "url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "size": 8778,
          "created_at": "2014-04-09T22:18:08Z",
          "updated_at": "2014-04-09T22:18:09Z",
          "unlock_at": null,
          "locked": false,
          "hidden": false,
          "lock_at": null,
          "hidden_for_user": false,
          "thumbnail_url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "locked_for_user": false,
          "type": "attachment",
          "token": "58ecf5d77d594c63d06fd6a6de1574ee3fcd7d26"
        },
        {
          "id": 49410466,
          "content-type": "image/jpeg",
          "display_name": "profilePic-2.jpg",
          "filename": "profilePic.jpg",
          "url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "size": 6977,
          "created_at": "2014-04-09T21:51:12Z",
          "updated_at": "2014-04-09T21:51:13Z",
          "unlock_at": null,
          "locked": false,
          "hidden": false,
          "lock_at": null,
          "hidden_for_user": false,
          "thumbnail_url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "locked_for_user": false,
          "type": "attachment",
          "token": "f48457208984e286a4d4a50f685e72d5009a3023"
        },
        {
          "id": 49410370,
          "content-type": "image/jpeg",
          "display_name": "profilePic-1.jpg",
          "filename": "profilePic.jpg",
          "url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "size": 11694,
          "created_at": "2014-04-09T21:46:08Z",
          "updated_at": "2014-04-09T21:46:09Z",
          "unlock_at": null,
          "locked": false,
          "hidden": false,
          "lock_at": null,
          "hidden_for_user": false,
          "thumbnail_url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "locked_for_user": false,
          "type": "attachment",
          "token": "404e494ff054b89909ce63a73ce0bc7af1ffd38f"
        },
        {
          "id": 49013380,
          "content-type": "image/jpeg",
          "display_name": "That Board_8.jpg",
          "filename": "That+Board_8.jpg",
          "url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "size": 312626,
          "created_at": "2014-04-01T14:56:04Z",
          "updated_at": "2014-04-01T14:56:05Z",
          "unlock_at": null,
          "locked": false,
          "hidden": false,
          "lock_at": null,
          "hidden_for_user": false,
          "thumbnail_url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "locked_for_user": false,
          "type": "attachment",
          "token": "7739b877293b27b3fc6b02aad1b3b496a8eb89fa"
        },
        {
          "url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "type": "no_pic",
          "display_name": "no pic",
          "token": "4e468a12ffe00af8549f0f440a7e84d2f2a39578"
        }
      ]"""

}
