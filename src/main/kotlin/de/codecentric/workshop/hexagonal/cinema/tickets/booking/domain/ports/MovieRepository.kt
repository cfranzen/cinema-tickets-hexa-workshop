package de.codecentric.workshop.hexagonal.cinema.tickets.booking.domain.ports

import de.codecentric.workshop.hexagonal.cinema.tickets.booking.domain.Movie


interface MovieRepository {

    fun getAllMoviesInTheater(): List<Movie>

    fun getAllMoviePreviews(): List<Movie>
}