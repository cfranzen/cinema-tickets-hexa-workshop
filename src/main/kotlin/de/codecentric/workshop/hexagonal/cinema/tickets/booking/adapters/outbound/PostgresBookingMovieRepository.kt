package de.codecentric.workshop.hexagonal.cinema.tickets.booking.adapters.outbound

import de.codecentric.workshop.hexagonal.cinema.tickets.booking.domain.Movie
import de.codecentric.workshop.hexagonal.cinema.tickets.booking.domain.ports.MovieRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.MovieSpringRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.MovieState
import org.springframework.stereotype.Repository

@Repository
internal class PostgresBookingMovieRepository(
    private val movieSpringRepository: MovieSpringRepository
) : MovieRepository {

    override fun getAllMoviesInTheater(): List<Movie> {
        return movieSpringRepository
            .findByState(MovieState.IN_THEATER)
            .map { Movie(id = it.id) }
    }

    override fun getAllMoviePreviews(): List<Movie> {
        return movieSpringRepository
            .findByState(MovieState.PREVIEW)
            .map { Movie(id = it.id) }
    }
}