package de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.domain.ports

import de.codecentric.workshop.hexagonal.cinema.tickets.model.Customer
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre

class FakeCustomerGenreSuggestionClient : CustomerGenreSuggestionClient {

    private val fixedGenres = mutableSetOf<Genre>()

    fun fixGenres(vararg genres: Genre) {
        fixedGenres.clear()
        fixedGenres.addAll(genres)
    }

    override fun suggestGenresForCustomer(customer: Customer): Set<Genre> {
        return fixedGenres
    }
}