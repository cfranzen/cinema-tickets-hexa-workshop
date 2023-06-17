package de.codecentric.workshop.hexagonal.cinema.tickets.booking.domain

data class BookingRequest(
    val customerId: Int,
    val screeningId: Int,
    val seats: Int,
) {
    init {
        if (seats <= 0) {
            throw IllegalArgumentException("Seats must be a positive integer but $seats was given")
        }
    }
}