package org.ratelog.search

import arrow.core.getOrElse
import org.ratelog.MediaType
import org.ratelog.annotations.CurrentUser
import org.ratelog.user.User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.mvc.support.RedirectAttributes

data class SearchApiResponse(
    val items: List<SearchResultResponse>,
    val hasMore: Boolean,
)

data class SearchResultResponse(
    val tmdbId: Int,
    val title: String,
    val overview: String?,
    val year: Int?,
    val posterPath: String?,
    val type: String,
)

@Controller
class SearchController(
    private val handler: SearchHandler,
) {

    @GetMapping("/")
    fun searchPage(
        @CurrentUser user: User,
        @RequestParam("q", required = false) query: String?,
        @RequestParam("mediaType", required = false) mediaType: String?,
        @RequestParam("page", required = false, defaultValue = "1") page: Int,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        if (!query.isNullOrBlank()) {
            val type = mediaType?.takeIf { it.isNotBlank() }
                ?.let(MediaType::valueOf)

            SearchQuery(
                query = query,
                userId = user.id!!,
                lang = user.metadataLang,
                mediaType = type,
                page = page,
            ).let(handler::handle)
            .fold(
                {
                    redirectAttributes.addFlashAttribute("error", it)
                },
                {
                    model.addAttribute("query", query)
                    model.addAttribute("results", it.items)
                    model.addAttribute("hasMore", it.hasMore)
                    model.addAttribute("mediaType", mediaType)
                    model.addAttribute("currentPage", page)
                }
            )
        }
        return "search"
    }

    @GetMapping("/api/search")
    @ResponseBody
    fun searchApi(
        @CurrentUser user: User,
        @RequestParam("q") query: String,
        @RequestParam("mediaType", required = false) mediaType: String?,
        @RequestParam("page", defaultValue = "1") page: Int,
    ): SearchApiResponse {
        val type = mediaType?.takeIf { it.isNotBlank() }
            ?.let(MediaType::valueOf)

        return SearchQuery(
            query = query,
            userId = user.id!!,
            lang = user.metadataLang,
            mediaType = type,
            page = page,
        ).let(handler::handle)
            .getOrElse { SearchResult(emptyList(), false) }
            .let { result ->
                SearchApiResponse(
                    items = result.items.map(::toResponse),
                    hasMore = result.hasMore,
                )
            }
    }

    private fun toResponse(item: SearchResultItem) = SearchResultResponse(
        tmdbId = item.tmdbId,
        title = item.title,
        overview = item.overview,
        year = item.year,
        posterPath = item.posterPath,
        type = item.type,
    )
}
