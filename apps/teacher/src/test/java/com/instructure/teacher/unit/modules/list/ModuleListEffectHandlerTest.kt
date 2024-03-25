/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
 */
package com.instructure.teacher.unit.modules.list

import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.apis.ProgressAPI
import com.instructure.canvasapi2.managers.ModuleManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.models.Progress
import com.instructure.canvasapi2.models.postmodels.BulkUpdateProgress
import com.instructure.canvasapi2.models.postmodels.BulkUpdateResponse
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.awaitApiResponse
import com.instructure.pandautils.features.progress.ProgressPreferences
import com.instructure.pandautils.room.appdatabase.daos.ModuleBulkProgressDao
import com.instructure.teacher.features.modules.list.BulkModuleUpdateAction
import com.instructure.teacher.features.modules.list.CollapsedModulesStore
import com.instructure.teacher.features.modules.list.ModuleListEffect
import com.instructure.teacher.features.modules.list.ModuleListEffectHandler
import com.instructure.teacher.features.modules.list.ModuleListEvent
import com.instructure.teacher.features.modules.list.ModuleListPageData
import com.instructure.teacher.features.modules.list.ui.ModuleListView
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import io.mockk.Ordering
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import okhttp3.Headers.Companion.toHeaders
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.util.concurrent.Executors

class ModuleListEffectHandlerTest : Assert() {

    private lateinit var view: ModuleListView
    private lateinit var effectHandler: ModuleListEffectHandler
    private lateinit var consumer: Consumer<ModuleListEvent>
    private lateinit var connection: Connection<ModuleListEffect>
    private val course: CanvasContext = Course()

    private val moduleApi: ModuleAPI.ModuleInterface = mockk(relaxed = true)
    private val progressApi: ProgressAPI.ProgressInterface = mockk(relaxed = true)
    private val progressPreferences: ProgressPreferences = mockk(relaxed = true)
    private val moduleBulkProgressDao: ModuleBulkProgressDao = mockk(relaxed = true)

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
        view = mockk(relaxed = true)
        effectHandler =
            ModuleListEffectHandler(moduleApi, progressApi, progressPreferences, moduleBulkProgressDao).apply {
                view = this@ModuleListEffectHandlerTest.view
            }
        consumer = mockk(relaxed = true)
        connection = effectHandler.connect(consumer)

        every { progressPreferences.cancelledProgressIds } returns mutableSetOf()
        every { progressPreferences.cancelledProgressIds = any() } returns Unit
        coEvery { moduleBulkProgressDao.findByCourseId(any()) } returns emptyList()
    }

    @After
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `ShowModuleItemDetailView calls routeToModuleItem on the view`() {
        val moduleItem = ModuleItem(id = 123L)
        val course = Course(name = "Course 101")
        connection.accept(ModuleListEffect.ShowModuleItemDetailView(moduleItem, course))
        verify(timeout = 100) { view.routeToModuleItem(moduleItem, course) }
        confirmVerified(view)
    }

    @Test
    fun `UpdateModuleItem loads new data and sends correct events`() {
        val items = List(3) { ModuleItem(id = it.toLong(), title = "Before") }
        val updatedItems = items.map { it.copy(title = "After") }
        val itemIds = items.map { it.id }.toSet()

        mockkObject(ModuleManager)
        every { ModuleManager.getModuleItemAsync(any(), any(), any(), any()) } returnsMany updatedItems.map {
            mockk<Deferred<DataResult<ModuleItem>>> {
                coEvery { await() } returns DataResult.Success(it)
            }
        }

        connection.accept(ModuleListEffect.UpdateModuleItems(Course(), items))
        verify(timeout = 500, ordering = Ordering.SEQUENCE) {
            consumer.accept(ModuleListEvent.ModuleItemLoadStatusChanged(itemIds, true))
            consumer.accept(ModuleListEvent.ReplaceModuleItems(updatedItems))
            consumer.accept(ModuleListEvent.ModuleItemLoadStatusChanged(itemIds, false))
        }
        confirmVerified(consumer)
        unmockkObject(ModuleManager)
    }

    @Test
    fun `MarkModuleExpanded calls markModuleCollapsed on CollapsedModulesStore`() {
        val course = Course()
        val moduleId = 123L
        mockkObject(CollapsedModulesStore)
        every { CollapsedModulesStore.markModuleCollapsed(any(), any(), any()) } returns Unit
        connection.accept(ModuleListEffect.MarkModuleExpanded(course, moduleId, false))
        verify { CollapsedModulesStore.markModuleCollapsed(course, moduleId, true) }
        confirmVerified(CollapsedModulesStore)
        unmockkObject(CollapsedModulesStore)
    }

    @Test
    fun `ScrollToItem calls scrollToItem on the view`() {
        val itemId = 123L
        connection.accept(ModuleListEffect.ScrollToItem(itemId))
        verify(timeout = 100) { view.scrollToItem(itemId) }
        confirmVerified(view)
    }

    @Test
    fun `LoadNextPage failure results in correct PageLoaded event`() {
        val errorMessage = "Error"
        val expectedEvent = ModuleListEvent.PageLoaded(
            ModuleListPageData(
                DataResult.Fail(Failure.Network(errorMessage))
            )
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<List<ModuleObject>>(any()) } throws RuntimeException(errorMessage)

        connection.accept(ModuleListEffect.LoadNextPage(course, ModuleListPageData(), null))
        verify(timeout = 100) { consumer.accept(expectedEvent) }
        confirmVerified(consumer)

        unmockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
    }

    @Test
    fun `LoadNextPage results in PageLoaded for single-page list`() {
        val pageModules = makeModulePage()
        val expectedEvent = ModuleListEvent.PageLoaded(
            ModuleListPageData(
                DataResult.Success(pageModules)
            )
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<List<ModuleObject>>(any()) } returns Response.success(pageModules)

        connection.accept(ModuleListEffect.LoadNextPage(course, ModuleListPageData(), null))
        verify(timeout = 100) { consumer.accept(expectedEvent) }
        confirmVerified(consumer)

        unmockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
    }

    @Test
    fun `LoadNextPage results in PageLoaded for multi-page list`() {
        val nextUrl = "fake_next_url"
        val linkHeader = makeLinkHeader(nextUrl)
        val pageModules = makeModulePage()
        val expectedEvent = ModuleListEvent.PageLoaded(
            ModuleListPageData(
                DataResult.Success(pageModules),
                nextPageUrl = nextUrl
            )
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<List<ModuleObject>>(any()) } returns Response.success(pageModules, linkHeader)

        connection.accept(ModuleListEffect.LoadNextPage(course, ModuleListPageData(), null))
        verify(timeout = 100) { consumer.accept(expectedEvent) }
        confirmVerified(consumer)

        unmockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
    }

    @Test
    fun `LoadNextPage results in correct PageLoaded event when scroll-to item is specified`() {
        val moduleId = 654321L
        val scrollToItemId = 123456L
        val nextUrl1 = "fake_next_url_1"
        val nextUrl2 = "fake_next_url_2"
        val firstPageModules = makeModulePage(pageNumber = 0)
        val secondPageModules =
            listOf(ModuleObject(id = moduleId, itemCount = 1, items = listOf(ModuleItem(scrollToItemId))))
        val thirdPageModules = makeModulePage(pageNumber = 1)

        val expectedEvent = ModuleListEvent.PageLoaded(
            ModuleListPageData(
                DataResult.Success(firstPageModules + secondPageModules),
                nextPageUrl = nextUrl2
            )
        )

        mockkObject(CollapsedModulesStore)
        every { CollapsedModulesStore.markModuleCollapsed(any(), any(), any()) } returns Unit

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery {
            awaitApiResponse<List<ModuleObject>>(any())
        } returnsMany listOf(
            Response.success(firstPageModules, makeLinkHeader(nextUrl1)),
            Response.success(secondPageModules, makeLinkHeader(nextUrl2)),
            Response.success(thirdPageModules)
        )

        connection.accept(ModuleListEffect.LoadNextPage(course, ModuleListPageData(), scrollToItemId))
        verify(timeout = 100) {
            CollapsedModulesStore.markModuleCollapsed(course, moduleId, false)
            consumer.accept(expectedEvent)
        }
        confirmVerified(consumer)

        unmockkObject(CollapsedModulesStore)
        unmockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
    }

    @Test
    fun `LoadNextPage results in correct PageLoaded event when scroll-to item is specified but does not exist`() {
        val scrollToItemId = 123456L
        val nextUrl1 = "fake_next_url_1"
        val nextUrl2 = "fake_next_url_2"
        val firstPageModules = makeModulePage(pageNumber = 0)
        val secondPageModules = makeModulePage(pageNumber = 1)
        val thirdPageModules = makeModulePage(pageNumber = 2)

        val expectedEvent = ModuleListEvent.PageLoaded(
            ModuleListPageData(
                DataResult.Success(firstPageModules + secondPageModules + thirdPageModules),
                nextPageUrl = null
            )
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery {
            awaitApiResponse<List<ModuleObject>>(any())
        } returnsMany listOf(
            Response.success(firstPageModules, makeLinkHeader(nextUrl1)),
            Response.success(secondPageModules, makeLinkHeader(nextUrl2)),
            Response.success(thirdPageModules)
        )

        connection.accept(ModuleListEffect.LoadNextPage(course, ModuleListPageData(), scrollToItemId))
        verify(timeout = 100) { consumer.accept(expectedEvent) }
        confirmVerified(consumer)

        unmockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
    }

    @Test
    fun `LoadNextPage results in correct PageLoaded event when page is missing module items`() {
        val itemCount = 3
        val emptyModule = ModuleObject(itemCount = itemCount)
        val missingItems = List(itemCount) { ModuleItem(id = it.toLong()) }
        val expectedModule = emptyModule.copy(items = missingItems)
        val pageModules = makeModulePage() + emptyModule
        val expectedModules = makeModulePage() + expectedModule
        val expectedEvent = ModuleListEvent.PageLoaded(
            ModuleListPageData(
                DataResult.Success(expectedModules)
            )
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<List<ModuleObject>>(any()) } returns Response.success(pageModules)
        coEvery { awaitApi<List<ModuleItem>>(any()) } returns missingItems

        connection.accept(ModuleListEffect.LoadNextPage(course, ModuleListPageData(), null))
        verify(timeout = 100) { consumer.accept(expectedEvent) }
        confirmVerified(consumer)

        unmockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
    }

    @Test
    fun `BulkUpdateStarted results in correct success event`() {
        val pageModules = makeModulePage()
        val expectedEvent = ModuleListEvent.BulkUpdateSuccess(false, BulkModuleUpdateAction.PUBLISH, false)

        coEvery {
            moduleApi.bulkUpdateModules(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(mockk(relaxed = true))
        coEvery { progressApi.getProgress(any(), any()) } returns DataResult.Success(
            Progress(
                1L,
                workflowState = "completed"
            )
        )

        connection.accept(
            ModuleListEffect.BulkUpdateStarted(
                1L,
                false,
                false,
                BulkModuleUpdateAction.PUBLISH
            )
        )

        verify(timeout = 1000) { consumer.accept(expectedEvent) }
        confirmVerified(consumer)
    }

    @Test
    fun `BulkUpdateModules results in correct failed event when call fails`() {
        val pageModules = makeModulePage()
        val expectedEvent = ModuleListEvent.BulkUpdateFailed(false)

        coEvery {
            moduleApi.bulkUpdateModules(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Fail()

        connection.accept(
            ModuleListEffect.BulkUpdateModules(
                course,
                pageModules.map { it.id },
                pageModules.map { it.id } + pageModules.flatMap { it.items.map { it.id } },
                BulkModuleUpdateAction.PUBLISH,
                false,
                false
            )
        )

        verify(timeout = 1000) { consumer.accept(expectedEvent) }
        confirmVerified(consumer)
    }

    @Test
    fun `BulkUpdateStarted results in correct failed event when progress fails`() {
        val pageModules = makeModulePage()
        val expectedEvent = ModuleListEvent.BulkUpdateFailed(false)

        coEvery {
            moduleApi.bulkUpdateModules(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(mockk(relaxed = true))
        coEvery { progressApi.getProgress(any(), any()) } returns DataResult.Success(
            Progress(
                1L,
                workflowState = "failed"
            )
        )

        connection.accept(
            ModuleListEffect.BulkUpdateStarted(
                1L,
                false,
                false,
                BulkModuleUpdateAction.PUBLISH
            )
        )

        verify(timeout = 1000) { consumer.accept(expectedEvent) }
        confirmVerified(consumer)
    }

    @Test
    fun `UpdateModuleItem results in correct success event`() {
        val moduleId = 1L
        val itemId = 2L
        val expectedEvent = ModuleListEvent.ModuleItemUpdateSuccess(
            ModuleItem(id = itemId, moduleId = moduleId, published = true),
            true
        )

        coEvery { moduleApi.publishModuleItem(any(), any(), any(), any(), any(), any()) } returns DataResult.Success(
            ModuleItem(2L, 1L, published = true)
        )

        connection.accept(ModuleListEffect.UpdateModuleItem(course, moduleId, itemId, true))

        verify(timeout = 100) { consumer.accept(expectedEvent) }
        confirmVerified(consumer)
    }

    @Test
    fun `UpdateModuleItem results in correct failed event`() {
        val moduleId = 1L
        val itemId = 2L
        val expectedEvent = ModuleListEvent.ModuleItemUpdateFailed(itemId)

        coEvery { moduleApi.publishModuleItem(any(), any(), any(), any(), any(), any()) } returns DataResult.Fail()

        connection.accept(ModuleListEffect.UpdateModuleItem(course, moduleId, itemId, true))

        verify(timeout = 100) { consumer.accept(expectedEvent) }
        confirmVerified(consumer)
    }

    @Test
    fun `ShowSnackbar calls showSnackbar on view`() {
        val message = 123
        connection.accept(ModuleListEffect.ShowSnackbar(message))
        verify(timeout = 100) { view.showSnackbar(message) }
        confirmVerified(view)
    }

    @Test
    fun `UpdateFileModuleItem calls showUpdateFileDialog on view`() {
        val fileId = 123L
        val contentDetails = ModuleContentDetails()
        connection.accept(ModuleListEffect.UpdateFileModuleItem(fileId, contentDetails))
        verify(timeout = 100) { view.showUpdateFileDialog(fileId, contentDetails) }
        confirmVerified(view)
    }

    @Test
    fun `Bulk update cancel emits correct event`() {
        val pageModules = makeModulePage()
        val expectedEvent = ModuleListEvent.BulkUpdateCancelled

        coEvery {
            moduleApi.bulkUpdateModules(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(
            BulkUpdateResponse(BulkUpdateProgress(Progress(id = 1L)))
        )
        coEvery { progressApi.getProgress(any(), any()) } returns DataResult.Success(
            Progress(
                1L,
                workflowState = "failed"
            )
        )
        every { progressPreferences.cancelledProgressIds } returns mutableSetOf(1L)

        connection.accept(
            ModuleListEffect.BulkUpdateStarted(
                1L,
                false,
                false,
                BulkModuleUpdateAction.PUBLISH
            )
        )

        verify(timeout = 1000) { consumer.accept(expectedEvent) }
        confirmVerified(consumer)
    }

    @Test
    fun `BulkUpdateModules result in correct event`() {
        val pageModules = makeModulePage()
        val expectedEvent = ModuleListEvent.BulkUpdateStarted(
            course,
            0L,
            true,
            false,
            pageModules.map { it.id } + pageModules.flatMap { it.items.map { it.id } },
            BulkModuleUpdateAction.PUBLISH)

        coEvery {
            moduleApi.bulkUpdateModules(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(mockk(relaxed = true))

        connection.accept(
            ModuleListEffect.BulkUpdateModules(
                course,
                pageModules.map { it.id },
                pageModules.map { it.id } + pageModules.flatMap { it.items.map { it.id } },
                BulkModuleUpdateAction.PUBLISH,
                false,
                true
            )
        )

        verify(timeout = 1000) { consumer.accept(expectedEvent) }
        confirmVerified(consumer)
    }

    @Test
    fun `ShowSnackbar with params calls showSnackbar on view`() {
        val message = 123
        val params = arrayOf<Any>("param1", "param2")
        connection.accept(ModuleListEffect.ShowSnackbar(message, params))
        verify(timeout = 100) { view.showSnackbar(message, params) }
        confirmVerified(view)
    }

    private fun makeLinkHeader(nextUrl: String) =
        mapOf("Link" to """<$nextUrl>; rel="next"""").toHeaders()

    private fun makeModulePage(
        moduleCount: Int = 3,
        itemsPerModule: Int = 3,
        pageNumber: Int = 0
    ): List<ModuleObject> {
        return List(3) { moduleIdx ->
            val moduleId = (moduleCount * pageNumber) + moduleIdx + 1L
            ModuleObject(
                id = moduleId,
                itemCount = itemsPerModule,
                items = List(3) { ModuleItem(id = (10000L * moduleId) + it) }
            )
        }
    }

}
