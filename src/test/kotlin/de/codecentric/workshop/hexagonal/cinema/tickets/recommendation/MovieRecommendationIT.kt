package de.codecentric.workshop.hexagonal.cinema.tickets.recommendation

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.anyUrl
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import de.codecentric.workshop.hexagonal.cinema.tickets.ErrorResponse
import de.codecentric.workshop.hexagonal.cinema.tickets.createCustomerEntity
import de.codecentric.workshop.hexagonal.cinema.tickets.createFavoritesEntity
import de.codecentric.workshop.hexagonal.cinema.tickets.createMovieEntity
import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.adapters.inbound.RecommendationDTO
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.CustomerSpringRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.MovieSpringRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.OffsetDateTime

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["customer.datakraken.url=http://localhost:\${wiremock.server.port}"]
)
@Testcontainers
@AutoConfigureWireMock(port = 0)
internal class MovieRecommendationIT(
    @Autowired private val customerSpringRepository: CustomerSpringRepository,
    @Autowired private val movieSpringRepository: MovieSpringRepository,
    @Autowired private val testRestTemplate: TestRestTemplate
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

    @AfterEach
    fun cleanupData() {
        customerSpringRepository.deleteAll()
        movieSpringRepository.deleteAll()
    }

    @Test
    fun `recommend movies to customer from favorites, filling up to 3 by equal genre`() {
        // Given
        val movie1 = movieSpringRepository.save(createMovieEntity(title = "Die Hard", genre = Genre.ACTION))
        val movie2 = movieSpringRepository.save(createMovieEntity(title = "Ace Ventura", genre = Genre.COMEDY))
        val movie3 = movieSpringRepository.save(createMovieEntity(title = "Mission Impossible", genre = Genre.ACTION))
        movieSpringRepository.save(createMovieEntity(title = "Titanic", genre = Genre.DRAMA))

        val customer = customerSpringRepository.save(
            createCustomerEntity(
                viewedMovies = emptyList(),
                favorites = createFavoritesEntity(movie1.id, movie2.id)
            )
        )

        // When
        val result = testRestTemplate.exchange(
            "/recommendation/{customerId}",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<RecommendationDTO>>() {},
            mapOf("customerId" to customer.id)
        )

        // Then
        assertTrue(result.statusCode.is2xxSuccessful)
        assertThat(result.body).containsExactlyInAnyOrder(
            RecommendationDTO(movieId = movie1.id, probability = 0.5),
            RecommendationDTO(movieId = movie2.id, probability = 0.5),
            RecommendationDTO(movieId = movie3.id, probability = 0.05)
        )
    }

    @Test
    fun `print customer recommendations as HTML`() {
        // Given
        val movie1 = movieSpringRepository.save(createMovieEntity(title = "Die Hard", genre = Genre.ACTION))
        val movie2 = movieSpringRepository.save(createMovieEntity(title = "Ace Ventura", genre = Genre.COMEDY))
        val movie3 = movieSpringRepository.save(createMovieEntity(title = "Mission Impossible", genre = Genre.ACTION))

        val customer = customerSpringRepository.save(
            createCustomerEntity(
                viewedMovies = emptyList(),
                favorites = createFavoritesEntity(movie1.id, movie2.id)
            )
        )

        // When
        val result = testRestTemplate.exchange(
            "/recommendation/{customerId}/html",
            HttpMethod.GET,
            null,
            String::class.java,
            mapOf("customerId" to customer.id)
        )

        // Then
        assertTrue(result.statusCode.is2xxSuccessful)
        assertThat(result.body).isEqualTo(
            """
          <html>
              <header>                
                  <title>Customer Recommendations</title>
              </header>
              <body>
              <table>
                  <th>
                      <td>Movie ID</td>
                      <td>Title</td>
                      <td>Probability</td>
                  </th>
                  <tr>
                      <td>${movie1.id}</td>
                      <td>${movie1.title}</td>
                      <td>0.5</td>
                  </tr>
                  <tr>
                      <td>${movie2.id}</td>
                      <td>${movie2.title}</td>
                      <td>0.5</td>
                  </tr>
                  <tr>
                      <td>${movie3.id}</td>
                      <td>${movie3.title}</td>
                      <td>0.05</td>
                  </tr>
              </table>
              </body>
          </html>
        """.trimIndent()
        )
    }

    @Test
    fun `throw on unknown customer`() {
        // Given
        val customer = customerSpringRepository.save(createCustomerEntity())
        val invalidCustomerId = customer.id + 1

        // When
        val result = testRestTemplate.exchange(
            "/recommendation/{customerId}",
            HttpMethod.GET,
            null,
            ErrorResponse::class.java,
            mapOf("customerId" to invalidCustomerId)
        )

        // Then
        assertTrue(result.statusCode.isError)
        assertThat(result.body)
            .usingRecursiveComparison()
            .ignoringFields("timestamp")
            .isEqualTo(
                ErrorResponse(
                    status = 500,
                    error = "Internal Server Error",
                    exception = "java.lang.IllegalArgumentException",
                    message = "Could not find customer with ID $invalidCustomerId",
                    path = "/recommendation/$invalidCustomerId",
                    timestamp = OffsetDateTime.MIN
                )
            )
    }

    @Test
    fun `use datakraken API for recommendations if customer has no favorites`() {
        // Given
        val movie1 = movieSpringRepository.save(createMovieEntity(title = "Ace Ventura", genre = Genre.COMEDY))
        val movie2 = movieSpringRepository.save(createMovieEntity(title = "Titanic", genre = Genre.DRAMA))
        val movie3 = movieSpringRepository.save(createMovieEntity(title = "Police Academy", genre = Genre.COMEDY))
        movieSpringRepository.save(createMovieEntity(title = "Die Hard", genre = Genre.ACTION))
        movieSpringRepository.save(createMovieEntity(title = "Mission Impossible", genre = Genre.ACTION))
        movieSpringRepository.save(createMovieEntity(title = "Mission Impossible 2", genre = Genre.ACTION))

        val customer = customerSpringRepository.save(
            createCustomerEntity(
                viewedMovies = emptyList(),
                favorites = emptyList()
            )
        )

        mockDatakrakenApi(customer.email, Genre.COMEDY, Genre.DRAMA)

        // When
        val result = testRestTemplate.exchange(
            "/recommendation/{customerId}",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<RecommendationDTO>>() {},
            mapOf("customerId" to customer.id)
        )

        // Then
        assertTrue(result.statusCode.is2xxSuccessful)
        assertThat(result.body).containsExactlyInAnyOrder(
            RecommendationDTO(movieId = movie1.id, probability = 0.01),
            RecommendationDTO(movieId = movie2.id, probability = 0.01),
            RecommendationDTO(movieId = movie3.id, probability = 0.01)
        )
    }

    @Test
    fun `throw if datakraken API does not provided sufficient results to recommend movies`() {
        // Given
        val customer = customerSpringRepository.save(
            createCustomerEntity(
                viewedMovies = emptyList(),
                favorites = emptyList()
            )
        )

        mockDatakrakenApi(customer.email)

        // When
        val result = testRestTemplate.exchange(
            "/recommendation/{customerId}",
            HttpMethod.GET,
            null,
            ErrorResponse::class.java,
            mapOf("customerId" to customer.id)
        )

        // Then
        assertTrue(result.statusCode.isError)
        assertThat(result.body)
            .usingRecursiveComparison()
            .ignoringFields("timestamp")
            .isEqualTo(
                ErrorResponse(
                    status = 500,
                    error = "Internal Server Error",
                    exception = "java.lang.IllegalStateException",
                    message = "Could not recommend movies for customer with ID ${customer.id}",
                    path = "/recommendation/${customer.id}",
                    timestamp = OffsetDateTime.MIN
                )
            )
    }

    @Test
    fun `throw if datakraken API reponds with a server error`() {
        // Given
        val customer = customerSpringRepository.save(
            createCustomerEntity(
                viewedMovies = emptyList(),
                favorites = emptyList()
            )
        )

        mockDatakrakenApiWithErrorReponse(HttpStatus.INTERNAL_SERVER_ERROR.value())

        // When
        val result = testRestTemplate.exchange(
            "/recommendation/{customerId}",
            HttpMethod.GET,
            null,
            ErrorResponse::class.java,
            mapOf("customerId" to customer.id)
        )

        // Then
        assertTrue(result.statusCode.isError)
        assertThat(result.body)
            .usingRecursiveComparison()
            .ignoringFields("timestamp")
            .isEqualTo(
                ErrorResponse(
                    status = 500,
                    error = "Internal Server Error",
                    exception = "java.lang.IllegalStateException",
                    message = "Could not recommend movies for customer with ID ${customer.id}",
                    path = "/recommendation/${customer.id}",
                    timestamp = OffsetDateTime.MIN
                )
            )
    }

    @Test
    fun `throw if datakraken API reponds with a client error`() {
        // Given
        val customer = customerSpringRepository.save(
            createCustomerEntity(
                viewedMovies = emptyList(),
                favorites = emptyList()
            )
        )

        mockDatakrakenApiWithErrorReponse(HttpStatus.BAD_REQUEST.value())

        // When
        val result = testRestTemplate.exchange(
            "/recommendation/{customerId}",
            HttpMethod.GET,
            null,
            ErrorResponse::class.java,
            mapOf("customerId" to customer.id)
        )

        // Then
        assertTrue(result.statusCode.isError)
        assertThat(result.body)
            .usingRecursiveComparison()
            .ignoringFields("timestamp")
            .isEqualTo(
                ErrorResponse(
                    status = 500,
                    error = "Internal Server Error",
                    exception = "java.lang.IllegalStateException",
                    message = "Could not recommend movies for customer with ID ${customer.id}",
                    path = "/recommendation/${customer.id}",
                    timestamp = OffsetDateTime.MIN
                )
            )
    }

    private fun mockDatakrakenApi(email: String, vararg genres: Genre) {
        val response = if (genres.isEmpty()) {
            "[]"
        } else {
            """["${genres.joinToString(separator = "\",\"") { it.name }}"]"""
        }

        stubFor(
            get(anyUrl())
                .withQueryParam("email", equalTo(email))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(response)
                )
        )
    }

    private fun mockDatakrakenApiWithErrorReponse(statusCode: Int) {
        stubFor(
            get(anyUrl())
                .willReturn(
                    aResponse()
                        .withStatus(statusCode)
                )
        )
    }
}