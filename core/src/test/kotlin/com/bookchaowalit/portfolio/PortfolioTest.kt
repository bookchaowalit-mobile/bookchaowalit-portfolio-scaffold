package com.bookchaowalit.portfolio

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PortfolioTest {
    private val projects = listOf(
        Project("minifier", "Minifier", Platform.WEB, ProjectStatus.LIVE, setOf("Next.js", "TypeScript"), liveUrl = "https://minifier.example", year = 2025),
        Project("podcast", "Podcast", Platform.MOBILE, ProjectStatus.IN_PROGRESS, setOf("Kotlin", "Compose"), featured = true, year = 2024),
        Project("old-game", "Old Game", Platform.GAME, ProjectStatus.ARCHIVED, setOf("Unity"), year = 2020),
        Project("status", "Status", Platform.MOBILE, ProjectStatus.LIVE, setOf("kotlin"), liveUrl = "https://status.example", year = 2026),
    )

    @Test
    fun validCatalogHasNoIssues() {
        assertTrue(Portfolio.validate(projects).isEmpty())
    }

    @Test
    fun reportsDataProblems() {
        val bad = projects + listOf(
            Project("status", "Dup", Platform.WEB, ProjectStatus.IN_PROGRESS, setOf("x"), year = 2026),
            Project("Bad Slug", " ", Platform.AI, ProjectStatus.LIVE, emptySet(), repoUrl = "http://github.com/x", year = 2026),
        )
        val messages = Portfolio.validate(bad).map { "${it.slug}: ${it.message}" }.toSet()
        assertEquals(
            setOf(
                "status: duplicate slug",
                "Bad Slug: slug must be kebab-case",
                "Bad Slug: title is blank",
                "Bad Slug: no tech tags",
                "Bad Slug: link must use https: http://github.com/x",
                "Bad Slug: LIVE project without liveUrl",
            ),
            messages,
        )
    }

    @Test
    fun showcaseOrderingAndFilters() {
        assertEquals(listOf("podcast", "status", "minifier"), Portfolio.showcase(projects).map { it.slug })
        assertEquals(4, Portfolio.showcase(projects, ProjectQuery(includeArchived = true)).size)
        assertEquals(listOf("podcast", "status"), Portfolio.showcase(projects, ProjectQuery(tech = "KOTLIN")).map { it.slug })
        assertEquals(listOf("minifier"), Portfolio.showcase(projects, ProjectQuery(text = "script")).map { it.slug })
        assertEquals(listOf("status"), Portfolio.showcase(projects, ProjectQuery(platforms = setOf(Platform.MOBILE), text = "stat")).map { it.slug })
    }

    @Test
    fun statistics() {
        assertEquals("Kotlin" to 2, Portfolio.techStats(projects).first())
        val counts = Portfolio.platformCounts(projects)
        assertEquals(2, counts[Platform.MOBILE])
        assertEquals(0, counts[Platform.DESKTOP])
        assertEquals(Platform.entries.size, counts.size)
    }
}
