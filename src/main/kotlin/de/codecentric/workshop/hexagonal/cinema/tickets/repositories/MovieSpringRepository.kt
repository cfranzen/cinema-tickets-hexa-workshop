package de.codecentric.workshop.hexagonal.cinema.tickets.repositories

import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Movie
import de.codecentric.workshop.hexagonal.cinema.tickets.model.MovieState
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MovieSpringRepository : CrudRepository<Movie, Int> {

    fun findByGenreIn(genres: Collection<Genre>): Iterable<Movie>

    fun findByState(state: MovieState): Iterable<Movie>
}