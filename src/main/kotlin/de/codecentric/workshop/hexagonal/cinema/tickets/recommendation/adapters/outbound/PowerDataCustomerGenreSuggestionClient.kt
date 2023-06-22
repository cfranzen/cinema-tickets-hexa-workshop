package de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.adapters.outbound

import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.config.PowerDataProperties
import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain.Customer
import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain.ports.CustomerGenreSuggestionClient
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException

//@Component
internal class PowerDataCustomerGenreSuggestionClient(
    private val powerDataProperties: PowerDataProperties,
    private val restTemplateBuilder: RestTemplateBuilder
) : CustomerGenreSuggestionClient {

    override fun suggestGenres(customer: Customer): Set<Genre> {
        val restTemplate = restTemplateBuilder.rootUri(powerDataProperties.url).build()
        val response = try {
            restTemplate.exchange(
                "/api/?email={email}",
                HttpMethod.GET,
                null,
                PowerDataResponse::class.java,
                mapOf("email" to customer.email)
            )
        } catch (e: RestClientException) {
            return emptySet()
        }

        if (response.statusCode.isError || response.body == null) {
            return emptySet()
        }

        return response.body!!.genres
            ?.map { powerDataGenres -> Genre.findByName(powerDataGenres) }
            ?.filterNotNull()
            ?.toSet()
            ?: emptySet()
    }
}


internal data class PowerDataResponse(
    val name: String,
    val mail: String,
    val movie: String,
    val genres: List<String>?,
)
