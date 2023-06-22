package de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.adapters.outbound

import com.github.tomakehurst.wiremock.client.WireMock
import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.config.PowerDataProperties
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.tests.createCustomer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

@Disabled
@AutoConfigureWebClient
@SpringBootTest(
    classes = [PowerDataCustomerGenreSuggestionClient::class],
    properties = ["customer.powerdata.url=http://localhost:\${wiremock.server.port}"],
)
@EnableConfigurationProperties(
    PowerDataProperties::class
)
@AutoConfigureWireMock(port = 0)
internal class PowerDataCustomerGenreSuggestionClientTest(
    @Autowired private val sut: PowerDataCustomerGenreSuggestionClient
) {

    @Test
    fun `return empty list if powerdata API does not supply any genres`() {
        // Given
        val customer = createCustomer()
        mockPowerDataApi(customer.email)

        // When
        val result = sut.suggestGenres(customer)

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `throw if powerdata API reponds with a server error`() {
        // Given
        val customer = createCustomer()
        mockPowerDataApiWithErrorReponse(HttpStatus.INTERNAL_SERVER_ERROR.value())

        // When
        val result = sut.suggestGenres(customer)

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `throw if powerdata API reponds with a client error`() {
        // Given
        val customer = createCustomer()
        mockPowerDataApiWithErrorReponse(HttpStatus.BAD_REQUEST.value())

        // When
        val result = sut.suggestGenres(customer)

        // Then
        assertThat(result).isEmpty()
    }


    private fun mockPowerDataApi(email: String, vararg genres: Genre) {
        val response = if (genres.isEmpty()) {
            """
                {
                    "name": "Hans Damp",
                    "mail": "hans@dampf.de",
                    "movie": "String",
                    "genres": []
                }
                """.trimIndent()
        } else {
            """
                {
                    "name": "Hans Damp",
                    "mail": "hans@dampf.de",
                    "movie": "String",
                    "genres": ["${genres.joinToString(separator = "\",\"") { it.name }}"]
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

    private fun mockPowerDataApiWithErrorReponse(statusCode: Int) {
        WireMock.stubFor(
            WireMock.get(WireMock.anyUrl())
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(statusCode)
                )
        )
    }
}