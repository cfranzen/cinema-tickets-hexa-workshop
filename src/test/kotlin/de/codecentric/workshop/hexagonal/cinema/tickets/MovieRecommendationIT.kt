package de.codecentric.workshop.hexagonal.cinema.tickets

import de.codecentric.workshop.hexagonal.cinema.tickets.controller.RecommendationDTO
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.repositories.CustomerRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.repositories.MovieRepository
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
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.OffsetDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class MovieRecommendationIT(
    @Autowired private val customerRepository: CustomerRepository,
    @Autowired private val movieRepository: MovieRepository,
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
        customerRepository.deleteAll()
        movieRepository.deleteAll()
    }

    @Test
    fun `recommend movies to customer from favorites, filling up to 3 by equal genre`() {
        // Given
        val movie1 = movieRepository.save(createMovie(title = "Die Hard", genre = Genre.ACTION))
        val movie2 = movieRepository.save(createMovie(title = "Ace Ventura", genre = Genre.COMEDY))
        val movie3 = movieRepository.save(createMovie(title = "Mission Impossible", genre = Genre.ACTION))
        movieRepository.save(createMovie(title = "Titanic", genre = Genre.DRAMA))

        val customer = customerRepository.save(
            createCustomer(
                viewedMovies = emptyList(),
                favorites = createFavorites(movie1.id, movie2.id)
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
    fun `recommend movies to customer from favorites, filling up to 3 by equal genre with most likes`() {
        // Given
        val movie1 = movieRepository.save(createMovie(title = "Die Hard", genre = Genre.ACTION))
        val movie2 = movieRepository.save(createMovie(title = "Ace Ventura", genre = Genre.COMEDY))
        val movie3 = movieRepository.save(createMovie(title = "Mission Impossible", genre = Genre.ACTION))
        val movie4 = movieRepository.save(createMovie(title = "Mission Impossible 2", genre = Genre.ACTION))

        val customer1 = customerRepository.save(
            createCustomer(
                viewedMovies = emptyList(),
                favorites = createFavorites(movie1.id, movie2.id)
            )
        )

        customerRepository.save(createCustomer(viewedMovies = emptyList(), favorites = createFavorites(movie3.id)))
        customerRepository.save(createCustomer(viewedMovies = emptyList(), favorites = createFavorites(movie4.id)))
        customerRepository.save(createCustomer(viewedMovies = emptyList(), favorites = createFavorites(movie4.id)))

        // When
        val result = testRestTemplate.exchange(
            "/recommendation/{customerId}",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<RecommendationDTO>>() {},
            mapOf("customerId" to customer1.id)
        )

        // Then
        assertTrue(result.statusCode.is2xxSuccessful)
        assertThat(result.body).containsExactlyInAnyOrder(
            RecommendationDTO(movieId = movie1.id, probability = 0.5),
            RecommendationDTO(movieId = movie2.id, probability = 0.5),
            RecommendationDTO(movieId = movie4.id, probability = 0.05)
        )
    }

    @Test
    fun `throw on unknown customer`() {
        // Given
        val customer = customerRepository.save(createCustomer())
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
}