package de.codecentric.workshop.hexagonal.cinema.tickets.tests

import de.codecentric.workshop.hexagonal.cinema.tickets.catalogs.controller.CustomerWithoutIdDTO
import de.codecentric.workshop.hexagonal.cinema.tickets.catalogs.controller.MovieWithoutIdDTO
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.BookingEntity
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.CustomerEntity
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.CustomerEntityData
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.MovieEntity
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.MovieFavoriteEntity
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.MovieRatingEntity
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.ViewedMovieEntity
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.MovieState
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*

val NOW: Instant = Instant.parse("2023-06-11T12:00:00.000Z")

val ZONE: ZoneId = ZoneId.of("Europe/Berlin")

internal fun createMovieEntity(
    title: String = "This is a new movie",
    genre: Genre = Genre.ACTION,
    state: MovieState = MovieState.IN_THEATER,
    posterId: String = UUID.randomUUID().toString()
) = MovieEntity(
    title = title,
    genre = genre,
    description = "This is some long description how cool this movie is",
    posterId = posterId,
    state = state
)

internal fun createMovieDTO(
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

internal fun createCustomerEntity(
    name: String = "Peter Brown",
    email: String = "peter.brown@gmail.com",
    favorites: List<MovieFavoriteEntity> = createFavoritesEntity(456),
    viewedMovies: List<ViewedMovieEntity> = listOf(
        ViewedMovieEntity(
            movieId = 123,
            viewedAt = NOW,
            rating = MovieRatingEntity(
                rating = 5,
                ratedAt = NOW
            )
        )
    )
): CustomerEntity {
    return CustomerEntity(
        name = name,
        email = email,
        data = CustomerEntityData(
            registeredSince = NOW,
            viewedMovies = viewedMovies,
            favorites = favorites
        )
    )
}

internal fun createFavoritesEntity(vararg movieIds: Int) = movieIds.map {
    MovieFavoriteEntity(
        movieId = it,
        favoriteSince = NOW
    )
}

internal fun createCustomerDTO(
    name: String = "Peter Brown",
    email: String = "peter.brown@gmail.com",
    favorites: List<Int> = listOf()
): CustomerWithoutIdDTO {
    return CustomerWithoutIdDTO(
        name = name,
        email = email,
        favorites = favorites
    )
}

internal fun createBookingEntity() = BookingEntity(
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