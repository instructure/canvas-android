/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.ui.interaction

import com.instructure.canvas.espresso.Stub
import com.instructure.student.ui.utils.StudentTest
import org.junit.Test

class GroupLinksInteractionTest : StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Stub
    @Test
    fun testGroupLink() {
        // Link to group opens group browser - eg: "/groups/:id"
    }

    @Stub
    @Test
    fun testGroupLink_dashboard() {
        // Link to groups opens dashboard - eg: "/groups"
    }

    @Stub
    @Test
    fun testGroupLink_filePreview() {
        // Link to file preview opens file - eg: "/groups/:id/files/folder/:id?preview=:id"
    }

    @Stub
    @Test
    fun testGroupLink_announcement() {
        // Link to group announcement opens announcement - eg: "/groups/:id/discussion_topics/:id"
    }

    @Stub
    @Test
    fun testGroupLink_announcementList() {
        // Link to group announcements list opens announcements - eg: "/groups/:id/announcements"
    }

    @Stub
    @Test
    fun testGroupLink_discussion() {
        // Link to group discussion opens discussion - eg: "/groups/:id/discussion_topics/:id"
    }

    @Stub
    @Test
    fun testGroupLink_discussionList() {
        // Link to group discussion list opens list - eg: "/groups/:id/discussion_topics"
    }

    @Stub
    @Test
    fun testGroupLink_files() {
        // Link to group files list opens group files list - eg: "/groups/:id/files"
    }

    @Stub
    @Test
    fun testGroupLink_fileFolder() {
        // Link to group files folder opens folder - eg: "/groups/:id/files/folder/:id/"
    }

    @Stub
    @Test
    fun testGroupLink_pagesList() {
        // Link to group page list opens pages - eg: "/groups/:id/pages"
    }

    @Stub
    @Test
    fun testGroupLink_Page() {
        // Link to group page opens page - eg: "/groups/:id/pages/:id"
    }

    @Stub
    @Test
    fun testGroupLink_people() {
        // Link to group people list opens list - eg: "/groups/:id/users"
    }
}
