package de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.adapters.outbound

import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Movie
import de.codecentric.workshop.hexagonal.cinema.tickets.model.MovieState
import de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.domain.ports.outbound.MovieRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.repositories.SpringMovieRepository
import org.springframework.stereotype.Component

@Component
class PostgresMovieRepository(
    private val springMovieRepository: SpringMovieRepository
) : MovieRepository {

    override fun getMoviesInTheater(movieIds: List<Int>): List<Movie> {
        return springMovieRepository.findAllById(movieIds)
            .filter { it.state == MovieState.IN_THEATER }
    }

    override fun getGenresForMovieIds(movieIds: List<Int>): Set<Genre> {
        return springMovieRepository.findAllById(movieIds).map { it.genre }.toSet()
    }

    override fun getMoviesByGenres(genres: Collection<Genre>): List<Movie> {
        return springMovieRepository.findByGenreIn(genres).toList()
    }
}