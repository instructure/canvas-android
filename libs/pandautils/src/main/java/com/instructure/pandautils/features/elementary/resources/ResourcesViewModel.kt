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
package com.instructure.pandautils.features.elementary.resources

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.pandautils.features.elementary.resources.itemviewmodels.ContactInfoItemViewModel
import com.instructure.pandautils.features.elementary.resources.itemviewmodels.ImportantLinksItemViewModel
import com.instructure.pandautils.features.elementary.resources.itemviewmodels.LtiApplicationItemViewModel
import com.instructure.pandautils.features.elementary.resources.itemviewmodels.ResourcesHeaderViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.toPx
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResourcesViewModel @Inject constructor(
    private val resources: Resources,
    private val courseManager: CourseManager,
    private val userManager: UserManager
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<ResourcesViewData>
        get() = _data
    private val _data = MutableLiveData(ResourcesViewData(emptyList(), emptyList()))

    val events: LiveData<Event<ResourcesAction>>
        get() = _events
    private val _events = MutableLiveData<Event<ResourcesAction>>()

    init {
        loadData()
    }

    private fun loadData() {
        _state.postValue(ViewState.Loading)
        viewModelScope.launch {
            delay(2000)
            val dummyData = createDummyData()
            _state.postValue(ViewState.Success)
            _data.postValue(dummyData)
        }
    }

    private fun createDummyData(): ResourcesViewData {
        val importantLinkHtml = ImportantLinksItemViewModel("""
            <p>
            Aptent, tempus sociosqu elit hendrerit maecenas magnis netus maecenas nisi porta scelerisque lacus. Suspendisse neque auctor iaculis fringilla. Justo mollis platea, magna risus dignissim dictum. Ornare convallis sagittis sollicitudin aptent orci. Fusce mauris, platea dictum mi magna? Tempor phasellus torquent quisque cubilia magnis per? Rutrum accumsan bibendum nisl phasellus feugiat eros. Molestie nostra varius non netus ipsum dolor auctor vehicula diam at taciti lectus? Vestibulum posuere arcu ultricies. Felis accumsan ad aliquam mi lobortis. Morbi erat dictumst risus. Neque magnis fringilla mi est.
            </p>
            <p>
            Congue ultricies condimentum, aliquam tincidunt laoreet dapibus volutpat dapibus laoreet etiam habitant. Habitasse nisl elementum a risus nullam elementum. Malesuada nisl elementum sollicitudin lacus quis ad curabitur. Risus cras placerat pretium amet hac, amet placerat placerat bibendum commodo turpis lacus. Neque taciti sed dignissim mattis vehicula conubia nisi tortor ullamcorper dolor id magna. Integer dolor consequat lorem congue at platea, enim elementum sed bibendum egestas. Malesuada dis molestie magnis, vivamus nec? Justo fusce montes bibendum neque eu nullam. Varius id platea nec sagittis! Pretium proin dictumst ante lacus rutrum at quis?
            </p>
            <p>
            Imperdiet torquent at dictumst dapibus nisi semper morbi. Commodo sit suscipit dis ipsum accumsan commodo habitasse ullamcorper laoreet. Mauris dictumst curae; urna penatibus nisl torquent varius tortor nunc. Pharetra torquent pretium massa ipsum consequat! Duis proin nullam torquent lacinia placerat orci tempor integer. Nostra integer netus tempus rutrum et cras suscipit nunc? Aliquam primis nisi lobortis litora vel convallis, bibendum dui. Nisi integer quam luctus vel euismod lobortis rhoncus natoque a condimentum mauris nulla. Malesuada ante malesuada; elit nulla pharetra hendrerit sapien ornare ultricies imperdiet! Rutrum inceptos dictumst pellentesque et.
            </p>
            <p>
            Mattis euismod montes ligula per lacus blandit. Bibendum pellentesque diam dictum fermentum habitasse auctor fusce fringilla. Varius ac congue nunc libero eget senectus auctor varius arcu tempus feugiat maecenas. Interdum mi imperdiet etiam! Facilisi sodales faucibus est fames commodo? Inceptos, nunc pulvinar conubia vitae cursus vulputate sodales ullamcorper. Semper vestibulum metus elit ultricies potenti tellus imperdiet volutpat. Cum ridiculus luctus, tortor maecenas vel vulputate nunc! Tincidunt justo netus parturient litora placerat dictumst non at litora ante sit felis. Magnis.
            </p>
            <p>
            Lacus mauris torquent ad at vivamus sollicitudin cum. Nisi quisque nunc fermentum? Semper mus hac class senectus leo senectus. Magnis dolor quam pharetra aliquam cum venenatis, ridiculus tempus molestie volutpat etiam. Himenaeos a ligula pellentesque cubilia nisl habitant amet ultricies quam bibendum. Rhoncus litora ad potenti vehicula mauris. Id facilisis vel condimentum vel vestibulum habitant platea habitant, cum!
            </p>
        """.trimIndent())

        val importantLinkHtml2 = ImportantLinksItemViewModel("""
            <p>
            Aptent, tempus sociosqu elit hendrerit maecenas magnis netus maecenas nisi porta scelerisque lacus. Suspendisse neque auctor iaculis fringilla. Justo mollis platea, magna risus dignissim dictum. Ornare convallis sagittis sollicitudin aptent orci. Fusce mauris, platea dictum mi magna? Tempor phasellus torquent quisque cubilia magnis per? Rutrum accumsan bibendum nisl phasellus feugiat eros. Molestie nostra varius non netus ipsum dolor auctor vehicula diam at taciti lectus? Vestibulum posuere arcu ultricies. Felis accumsan ad aliquam mi lobortis. Morbi erat dictumst risus. Neque magnis fringilla mi est.
            </p>
            <p>
            Congue ultricies condimentum, aliquam tincidunt laoreet dapibus volutpat dapibus laoreet etiam habitant. Habitasse nisl elementum a risus nullam elementum. Malesuada nisl elementum sollicitudin lacus quis ad curabitur. Risus cras placerat pretium amet hac, amet placerat placerat bibendum commodo turpis lacus. Neque taciti sed dignissim mattis vehicula conubia nisi tortor ullamcorper dolor id magna. Integer dolor consequat lorem congue at platea, enim elementum sed bibendum egestas. Malesuada dis molestie magnis, vivamus nec? Justo fusce montes bibendum neque eu nullam. Varius id platea nec sagittis! Pretium proin dictumst ante lacus rutrum at quis?
            </p>
        """.trimIndent())

        val applicationsHeader = ResourcesHeaderViewModel(ResourcesHeaderViewData("Student Applications"))

        val lti1 = LtiApplicationItemViewModel(LtiApplicationViewData("Aeris", "", ""))
        val lti2 = LtiApplicationItemViewModel(LtiApplicationViewData("Core", "", ""))
        val lti3 = LtiApplicationItemViewModel(LtiApplicationViewData("Flipgrid", "", ""), 28.toPx)

        val staffInfo = ResourcesHeaderViewModel(ResourcesHeaderViewData("Staff info", true))

        val staff1 = ContactInfoItemViewModel(ContactInfoViewData("Mrs. Thompson", "Teacher, Office hours 9:00am to 3:00pm, Monday to Friday", ""))
        val staff2 = ContactInfoItemViewModel(ContactInfoViewData("Mrs. Johnson", "Teacher Assistant", ""))
        val staff3 = ContactInfoItemViewModel(ContactInfoViewData("Mrs. Wu", "Teacher Assistant", ""))

        val importantLinksItems = listOf(importantLinkHtml, importantLinkHtml2)
        val otherItems = listOf(applicationsHeader, lti1, lti2, lti3, staffInfo, staff1, staff2, staff3)

        return ResourcesViewData(importantLinksItems, otherItems)
    }

    fun refresh() {

    }
}