package de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.adapters.outbound

import com.github.tomakehurst.wiremock.client.WireMock
import de.codecentric.workshop.hexagonal.cinema.tickets.config.DatakrakenProperties
import de.codecentric.workshop.hexagonal.cinema.tickets.createCustomer
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.MediaType

@SpringBootTest(
    classes = [DatakrakenCustomerGenreSuggestionClient::class],
    properties = ["customer.datakraken.url=http://localhost:\${wiremock.server.port}"],
)
@AutoConfigureWebClient
@EnableConfigurationProperties(DatakrakenProperties::class)
@AutoConfigureWireMock(port = 0)
class DatakrakenCustomerGenreSuggestionClientTest(
    @Autowired private val sut: DatakrakenCustomerGenreSuggestionClient
) {

    @Test
    fun `use datakraken API for recommendations if customer has no favorites`() {
        // Given
        val customer = createCustomer(
            viewedMovies = emptyList(),
            favorites = emptyList()
        )

        mockDatakrakenApi(customer.email, Genre.COMEDY, Genre.DRAMA)

        // When
        val genres: Set<Genre> = sut.suggestGenresForCustomer(customer)

        // Then
        assertThat(genres).containsExactlyInAnyOrder(Genre.COMEDY, Genre.DRAMA)
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