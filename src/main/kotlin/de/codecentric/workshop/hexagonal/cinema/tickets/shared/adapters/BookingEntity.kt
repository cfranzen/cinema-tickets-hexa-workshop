package de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.time.LocalDateTime

@Table("booking")
internal data class BookingEntity(
    @Id
    val id: Int = 0,
    val customerId: Int,
    val movieId: Int,
    val startTime: LocalDateTime,
    val seats: Int,
    val bookedAt: Instant
)