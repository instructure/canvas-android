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
package com.instructure.teacher.unit.modules

import com.instructure.canvasapi2.managers.FeaturesManager
import com.instructure.canvasapi2.managers.FileFolderManager
import com.instructure.canvasapi2.managers.ModuleManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.awaitApiResponse
import com.instructure.teacher.features.modules.list.*
import com.instructure.teacher.features.modules.list.ui.ModuleListView
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import io.mockk.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import okhttp3.Headers
import okhttp3.Headers.Companion.toHeaders
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

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
        view = mockk(relaxed = true)
        effectHandler = ModuleListEffectHandler().apply { view = this@ModuleListEffectHandlerTest.view }
        consumer = mockk(relaxed = true)
        connection = effectHandler.connect(consumer)
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
    fun `LoadFileInfo loads file data and calls routeToFile on the view`() {
        val item = ModuleItem(id = 123L, type = "File", url = "fake url")
        val course = Course(name = "Course 101")

        val file: FileFolder = mockk()
        val licenses = arrayListOf(License("1", "Fake license", "fake url"))

        mockkObject(FileFolderManager, FeaturesManager)

        every { FileFolderManager.getFileFolderFromUrlAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(file)
        }

        every { FeaturesManager.getEnabledFeaturesForCourseAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf("usage_rights_required"))
        }

        every { FileFolderManager.getCourseFileLicensesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(licenses)
        }

        connection.accept(ModuleListEffect.LoadFileInfo(item, course))
        verify(timeout = 100, ordering = Ordering.SEQUENCE) {
            consumer.accept(ModuleListEvent.ModuleItemLoadStatusChanged(setOf(item.id), true))
            view.routeToFile(course, file, true, licenses)
            consumer.accept(ModuleListEvent.ModuleItemLoadStatusChanged(setOf(item.id), false))
        }
        confirmVerified(view)

        unmockkObject(FileFolderManager, FeaturesManager)
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
        verify(timeout = 100, ordering = Ordering.SEQUENCE) {
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
        val secondPageModules = listOf(ModuleObject(id = moduleId, itemCount = 1, items = listOf(ModuleItem(scrollToItemId))))
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


    private fun makeLinkHeader(nextUrl: String) =
        mapOf("Link" to """<$nextUrl>; rel="next"""").toHeaders()

}
