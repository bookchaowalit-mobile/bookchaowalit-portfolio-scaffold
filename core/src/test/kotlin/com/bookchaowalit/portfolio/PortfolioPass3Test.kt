package com.bookchaowalit.portfolio

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PortfolioPass3Test {
    private fun project(slug: String, title: String = "Title", tech: Set<String> = setOf("Kotlin"), liveUrl: String? = "https://x.dev") =
        Project(slug, title, Platform.MOBILE, ProjectStatus.LIVE, tech, liveUrl = liveUrl, year = 2026)

    @Test
    fun zeroWidthOnlyTitleAndTagsAreBlank() {
        val issues = Portfolio.validate(listOf(project("a", title = "​", tech = setOf("﻿", "⁠ "))))
        assertEquals(setOf("title is blank", "no tech tags"), issues.map { it.message }.toSet())
    }

    @Test
    fun zeroWidthQueriesDoNotHideEverything() {
        val projects = listOf(project("a"), project("b", tech = setOf("Swift")))
        assertEquals(2, Portfolio.showcase(projects, ProjectQuery(text = "​")).size)
        assertEquals(2, Portfolio.showcase(projects, ProjectQuery(tech = "​")).size)
        assertEquals(listOf("a"), Portfolio.showcase(projects, ProjectQuery(tech = "﻿kotlin")).map { it.slug })
    }

    @Test
    fun zeroWidthTagsAreNotCountedAndDoNotSplitCounts() {
        val projects = listOf(project("a", tech = setOf("Kotlin", "​")), project("b", tech = setOf("Kotlin​")))
        assertEquals(listOf("Kotlin" to 2), Portfolio.techStats(projects))
    }

    @Test
    fun schemeIsCaseInsensitive() {
        assertTrue(Portfolio.validate(listOf(project("a", liveUrl = "HTTPS://x.dev/app"))).isEmpty())
    }
}
