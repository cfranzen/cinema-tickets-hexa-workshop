package de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.adapters.outbound

import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Movie
import de.codecentric.workshop.hexagonal.cinema.tickets.model.MovieState
import de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.domain.ports.MovieRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.repositories.MovieSpringRepository
import org.springframework.stereotype.Repository

@Repository
internal class PostgresMovieRepository(
    private val movieSpringRepository: MovieSpringRepository,
) : MovieRepository {

    override fun findAllMoviesByIdInTheater(ids: List<Int>): List<Movie> {
        return movieSpringRepository
            .findAllById(ids)
            .filter { it.state == MovieState.IN_THEATER }
    }

    override fun findGenresForMovieIds(
        movieIds: Set<Int>,
    ): Set<Genre> {
        val moviesById = movieSpringRepository.findAllById(movieIds).associateBy { it.id }
        return moviesById.values.mapNotNull { it.genre }.toSet()
    }

    override fun findMoviesByGenre(currentGenres: Set<Genre>) =
        movieSpringRepository.findByGenreIn(currentGenres).toList()
}