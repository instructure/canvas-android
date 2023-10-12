package com.instructure.student.features.discussion.routing.datasource

import com.instructure.canvasapi2.apis.DiscussionAPI
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.GroupTopicChild
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.utils.FeatureFlagProvider
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class DiscussionRouteHelperStudentNetworkDataSourceTest {
    private val discussionApi: DiscussionAPI.DiscussionInterface = mockk(relaxed = true)
    private val groupApi: GroupAPI.GroupInterface = mockk(relaxed = true)
    private val featuresApi: FeaturesAPI.FeaturesInterface = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val dataSource = DiscussionRouteHelperStudentNetworkDataSource(discussionApi, groupApi, featuresApi, featureFlagProvider)

    @Test
    fun `getEnabledFeaturesForCourse returns api result if discussion redesign flag is true`() = runTest {
        val canvasContext = CanvasContext.emptyCourseContext()
        coEvery { featureFlagProvider.getDiscussionRedesignFeatureFlag() } returns true
        coEvery { featuresApi.getEnabledFeaturesForCourse(any(), any()) } returns DataResult.Success(listOf("react_discussions_post"))

        val expected = true

        val result = dataSource.getEnabledFeaturesForCourse(canvasContext, true)

        assertEquals(expected, result)
    }

    @Test
    fun `getEnabledFeaturesForCourse returns api result if discussion redesign flag is false`() = runTest {
        val canvasContext = CanvasContext.defaultCanvasContext()
        coEvery { featureFlagProvider.getDiscussionRedesignFeatureFlag() } returns false
        coEvery { featuresApi.getEnabledFeaturesForCourse(any(), any()) } returns DataResult.Success(listOf("react_discussions_post"))

        val expected = false

        val result = dataSource.getEnabledFeaturesForCourse(canvasContext, true)

        assertEquals(expected, result)
    }

    @Test
    fun `getDiscussionTopicHeader returns correct data`() = runTest {
        val canvasContext = CanvasContext.defaultCanvasContext()
        val expected = DiscussionTopicHeader(1L)

        coEvery { discussionApi.getDiscussionTopicHeader(any(), any(), any(), any()) } returns DataResult.Success(expected)

        val result = dataSource.getDiscussionTopicHeader(canvasContext, 1L, true)

        assertEquals(expected, result)
    }

    @Test
    fun `getAllGroups returns correct data if group exists`() = runTest {
        val discussionTopicHeader = DiscussionTopicHeader(1L, groupTopicChildren = listOf(
            GroupTopicChild(1L, 1L)
        ))
        val groups = listOf(Group(1L))
        val expected = Pair(groups[0], 1L)

        coEvery { groupApi.getFirstPageGroups(any()) } returns DataResult.Success(groups)
        coEvery { groupApi.getNextPageGroups(any(), any()) } returns DataResult.Success(emptyList())

        val result = dataSource.getAllGroups(discussionTopicHeader, 1L, true)

        assertEquals(expected, result)
    }
}