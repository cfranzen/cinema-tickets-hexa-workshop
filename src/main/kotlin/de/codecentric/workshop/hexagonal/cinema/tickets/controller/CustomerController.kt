package de.codecentric.workshop.hexagonal.cinema.tickets.controller

import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.CustomerEntity
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.CustomerEntityData
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.MovieFavoriteEntity
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.CustomerSpringRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.Clock
import java.time.Instant

@RestController
internal class CustomerController(
    private val clock: Clock,
    private val customerSpringRepository: CustomerSpringRepository
) {

    @PostMapping("/customers")
    fun createNewCustomer(@RequestBody request: CustomerWithoutIdDTO): ResponseEntity<CustomerEntity> {
        val now = clock.instant()
        val newCustomer = request.toCustomer(now)
        val persistedCustomer = customerSpringRepository.save(newCustomer)
        return ResponseEntity.ok().body(persistedCustomer)
    }

    @GetMapping("/customers/{id}")
    fun findCustomer(@PathVariable("id") customerId: Int): ResponseEntity<CustomerEntity> {
        return customerSpringRepository
            .findById(customerId)
            .map { ResponseEntity.ok().body(it) }
            .orElseGet { ResponseEntity.notFound().build() }
    }

}

internal data class CustomerWithoutIdDTO(
    val name: String,
    val email: String,
    val favorites: List<Int>
) {
    fun toCustomer(now: Instant) = CustomerEntity(
        id = 0,
        name = this.name,
        email = this.email,
        data = CustomerEntityData(
            registeredSince = now,
            viewedMovies = listOf(),
            favorites = this.favorites.map {
                MovieFavoriteEntity(
                    movieId = it,
                    favoriteSince = now
                )
            }
        )
    )
}