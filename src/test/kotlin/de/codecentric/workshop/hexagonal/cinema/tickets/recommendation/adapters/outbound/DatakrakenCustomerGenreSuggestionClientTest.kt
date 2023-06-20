package de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.adapters.outbound

import com.github.tomakehurst.wiremock.client.WireMock
import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.config.DatakrakenProperties
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.tests.createCustomer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

@AutoConfigureWebClient
@SpringBootTest(classes = [DatakrakenCustomerGenreSuggestionClient::class])
@EnableConfigurationProperties(
    DatakrakenProperties::class
)
@AutoConfigureWireMock(port = 0)
internal class DatakrakenCustomerGenreSuggestionClientTest(
    @Autowired private val sut: DatakrakenCustomerGenreSuggestionClient
) {

    @Test
    fun `return empty list if datakraken API does not supply any genres`() {
        // Given
        val customer = createCustomer()
        mockDatakrakenApi(customer.email)

        // When
        val result = sut.suggestGenres(customer)

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `throw if datakraken API reponds with a server error`() {
        // Given
        val customer = createCustomer()
        mockDatakrakenApiWithErrorReponse(HttpStatus.INTERNAL_SERVER_ERROR.value())

        // When
        val result = sut.suggestGenres(customer)

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `throw if datakraken API reponds with a client error`() {
        // Given
        val customer = createCustomer()
        mockDatakrakenApiWithErrorReponse(HttpStatus.BAD_REQUEST.value())

        // When
        val result = sut.suggestGenres(customer)

        // Then
        assertThat(result).isEmpty()
    }

    private fun mockDatakrakenApi(email: String, vararg genres: Genre) {
        val response = if (genres.isEmpty()) {
            """
                {
                    "data": [
                        {
                            "name": "Hans Damp",
                            "mail": "hans@dampf.de",
                            "movie": "String",
                            "genres": []
                        }
                    ]
                }
                """.trimIndent()
        } else {
            """
                {
                    "data": [
                        {
                            "name": "Hans Damp",
                            "mail": "hans@dampf.de",
                            "movie": "String",
                            "genres": ["${genres.joinToString(separator = "\",\"") { it.name }}"]
                        }
                    ]
                }
                """.trimIndent()
        }

        WireMock.stubFor(
            WireMock.get(WireMock.anyUrl())
                .withQueryParam("email", WireMock.equalTo(email))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(response)
                )
        )
    }

    private fun mockDatakrakenApiWithErrorReponse(statusCode: Int) {
        WireMock.stubFor(
            WireMock.get(WireMock.anyUrl())
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(statusCode)
                )
        )
    }
}