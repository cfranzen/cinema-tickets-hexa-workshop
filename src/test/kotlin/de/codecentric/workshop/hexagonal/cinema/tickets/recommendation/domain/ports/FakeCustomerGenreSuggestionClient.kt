package de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain.ports

import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain.Customer
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre

class FakeCustomerGenreSuggestionClient : CustomerGenreSuggestionClient {

    private val mapping = mutableMapOf<Customer, Set<Genre>>()

    fun addMapping(customer: Customer, vararg genres: Genre) {
        mapping.put(customer, genres.toSet())
    }

    override fun suggestGenres(customer: Customer): Set<Genre> =
        mapping.getOrDefault(customer, emptySet())
}