package de.codecentric.workshop.hexagonal.cinema.tickets.booking.domain.ports

interface CustomerRepository {

    fun customerExists(customerId: Int): Boolean
}