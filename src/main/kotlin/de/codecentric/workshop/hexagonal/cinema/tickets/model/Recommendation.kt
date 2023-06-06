package de.codecentric.workshop.hexagonal.cinema.tickets.model

data class Recommendation(
    val movieId: Int,
    val probability: Double
)