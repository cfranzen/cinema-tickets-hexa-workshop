package de.codecentric.workshop.hexagonal.cinema.tickets.booking.adapters.outbound

import de.codecentric.workshop.hexagonal.cinema.tickets.booking.domain.ports.CustomerRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.CustomerSpringRepository
import org.springframework.stereotype.Repository

@Repository
internal class PostgresCustomerRepository(
    private val customerSpringRepository: CustomerSpringRepository,
) : CustomerRepository {

    override fun customerExists(customerId: Int): Boolean {
        return customerSpringRepository.existsById(customerId)
    }
}