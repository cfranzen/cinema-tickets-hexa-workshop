package de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.adapters.outbound

import de.codecentric.workshop.hexagonal.cinema.tickets.config.DatakrakenProperties
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Customer
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.domain.ports.outbound.CustomerGenreSuggestionRepository
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component

@Component
class DatakrakenCustomerSuggestionRepository(
    private val datakrakenProperties: DatakrakenProperties,
    private val restTemplateBuilder: RestTemplateBuilder
) : CustomerGenreSuggestionRepository {

    override fun suggestGenres(customer: Customer): List<Genre> {
        val restTemplate = restTemplateBuilder.rootUri(datakrakenProperties.url).build()
        val response = restTemplate.exchange(
            "/api/?email={email}",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<Genre>>() {},
            mapOf("email" to customer.email)
        )

        if (response.statusCode.isError || response.body == null || response.body!!.isEmpty()) {
            return emptyList()
        }

        return response.body!!
    }

}