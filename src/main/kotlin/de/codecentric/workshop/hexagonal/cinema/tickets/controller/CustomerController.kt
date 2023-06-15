package de.codecentric.workshop.hexagonal.cinema.tickets.controller

import de.codecentric.workshop.hexagonal.cinema.tickets.model.Customer
import de.codecentric.workshop.hexagonal.cinema.tickets.model.CustomerData
import de.codecentric.workshop.hexagonal.cinema.tickets.model.MovieFavorite
import de.codecentric.workshop.hexagonal.cinema.tickets.repositories.CustomerRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.Clock
import java.time.Instant

@RestController
class CustomerController(
    private val clock: Clock,
    private val customerRepository: CustomerRepository
) {

    @PostMapping("/customers")
    fun createNewCustomer(@RequestBody request: CustomerWithoutIdDTO): ResponseEntity<Customer> {
        val now = clock.instant()
        val newCustomer = request.toCustomer(now)
        val persistedCustomer = customerRepository.save(newCustomer)
        return ResponseEntity.ok().body(persistedCustomer)
    }

    @GetMapping("/customers/{id}")
    fun findCustomer(@PathVariable("id") customerId: Int): ResponseEntity<Customer> {
        return customerRepository
            .findById(customerId)
            .map { ResponseEntity.ok().body(it) }
            .orElseGet { ResponseEntity.notFound().build() }
    }

}

data class CustomerWithoutIdDTO(
    val name: String,
    val email: String,
    val favorites: List<Int>
) {
    fun toCustomer(now: Instant) = Customer(
        id = 0,
        name = this.name,
        email = this.email,
        data = CustomerData(
            registeredSince = now,
            viewedMovies = listOf(),
            favorites = this.favorites.map {
                MovieFavorite(
                    movieId = it,
                    favoriteSince = now
                )
            }
        )
    )
}