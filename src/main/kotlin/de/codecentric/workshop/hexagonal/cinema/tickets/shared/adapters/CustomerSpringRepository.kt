package de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
internal interface CustomerSpringRepository : CrudRepository<CustomerEntity, Int> {
}