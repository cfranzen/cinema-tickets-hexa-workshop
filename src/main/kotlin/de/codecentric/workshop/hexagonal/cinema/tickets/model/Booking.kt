package de.codecentric.workshop.hexagonal.cinema.tickets.model

import org.springframework.data.annotation.Id
import java.time.Instant
import java.time.LocalDateTime

data class Booking(
    @Id
    val id: Int = 0,
    val customerId: Int,
    val movieId: Int,
    val startTime: LocalDateTime,
    val seats: Int,
    val bookedAt: Instant
)