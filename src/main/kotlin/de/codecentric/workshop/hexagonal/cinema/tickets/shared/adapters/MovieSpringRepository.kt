package de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters

import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.MovieState
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
internal interface MovieSpringRepository : CrudRepository<MovieEntity, Int> {

    fun findByGenreIn(genres: Collection<Genre>): Iterable<MovieEntity>

    fun findByState(state: MovieState): Iterable<MovieEntity>
}