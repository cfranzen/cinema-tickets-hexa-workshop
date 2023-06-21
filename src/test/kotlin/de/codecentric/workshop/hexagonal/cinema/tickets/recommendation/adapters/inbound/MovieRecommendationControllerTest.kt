package de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.adapters.inbound

import com.ninjasquad.springmockk.MockkBean
import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain.Customer
import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain.Recommendation
import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain.RecommendationService
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.CustomerSpringRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.tests.ErrorResponse
import de.codecentric.workshop.hexagonal.cinema.tickets.tests.createCustomerEntity
import de.codecentric.workshop.hexagonal.cinema.tickets.tests.createMovie
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.OffsetDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
internal class MovieRecommendationControllerTest(
    @Autowired private val customerSpringRepository: CustomerSpringRepository,
    @Autowired private val testRestTemplate: TestRestTemplate,
) {

    @MockkBean
    private lateinit var recommendationService: RecommendationService

    companion object {
        @Container
        val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:15.3"))
            .withReuse(true)
            .waitingFor(Wait.forListeningPort())

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
    }

    @Test
    fun `return customer recommendations as json`() {
        // Given
        val movie1 = createMovie(id = 1, title = "Die Hard", genre = Genre.ACTION)
        val movie2 = createMovie(id = 2, title = "Ace Ventura", genre = Genre.COMEDY)
        val movie3 = createMovie(id = 3, title = "Mission Impossible", genre = Genre.ACTION)

        val customerEntity = customerSpringRepository.save(createCustomerEntity())
        val customer = Customer(
            id = customerEntity.id,
            name = customerEntity.name,
            email = customerEntity.email,
            favoriteMovieIds = customerEntity.data.favorites.map { it.movieId }
        )

        every { recommendationService.calcRecommendations(customer) } returns listOf(
            Recommendation(customer = customer, movie = movie1, probability = 0.5),
            Recommendation(customer = customer, movie = movie2, probability = 0.5),
            Recommendation(customer = customer, movie = movie3, probability = 0.05),
        )

        // When
        val result = testRestTemplate.exchange(
            "/recommendation/{customerId}",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<RecommendationDTO>>() {},
            mapOf("customerId" to customerEntity.id)
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
        val movie1 = createMovie(id = 1, title = "Die Hard", genre = Genre.ACTION)
        val movie2 = createMovie(id = 2, title = "Ace Ventura", genre = Genre.COMEDY)
        val movie3 = createMovie(id = 3, title = "Mission Impossible", genre = Genre.ACTION)

        val customerEntity = customerSpringRepository.save(createCustomerEntity())
        val customer = Customer(
            id = customerEntity.id,
            name = customerEntity.name,
            email = customerEntity.email,
            favoriteMovieIds = customerEntity.data.favorites.map { it.movieId }
        )

        every { recommendationService.calcRecommendations(customer) } returns listOf(
            Recommendation(customer = customer, movie = movie1, probability = 0.5),
            Recommendation(customer = customer, movie = movie2, probability = 0.5),
            Recommendation(customer = customer, movie = movie3, probability = 0.05),
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
    fun `throw on unknown customer instead of returning json`() {
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
    fun `throw on unknown customer instead of returning html`() {
        // Given
        val customer = customerSpringRepository.save(createCustomerEntity())
        val invalidCustomerId = customer.id + 1

        // When
        val result = testRestTemplate.exchange(
            "/recommendation/{customerId}/html",
            HttpMethod.GET,
            null,
            String::class.java,
            mapOf("customerId" to invalidCustomerId)
        )

        // Then
        assertTrue(result.statusCode.isError)
    }
}