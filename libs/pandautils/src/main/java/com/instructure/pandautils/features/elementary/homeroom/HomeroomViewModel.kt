/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.pandautils.features.elementary.homeroom

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.homeroom.itemviewmodels.AnnouncementViewModel
import com.instructure.pandautils.features.elementary.homeroom.itemviewmodels.CourseCardViewModel
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class HomeroomViewModel @Inject constructor(
    private val apiPrefs: ApiPrefs,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<HomeroomViewData>
        get() = _data
    private val _data = MutableLiveData<HomeroomViewData>()

    init {
        loadData()
    }

    private fun loadData() {
        // TODO Load announcements and courses will be implemented in a separate ticket, currently we just use dummy data.
        _state.postValue(ViewState.Loading)

        val greetingString = context.getString(R.string.homeroomWelcomeMessage, apiPrefs.user?.shortName)

        val announcements = createAnnouncements()
        _data.postValue(HomeroomViewData(greetingString, announcements, createDummyCourses()))
        _state.postValue(ViewState.Success)
    }

    private fun createDummyCourses(): List<ItemViewModel> {
        return (1..10).map { CourseCardViewModel(CourseCardViewData("Course: $it")) }
    }

    private fun createAnnouncements(): List<AnnouncementViewModel> {
        return listOf(AnnouncementViewModel(AnnouncementViewData("Announcement 1", createDummyHtml())),
            AnnouncementViewModel(AnnouncementViewData("Announcement 2", createDummyHtml())))
    }

    private fun createDummyHtml(): String {
        return """
            <h1>Ex ea difficultate illae fallaciloquae, ut ait Accius, malitiae natae sunt.</h1>

<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. <a href="http://loripsum.net/" target="_blank">Non potes, nisi retexueris illa.</a> Non enim, si malum est dolor, carere eo malo satis est ad bene vivendum. <b>Nunc omni virtuti vitium contrario nomine opponitur.</b> Pollicetur certe. Duo Reges: constructio interrete. Roges enim Aristonem, bonane ei videantur haec: vacuitas doloris, divitiae, valitudo; Deinde dolorem quem maximum? Memini vero, inquam; </p>

<blockquote cite="http://loripsum.net">
	Nos beatam vitam non depulsione mali, sed adeptione boni iudicemus, nec eam cessando, sive gaudentem, ut Aristippus, sive non dolentem, ut hic, sed agendo aliquid considerandove quaeramus.
</blockquote>


<ul>
	<li>Primum cur ista res digna odio est, nisi quod est turpis?</li>
	<li>Ut in geometria, prima si dederis, danda sunt omnia.</li>
	<li>Tum Torquatus: Prorsus, inquit, assentior;</li>
	<li>Et si turpitudinem fugimus in statu et motu corporis, quid est cur pulchritudinem non sequamur?</li>
</ul>
        """.trimIndent()
    }

    // Currently we do nothing here, api calls will be implemented in an other ticket.
    fun refresh() {
        _state.postValue(ViewState.Refresh)
        _state.postValue(ViewState.Success)
    }
}