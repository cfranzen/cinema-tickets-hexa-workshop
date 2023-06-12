package de.codecentric.workshop.hexagonal.cinema.tickets

import de.codecentric.workshop.hexagonal.cinema.tickets.controller.CustomerWithoutIdDTO
import de.codecentric.workshop.hexagonal.cinema.tickets.controller.MovieWithoutIdDTO
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Booking
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Customer
import de.codecentric.workshop.hexagonal.cinema.tickets.model.CustomerData
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Movie
import de.codecentric.workshop.hexagonal.cinema.tickets.model.MovieFavorite
import de.codecentric.workshop.hexagonal.cinema.tickets.model.MovieRating
import de.codecentric.workshop.hexagonal.cinema.tickets.model.MovieState
import de.codecentric.workshop.hexagonal.cinema.tickets.model.ViewedMovie
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*

val NOW: Instant = Instant.parse("2023-06-11T12:00:00.000Z")

val ZONE: ZoneId = ZoneId.of("Europe/Berlin")

fun createMovie(
    title: String = "This is a new movie",
    genre: Genre = Genre.ACTION,
    state: MovieState = MovieState.IN_THEATER,
    posterId: String = UUID.randomUUID().toString()
) = Movie(
    title = title,
    genre = genre,
    description = "This is some long description how cool this movie is",
    posterId = posterId,
    state = state
)

fun createMovieDTO(
    title: String = "This is a new movie",
    genre: Genre = Genre.ACTION,
    state: MovieState = MovieState.IN_THEATER
) = MovieWithoutIdDTO(
    title = title,
    genre = genre,
    description = "This is some long description how cool this movie is",
    posterId = UUID.randomUUID().toString(),
    state = state
)

fun createCustomer(
    name: String = "Peter Brown",
    favorites: List<MovieFavorite> = createFavorites(456),
    viewedMovies: List<ViewedMovie> = listOf(
        ViewedMovie(
            movieId = 123,
            viewedAt = NOW,
            rating = MovieRating(
                rating = 5,
                ratedAt = NOW
            )
        )
    )
): Customer {
    return Customer(
        name = name,
        data = CustomerData(
            registeredSince = NOW,
            viewedMovies = viewedMovies,
            favorites = favorites
        )
    )
}

fun createFavorites(vararg movieIds: Int) = movieIds.map {
    MovieFavorite(
        movieId = it,
        favoriteSince = NOW
    )
}

fun createCustomerDTO(
    name: String = "Peter Brown",
    favorites: List<Int> = listOf()
): CustomerWithoutIdDTO {
    return CustomerWithoutIdDTO(
        name = name,
        favorites = favorites
    )
}

fun createBooking() = Booking(
    id = 0,
    customerId = 1,
    movieId = 2,
    startTime = LocalDateTime.now(),
    seats = 3,
    bookedAt = Instant.now()
)

data class ErrorResponse(
    val status: Int,
    val error: String,
    val exception: String,
    val message: String,
    val path: String,
    val timestamp: OffsetDateTime
)