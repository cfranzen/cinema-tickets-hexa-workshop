package de.codecentric.workshop.hexagonal.cinema.tickets.repositories

import de.codecentric.workshop.hexagonal.cinema.tickets.model.Customer
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerRepository : CrudRepository<Customer, Int> {
}