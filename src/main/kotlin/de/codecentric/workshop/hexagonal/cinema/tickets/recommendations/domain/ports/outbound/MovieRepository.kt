package de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.domain.ports.outbound

import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Movie

interface MovieRepository {

    fun getMoviesInTheater(movieIds: List<Int>): List<Movie>

    fun getGenresForMovieIds(movieIds: List<Int>): Set<Genre>

    fun getMoviesByGenres(genres: Collection<Genre>): List<Movie>
}