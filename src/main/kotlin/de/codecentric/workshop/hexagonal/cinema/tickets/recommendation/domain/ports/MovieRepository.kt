package de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain.ports

import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain.Movie
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre


interface MovieRepository {

    fun getMoviesInTheater(movieIds: List<Int>): List<Movie>

    fun getGenresForMovieIds(movieIds: List<Int>): Set<Genre>

    fun getMoviesByGenres(genres: Collection<Genre>): List<Movie>
}