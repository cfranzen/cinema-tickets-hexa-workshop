package de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.adapters.outbound


import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain.Movie
import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain.ports.MovieRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.MovieEntity
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.MovieSpringRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.MovieState
import org.springframework.stereotype.Repository

@Repository
internal class PostgresMovieRepository(
    private val movieSpringRepository: MovieSpringRepository
) : MovieRepository {

    override fun getMoviesInTheater(movieIds: List<Int>): List<Movie> {
        return movieSpringRepository
            .findAllById(movieIds)
            .filter { it.state == MovieState.IN_THEATER }
            .map { it.toDomainObject() }
    }

    override fun getGenresForMovieIds(movieIds: List<Int>): Set<Genre> {
        return movieSpringRepository
            .findAllById(movieIds)
            .map { it.genre }
            .toSet()
    }

    override fun getMoviesByGenres(genres: Collection<Genre>): List<Movie> {
        return movieSpringRepository
            .findByGenreIn(genres)
            .map { it.toDomainObject() }
    }

    private fun MovieEntity.toDomainObject() = Movie(
        id = this.id,
        title = this.title,
        genre = this.genre,
        state = this.state
    )

}

