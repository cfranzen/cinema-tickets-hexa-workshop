package de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain

import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.MovieState

data class Movie(
    val id: Int = 0,
    val title: String,
    val genre: Genre,
    val state: MovieState
)

