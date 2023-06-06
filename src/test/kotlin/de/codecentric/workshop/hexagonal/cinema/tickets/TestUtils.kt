package de.codecentric.workshop.hexagonal.cinema.tickets

import de.codecentric.workshop.hexagonal.cinema.tickets.model.Customer
import de.codecentric.workshop.hexagonal.cinema.tickets.model.CustomerData
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Movie
import de.codecentric.workshop.hexagonal.cinema.tickets.model.MovieFavorite
import de.codecentric.workshop.hexagonal.cinema.tickets.model.MovieRating
import de.codecentric.workshop.hexagonal.cinema.tickets.model.MovieState
import de.codecentric.workshop.hexagonal.cinema.tickets.model.ViewedMovie
import java.time.Instant
import java.util.*

fun createMovie(
    title: String = "This is a new movie",
    genre: Genre = Genre.ACTION,
    state: MovieState = MovieState.IN_THEATER
): Movie = Movie(
    title = title,
    genre = genre,
    description = "This is some long description how cool this movie is",
    posterId = UUID.randomUUID().toString(),
    state = state
)

fun createCustomer(
    favoriteMovies: List<MovieFavorite> = listOf(
        MovieFavorite(
            movieId = 456,
            favoriteSince = Instant.now()
        )
    ),
    viewedMovies: List<ViewedMovie> = listOf(
        ViewedMovie(
            movieId = 123,
            viewedAt = Instant.now(),
            rating = MovieRating(
                rating = 5,
                ratedAt = Instant.now()
            )
        )
    )
): Customer {
    return Customer(
        data = CustomerData(
            registeredSince = Instant.now(),
            viewedMovies = viewedMovies,
            favoriteMovies = favoriteMovies
        )
    )
}