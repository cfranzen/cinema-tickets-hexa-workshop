package de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.domain.ports

import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Movie
import de.codecentric.workshop.hexagonal.cinema.tickets.model.MovieState

class FakeMovieRepository : MovieRepository {

    private val movies = mutableListOf<Movie>()

    fun save(movie: Movie): Movie {
        val nextId = (movies.maxOfOrNull { it.id } ?: 0) + 1
        val newMovie = movie.copy(id = nextId)
        movies.add(newMovie)
        return newMovie
    }

    override fun findAllMoviesByIdInTheater(ids: List<Int>): List<Movie> {
        return movies.filter { ids.contains(it.id) }
            .filter { it.state == MovieState.IN_THEATER }
    }

    override fun findGenresForMovieIds(movieIds: Set<Int>): Set<Genre> {
        return movies.filter { movieIds.contains(it.id) }
            .map { it.genre }
            .toSet()
    }

    override fun findMoviesByGenre(genres: Set<Genre>): List<Movie> {
        return movies.filter { genres.contains(it.genre) }
    }
}