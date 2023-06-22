package de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.domain.ports

import de.codecentric.workshop.hexagonal.cinema.tickets.model.Customer
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre

interface CustomerGenreSuggestionClient {

    fun suggestGenresForCustomer(customer: Customer): Set<Genre>
}