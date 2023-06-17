package de.codecentric.workshop.hexagonal.cinema.tickets.booking.adapters.outbound

import de.codecentric.workshop.hexagonal.cinema.tickets.booking.domain.Booking
import de.codecentric.workshop.hexagonal.cinema.tickets.booking.domain.ports.BookingRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.BookingEntity
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.BookingSpringRepository
import org.springframework.stereotype.Repository

@Repository
internal class PostgresBookingRepository(
    private val bookingSpringRepository: BookingSpringRepository,
) : BookingRepository {

    override fun save(booking: Booking): Booking {
        val bookingEntity = bookingSpringRepository.save(
            BookingEntity(
                customerId = booking.customerId,
                movieId = booking.movieId,
                startTime = booking.startTime,
                seats = booking.seats,
                bookedAt = booking.bookedAt
            )
        )
        return Booking(
            id = bookingEntity.id,
            customerId = bookingEntity.customerId,
            movieId = bookingEntity.movieId,
            startTime = bookingEntity.startTime,
            seats = bookingEntity.seats,
            bookedAt = bookingEntity.bookedAt
        )
    }
}