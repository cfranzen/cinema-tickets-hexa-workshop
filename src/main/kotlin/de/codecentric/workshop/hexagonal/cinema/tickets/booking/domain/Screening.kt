package de.codecentric.workshop.hexagonal.cinema.tickets.booking.domain

import java.time.LocalDateTime

data class Screening(
    val id: Int,
    val movieId: Int,
    val startTime: LocalDateTime,
)