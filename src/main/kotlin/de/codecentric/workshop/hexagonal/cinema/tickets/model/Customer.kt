package de.codecentric.workshop.hexagonal.cinema.tickets.model

import org.springframework.data.annotation.Id
import java.time.Instant

data class Customer(
    @Id
    val id: Int = 0,
    val name: String,
    val email: String,
    val data: CustomerData
)

data class CustomerData(
    val registeredSince: Instant,
    val viewedMovies: List<ViewedMovie>,
    val favorites: List<MovieFavorite>
)

data class ViewedMovie(
    val movieId: Int,
    val viewedAt: Instant,
    val rating: MovieRating?
)

data class MovieRating(
    val rating: Int,
    val ratedAt: Instant,
)

data class MovieFavorite(
    val movieId: Int,
    val favoriteSince: Instant,
)