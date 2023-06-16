package de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.adapters.outbound

import com.github.tomakehurst.wiremock.client.WireMock
import de.codecentric.workshop.hexagonal.cinema.tickets.createCustomer
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.web.client.HttpServerErrorException
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["customer.datakraken.url=http://localhost:\${wiremock.server.port}"],
)
@AutoConfigureWireMock(port = 0)
@Testcontainers
class DatakrakenCustomerSuggestionRepositoryTest(
    @Autowired private val repository: DatakrakenCustomerSuggestionRepository
) {

    companion object {
        @Container
        val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:15.3"))
            .withReuse(true)

        @JvmStatic
        @DynamicPropertySource
        fun props(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
        }
    }

    @Test
    fun `use datakraken API for recommendations if customer has no favorites`() {
        // Given
        val customer = createCustomer(
            viewedMovies = emptyList(),
            favorites = emptyList()
        )

        mockDatakrakenApi(customer.email, Genre.COMEDY, Genre.DRAMA)

        // When
        val result = repository.suggestGenres(customer)

        // Then
        Assertions.assertThat(result).containsExactlyInAnyOrder(
            Genre.COMEDY, Genre.DRAMA
        )
    }

    @Test
    fun `throw if datakraken API reponds with a server error`() {
        // Given
        val customer = createCustomer(
            viewedMovies = emptyList(),
            favorites = emptyList()
        )

        mockDatakrakenApiWithErrorReponse(HttpStatus.INTERNAL_SERVER_ERROR.value())

        // When / Then
        assertThrows<HttpServerErrorException.InternalServerError> { repository.suggestGenres(customer) }
    }

    private fun mockDatakrakenApi(email: String, vararg genres: Genre) {
        val response = if (genres.isEmpty()) {
            "[]"
        } else {
            """["${genres.joinToString(separator = "\",\"") { it.name }}"]"""
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