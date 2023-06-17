package de.codecentric.workshop.hexagonal.cinema.tickets.booking.domain

import java.time.Instant
import java.time.LocalDateTime

data class Booking(
    val id: Int = 0,
    val customerId: Int,
    val movieId: Int,
    val startTime: LocalDateTime,
    val seats: Int,
    val bookedAt: Instant
)