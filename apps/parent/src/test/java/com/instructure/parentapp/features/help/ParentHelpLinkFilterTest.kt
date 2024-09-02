package com.instructure.parentapp.features.help

import com.instructure.canvasapi2.models.HelpLink
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ParentHelpLinkFilterTest {
    private lateinit var filter: ParentHelpLinkFilter

    @Before
    fun setup() {
        filter = ParentHelpLinkFilter()
    }

    @Test
    fun `Test if available options are filtered properly`() {
        val mockData = getMockHelpLinks()
        val filteredData = mockData.filter { filter.isLinkAllowed(it, emptyList()) }

        assertTrue(filteredData.contains(mockData[0]))
        assertTrue(filteredData.contains(mockData[1]))
        assertTrue(filteredData.contains(mockData[2]))
        assertFalse(filteredData.contains(mockData[3]))
    }

    private fun getMockHelpLinks(): List<HelpLink> {
        return listOf(
            HelpLink(
                id = "1",
                type = "type",
                availableTo = listOf("observer", "user"),
                url = "url",
                text = "text",
                subtext = "subtext"
            ),
            HelpLink(
                id = "2",
                type = "type",
                availableTo = listOf("user"),
                url = "url",
                text = "text",
                subtext = "subtext"
            ),
            HelpLink(
                id = "3",
                type = "type",
                availableTo = listOf("observer"),
                url = "url",
                text = "text",
                subtext = "subtext"
            ),
            HelpLink(
                id = "4",
                type = "type",
                availableTo = listOf("other"),
                url = "url",
                text = "text",
                subtext = "subtext"
            )
        )
    }
}