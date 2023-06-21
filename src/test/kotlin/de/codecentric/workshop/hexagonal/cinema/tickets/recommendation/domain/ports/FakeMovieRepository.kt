package de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain.ports

import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain.Movie
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.MovieState

class FakeMovieRepository : MovieRepository {

    private val movies = mutableSetOf<Movie>()

    fun add(movie: Movie): Movie {
        val nextId = (movies.maxOfOrNull { it.id } ?: 0) + 1
        val movieWithId = movie.copy(id = nextId)
        movies.add(movieWithId)
        return movieWithId
    }

    override fun getMoviesInTheater(movieIds: List<Int>): List<Movie> =
        movies
            .filter { movieIds.contains(it.id) }
            .filter { it.state == MovieState.IN_THEATER }

    override fun getGenresForMovieIds(movieIds: List<Int>): Set<Genre> =
        movies
            .filter { movieIds.contains(it.id) }
            .map { it.genre }
            .toSet()

    override fun getMoviesByGenres(genres: Collection<Genre>): List<Movie> =
        movies.filter { genres.contains(it.genre) }
}