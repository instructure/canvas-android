package com.instructure.pandautils.features.discussion.router

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.instructure.canvasapi2.managers.DiscussionManager
import com.instructure.canvasapi2.managers.FeaturesManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.utils.FeatureFlagProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import junit.framework.Assert.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class DiscussionRouteHelperTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)

    private val testDispatcher = TestCoroutineDispatcher()

    private val featuresManager: FeaturesManager = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)
    private val discussionManager: DiscussionManager = mockk(relaxed = true)
    private val groupManager: GroupManager = mockk(relaxed = true)
    private lateinit var discussionRouteHelper: DiscussionRouteHelper

    @Before
    fun setUp() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        mockkStatic("kotlinx.coroutines.AwaitKt")

        discussionRouteHelper =
            DiscussionRouteHelper(featuresManager, featureFlagProvider, discussionManager, groupManager)

        every { featureFlagProvider.getDiscussionRedesignFeatureFlag() } returns true
    }

    @Test
    fun `Discussion redesign local feature flag`() = runBlockingTest {
        val course = Course()
        every { featureFlagProvider.getDiscussionRedesignFeatureFlag() } returns true
        every { featuresManager.getEnabledFeaturesForCourseAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf("react_discussions_post"))
        }

        assert(discussionRouteHelper.isDiscussionRedesignEnabled(course))

        every { featureFlagProvider.getDiscussionRedesignFeatureFlag() } returns false
        assertFalse(discussionRouteHelper.isDiscussionRedesignEnabled(course))
    }

    @Test
    fun `Feature flag always false for User context`() = runBlockingTest {
        val user = User()
        every { featuresManager.getEnabledFeaturesForCourseAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf("react_discussions_post"))
        }
        assertFalse(discussionRouteHelper.isDiscussionRedesignEnabled(user))
    }

    @Test
    fun `Feature flag always false for Section context`() = runBlockingTest {
        val section = Section()
        every { featuresManager.getEnabledFeaturesForCourseAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf("react_discussions_post"))
        }
        assertFalse(discussionRouteHelper.isDiscussionRedesignEnabled(section))
    }

    @Test
    fun `Discussion feature flag set for Course`() = runBlockingTest {
        val course = Course()
        every { featuresManager.getEnabledFeaturesForCourseAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf("react_discussions_post"))
        }

        assert(discussionRouteHelper.isDiscussionRedesignEnabled(course))
    }

    @Test
    fun `Discussion feature flag not set for Course`() = runBlockingTest {
        val course = Course()
        every { featuresManager.getEnabledFeaturesForCourseAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf())
        }

        assertFalse(discussionRouteHelper.isDiscussionRedesignEnabled(course))
    }

    @Test
    fun `Discussion feature flag set for Group`() = runBlockingTest {
        val group = Group()
        every { featuresManager.getEnabledFeaturesForCourseAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf("react_discussions_post"))
        }

        assert(discussionRouteHelper.isDiscussionRedesignEnabled(group))
    }

    @Test
    fun `Discussion feature flag not set for Group`() = runBlockingTest {
        val group = Group()
        every { featuresManager.getEnabledFeaturesForCourseAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf())
        }

        assertFalse(discussionRouteHelper.isDiscussionRedesignEnabled(group))
    }

    @Test
    fun `Discussion feature flag false if fetching failed`() = runBlockingTest {
        val context = CanvasContext.getGenericContext(CanvasContext.Type.COURSE)
        every { featuresManager.getEnabledFeaturesForCourseAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        assertFalse(discussionRouteHelper.isDiscussionRedesignEnabled(context))
    }

    @Test
    fun `Get Group with DiscussionTopic`() = runBlockingTest {
        val group1 = Group(1L)
        val group2 = Group(2L)
        val discussionTopicHeader = DiscussionTopicHeader(groupTopicChildren = listOf(GroupTopicChild(0L, 2L)))

        every { groupManager.getAllGroupsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(group1, group2))
        }

        assertEquals(Pair(group2, 0L), discussionRouteHelper.getDiscussionGroup(discussionTopicHeader))
    }

    @Test
    fun `Get Group returns null if no Group found`() = runBlockingTest {
        val group1 = Group(1L)
        val group2 = Group(2L)
        val discussionTopicHeader = DiscussionTopicHeader(groupTopicChildren = listOf(GroupTopicChild(0L, 3L)))

        every { groupManager.getAllGroupsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(group1, group2))
        }

        assertNull(discussionRouteHelper.getDiscussionGroup(discussionTopicHeader))
    }
}