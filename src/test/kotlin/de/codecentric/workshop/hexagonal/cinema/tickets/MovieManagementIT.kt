package de.codecentric.workshop.hexagonal.cinema.tickets

import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Movie
import de.codecentric.workshop.hexagonal.cinema.tickets.repositories.MovieRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class MovieManagementIT(
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
        movieRepository.deleteAll()
    }

    @Test
    fun `create new movies and assign unique id to each of them`() {
        // Given
        val movie1 = createMovieDTO(title = "Die Hard", genre = Genre.ACTION)
        val movie2 = createMovieDTO(title = "Ace Ventura", genre = Genre.COMEDY)
        val movie3 = createMovieDTO(title = "Mission Impossible", genre = Genre.ACTION)
        val moviesRequest = listOf(movie1, movie2, movie3)

        // When
        val movieResponses = mutableListOf<Movie>()
        moviesRequest.forEach { movie ->
            val result = testRestTemplate.exchange(
                "/movies",
                HttpMethod.POST,
                HttpEntity(movie),
                Movie::class.java
            )
            assertTrue(result.statusCode.is2xxSuccessful)
            result.body?.let { movieResponses.add(it) }
        }

        // Then
        for (i in moviesRequest.indices) {
            assertThat(movieResponses[i])
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(moviesRequest[i].toMovie())
        }

        val movieIds = movieResponses.map { it.id }.toSet()
        assertThat(movieIds).hasSize(moviesRequest.size)
    }

    @Test
    fun `retrieve existing movie by its ID`() {
        // Given
        val movie1 = movieRepository.save(createMovie(title = "Die Hard", genre = Genre.ACTION))
        val movie2 = movieRepository.save(createMovie(title = "Ace Ventura", genre = Genre.COMEDY))
        val movie3 = movieRepository.save(createMovie(title = "Mission Impossible", genre = Genre.ACTION))
        val moviesRequest = listOf(movie1, movie2, movie3)

        // When
        val movieResponses = mutableListOf<Movie>()
        moviesRequest.forEach { movie ->
            val result = testRestTemplate.exchange(
                "/movies/{id}",
                HttpMethod.GET,
                null,
                Movie::class.java,
                mapOf("id" to movie.id)
            )
            assertTrue(result.statusCode.is2xxSuccessful)
            result.body?.let { movieResponses.add(it) }
        }

        // Then
        for (i in moviesRequest.indices) {
            assertThat(movieResponses[i])
                .usingRecursiveComparison()
                .isEqualTo(moviesRequest[i])
        }
    }

    @Test
    fun `throw HTTP 404 NOT FOUND if movie does not exists`() {
        // Given
        val movie = movieRepository.save(createMovie(title = "Die Hard", genre = Genre.ACTION))
        val invalidMovieId = movie.id + 1

        // When
        val result = testRestTemplate.exchange(
            "/movies/{id}",
            HttpMethod.GET,
            null,
            Void::class.java,
            mapOf("id" to invalidMovieId)
        )

        // Then
        assertTrue(result.statusCode.isSameCodeAs(HttpStatus.NOT_FOUND))
    }
}