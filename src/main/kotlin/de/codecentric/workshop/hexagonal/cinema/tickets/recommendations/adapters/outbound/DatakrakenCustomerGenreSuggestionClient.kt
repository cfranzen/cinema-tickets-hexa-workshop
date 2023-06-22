package de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.adapters.outbound

import de.codecentric.workshop.hexagonal.cinema.tickets.config.DatakrakenProperties
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Customer
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.domain.ports.CustomerGenreSuggestionClient
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException

@Component
class DatakrakenCustomerGenreSuggestionClient(
    private val datakrakenProperties: DatakrakenProperties,
    private val restTemplateBuilder: RestTemplateBuilder,
) : CustomerGenreSuggestionClient {

    override fun suggestGenresForCustomer(customer: Customer): Set<Genre> {
        val restTemplate = restTemplateBuilder.rootUri(datakrakenProperties.url).build()
        val response = try {
            restTemplate.exchange(
                "/api/?email={email}",
                HttpMethod.GET,
                null,
                object : ParameterizedTypeReference<DatakrakenCustomerData>() {},
                mapOf("email" to customer.email)
            )
        } catch (e: RestClientException) {
            return emptySet()
        }

        if (response.statusCode.isError || response.body == null) {
            return emptySet()
        }

        return response.body!!.data
            .flatMap { dataEntry -> dataEntry.genres ?: emptyList() }
            .map { datakrakenGenres -> Genre.findByName(datakrakenGenres) }
            .filterNotNull()
            .toSet()
    }
}

internal data class DatakrakenCustomerData(val data: List<CustomerDataEntry>)

internal data class CustomerDataEntry(
    val name: String,
    val mail: String,
    val movie: String,
    val genres: List<String>?,
)
