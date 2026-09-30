package com.bookchaowalit.portfolio

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PortfolioEdgeCaseTest {
    private fun p(
        slug: String,
        tech: Set<String> = setOf("Kotlin"),
        status: ProjectStatus = ProjectStatus.IN_PROGRESS,
        repoUrl: String? = null,
        liveUrl: String? = null,
        featured: Boolean = false,
        year: Int = 2025,
        title: String = slug,
    ) = Project(slug, title, Platform.MOBILE, status, tech, repoUrl, liveUrl, featured, year)

    private fun messages(vararg projects: Project) = Portfolio.validate(projects.toList()).map { it.message }

    @Test
    fun linksNeedAnHttpsSchemeAndAHost() {
        assertTrue(messages(p("a", repoUrl = "https://")).any { it.startsWith("link must use https") })
        assertTrue(messages(p("a", repoUrl = "https:// github.com")).any { it.startsWith("link must use https") })
        assertTrue(messages(p("a", repoUrl = "http://github.com/x")).any { it.startsWith("link must use https") })
        assertTrue(messages(p("a", repoUrl = "https://github.com/bookchaowalit/x")).isEmpty())
    }

    @Test
    fun blankTechTagsDoNotCount() {
        assertEquals(listOf("no tech tags"), messages(p("a", tech = setOf(" ", ""))))
    }

    @Test
    fun slugRules() {
        for (bad in listOf("A", "a_b", "-a", "a-", "a--b", "", "ก")) {
            assertTrue(messages(p(bad)).contains("slug must be kebab-case"), bad)
        }
        assertTrue(messages(p("a-1-b")).isEmpty())
        assertEquals(1, Portfolio.validate(listOf(p("dup"), p("dup"), p("dup"))).count { it.message == "duplicate slug" })
    }

    @Test
    fun blankOrPaddedTechFilter() {
        val items = listOf(p("a", tech = setOf("Kotlin ")), p("b", tech = setOf("Swift")))
        assertEquals(listOf("a"), Portfolio.showcase(items, ProjectQuery(tech = "  kotlin")).map { it.slug })
        assertEquals(2, Portfolio.showcase(items, ProjectQuery(tech = " ")).size)
    }

    @Test
    fun showcaseTieBreakers() {
        val items = listOf(
            p("b", title = "beta", year = 2024),
            p("a", title = "Alpha", year = 2024),
            p("c", title = "Gamma", year = 2026),
            p("d", title = "Delta", status = ProjectStatus.LIVE, liveUrl = "https://d.example", year = 2020),
            p("e", title = "Eps", status = ProjectStatus.ARCHIVED, featured = true),
        )
        assertEquals(listOf("d", "c", "a", "b"), Portfolio.showcase(items).map { it.slug })
        assertEquals("e", Portfolio.showcase(items, ProjectQuery(includeArchived = true)).first().slug) // featured wins
    }

    @Test
    fun statsOnEmptyCatalog() {
        assertTrue(Portfolio.techStats(emptyList()).isEmpty())
        assertEquals(Platform.entries.size, Portfolio.platformCounts(emptyList()).size)
        assertTrue(Portfolio.platformCounts(emptyList()).values.all { it == 0 })
        assertTrue(Portfolio.validate(emptyList()).isEmpty())
    }
}
