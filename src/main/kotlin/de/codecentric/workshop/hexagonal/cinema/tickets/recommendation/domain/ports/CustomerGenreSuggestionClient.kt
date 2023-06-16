package de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain.ports

import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain.Customer
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre


interface CustomerGenreSuggestionClient {

    fun suggestGenres(customer: Customer): Set<Genre>
}