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

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.awaitApiResponse
import com.instructure.teacher.features.modules.list.ModuleListEffectHandler
import com.instructure.teacher.features.modules.list.ModuleListPageData
import com.instructure.teacher.features.modules.list.ModulesListEffect
import com.instructure.teacher.features.modules.list.ModulesListEvent
import com.instructure.teacher.features.modules.list.ui.ModuleListView
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import okhttp3.Headers
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.util.concurrent.Executors

class ModuleListEffectHandlerTest : Assert() {

    private lateinit var view: ModuleListView
    private lateinit var effectHandler: ModuleListEffectHandler
    private lateinit var consumer: Consumer<ModulesListEvent>
    private lateinit var connection: Connection<ModulesListEffect>
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
        connection.accept(ModulesListEffect.ShowModuleItemDetailView(moduleItem))
        verify(timeout = 100) { view.routeToModuleItem(moduleItem) }
        confirmVerified(view)
    }

    @Test
    fun `ScrollToItem calls scrollToItem on the view`() {
        val itemId = 123L
        connection.accept(ModulesListEffect.ScrollToItem(itemId))
        verify(timeout = 100) { view.scrollToItem(itemId) }
        confirmVerified(view)
    }

    @Test
    fun `LoadNextPage failure results in correct PageLoaded event`() {
        val errorMessage = "Error"
        val expectedEvent = ModulesListEvent.PageLoaded(
            ModuleListPageData(
                DataResult.Fail(Failure.Network(errorMessage))
            )
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<List<ModuleObject>>(any()) } throws RuntimeException(errorMessage)

        connection.accept(ModulesListEffect.LoadNextPage(course, ModuleListPageData(), null))
        verify(timeout = 100) { consumer.accept(expectedEvent) }
        confirmVerified(consumer)

        unmockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
    }

    @Test
    fun `LoadNextPage results in PageLoaded for single-page list`() {
        val pageModules = makeModulePage()
        val expectedEvent = ModulesListEvent.PageLoaded(
            ModuleListPageData(
                DataResult.Success(pageModules)
            )
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<List<ModuleObject>>(any()) } returns Response.success(pageModules)

        connection.accept(ModulesListEffect.LoadNextPage(course, ModuleListPageData(), null))
        verify(timeout = 100) { consumer.accept(expectedEvent) }
        confirmVerified(consumer)

        unmockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
    }

    @Test
    fun `LoadNextPage results in PageLoaded for multi-page list`() {
        val nextUrl = "fake_next_url"
        val linkHeader = makeLinkHeader(nextUrl)
        val pageModules = makeModulePage()
        val expectedEvent = ModulesListEvent.PageLoaded(
            ModuleListPageData(
                DataResult.Success(pageModules),
                nextPageUrl = nextUrl
            )
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<List<ModuleObject>>(any()) } returns Response.success(pageModules, linkHeader)

        connection.accept(ModulesListEffect.LoadNextPage(course, ModuleListPageData(), null))
        verify(timeout = 100) { consumer.accept(expectedEvent) }
        confirmVerified(consumer)

        unmockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
    }

    @Test
    fun `LoadNextPage results in correct PageLoaded event when scroll-to item is specified`() {
        val scrollToItemId = 123456L
        val nextUrl1 = "fake_next_url_1"
        val nextUrl2 = "fake_next_url_2"
        val firstPageModules = makeModulePage(pageNumber = 0)
        val secondPageModules = listOf(ModuleObject(itemCount = 1, items = listOf(ModuleItem(scrollToItemId))))
        val thirdPageModules = makeModulePage(pageNumber = 1)

        val expectedEvent = ModulesListEvent.PageLoaded(
            ModuleListPageData(
                DataResult.Success(firstPageModules + secondPageModules),
                nextPageUrl = nextUrl2
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

        connection.accept(ModulesListEffect.LoadNextPage(course, ModuleListPageData(), scrollToItemId))
        verify(timeout = 100) { consumer.accept(expectedEvent) }
        confirmVerified(consumer)

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

        val expectedEvent = ModulesListEvent.PageLoaded(
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

        connection.accept(ModulesListEffect.LoadNextPage(course, ModuleListPageData(), scrollToItemId))
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
        val expectedEvent = ModulesListEvent.PageLoaded(
            ModuleListPageData(
                DataResult.Success(expectedModules)
            )
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<List<ModuleObject>>(any()) } returns Response.success(pageModules)
        coEvery { awaitApi<List<ModuleItem>>(any()) } returns missingItems

        connection.accept(ModulesListEffect.LoadNextPage(course, ModuleListPageData(), null))
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


    private fun makeLinkHeader(nextUrl: String) = Headers.of(mapOf("Link" to """<$nextUrl>; rel="next""""))

}
