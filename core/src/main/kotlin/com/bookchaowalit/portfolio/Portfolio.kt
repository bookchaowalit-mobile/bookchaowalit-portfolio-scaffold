package com.bookchaowalit.portfolio

enum class Platform { WEB, MOBILE, BACKEND, AI, GAME, DESKTOP }

enum class ProjectStatus { LIVE, IN_PROGRESS, ARCHIVED }

data class Project(
    val slug: String,
    val title: String,
    val platform: Platform,
    val status: ProjectStatus,
    val tech: Set<String>,
    val repoUrl: String? = null,
    val liveUrl: String? = null,
    val featured: Boolean = false,
    val year: Int,
)

data class ValidationIssue(val slug: String, val message: String)

data class ProjectQuery(
    val text: String = "",
    val platforms: Set<Platform> = emptySet(),
    val tech: String? = null,
    val includeArchived: Boolean = false,
)

object Portfolio {
    private val SLUG = Regex("^[a-z0-9]+(-[a-z0-9]+)*$")

    /** Data-quality checks for the catalog shown in the app. */
    fun validate(projects: List<Project>): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()
        projects.groupBy { it.slug }.filter { it.value.size > 1 }.keys.forEach { issues += ValidationIssue(it, "duplicate slug") }
        for (p in projects) {
            if (!SLUG.matches(p.slug)) issues += ValidationIssue(p.slug, "slug must be kebab-case")
            if (p.title.isBlank()) issues += ValidationIssue(p.slug, "title is blank")
            if (p.tech.none { it.isNotBlank() }) issues += ValidationIssue(p.slug, "no tech tags")
            listOfNotNull(p.repoUrl, p.liveUrl).filterNot { isHttpsUrl(it) }
                .forEach { issues += ValidationIssue(p.slug, "link must use https: $it") }
            if (p.status == ProjectStatus.LIVE && p.liveUrl == null) issues += ValidationIssue(p.slug, "LIVE project without liveUrl")
        }
        return issues
    }

    /**
     * Showcase ordering: featured first, then live, in-progress, archived;
     * newer years first; title as a stable tie-breaker.
     */
    fun showcase(projects: List<Project>, q: ProjectQuery = ProjectQuery()): List<Project> {
        val text = q.text.trim().lowercase()
        val tech = q.tech?.trim()?.takeIf { it.isNotEmpty() } // blank = no tech filter
        return projects.filter { p ->
            (q.includeArchived || p.status != ProjectStatus.ARCHIVED) &&
                (q.platforms.isEmpty() || p.platform in q.platforms) &&
                (tech == null || p.tech.any { it.trim().equals(tech, ignoreCase = true) }) &&
                (text.isEmpty() || text in p.title.lowercase() || p.tech.any { text in it.lowercase() })
        }.sortedWith(
            compareByDescending<Project> { it.featured }
                .thenBy { it.status.ordinal }
                .thenByDescending { it.year }
                .thenBy { it.title.lowercase() },
        )
    }

    /** Tech tag -> project count (case-insensitive, displayed with the first-seen spelling). */
    fun techStats(projects: List<Project>): List<Pair<String, Int>> {
        val display = LinkedHashMap<String, String>()
        val counts = HashMap<String, Int>()
        for (p in projects) for (t in p.tech.map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() }) {
            val key = t.lowercase()
            display.putIfAbsent(key, t)
            counts[key] = (counts[key] ?: 0) + 1
        }
        return counts.entries.sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .map { display.getValue(it.key) to it.value }
    }

    /** Absolute https URL with a host, e.g. rejects "https://" and "https:// x". */
    private fun isHttpsUrl(url: String): Boolean {
        val uri = runCatching { java.net.URI(url) }.getOrNull() ?: return false
        return uri.scheme == "https" && !uri.host.isNullOrEmpty()
    }

    /** Count per platform, including zero entries, in enum order. */
    fun platformCounts(projects: List<Project>): Map<Platform, Int> =
        Platform.entries.associateWith { pl -> projects.count { it.platform == pl } }
}
