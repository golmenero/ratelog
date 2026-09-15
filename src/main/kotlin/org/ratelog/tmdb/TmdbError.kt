package org.ratelog.tmdb

import org.ratelog.movie.detail.DetailMovieHandlerError
import org.ratelog.movie.premieres.MoviePremieresHandlerError
import org.ratelog.search.SearchHandlerError
import org.ratelog.search.trending.movies.SearchTrendingMoviesHandlerError
import org.ratelog.search.trending.tvshows.SearchTrendingTvShowsHandlerError
import org.ratelog.tvshow.detail.DetailTvShowHandlerError
import org.ratelog.tvshow.premieres.TvShowPremieresHandlerError

interface TmdbError:
    DetailMovieHandlerError,
    DetailTvShowHandlerError,
    SearchHandlerError,
    SearchTrendingMoviesHandlerError,
    SearchTrendingTvShowsHandlerError,
    MoviePremieresHandlerError,
    TvShowPremieresHandlerError {
    object MovieNotFound: TmdbError
    object TvShowNotFound: TmdbError
}
