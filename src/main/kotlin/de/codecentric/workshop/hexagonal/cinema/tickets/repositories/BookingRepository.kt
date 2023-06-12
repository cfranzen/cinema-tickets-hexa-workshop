package de.codecentric.workshop.hexagonal.cinema.tickets.repositories

import de.codecentric.workshop.hexagonal.cinema.tickets.model.Booking
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BookingRepository : CrudRepository<Booking, Int> {
}