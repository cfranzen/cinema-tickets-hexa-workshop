package de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain

data class Customer(
    val id: Int = 0,
    val name: String,
    val email: String,
    val favoriteMovieIds: List<Int>
)