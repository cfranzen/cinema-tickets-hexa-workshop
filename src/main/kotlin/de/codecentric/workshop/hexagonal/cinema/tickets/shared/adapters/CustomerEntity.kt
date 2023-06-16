package de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("customer")
internal data class CustomerEntity(
    @Id
    val id: Int = 0,
    val name: String,
    val email: String,
    val data: CustomerEntityData
)

internal data class CustomerEntityData(
    val registeredSince: Instant,
    val viewedMovies: List<ViewedMovieEntity>,
    val favorites: List<MovieFavoriteEntity>
)

internal data class ViewedMovieEntity(
    val movieId: Int,
    val viewedAt: Instant,
    val rating: MovieRatingEntity?
)

internal data class MovieRatingEntity(
    val rating: Int,
    val ratedAt: Instant,
)

internal data class MovieFavoriteEntity(
    val movieId: Int,
    val favoriteSince: Instant,
)