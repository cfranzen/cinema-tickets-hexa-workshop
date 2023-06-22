package de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.domain.ports

import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Movie

interface MovieRepository {
    fun findAllMoviesByIdInTheater(ids: List<Int>): List<Movie>
    fun findGenresForMovieIds(movieIds: Set<Int>): Set<Genre>
    fun findMoviesByGenre(genres: Set<Genre>): List<Movie>
}