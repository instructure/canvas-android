package com.instructure.pandautils.features.dashboard.widget.usecase

import com.google.gson.Gson
import com.instructure.pandautils.features.dashboard.widget.GlobalConfig
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.repository.WidgetConfigDataRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ObserveGlobalConfigUseCaseTest {

    private val repository: WidgetConfigDataRepository = mockk(relaxed = true)
    private val gson = Gson()
    private lateinit var useCase: ObserveGlobalConfigUseCase

    @Before
    fun setUp() {
        useCase = ObserveGlobalConfigUseCase(repository, gson)
    }

    @Test
    fun `execute returns default config when json is null`() = runTest {
        coEvery { repository.observeConfigJson(WidgetMetadata.Companion.WIDGET_ID_GLOBAL) } returns flowOf(null)

        val result = useCase(Unit).first()

        Assert.assertEquals(WidgetMetadata.Companion.WIDGET_ID_GLOBAL, result.widgetId)
        Assert.assertEquals(0xFF2573DF.toInt(), result.backgroundColor)
    }

    @Test
    fun `execute returns parsed config when json is valid`() = runTest {
        val customColor = 0xFF00FF00.toInt()
        val configJson = """{"widgetId":"global","backgroundColor":$customColor}"""
        coEvery { repository.observeConfigJson(WidgetMetadata.Companion.WIDGET_ID_GLOBAL) } returns flowOf(configJson)

        val result = useCase(Unit).first()

        Assert.assertEquals(WidgetMetadata.Companion.WIDGET_ID_GLOBAL, result.widgetId)
        Assert.assertEquals(customColor, result.backgroundColor)
    }

    @Test
    fun `execute returns default config when json is invalid`() = runTest {
        val invalidJson = "invalid json"
        coEvery { repository.observeConfigJson(WidgetMetadata.Companion.WIDGET_ID_GLOBAL) } returns flowOf(invalidJson)

        val result = useCase(Unit).first()

        Assert.assertEquals(WidgetMetadata.Companion.WIDGET_ID_GLOBAL, result.widgetId)
        Assert.assertEquals(0xFF2573DF.toInt(), result.backgroundColor)
    }

    @Test
    fun `execute returns default config when json parsing throws exception`() = runTest {
        val malformedJson = """{"widgetId":"global","backgroundColor":"not a number"}"""
        coEvery { repository.observeConfigJson(WidgetMetadata.Companion.WIDGET_ID_GLOBAL) } returns flowOf(malformedJson)

        val result = useCase(Unit).first()

        Assert.assertEquals(WidgetMetadata.Companion.WIDGET_ID_GLOBAL, result.widgetId)
        Assert.assertEquals(0xFF2573DF.toInt(), result.backgroundColor)
    }

    @Test
    fun `execute emits updated config when json changes`() = runTest {
        val color1 = 0xFF0000FF.toInt()
        val color2 = 0xFFFF0000.toInt()
        val configJson1 = """{"widgetId":"global","backgroundColor":$color1}"""
        val configJson2 = """{"widgetId":"global","backgroundColor":$color2}"""

        coEvery { repository.observeConfigJson(WidgetMetadata.Companion.WIDGET_ID_GLOBAL) } returns flowOf(configJson1, configJson2)

        val results = mutableListOf<GlobalConfig>()
        useCase(Unit).collect { results.add(it) }

        Assert.assertEquals(2, results.size)
        Assert.assertEquals(color1, results[0].backgroundColor)
        Assert.assertEquals(color2, results[1].backgroundColor)
    }

    @Test
    fun `execute returns default config with correct widget id`() = runTest {
        coEvery { repository.observeConfigJson(WidgetMetadata.Companion.WIDGET_ID_GLOBAL) } returns flowOf(null)

        val result = useCase(Unit).first()

        Assert.assertEquals(WidgetMetadata.Companion.WIDGET_ID_GLOBAL, result.widgetId)
    }

    @Test
    fun `execute handles transition from null to valid config`() = runTest {
        val customColor = 0xFF123456.toInt()
        val configJson = """{"widgetId":"global","backgroundColor":$customColor}"""

        coEvery { repository.observeConfigJson(WidgetMetadata.Companion.WIDGET_ID_GLOBAL) } returns flowOf(null, configJson)

        val results = mutableListOf<GlobalConfig>()
        useCase(Unit).collect { results.add(it) }

        Assert.assertEquals(2, results.size)
        Assert.assertEquals(0xFF2573DF.toInt(), results[0].backgroundColor)
        Assert.assertEquals(customColor, results[1].backgroundColor)
    }

    @Test
    fun `execute handles transition from valid config to null`() = runTest {
        val customColor = 0xFF123456.toInt()
        val configJson = """{"widgetId":"global","backgroundColor":$customColor}"""

        coEvery { repository.observeConfigJson(WidgetMetadata.Companion.WIDGET_ID_GLOBAL) } returns flowOf(configJson, null)

        val results = mutableListOf<GlobalConfig>()
        useCase(Unit).collect { results.add(it) }

        Assert.assertEquals(2, results.size)
        Assert.assertEquals(customColor, results[0].backgroundColor)
        Assert.assertEquals(0xFF2573DF.toInt(), results[1].backgroundColor)
    }
}