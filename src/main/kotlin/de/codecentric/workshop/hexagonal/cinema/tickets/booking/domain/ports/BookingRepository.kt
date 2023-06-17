package de.codecentric.workshop.hexagonal.cinema.tickets.booking.domain.ports

import de.codecentric.workshop.hexagonal.cinema.tickets.booking.domain.Booking

interface BookingRepository {

    fun save(booking: Booking): Booking
}