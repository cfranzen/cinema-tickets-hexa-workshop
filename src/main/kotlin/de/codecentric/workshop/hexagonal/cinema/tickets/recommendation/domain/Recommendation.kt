package de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain

data class Recommendation(
    val customer: Customer,
    val movie: Movie,
    val probability: Double
)