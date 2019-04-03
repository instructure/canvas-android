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
package com.instructure.parentapp.unit

import com.instructure.canvasapi2.models.ObserverAlert
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.parentapp.presenters.AlertPresenter
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.*

class AlertPresenterTest {

    lateinit var presenter: AlertPresenter
    lateinit var alert1: ObserverAlert
    lateinit var alert2: ObserverAlert

    @Before
    fun setup() {
        presenter = AlertPresenter(User())
        alert1 = ObserverAlert(title = "Hodor", workflowState = "read", date = null)
        alert2 = ObserverAlert(title = "Hodor", workflowState = "read", date = null)
    }

    @Test
    @Throws(Exception::class)
    fun compare_MarkedReadBefore() {
        alert2.workflowState = ""

        assertEquals(1, presenter.compare(alert1, alert2).toLong())
    }

    @Test
    @Throws(Exception::class)
    fun compare_MarkedReadAfter() {
        alert1.workflowState = ""

        assertEquals(-1, presenter.compare(alert1, alert2).toLong())
    }

    @Test
    @Throws(Exception::class)
    fun compare_NullActionDates() {
        assertEquals(0, presenter.compare(alert1, alert2).toLong())
    }

    @Test
    @Throws(Exception::class)
    fun compare_NullActionDateBefore() {
        alert1.date = Date().toApiString()

        assertEquals(1, presenter.compare(alert1, alert2).toLong())
    }

    @Test
    @Throws(Exception::class)
    fun compare_NullActionDateAfter() {
        alert2.date = Date().toApiString()

        assertEquals(-1, presenter.compare(alert1, alert2).toLong())
    }

    @Test
    @Throws(Exception::class)
    fun compare_SameDate() {
        val date = Date()
        alert1.date = date.toApiString()
        alert2.date = date.toApiString()

        assertEquals(0, presenter.compare(alert1, alert2).toLong())
    }

    @Test
    @Throws(Exception::class)
    fun compare_DateBefore() {
        val date1 = Date()
        val date2 = Date(Calendar.getInstance().timeInMillis + 10000)
        alert1.date = date1.toApiString()
        alert2.date = date2.toApiString()

        assertEquals(1, presenter.compare(alert1, alert2).toLong())
    }

    @Test
    @Throws(Exception::class)
    fun compare_DateAfter() {
        val date1 = Date(Calendar.getInstance().timeInMillis + 10000)
        val date2 = Date()
        val a = alert1.copy(date = date1.toApiString())
        val a2 = alert2.copy(date = date2.toApiString())

        assertEquals(-1, presenter.compare(a, a2).toLong())
    }

    @Test
    @Throws(Exception::class)
    fun areContentsTheSame_NullFalse() {
        val a = alert1.copy(title = null)
        val a2 = alert2.copy(title = null)

        assertEquals(false, presenter.areContentsTheSame(a, a2))
    }

    @Test
    @Throws(Exception::class)
    fun areContentsTheSame_True() {
        assertEquals(true, presenter.areContentsTheSame(alert1, alert2))
    }

    @Test
    @Throws(Exception::class)
    fun areContentsTheSame_FalseNotNull() {
        val a2 = alert2.copy(workflowState = "")

        assertEquals(false, presenter.areContentsTheSame(alert2, a2))
    }

}
