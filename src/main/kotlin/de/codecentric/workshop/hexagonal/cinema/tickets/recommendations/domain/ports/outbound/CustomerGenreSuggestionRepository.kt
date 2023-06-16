package de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.domain.ports.outbound

import de.codecentric.workshop.hexagonal.cinema.tickets.model.Customer
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre

interface CustomerGenreSuggestionRepository {

    fun suggestGenres(customer: Customer) : List<Genre>
}